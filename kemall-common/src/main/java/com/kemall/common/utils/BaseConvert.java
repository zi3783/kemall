package com.kemall.common.utils;

import java.util.List;

public interface BaseConvert <S, T> {
    T toTarget(S source);
    List<T> toTargetList(List<S> sources);
}
