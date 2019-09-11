package com.atguigu.gmallpublisher.service.impl;


import com.atguigu.gmallpublisher.mapper.DauMapper;
import com.atguigu.gmallpublisher.mapper.OrderMapper;
import com.atguigu.gmallpublisher.service.DauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DauServiceImpl implements DauService {

    @Autowired
    private DauMapper dauMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Long getTotalDau(String date) {
        return dauMapper.getTotalDau(date);
    }

    @Override
    public Map getHourDau(String date) {

        //定义返回值类型
        HashMap<String, Long> hourDauMap = new HashMap<>();
        List<Map> list = dauMapper.selectDauTotalHourMap(date);

        for (Map map : list) {
            String lh = (String) map.get("LOGHOUR");
            long ct = (long) map.get("CT");
            System.out.println(lh + "----" + ct);
            hourDauMap.put(lh, ct);
        }

        return hourDauMap;
    }

    @Override
    public Double getOrderAmountTotal(String date) {
        return orderMapper.selectOrderAmountTotal(date);
    }

    @Override
    public Map getOrderAmountHourMap(String date) {
        List<Map> mapList = orderMapper.selectOrderAmountHourMap(date);
        Map orderAmountHourMap=new HashMap();
        for (Map map : mapList) {
            orderAmountHourMap.put(map.get("CREATE_HOUR"), map.get("SUM_AMOUNT"));
        }
        return orderAmountHourMap;

    }
}
