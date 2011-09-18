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

import org.activiti.engine.test.Deployment;
import org.springframework.test.context.ContextConfiguration;

/**
 * This component demonstrates creating a {@link org.springframework.integration.activiti.gateway.AsyncActivityBehaviorMessagingGateway} (factoried from Spring)
 * and exposed for use in a BPMN 2 process.
 *
 * @author Josh Long
 */
@ContextConfiguration("AsynchronuousGatewayTest-context.xml")
public class AsynchronousGatewayTest extends AbstractGatewayTest {

  @Deployment(resources = "processes/si_gateway_example.bpmn20.xml")
  public void testAsync() throws Throwable {
    this.doGatewayTesting();
  }
}
