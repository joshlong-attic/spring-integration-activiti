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
package org.springframework.integration.activiti.gateway;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.integration.Message;
import org.springframework.integration.activiti.ProcessSupport;
import org.springframework.integration.support.MessageBuilder;

public class SyncActivityBehaviorMessagingGateway extends AbstractActivityBehaviorMessagingGateway {

    @Override
    protected void onExecute(ActivityExecution ex) throws Exception {
        MessageBuilder<?> mb = doBasicOutboundMessageConstruction(ex);
        Message<?> reply = this.messagingTemplate.sendAndReceive(this.requestChannel, mb.build());
        ProcessSupport.signalProcessExecution(this.processEngine, ex, new TransactionAwareProcessExecutionSignallerCallback(), headerMapper, reply);
        leave(ex); // undo the wait state nature of this class by explicitly leaving now
    }

    static class TransactionAwareProcessExecutionSignallerCallback implements ProcessSupport.ProcessExecutionSignallerCallback {
        public void signal(ProcessEngine en, ActivityExecution ex) {
            // noop since effectively we're dismantling the wait-stateiness of the clients of this class
        }

        public void setProcessVariable(ProcessEngine en, ActivityExecution ex, String k, Object o) {
            ex.setVariable(k, o);
        }
    }

    @Override
    protected void onInit() throws Exception {
        // noop
    }
}
