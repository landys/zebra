package com.yiran.xingtian.web;

import com.dianping.cat.Cat;
import com.dianping.cat.servlet.CatFilter;
import com.dianping.zebra.dao.mybatis.ZebraMapperScannerConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;

/**
 * Created by Xingtian on 2017-01-13.
 */
@SpringBootApplication
public class ZebraWebApplication {
    @Bean
    public ZebraMapperScannerConfigurer zebraShardMapperScannerConfigurer() {
        ZebraMapperScannerConfigurer configurer = new ZebraMapperScannerConfigurer();
        configurer.setBasePackage("com.yiran.xingtian.common.mapper");
        configurer.setSqlSessionFactoryBeanName("sqlSessionFactoryBean");
        // we don't use async dao, so minimal the pool.
        configurer.setCorePoolSize("1");
        configurer.setMaxPoolSize("1");
        configurer.setQueueSize("1");
        return configurer;
    }

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
