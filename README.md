# MyBatis的读写分离插件
> 该插件的最大特点就是无入侵式，可以通过环境配置的方式，自由实现是否开启读写分离模式，原理则是通过判断sql的类型，在执行代码前，改变当前线程的datasource源


# 使用方式
由于是springboot-stater,则默认在SpringBoot环境下进行生效
1、pom.xml
```xml
    <dependency>
        <groupId>cn.wujiwen</groupId>
        <artifactId>spring-boot-starter-dynamic-datasource</artifactId>
        <version>1.0.0</version>
    </dependency>
```
由于本人并没有上传各种仓库，请自行下载代码打包即可

2、properties/yml配置
```properties
## 是否开启
mybatis-dynamic.enabled=true

spring.datasource.write.url=jdbc:mysql://*****
spring.datasource.write.password=****
spring.datasource.write.username=*****
spring.datasource.write.driver-class-name=com.mysql.cj.jdbc.Driver

spring.datasource.read.url=jdbc:mysql://*****
spring.datasource.read.password=****
spring.datasource.read.username=*****
spring.datasource.read.driver-class-name=com.mysql.cj.jdbc.Driver

```

接下来就可以在你已经生效的mybatis的环境下自由使用读写分离模式了
如果开启debug的日志级别，则可以看到每一条语句的执行时选取的是什么数据源