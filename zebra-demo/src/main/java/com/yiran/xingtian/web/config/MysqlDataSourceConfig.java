package com.yiran.xingtian.web.config;

import com.dianping.zebra.group.jdbc.GroupDataSource;
import com.dianping.zebra.shard.jdbc.ShardDataSource;
import com.dianping.zebra.shard.router.RouterBuilder;
import com.dianping.zebra.shard.router.builder.XmlResourceRouterBuilder;
import com.google.common.collect.Maps;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Xingtian on 2017-01-13.
 */
@Configuration
public class MysqlDataSourceConfig {
    private String poolType = "c3p0";

    private int minPoolSize = 50;

    private int maxPoolSize = 200;

    private int initialPoolSize = 50;

    private int connectionTimeout = 120000;

    private int parallelPoolCoreSize = 50;

    private int parallelPoolMaxSize = 512;

    private int parallelWorkQueueSize = 5000;

    private int parallelExecuteTimeOut = 100000;

    /**
     * ShardDataSource for api server.
     * ShardDataSource的close会自动调用所有GroupDataSource的close, 所以这里不需要让spring去close每个GroupDataSource,
     * 也即每个GroupDataSource不需要单独定义成beans。
     * 并且单独定义也会触发这个issue: https://github.com/mybatis/spring/issues/186
     * @return
     */
    @Bean(destroyMethod = "close", name = "dataSource")
    public DataSource dataSource() {
        Map<String, DataSource> dataSourcePool = Maps.newHashMap();
        dataSourcePool.put("id0", createGroupDataSource("olmeca.dbs1"));
        dataSourcePool.put("id1", createGroupDataSource("olmeca.dbs2"));
        dataSourcePool.put("id2", createGroupDataSource("olmeca.dbs3"));
        dataSourcePool.put("id3", createGroupDataSource("olmeca.dbs4"));
        return createShardDataSource(dataSourcePool);
    }

    public RouterBuilder routerFactory() {
        return new XmlResourceRouterBuilder("zebra-router-rule.xml");
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(@Qualifier("dataSource") DataSource dataSource) {
        return createSqlSessionFactoryBean(dataSource);
    }

    private SqlSessionFactoryBean createSqlSessionFactoryBean(DataSource ds) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(ds);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.yiran.xingtian.common.model");
        Properties properties = new Properties();
        properties.setProperty("dialect", "mysql");
        sqlSessionFactoryBean.setConfigurationProperties(properties);
        return sqlSessionFactoryBean;
    }

    private DataSource createShardDataSource(Map<String, DataSource> dataSourcePool) {
        ShardDataSource ds = new ShardDataSource();
        ds.setDataSourcePool(dataSourcePool);
        ds.setRouterFactory(routerFactory());
        ds.setParallelCorePoolSize(parallelPoolCoreSize);
        ds.setParallelMaxPoolSize(parallelPoolMaxSize);
        ds.setParallelWorkQueueSize(parallelWorkQueueSize);
        ds.setParallelExecuteTimeOut(parallelExecuteTimeOut);
        ds.init();
        return ds;
    }

    private DataSource createGroupDataSource(String jdbcRef) {
        GroupDataSource ds = new GroupDataSource();
        ds.setJdbcRef(jdbcRef);
        ds.setDriverClass("com.mysql.jdbc.Driver");
        ds.setInitialPoolSize(initialPoolSize);
        ds.setMinPoolSize(minPoolSize);
        ds.setMaxPoolSize(maxPoolSize);
        ds.setPoolType(poolType);
        ds.setMaxIdleTime(connectionTimeout);
        ds.setIdleConnectionTestPeriod(60);
        ds.setAcquireRetryAttempts(3);
        ds.setAcquireRetryDelay(300);
        ds.setMaxStatements(0);
        ds.setMaxStatementsPerConnection(100);
        ds.setNumHelperThreads(6);
        ds.setMaxAdministrativeTaskTime(5);
        ds.setPreferredTestQuery("SELECT 1");
        ds.init();
        return ds;
    }
}
