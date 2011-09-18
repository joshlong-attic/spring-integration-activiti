package org.springframework.integration.activiti.util;

//~--- non-JDK imports --------------------------------------------------------

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * Factories a thread-safe {@link ActivityExecution} instance that can be used safely any time there is an overarching {@link ProcessInstance},
 * including for most cases supported by {@link org.springframework.integration.activiti.mapping.DefaultProcessVariableHeaderMapper}.
 *
 * @author Josh Long
 */
public class ActivityExecutionFactoryBean implements FactoryBean<ActivityExecution>, InitializingBean {
	private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
	private Log log = LogFactory.getLog(getClass());
	private ActivityExecution activityExecution;

	public ActivityExecution getObject() throws Exception {
		return this.activityExecution;
	}

	public Class<?> getObjectType() {
		return ActivityExecution.class;
	}

	public boolean isSingleton() {
		return false;
	}

	public void afterPropertiesSet() throws Exception {
		this.activityExecution = createSharedProcessInstance();
	}

	private void addInterfaces(ProxyFactory pf,Class cl){
		if(cl.isInterface())
		pf.addInterface(cl);
		for(Class c : cl.getInterfaces())
			pf.addInterface(c);
	}

	private ActivityExecution createSharedProcessInstance() {
		ProxyFactory proxyFactoryBean = new ProxyFactory(ActivityExecution.class, new MethodInterceptor() {
			public Object invoke(MethodInvocation methodInvocation) throws Throwable {
				String methodName = methodInvocation.getMethod().getName();

				log.info("method invocation for " + methodName + ".");

				if (methodName.equals("toString")) {
					return "SharedActivitiExecution";
				}

				ActivityExecution processInstance = Context.getExecutionContext().getExecution();
				Method method = methodInvocation.getMethod();
				Object[] args = methodInvocation.getArguments();
				Object result = method.invoke(processInstance, args);
				return result;
			}
		});


		addInterfaces(proxyFactoryBean, ActivityExecution.class);
		addInterfaces(proxyFactoryBean, ExecutionEntity.class);
		proxyFactoryBean.setProxyTargetClass(true);
		proxyFactoryBean.setTargetClass(ActivityExecution.class);

		return (ActivityExecution) proxyFactoryBean.getProxy(this.classLoader);
	}
}
