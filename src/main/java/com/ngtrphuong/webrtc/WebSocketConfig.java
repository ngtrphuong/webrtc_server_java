package com.ngtrphuong.webrtc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Enable WebSocket support Configure websocket and enable
 */
@Configuration
public class WebSocketConfig extends WebMvcConfigurationSupport {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
       super.addResourceHandlers(registry);
        // Relative path of image
       registry.addResourceHandler("image/**").addResourceLocations("classpath:/static/image/");
        // Absolute path
        registry.addResourceHandler("image/**").addResourceLocations("file:" + "image/");
    }
}
