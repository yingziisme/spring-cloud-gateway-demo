package com.mt.demo.client.controller;

import com.alibaba.fastjson.JSONObject;
import com.mt.demo.client.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

/**
 * DemoController
 *
 * @author mt.luo
 * @description:
 */
@Slf4j
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private DemoService demoService;

    @GetMapping("/get")
    public void get(@RequestParam String param, HttpServletRequest request) {
        log.info("aaaa: {}", param);

        log.info("websession: {}", request.getSession().getId());
        Enumeration<String> e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String header = e.nextElement();
            log.info("header: {}, {}", header, request.getHeader(header));
        }

    }

    @GetMapping("/weight")
    public String weight(@RequestParam String param) {
        log.info("aaaa: {}", param);
        return "aaa";
    }


    @GetMapping("/demo/weight")
    public String demoWeight(@RequestParam String param) {
        log.info("===============sb: {}", param);
        return "bbb";
    }

    @PostMapping("/post")
    public JSONObject post(@RequestBody JSONObject object) {
        log.info("obje: {}", object.toJSONString());
        return object;
    }

    @GetMapping("/rate")
    public void rate(@RequestParam int size) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        for (int i = 0; i < size; i++) {
            demoService.rate(httpEntity);
        }
    }
}
