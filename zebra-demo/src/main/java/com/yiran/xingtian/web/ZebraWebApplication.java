package com.yiran.xingtian.web;

import com.dianping.cat.Cat;
import com.dianping.cat.servlet.CatFilter;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.File;
import java.util.Properties;
//import org.apache.ibatis.plugin.Interceptor;

/**
 * Created by Xingtian on 2017-01-13.
 */
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@MapperScan("com.yiran.xingtian.web.mapper")
@EnableTransactionManagement
public class ZebraWebApplication {
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) throws Exception {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        Properties properties = new Properties();
        properties.setProperty("dialect", "mysql");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mybatis/*Mapper.xml"));
        sqlSessionFactoryBean.setConfigurationProperties(properties);
        //sqlSessionFactoryBean.setPlugins(new Interceptor[]{pageInterceptorBean()});
        return sqlSessionFactoryBean;
    }

//    @Bean
//    public SqlSessionTemplate sqlSessionTemplate(DataSource dataSource) throws Exception {
//        SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactoryBean(dataSource).getObject()) {
//            @Override
//            public void close() {
//            }
//        };
//        return sqlSessionTemplate;
//    }

    public static void main(String[] args) {
        SpringApplication.run(ZebraWebApplication.class, args);
    }

    @Bean
    // 设置cat filter，用于Http请求的打点
    public CatFilter catFilter() {
        Cat.initialize(new File(Cat.getCatHome(), "client.xml"));
        return new CatFilter();
    }
}
