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

import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.activiti.adapter.ProcessStartingOutboundChannelAdapter;
import org.springframework.integration.activiti.gateway.AbstractActivityBehaviorMessagingGateway;
import org.springframework.integration.activiti.gateway.AsyncActivityBehaviorMessagingGateway;
import org.springframework.integration.activiti.gateway.SyncActivityBehaviorMessagingGateway;
import org.springframework.integration.activiti.mapping.DefaultProcessVariableHeaderMapper;
import org.springframework.integration.config.xml.AbstractInboundGatewayParser;
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
        registerBeanDefinitionParser("inbound-gateway", new InboundGatewayParser());
    }
}

class ProcessLaunchingOutboundChannelAdapterParser extends AbstractOutboundChannelAdapterParser {
    static private String HEADER_MAPPER_PROPERTY = "processVariableHeaderMapper";
    static private String MAPPED_PROCESS_VARIABLES_ATTR = "mapped-message-headers";

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
            builder.addPropertyReference(HEADER_MAPPER_PROPERTY, headerMapper);
        }

        if (StringUtils.hasText(mappedProcessVariables)) {
            BeanDefinitionBuilder headerMapperBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultProcessVariableHeaderMapper.class.getName());
            IntegrationNamespaceUtils.setValueIfAttributeDefined(headerMapperBuilder, element, MAPPED_PROCESS_VARIABLES_ATTR, "headerToProcessVariableNames");
            builder.addPropertyValue(HEADER_MAPPER_PROPERTY, headerMapperBuilder.getBeanDefinition());
        }

        return builder.getBeanDefinition();
    }
}

/**
 * Parser for the inbound gateway, which has two modes,
 * one synchronous and one asynchronous. The default is the asynchronous mode.
 *
 * @author Josh Long
 */
class InboundGatewayParser extends AbstractInboundGatewayParser {

    static private String HEADER_MAPPER_HEADER = "header-mapper" ;

    static private String MAPPED_OUTBOUND_MESSAGE_HEADERS = "mapped-outbound-message-headers";

    static private String MAPPED_INBOUND_MESSAGE_HEADERS = "mapped-inbound-process-variables";

    @Override
    protected boolean isEligibleAttribute(String attributeName) {
        return !attributeName.equals(MAPPED_INBOUND_MESSAGE_HEADERS) &&
               !attributeName.equals(MAPPED_OUTBOUND_MESSAGE_HEADERS) &&
               super.isEligibleAttribute(attributeName);
    }

    // bleargh cheating a bit to get access to the ParserContext
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        super.doParse(element, parserContext, builder);

        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "process-engine");

        TypedStringValue boolTrue = new TypedStringValue("true");

        // forget about header-mapper for now. simpler to focus on this
        BeanDefinitionBuilder headerMapperBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultProcessVariableHeaderMapper.class.getName());

        // inbound (e.g., from Activiti into SI)
        String inboundProcessVariablesToHeaders = IntegrationNamespaceUtils.getTextFromAttributeOrNestedElement(element, MAPPED_INBOUND_MESSAGE_HEADERS, parserContext);
        if (StringUtils.hasText(inboundProcessVariablesToHeaders)) {
            builder.addPropertyValue("forwardProcessVariablesAsMessageHeaders", boolTrue);
            IntegrationNamespaceUtils.setValueIfAttributeDefined(headerMapperBuilder, element, MAPPED_INBOUND_MESSAGE_HEADERS, "processVariableToHeaderNames");
        }

        // outbound (e.g., SI back to Activiti)
        String outboundHeadersToProcessVariables = IntegrationNamespaceUtils.getTextFromAttributeOrNestedElement(element, MAPPED_OUTBOUND_MESSAGE_HEADERS, parserContext);
        if (StringUtils.hasText(outboundHeadersToProcessVariables)) {
            builder.addPropertyValue("updateProcessVariablesFromReplyMessageHeaders", boolTrue);
            IntegrationNamespaceUtils.setValueIfAttributeDefined(headerMapperBuilder, element, MAPPED_OUTBOUND_MESSAGE_HEADERS, "headerToProcessVariableNames");
        }
        builder.addPropertyValue("headerMapper", headerMapperBuilder.getBeanDefinition());
    }


    @Override
    protected String getBeanClassName(Element element) {
        String synchronousAttribute = element.getAttribute("synchronous"); // default is false, as asynchronous is a safer, more powerful option.
        boolean synchronous = StringUtils.hasText(synchronousAttribute) && Boolean.parseBoolean(synchronousAttribute);

        Class<? extends AbstractActivityBehaviorMessagingGateway> mgwClass = synchronous ?
                                                                                     SyncActivityBehaviorMessagingGateway.class :
                                                                                     AsyncActivityBehaviorMessagingGateway.class;
        return mgwClass.getName();
    }


}
