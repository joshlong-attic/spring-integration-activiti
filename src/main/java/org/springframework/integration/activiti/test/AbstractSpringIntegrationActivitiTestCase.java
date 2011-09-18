package org.springframework.integration.activiti.test;

import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.spring.impl.test.SpringActivitiTestCase;

/**
 * @author Josh Long
 * @since 5.3
 */
public class AbstractSpringIntegrationActivitiTestCase extends SpringActivitiTestCase {
	protected void assertAndEnsureCleanDb() throws Throwable {
		CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutorTxRequired();
		commandExecutor.execute(new Command<Object>() {
			public Object execute(CommandContext commandContext) {
				DbSqlSession session = commandContext.getSession(DbSqlSession.class);
				session.dbSchemaDrop();
				session.dbSchemaCreate();
				return null;
			}
		});
	}
}
