package com.kemall.seckill.controller;

import com.kemall.common.core.utils.bean.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "秒杀接口")
@Controller
@RequestMapping("/scekill")
public class SeckillController {

    @DubboReference


    public Result<String> seckill(Long skuId){

    }

}
