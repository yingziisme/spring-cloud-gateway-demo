package com.mt.demo.gateway.configuration;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * GlobalExceptionHandler
 *
 * @author mt.luo
 * @description:
 */
@Slf4j
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {

        log.info("GLOBAL EXCEPTION:{}, \n{}", serverWebExchange.getRequest().getPath(), throwable);
        JSONObject object = new JSONObject();
        if (throwable instanceof NotFoundException) {
            object.put("message", "not found");
        } else if (throwable instanceof ResponseStatusException) {
            object.put("message", "status exception");
        } else {
            object.put("message", "exception");
        }


        DataBufferFactory bufferFactory = serverWebExchange.getResponse().bufferFactory();
        serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
        return serverWebExchange.getResponse().writeWith(Flux.just(bufferFactory.wrap(object.toJSONString().getBytes())));
    }
}
