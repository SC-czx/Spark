package com.atguigu.gmallpublisher.service;

import java.util.Map;

public interface DauService {

    public Long getTotalDau(String date);

    public Map getHourDau(String date);

    public Double getOrderAmountTotal(String date);

    public Map getOrderAmountHourMap(String date);

}
