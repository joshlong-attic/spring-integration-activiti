package org.springframework.integration.activiti.namespace.outbound;


import org.activiti.engine.ProcessEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.activiti.ActivitiConstants;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Josh Long
 */
@ContextConfiguration("NamespaceOutboundChannelAdapterTest-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class NamespaceOutboundChannelAdapterTest {

    //@Value("#{triggerChannel}")
    @Autowired @Qualifier("triggerChannel")
    private MessageChannel messageChannel;

    @Autowired
    private ProcessEngine pe;

    private MessagingTemplate messagingTemplate = new MessagingTemplate();

        @Test
        public void testOutboundAdapter() throws Throwable {

            pe.getRepositoryService().createDeployment().addClasspathResource("processes/hello.bpmn20.xml").deploy();

            Message<?> msg = MessageBuilder.withPayload("hello, from " + System.currentTimeMillis())
                                     .setHeader(ActivitiConstants.WELL_KNOWN_PROCESS_DEFINITION_NAME_HEADER_KEY, "hello")
                                     .setHeader("customerId", 434L)
                                     .setHeader("customerAge",  59)
                                     .setHeader("customerFirstName", "Josh")
                                     .setHeader("customerLastName", "Long")
                                     .build();
            messagingTemplate.send(this.messageChannel, msg);
        }


}
