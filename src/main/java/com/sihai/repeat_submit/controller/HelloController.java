package com.sihai.repeat_submit.controller;

import com.sihai.repeat_submit.annotation.RepeatSubmit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    /**
     * request body 可以接收json格式的数据   -获取请求体的内容
     * @param json
     * @return
     */
    @PostMapping("/hello")
    @RepeatSubmit(interval = 10000)
    public String hello(@RequestBody String json) {
        return json;
    }
}
