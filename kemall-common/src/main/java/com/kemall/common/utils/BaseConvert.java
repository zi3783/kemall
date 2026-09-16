package com.kemall.common.utils;

import java.util.List;


public interface BaseConvert <S, T> {
    T toTarget(S source);
    S toSource(T target);
    List<T> toTargetList(List<S> sources);
    List<S> toSourceList(List<T> targets);
}
