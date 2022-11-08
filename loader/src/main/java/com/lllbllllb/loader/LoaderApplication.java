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
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

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
        var url = "/prey";

        return route(POST(url), request -> request.bodyToMono(Prey.class)
            .flatMap((Prey prey) -> {
                loadService.registerPrey(prey);

                return noContent().build();
            }))
            .and(route(GET(url), request -> ok().body(Flux.fromIterable(loadService.getAllPreys()), Prey.class)))
            .and(route(DELETE(url + "/{name}"), request -> {
                var name = request.pathVariable("name");

                loadService.deletePrey(name);

                return noContent().build();
            }));
    }

    @Bean
    static Clock clock() {
        return Clock.systemDefaultZone();
    }

}
