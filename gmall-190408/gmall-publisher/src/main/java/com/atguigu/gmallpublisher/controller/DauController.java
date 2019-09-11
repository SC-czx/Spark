package com.atguigu.gmallpublisher.controller;

import com.atguigu.gmallpublisher.service.DauService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class DauController {

    @Autowired
    private DauService dauService;

    @GetMapping("realtime-total")
    public String getTotal(@RequestParam("date") String date) {

        //获取数据
        Long totalDau = dauService.getTotalDau(date);
        Double orderAmountTotal = dauService.getOrderAmountTotal(date);
//        Map orderAmountHourMap = dauService.getOrderAmountHourMap(date);

        //定义返回值类型
        List<Map> result = new ArrayList<>();
        HashMap<String, Object> dauMap = new HashMap<>();
        dauMap.put("id", "dau");
        dauMap.put("name", "新增日活");
        dauMap.put("value", totalDau);
        result.add(dauMap);



        HashMap<String, Object> newMiDMap = new HashMap<>();
        newMiDMap.put("id", "new_mid");
        newMiDMap.put("name", "新增设备");
        newMiDMap.put("value", 233);
        result.add(newMiDMap);


        HashMap<String, Object> totalAmount = new HashMap<>();
        totalAmount.put("id", "order_amount");
        totalAmount.put("name", "新增交易额");
        totalAmount.put("value", orderAmountTotal);
        result.add(totalAmount);

        return JSON.toJSONString(result);
    }

    @GetMapping("realtime-hours")
    public String getHourDau(@RequestParam("id") String id, @RequestParam("date") String today) {

        if ("dau".equals(id)) {
            JSONObject jsonObject = new JSONObject();
            //查询日活的分时统计
            Map todayHourDau = dauService.getHourDau(today);
            jsonObject.put("today", todayHourDau);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //获取昨天日期
            try {
                Date yesterdayDate = DateUtils.addDays(sdf.parse(today), -1);
                String yesterdayStr = sdf.format(yesterdayDate);

                Map yesterdayDauMap = dauService.getHourDau(yesterdayStr);
                jsonObject.put("yesterday", yesterdayDauMap);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return jsonObject.toJSONString();
        }else if("order_amount".equals(id)){
            JSONObject jsonObject = new JSONObject();
            //查询日活的分时统计
            Map todayAmountHourMap = dauService.getOrderAmountHourMap(today);
            jsonObject.put("today", todayAmountHourMap);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //获取昨天日期
            try {
                Date yesterdayDate = DateUtils.addDays(sdf.parse(today), -1);
                String yesterdayStr = sdf.format(yesterdayDate);

                Map yesterdayAmountHourMap = dauService.getOrderAmountHourMap(yesterdayStr);
                jsonObject.put("yesterday", yesterdayAmountHourMap);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return jsonObject.toJSONString();
        }


        return null;
    }

}
