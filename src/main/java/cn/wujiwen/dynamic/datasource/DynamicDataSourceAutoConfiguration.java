package cn.wujiwen.dynamic.datasource;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Desc:
 *
 * @author wujw
 * @email jiwenwu@outlook.com
 * @date 2021/6/18
 */

@Configuration
@ConditionalOnProperty(prefix = "mybatis-dynamic",name = "enabled", havingValue = "true")
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceAutoConfiguration {
    private DynamicDataSourceProperties dynamicDataSourceProperties;

    public DynamicDataSourceAutoConfiguration(DynamicDataSourceProperties dynamicDataSourceProperties) {
        this.dynamicDataSourceProperties = dynamicDataSourceProperties;
    }

    @Primary
    @Bean("writeDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSourceProperties writeDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Primary
    @Bean("writeDataSource")
    public DataSource writeDataSource(@Qualifier("writeDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean("readDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSourceProperties readDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Bean("readDataSource")
    public DataSource readDataSource(@Qualifier("readDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    public DynamicDataSource dynamicDataSource(@Qualifier("writeDataSource") DataSource writeDataSource, @Qualifier("readDataSource") DataSource readDataSource) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setReadDataSource(readDataSource);
        dynamicDataSource.setWriteDataSource(writeDataSource);
        return dynamicDataSource;
    }

    @Bean
    public DynamicInterceptor dynamicInterceptor() {
        return new DynamicInterceptor();
    }

    @Bean
    public DataSourceTransactionManager dataSourceTransactionManager(DynamicDataSource dynamicDataSource) {
        DynamicDataSourceTransactionManager dynamicDataSourceTransactionManager = new DynamicDataSourceTransactionManager();
        dynamicDataSourceTransactionManager.setDataSource(dynamicDataSource);
        return dynamicDataSourceTransactionManager;
    }

    @Bean(name = "sqlSessionFactoryBean")
    public SqlSessionFactoryBean sqlSessionFactoryBean(DynamicDataSource dynamicDataSource, DynamicInterceptor dynamicInterceptor, Environment environment) throws IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        // 配置数据源
        sqlSessionFactoryBean.setDataSource(dynamicDataSource);
        // mapper xml 文件路径
        sqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(Objects.requireNonNull(environment.getProperty("mybatis.mapper-locations", "classpath*:/mapper/**/*.xml"))));
        // 插件
        sqlSessionFactoryBean.setPlugins(new Interceptor[]{dynamicInterceptor});
        return sqlSessionFactoryBean;
    }

    @Configuration
    @Import(DynamicDataSourceAutoConfiguration.AutoConfiguredMapperScannerRegistrar.class)
    @ConditionalOnClass({SqlSessionFactoryBean.class})
    public static class MapperScannerRegistrarNotFoundConfiguration implements InitializingBean {

        @Override
        public void afterPropertiesSet() throws Exception {

        }
    }


    public static class AutoConfiguredMapperScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar{
        private BeanFactory beanFactory;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
            builder.addPropertyValue("processPropertyPlaceHolders", true);
            builder.addPropertyValue( "annotationClass", Mapper.class);
            builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(packages));
            builder.addPropertyValue("sqlSessionFactoryBeanName","sqlSessionFactoryBean");
            registry.registerBeanDefinition(MapperScannerConfigurer.class.getName(), builder.getBeanDefinition());

        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }
    }

    @Bean(name = "writeJdbcTemplate")
    public JdbcTemplate writeJdbcTemplate(
            @Qualifier("writeDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "readJdbcTemplate")
    public JdbcTemplate readJdbcTemplate(
            @Qualifier("readDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
