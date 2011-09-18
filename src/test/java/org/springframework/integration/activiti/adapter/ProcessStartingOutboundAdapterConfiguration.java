package org.springframework.integration.activiti.adapter;

import org.activiti.engine.ProcessEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.activiti.CommonConfiguration;
import org.springframework.integration.activiti.mapping.DefaultProcessVariableHeaderMapper;
import org.springframework.integration.activiti.util.ActivityExecutionFactoryBean;

@Configuration
@SuppressWarnings("unused")
public class ProcessStartingOutboundAdapterConfiguration extends CommonConfiguration {

    @Bean
    public ActivityExecutionFactoryBean activityExecutionFactoryBean() throws Throwable {
        ActivityExecutionFactoryBean activityExecutionFactoryBean = new ActivityExecutionFactoryBean();
        activityExecutionFactoryBean.afterPropertiesSet();
        return activityExecutionFactoryBean;
    }

    @Bean
    public DefaultProcessVariableHeaderMapper headerMapper() throws Throwable {
        ProcessEngine e = this.processEngine().getObject();
        return new DefaultProcessVariableHeaderMapper(this.activityExecutionFactoryBean().getObject());
    }

    @Bean
    public ProcessStartingOutboundChannelAdapter processStartingOutboundChannelAdapter() throws Throwable {
        ProcessStartingOutboundChannelAdapter processStartingOutboundChannelAdapter = new ProcessStartingOutboundChannelAdapter();
        processStartingOutboundChannelAdapter.setProcessEngine(this.processEngine().getObject());
        processStartingOutboundChannelAdapter.setProcessVariableHeaderMapper(headerMapper());
        return processStartingOutboundChannelAdapter;
    }
}
