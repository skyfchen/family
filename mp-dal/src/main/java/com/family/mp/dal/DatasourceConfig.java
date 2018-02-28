package com.family.mp.dal;

import com.github.pagehelper.PageInterceptor;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "com.family.mp.dal", sqlSessionTemplateRef = "sqlSessionTemplate")
public class DatasourceConfig {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${c3p0.jdbcUrl}")
    private String jdbcUrl;

    @Value("${c3p0.driverClass:oracle.jdbc.driver.OracleDriver}")
    private String driverClass;

    @Value("${c3p0.user}")
    private String user;

    @Value("${c3p0.password}")
    private String password;

    @Value("${c3p0.initialPoolSize:5}")
    private int initialPoolSize;

    @Value("${c3p0.minPoolSize:5}")
    private int minPoolSize;

    @Value("${c3p0.maxPoolSize:15}")
    private int maxPoolSize;

    @Value("${c3p0.maxIdleTime:86400}")
    private int maxIdleTime;

    @Value("${c3p0.acquireIncrement:3}")
    private int acquireIncrement;

    @Value("${c3p0.idleConnectionTestPeriod:60}")
    private int idleConnectionTestPeriod;

    @Value("${c3p0.checkoutTimeout:300000}")
    private int checkoutTimeout;

    @Value("${c3p0.preferredTestQuery:select 1 from dual}")
    private String preferredTestQuery;

    @Value("${c3p0.maxStatements:450}")
    private int maxStatements;

    @Value("${c3p0.maxStatementsPerConnection:30}")
    private int maxStatementsPerConnection;

    @Value("${c3p0.statementCacheNumDeferredCloseThreads:1}")
    private int statementCacheNumDeferredCloseThreads;

    @Bean(destroyMethod = "close", initMethod = "getConnection")
    public DataSource dataSource() throws PropertyVetoException {

        ComboPooledDataSource dataSource = new ComboPooledDataSource(true);
        dataSource.setDriverClass(driverClass);
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        dataSource.setInitialPoolSize(initialPoolSize);
        dataSource.setMinPoolSize(minPoolSize);
        dataSource.setMaxPoolSize(maxPoolSize);
        dataSource.setMaxIdleTime(maxIdleTime);
        dataSource.setAcquireIncrement(acquireIncrement);
        dataSource.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
        dataSource.setCheckoutTimeout(checkoutTimeout);
        dataSource.setPreferredTestQuery(preferredTestQuery);
        dataSource.setMaxStatements(maxStatements);
        dataSource.setMaxStatementsPerConnection(maxStatementsPerConnection);
        dataSource.setStatementCacheNumDeferredCloseThreads(statementCacheNumDeferredCloseThreads);

        return dataSource;
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
        return sqlSessionTemplate;
    }

    @Autowired
    PageInterceptor pageInterceptor;

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("com.family.mp.dal.mapper,com.family.mp.dal.repository");
        String[] mapperLocations = new String[2];
        mapperLocations[0] = "classpath*:com/family/mp/dal/mapper/**/*Mapper.xml";
        mapperLocations[1] = "classpath*:com/family/mp/dal/repository/**/*.xml";
        sqlSessionFactoryBean.setMapperLocations(resolveMapperLocations(mapperLocations));
        sqlSessionFactoryBean.setPlugins(new Interceptor[]{pageInterceptor});
        return sqlSessionFactoryBean.getObject();
    }

    public Resource[] resolveMapperLocations(String[] mapperLocations) {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = new ArrayList<Resource>();
        if (mapperLocations != null) {
            for (String mapperLocation : mapperLocations) {
                try {
                    Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }


    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(transactionManager);
        return transactionTemplate;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }


}
