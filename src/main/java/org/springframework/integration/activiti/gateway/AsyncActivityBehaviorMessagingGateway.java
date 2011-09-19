/*Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 */
package org.springframework.integration.activiti.gateway;

//~--- non-JDK imports --------------------------------------------------------

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.runtime.Execution;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.activiti.ActivitiConstants;
import org.springframework.integration.activiti.ProcessSupport;
import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Asynchronous inbound gateway. When the wait state that this gateway provides
 * is reached, the gateway sends a message <em>into</em> Spring Integration
 * on the {@link #requestChannel}, which can react to it like you might any
 * other <em>inbound</em> event (like receiving an email, or an AMQP message.).
 * The Activiti wait-state is paused, and stops execution, pending a
 * {@link org.activiti.engine.RuntimeService#signal(String)}, which this gateway
 * will do when a <em>reply</em> message is sent on the {@link #replyChannel}
 * <p/>
 * There is an alternative to this gateway - the {@link SyncActivityBehaviorMessagingGateway} -
 * that (for most situations) is identical, except that instead of pausing on the wait-state,
 * the thread stays open and the reply is expected back quickly, otherwise you risk starvation of the thread pool.
 * The benefit to this is that, from the perspective of the workflow engine, everything happens
 * inside of one transaction. If you're not concerned with having a wait-state execute like a regular state,
 * then ignore that one and prefer this asynchronous gateway.
 *
 * @author Josh Long
 * @see ReceiveTaskActivityBehavior the {@link ActivityBehavior} implementation that ships with Activiti that has the machinery to wake up when signaled
 * @see ProcessEngine the process engine instance is required to be able to use this namespace
 * @see org.activiti.spring.ProcessEngineFactoryBean - use this class to create the aforementioned ProcessEngine instance!
 *
 */
public class AsyncActivityBehaviorMessagingGateway extends AbstractActivityBehaviorMessagingGateway {

    protected MessageHandler replyMessageHandler = new MessageHandler() {
        private ProcessSupport.ProcessExecutionSignallerCallback processExecutionSignallerCallback =
                new ProcessSupport.ProcessExecutionSignallerCallback() {
                    public void setProcessVariable(ProcessEngine en, ActivityExecution ex, String k, Object o) {
                        en.getRuntimeService().setVariable(ex.getId(), k, o);
                    }

                    public void signal(ProcessEngine en, ActivityExecution ex) {
                        en.getRuntimeService().signal(ex.getId());
                    }
                };

        public void handleMessage(Message<?> message) throws MessagingException {
            String executionId =
                    (String) message.getHeaders().get(ActivitiConstants.WELL_KNOWN_EXECUTION_ID_HEADER_KEY);

            Assert.notNull(executionId, "the messages coming into this channel must have a header equal "
                                                + "to the value of ActivitiConstants.WELL_KNOWN_EXECUTION_ID_HEADER_KEY ("
                                                + ActivitiConstants.WELL_KNOWN_EXECUTION_ID_HEADER_KEY + ")");

            Execution execution = processEngine.getRuntimeService().createExecutionQuery().executionId(executionId).singleResult();
            ActivityExecution activityExecution = (ActivityExecution) execution;

            try {
                ProcessSupport.signalProcessExecution(processEngine, activityExecution,
                                                             processExecutionSignallerCallback, headerMapper, message);
            } catch (Exception e) {
                log.error(e);

                throw new RuntimeException(e);
            }
        }
    };

    @Override
    protected void onExecute(ActivityExecution execution) throws Exception {
        MessageBuilder<?> messageBuilder = this.doBasicOutboundMessageConstruction(execution);

        messageBuilder.setReplyChannel(this.replyChannel);
        this.messagingTemplate.send(this.requestChannel, messageBuilder.build());
    }

    @Override
    protected void onInit() throws Exception {
        ConsumerEndpointFactoryBean consumerEndpointFactoryBean = new ConsumerEndpointFactoryBean();

        consumerEndpointFactoryBean.setHandler(replyMessageHandler);
        consumerEndpointFactoryBean.setBeanClassLoader(ClassUtils.getDefaultClassLoader());
        consumerEndpointFactoryBean.setAutoStartup(false);
        consumerEndpointFactoryBean.setInputChannel(this.replyChannel);

        if (this.replyChannel instanceof PollableChannel) {
            PollerMetadata pollerMetadata = new PollerMetadata();

            pollerMetadata.setReceiveTimeout(10);
            consumerEndpointFactoryBean.setPollerMetadata(pollerMetadata);
        }

        consumerEndpointFactoryBean.setBeanFactory(this.beanFactory);
        consumerEndpointFactoryBean.setBeanName(this.beanName + "ConsumerEndpoint");
        consumerEndpointFactoryBean.afterPropertiesSet();
        consumerEndpointFactoryBean.start();
    }
}


