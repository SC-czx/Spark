package com.atguigu.app

import java.text.SimpleDateFormat
import java.util.Date
import java.util

import com.alibaba.fastjson.JSON
import com.atguigu.bean.{AlertInfo, EventInfo}
import com.atguigu.constants.GmallConstants
import com.atguigu.utils.{MyEsUtil, MyKafkaUtil}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.util.control.Breaks._

object EventApp {
  def main(args: Array[String]): Unit = {
    val sdf = new SimpleDateFormat("yyyy-MMM-dd HH")
    val conf: SparkConf = new SparkConf().setAppName("EventApp").setMaster("local[*]")

    val context = new StreamingContext(conf, Seconds(5))

    //3：读取kafka数据

    val kafkaDStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(context, Set(GmallConstants.GMALL_EVENT))

    //4：格式化数据
    val midToEventDStream: DStream[(String, EventInfo)] = kafkaDStream.map { case (_, value) =>
      val eventInfo: EventInfo = JSON.parseObject(value, classOf[EventInfo])

      val ts: Long = eventInfo.ts
      val dateArr: Array[String] = sdf.format(new Date(ts)).split(" ")
      eventInfo.logDate = dateArr(0)
      eventInfo.logHour = dateArr(1)
      (eventInfo.mid, eventInfo)
    }

    //5:开窗并分组
    val midToEventIterDStream: DStream[(String, Iterable[EventInfo])] = midToEventDStream.window(Seconds(60)).groupByKey()

    //6:过滤数据
    val flagTOAlert: DStream[(Boolean, AlertInfo)] = midToEventIterDStream.map { case (mid, eventInfoIter) =>

      val uids = new util.HashSet[String]()
      val itemids = new util.HashSet[String]()
      val events = new util.ArrayList[String]()

      var flag: Boolean = true
      breakable {
        eventInfoIter.foreach((eventInfo: EventInfo) => {

          events.add(eventInfo.evid)
          if ("coupon".equals(eventInfo.evid)) {
            uids.add(eventInfo.uid)
            itemids.add(eventInfo.itemid)
          } else if ("clickItem".equals(eventInfo.evid)) {
            flag = false
            break()
          }

        })
      }
      (flag && uids.size() >= 3, AlertInfo(mid, uids, itemids, events, System.currentTimeMillis()))
    }

    //7:过滤数据
    val alertInfoDStream: DStream[AlertInfo] = flagTOAlert.filter((_: (Boolean, AlertInfo))._1).map((_: (Boolean, AlertInfo))._2)

    //8:将数据写入ES
    alertInfoDStream.map((alertInfo: AlertInfo) =>{
      val mint: Long = alertInfo.ts/1000/60
      (s"${alertInfo.mid}_$mint",alertInfo)
    }).foreachRDD((rdd: RDD[(String, AlertInfo)]) =>{
      //按照分区写入数据
      rdd.foreachPartition((iter: Iterator[(String, AlertInfo)]) =>{
        MyEsUtil.insertBulk("gmall_coupon_alert",iter.toList)
      })
    })


    //测试，打印
//    flagTOAlert.print()

    context.start()
    context.awaitTermination()  //开启nginx 、gmall.jar zookeeper ，kafka
  }
}
