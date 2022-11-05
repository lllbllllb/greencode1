package com.lllbllllb.loader;

import java.time.Clock;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.noContent;

@SpringBootApplication
public class LoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoaderApplication.class, args);
    }

    @Bean
    static HandlerAdapter wsHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    static HandlerMapping webSocketMapping(LoaderWebSocketHandler loaderWebSocketHandler) {
        var map = Map.of("/websocket/load", loaderWebSocketHandler);
        var simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(map);
        simpleUrlHandlerMapping.setOrder(10);

        return simpleUrlHandlerMapping;
    }

    @Bean
    static RouterFunction<ServerResponse> loaderRestController(LoadService loadService) {
        return route(POST("/prey"), request -> request.bodyToMono(Prey.class)
            .flatMap((Prey prey) -> {
                loadService.registerPrey(prey);

                return noContent().build();
            }));
    }

    @Bean
    static Clock clock() {
        return Clock.systemDefaultZone();
    }

}
