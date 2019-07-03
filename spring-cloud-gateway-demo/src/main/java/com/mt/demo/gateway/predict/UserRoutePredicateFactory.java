//package com.mt.demo.gateway.predict;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.server.ServerWebExchange;
//
//import javax.validation.constraints.NotEmpty;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.function.Predicate;
//
///**
// * UserRoutePredicateFactory
// *
// * @author mt.luo
// * @description:
// */
//@Slf4j
//@Component
//@Order(1)
//public class UserRoutePredicateFactory extends AbstractRoutePredicateFactory<UserRoutePredicateFactory.Config> {
//
//
//    private String user = "mt";
//
//    public UserRoutePredicateFactory() {
//        super(UserRoutePredicateFactory.Config.class);
//    }
//
//    @Override
//    public ShortcutType shortcutType() {
//        return ShortcutType.GATHER_LIST;
//    }
//
//    @Override
//    public List<String> shortcutFieldOrder() {
//        return Collections.singletonList("sources");
//    }
//
//    @Override
//    public Predicate<ServerWebExchange> apply(UserRoutePredicateFactory.Config config) {
//        log.info("================config: {}", config);
//        return exchange -> {
//            if (config.sources.contains(user)) {
//                return true;
//            }
//            return false;
//        };
//    }
//
//    @Validated
//    public static class Config {
//        @NotEmpty
//        private List<String> sources = new ArrayList<>();
//
//        public List<String> getSources() {
//            return sources;
//        }
//
//        public UserRoutePredicateFactory.Config setSources(List<String> sources) {
//            this.sources = sources;
//            return this;
//        }
//
//        public UserRoutePredicateFactory.Config setSources(String... sources) {
//            this.sources = Arrays.asList(sources);
//            return this;
//        }
//    }
//}
