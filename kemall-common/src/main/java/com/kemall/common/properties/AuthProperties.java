package com.kemall.common.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties("kemall.auth")
@Data
public class AuthProperties {
    private List<String> whiteList = new ArrayList<>(List.of(
            "/v3/api-docs",
            "/v3/api-docs/swagger-config",
            "/swagger-ui/index.html"
    ));
}
