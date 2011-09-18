package org.springframework.integration.activiti.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SynchronousGatewayConfiguration extends GatewayTestConfiguration {
    @Bean
    public SyncActivityBehaviorMessagingGateway gateway() throws Exception {
        return this.configureAbstractActivityBehaviorMessagingGateway(new SyncActivityBehaviorMessagingGateway());
    }
}
