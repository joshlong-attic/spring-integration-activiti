package org.springframework.integration.activiti.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsynchronousGatewayConfiguration extends GatewayTestConfiguration {
    @Bean
    public AsyncActivityBehaviorMessagingGateway gateway() throws Exception {
        return this.configureAbstractActivityBehaviorMessagingGateway(new AsyncActivityBehaviorMessagingGateway());
    }
}
