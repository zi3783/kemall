package com.kemall.common.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BeanUtil {

    public static <T> T copyBean(Object source, Class<T> targetClass) {
        try{
            if (source == null) {
                throw new RuntimeException("source is null");
            }
            T target = (T) targetClass.getConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        }catch (Exception e){
            throw new RuntimeException("copy bean error", e);
        }
    }

    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        if(sourceList == null){
            throw new RuntimeException("sourceList is null");
        }
        return sourceList.stream()
                .filter(Objects::nonNull)
                .map(x->BeanUtil.copyBean(x, targetClass))
                .toList();
    }

}
