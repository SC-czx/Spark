package com.atguigu.app;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.atguigu.constants.GmallConstants;
import com.atguigu.utils.KafkaSender;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.List;

public class CanalClient {
    public static void main(String[] args) {
        //1：获取Canal连接
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("hadoop110",
                11111), "example", "", "");

        //2:抓取数据
        while (true) {
            canalConnector.connect();

            //3:订阅表
            canalConnector.subscribe("gmall.*");


            Message message = canalConnector.get(100);
            //判断是否有数据
            if (message.getEntries().size() == 0) {
                System.out.println("没有数据，神！树界降临");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                //遍历message中的entry
                for (CanalEntry.Entry entry : message.getEntries()) {
                    //过滤掉写操作数据中非操作数据的数据，事务的开启与关闭
                    if (entry.getEntryType().equals(CanalEntry.EntryType.ROWDATA)) {
                        CanalEntry.RowChange rowChange = null;
                        try {
                            rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

                            CanalEntry.EventType eventType = rowChange.getEventType();//insert update delete
                            String tableName = entry.getHeader().getTableName();//表名
                            List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();

                            handle(tableName, eventType, rowDatasList);

                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static void handle(String tableName, CanalEntry.EventType eventType, List<CanalEntry.RowData> rowDatasList) {
        //1：过滤出下单数据（类型为insert的数据）
        if ("order_info".equals(tableName)) {

            if (eventType.equals(CanalEntry.EventType.INSERT)) {
                for (CanalEntry.RowData rowData : rowDatasList) {
                    List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
                    JSONObject jsonObject = new JSONObject();
                    for (CanalEntry.Column column : afterColumnsList) {
                        jsonObject.put(column.getName(), column.getValue());
                    }
                    KafkaSender.sendCanalData(GmallConstants.GMALL_ORDER_INFO, jsonObject.toJSONString());
                    System.out.println(jsonObject.toJSONString());
                }
            }
        }
    }
}
