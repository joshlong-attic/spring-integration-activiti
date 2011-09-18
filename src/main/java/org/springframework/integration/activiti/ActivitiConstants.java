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
package org.springframework.integration.activiti;

/**
 * Represents header keys common to the bidirectional interaction with Spring Integration and Activiti
 *
 * @author Josh Long
 */
public abstract class ActivitiConstants {

	public static final String WELL_KNOWN_ACTIVITY_ID_HEADER_KEY = "activiti_spring_integration_activityId";

	/**
	 * In order for the asyncGateway to correctly signalProcessExecution execution to Activiti, it needs to the executionId so that it can look up the {@link	org.activiti.engine.runtime.Execution} instance.
	 * the <code>executionId</code> is expected to be under this header.
	 */
	public static final String WELL_KNOWN_EXECUTION_ID_HEADER_KEY = "activiti_spring_integration_executionId";

	public static final String WELL_KNOWN_PROCESS_DEFINITION_ID_HEADER_KEY = "activiti_spring_integration_processDefinitionId";

	/**
	 * This is the key under which we will look up the custom <code>processDefinitionName</code> up. This value will be used to spawn
	 */
	public static final String WELL_KNOWN_PROCESS_DEFINITION_NAME_HEADER_KEY = "activiti_spring_integration_processDefinitionName";

	public static final String WELL_KNOWN_PROCESS_INSTANCE_ID_HEADER_KEY = "activiti_spring_integration_processInstanceId";

	/**
	 * Assuming #updateProcessVariablesFromReplyMessageHeaders is true, then any {@link org.springframework.integration.MessageHeaders} header key that starts with String will be propagated as an Activiti process variable.
	 */
	public static final String WELL_KNOWN_SPRING_INTEGRATION_HEADER_PREFIX = "activiti_spring_integration_";
}

