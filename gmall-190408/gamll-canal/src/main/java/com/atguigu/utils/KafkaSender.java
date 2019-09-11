package com.atguigu.utils;

import com.atguigu.constants.GmallConstants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;


import java.util.Properties;


public class KafkaSender {


    private static  KafkaProducer<String, String> kafkaProducer;

    //获取Producer
    static {

        Properties properties = PropertiesUtil.load("kafka.producer.properties");
        kafkaProducer = new KafkaProducer<>(properties);
    }

    //发送数据
    public static void sendCanalData(String topic,String date) {

        kafkaProducer.send(new ProducerRecord<>(topic, date));

    }
}
