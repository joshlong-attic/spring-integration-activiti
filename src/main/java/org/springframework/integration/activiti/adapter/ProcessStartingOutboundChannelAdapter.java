/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.springframework.integration.activiti.adapter;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.integration.Message;
import org.springframework.integration.activiti.ActivitiConstants;
import org.springframework.integration.activiti.mapping.ProcessVariableHeaderMapper;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageHandler;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Supports spawning a {@link org.activiti.engine.runtime.ProcessInstance} as a result of a trigger {@link org.springframework.integration.Message}.
 * The component also supports propagating headers as processVariables. This support is similar
 * to the classic EIP book's "Process Manager" pattern. Thanks to Mark Fisher for the idea.
 *
 * @author Josh Long
 * @since 2.1
 */
public class ProcessStartingOutboundChannelAdapter extends IntegrationObjectSupport implements MessageHandler {

    private String processHeaderMustNotBeNullMessage = String.format(
                                                                            "you must specify a processDefinitionName, either through " +
                                                                                    "an inbound header mapped to the key '%s' " +
                                                                                    "(ActivitiConstants.WELL_KNOWN_PROCESS_DEFINITION_NAME_HEADER_KEY), " +
                                                                                    ", or on the 'process-definition-name' property of this adapter",
                                                                            ActivitiConstants.WELL_KNOWN_PROCESS_DEFINITION_NAME_HEADER_KEY);

    /**
     * A reference to the {@link ProcessEngine} (see {@link    org.activiti.spring.ProcessEngineFactoryBean}
     */
    private ProcessEngine processEngine;

    private ProcessVariableHeaderMapper processVariableHeaderMapper;

    public void setProcessVariableHeaderMapper(ProcessVariableHeaderMapper processVariableHeaderMapper) {
        this.processVariableHeaderMapper = processVariableHeaderMapper;
    }

    /**
     * Do you want all flows that come into this component to launch the same business process? Hard code the process name here.
     * If this is null, the component will expect a well known header value and use that to spawn the process definition name.
     */
    private String processDefinitionName;

    @Override
    protected void onInit() throws Exception {
        Assert.notNull(this.processEngine, "'processEngine' must not be null!");
    }

    @SuppressWarnings("unused")
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @SuppressWarnings("unused")
    public void setProcessDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
    }

    public void handleMessage(Message<?> message) {
        Map<String, Object> processVariablesFromHeaders = new HashMap<String, Object>();

        String processName = (String) message.getHeaders().get(ActivitiConstants.WELL_KNOWN_PROCESS_DEFINITION_NAME_HEADER_KEY);

        if ((processName == null) || !StringUtils.hasText(processName)) {
            processName = this.processDefinitionName;
        }

        processVariableHeaderMapper.fromHeaders(message.getHeaders(), processVariablesFromHeaders);

        Assert.notNull(processName, processHeaderMustNotBeNullMessage);

        ProcessInstance pi = processEngine.getRuntimeService().startProcessInstanceByKey(processName, processVariablesFromHeaders);
        logger.debug("started process instance " + pi.getProcessDefinitionId() + "having business Id of " + pi.getBusinessKey());
    }
}

