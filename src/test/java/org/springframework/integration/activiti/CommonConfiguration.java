package org.springframework.integration.activiti;

import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Configuration common to all the tests
 *
 * @author Josh Long
 */
public class CommonConfiguration {
    protected Log log = LogFactory.getLog(getClass());

    @Value("${db.url}")
    protected String url;

    @Value("${db.password}")
    protected String pw;

    @Value("${db.user}")
    protected String user;

    @PostConstruct
    public void setup() {
        log.debug("starting up " + getClass().getName());
    }

    /**
     * clients can override this
     */
    protected String getDatabaseSchemaUpdate() {
        return SpringProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;
    }

    @Bean
    public ProcessEngineFactoryBean processEngine() {
        ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();

        SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
        configuration.setTransactionManager(dataSourceTransactionManager());
        configuration.setDatabaseType("h2");
        configuration.setJobExecutorActivate(false);
        configuration.setDataSource(targetDataSource());
        configuration.setDatabaseSchemaUpdate(getDatabaseSchemaUpdate());
        processEngineFactoryBean.setProcessEngineConfiguration(configuration);
        return processEngineFactoryBean;
    }

    @Bean
    public DataSource targetDataSource() {
        TransactionAwareDataSourceProxy transactionAwareDataSourceProxy = new TransactionAwareDataSourceProxy();
        SimpleDriverDataSource simpleDriverDataSource = new SimpleDriverDataSource();
        simpleDriverDataSource.setPassword(this.pw);
        simpleDriverDataSource.setUsername(this.user);
        simpleDriverDataSource.setUrl(this.url);
        simpleDriverDataSource.setDriverClass(org.h2.Driver.class);
        transactionAwareDataSourceProxy.setTargetDataSource(simpleDriverDataSource);
        return transactionAwareDataSourceProxy;
    }

    @Bean
    public DataSourceTransactionManager dataSourceTransactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(this.targetDataSource());
        return dataSourceTransactionManager;
    }
}
