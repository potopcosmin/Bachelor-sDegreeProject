package com.licenta.server.Websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {


public int websocketPort;
    private final SocketHandler socketHandler;

    public WebSocketConfig(SocketHandler socketControler) {
        this.socketHandler = socketControler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socketHandler, "/trip").setAllowedOrigins("*");
    }

}
