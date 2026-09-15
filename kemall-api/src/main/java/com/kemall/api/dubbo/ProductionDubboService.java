package com.kemall.api.dubbo;


import java.util.List;
import java.util.Map;

public interface ProductionDubboService {

    Map<Long, Long> getSkuPriceByIds(List<Long> skuIds);
}
