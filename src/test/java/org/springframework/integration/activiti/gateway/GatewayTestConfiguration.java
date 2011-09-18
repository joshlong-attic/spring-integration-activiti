package org.springframework.integration.activiti.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.activiti.CommonConfiguration;
import org.springframework.integration.activiti.utils.PrintingServiceActivator;

/**
 * Simple configuration for the asyncGateway test
 *
 * @author Josh Long
 */
@Configuration
public class GatewayTestConfiguration extends CommonConfiguration {

  @Value("#{response}")
  private MessageChannel replies;

  @Value("#{request}")
  private MessageChannel requests;

  @Bean
  public PrintingServiceActivator serviceActivator() {
    return new PrintingServiceActivator();
  }
  protected
  <T extends AbstractActivityBehaviorMessagingGateway> T configureAbstractActivityBehaviorMessagingGateway(T gateway) throws Exception {
    gateway.setForwardProcessVariablesAsMessageHeaders(true);
    gateway.setProcessEngine(this.processEngine().getObject());
    gateway.setUpdateProcessVariablesFromReplyMessageHeaders(true);
    gateway.setRequestChannel(this.requests);
    gateway.setReplyChannel(this.replies);
    return gateway;
  }

}
