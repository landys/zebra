package com.yiran.xingtian.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Xingtian on 2017-01-13.
 */
@Configuration
@ImportResource("classpath:data-source-single.xml")
public class MysqlDataSourceConfig {

}
