/*
 * Copyright 2010 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.springframework.integration.activiti.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.activiti.adapter.ProcessStartingOutboundChannelAdapter;
import org.springframework.integration.activiti.mapping.DefaultProcessVariableHeaderMapper;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Provides namespace support for all the Activiti support
 *
 * @author Josh Long
 * @since 2.1
 */
@SuppressWarnings("unused")
public class ActivitiNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("outbound-channel-adapter", new ProcessLaunchingOutboundChannelAdapterParser());
    }
}

class ProcessLaunchingOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {
    static private String HEADER_MAPPER_PROPERTY = "processVariableHeaderMapper";
    static private String MAPPED_PROCESS_VARIABLES_ATTR = "mapped-process-variables";

    @Override
    protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ProcessStartingOutboundChannelAdapter.class.getName());

        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "process-engine");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "process-definition-name");

        // -------------------------------------------------------------
        // HEADER MAPPERS
        String headerMapper = element.getAttribute("header-mapper");
        String mappedProcessVariables = IntegrationNamespaceUtils.getTextFromAttributeOrNestedElement(element, MAPPED_PROCESS_VARIABLES_ATTR, parserContext);
        if (StringUtils.hasText(headerMapper)) {
            if (StringUtils.hasText(mappedProcessVariables)) {
                parserContext.getReaderContext().error("the 'mapped-process-variables' attribute is not allowed when a 'header-mapper' has been specified", parserContext.extractSource(element));
            }
            builder.addPropertyReference(HEADER_MAPPER_PROPERTY , headerMapper);
        }

        if (StringUtils.hasText(mappedProcessVariables)) {
            BeanDefinitionBuilder headerMapperBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultProcessVariableHeaderMapper.class.getName());
            IntegrationNamespaceUtils.setValueIfAttributeDefined(headerMapperBuilder, element, MAPPED_PROCESS_VARIABLES_ATTR, "headerToProcessVariableNames");
            builder.addPropertyValue(HEADER_MAPPER_PROPERTY , headerMapperBuilder.getBeanDefinition());
        }

        return builder.getBeanDefinition();
    }
}


//    public void init() {
//     //   this.registerBeanDefinitionParser("inbound-asyncGateway", new ActivitiInboundGatewayParser());
//       // this.registerBeanDefinitionParser("adapter-channel-adapter", new ActivitiOutboundChannelAdapterParser());
//    }

/*  private static class ActivitiOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {
        @Override
        protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ProcessStartingOutboundChannelAdapter.class.getName());
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "process-engine");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "process-definition-name");
            return builder.getBeanDefinition();
        }
    }

    private static class ActivitiInboundGatewayParser extends AbstractSingleBeanDefinitionParser {
        @Override
        protected String getBeanClassName(Element element) {
            return null;//ActivityBehaviorMessagingGatewayFactoryBean.class.getName();
        }

        @Override
        protected boolean shouldGenerateIdAsFallback() {
            return true;
        }

        @Override
        protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "request-channel");
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "reply-channel");
            IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "process-engine");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "update-process-variables-from-reply-message-headers");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "async");
            IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "forward-process-variables-as-message-headers");
        }
    }
}*/
