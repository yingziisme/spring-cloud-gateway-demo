package com.mt.demo.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * DemoService
 *
 * @author mt.luo
 * @description:
 */
@Slf4j
@Service
public class DemoService {

    @Autowired
    private RestTemplate restTemplate;

    @Async
    public void rate(HttpEntity httpEntity) {
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(new URI("http://localhost:10001/cloud/demo/weight?param=mt"), HttpMethod.GET, httpEntity, String.class);
            log.info("===={}==", responseEntity);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
