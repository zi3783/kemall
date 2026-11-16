package com.kemall.trade;

import com.kemall.common.core.utils.BaseConvert;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseConvert<User, UserDTO> {
}