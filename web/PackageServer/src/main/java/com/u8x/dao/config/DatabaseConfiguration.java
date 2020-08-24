package com.u8x.dao.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.u8x.common.XLogger;
import com.u8x.controller.AdminController;
import com.u8x.utils.FileUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by guofeng.qin on 2017/2/17.
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.u8x.dao.mapper")
public class DatabaseConfiguration {

    private static final XLogger logger = XLogger.getLogger(DatabaseConfiguration.class);

    @Autowired
    private Environment env;



    private String getEnvProp(RelaxedPropertyResolver resolver, String key) {
        return resolver.getProperty(key);
    }

    private int toInt(String v) {
        return Integer.parseInt(v.trim());
    }

    private boolean toBoolean(String v) {
        return Boolean.valueOf(v.trim());
    }

    private long toLong(String v) {
        return Long.valueOf(v.trim());
    }


    @Bean
    @Primary
    public DataSource dataSource() throws SQLException {

        return dataSourceForMysql();
    }


    private DataSource dataSourceForMysql() throws SQLException {

        DruidDataSource dataSource = new DruidDataSource();

        RelaxedPropertyResolver primaryResolver = new RelaxedPropertyResolver(env, "datasource.primary.");
        dataSource.setDriverClassName(getEnvProp(primaryResolver, "driverClassName"));
        dataSource.setUrl(getEnvProp(primaryResolver, "url"));
        dataSource.setUsername(getEnvProp(primaryResolver, "username"));
        dataSource.setPassword(getEnvProp(primaryResolver, "password"));

        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(env, "spring.datasource.");
        dataSource.setInitialSize(toInt(getEnvProp(resolver, "initialSize")));
        dataSource.setMinIdle(toInt(getEnvProp(resolver, "minIdle")));
        dataSource.setMaxActive(toInt(getEnvProp(resolver, "maxActive")));
        dataSource.setMaxWait(toInt(getEnvProp(resolver, "maxWait")));
        dataSource.setTimeBetweenEvictionRunsMillis(toLong(getEnvProp(resolver, "timeBetweenEvictionRunsMillis")));
        dataSource.setMinEvictableIdleTimeMillis(toLong(getEnvProp(resolver, "minEvictableIdleTimeMillis")));
        dataSource.setValidationQuery(getEnvProp(resolver, "validationQuery"));
        dataSource.setTestWhileIdle(toBoolean(getEnvProp(resolver, "testWhileIdle")));
        dataSource.setTestOnBorrow(toBoolean(getEnvProp(resolver, "testOnBorrow")));
        dataSource.setTestOnReturn(toBoolean(getEnvProp(resolver, "testOnReturn")));
        dataSource.setPoolPreparedStatements(toBoolean(getEnvProp(resolver, "poolPreparedStatements")));
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(toInt(getEnvProp(resolver, "maxPoolPreparedStatementPerConnectionSize")));
        dataSource.setFilters(getEnvProp(resolver, "filters"));
        return dataSource;
    }


//    private DataSource dataSourceForSqlite() throws SQLException {
//
//        RelaxedPropertyResolver primaryResolver = new RelaxedPropertyResolver(env, "u8x.package.");
//        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//        dataSourceBuilder.driverClassName(getEnvProp(primaryResolver, "driverClassName"));
//
//        String dbUrlPath = FileUtils.formatPath(getEnvProp(primaryResolver, "toolPath"));
//        dbUrlPath += "/config/local/db/config.db";
//        dbUrlPath = "jdbc:sqlite:"+dbUrlPath;
//
//        logger.debug("sqlite db url str:{}", dbUrlPath);
//
//        dataSourceBuilder.url(dbUrlPath);
//        dataSourceBuilder.type(SQLiteDataSource.class);
//        return dataSourceBuilder.build();
//    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();

        sqlSessionFactoryBean.setDataSource(dataSource());

        return sqlSessionFactoryBean.getObject();
    }
}
