package com.corsairops.maintenance.config;

import com.corsairops.shared.client.AssetServiceClient;
import com.corsairops.shared.client.ClientFactory;
import com.corsairops.shared.client.UserServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Value("${user-service.url}")
    private String userServiceUrl;

    @Value("${asset-service.url}")
    private String assetServiceUrl;

    @Bean
    public UserServiceClient userServiceClient() {
        return ClientFactory.createUserServiceClient(userServiceUrl);
    }

    @Bean
    public AssetServiceClient assetServiceClient() {
        return ClientFactory.createAssetServiceClient(assetServiceUrl);
    }


}