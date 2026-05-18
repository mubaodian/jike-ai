package com.swl.jikeai.controller;

import com.swl.jikeai.common.BaseResponse;
import com.swl.jikeai.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/health")
    public BaseResponse health(){
        return ResultUtils.success("ok");
    }
}
