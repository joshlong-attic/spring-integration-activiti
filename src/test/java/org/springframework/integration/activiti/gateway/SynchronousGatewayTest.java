package org.springframework.integration.activiti.gateway;

import org.activiti.engine.test.Deployment;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("SynchronousGatewayTest-context.xml")
public class SynchronousGatewayTest extends AbstractGatewayTest {

    @Deployment(resources = "processes/si_gateway_example.bpmn20.xml")
    public void testSync() throws Throwable {
        this.doGatewayTesting();
    }
}
