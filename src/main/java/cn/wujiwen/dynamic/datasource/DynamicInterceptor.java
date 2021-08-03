package cn.wujiwen.dynamic.datasource;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Desc:
 *
 * @author wujw
 * @email jiwenwu@outlook.com
 * @date 2021/6/18
 */
@Intercepts({@Signature(type = org.apache.ibatis.executor.Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = org.apache.ibatis.executor.Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class DynamicInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(DynamicInterceptor.class);

    private static final String REGEX = ".*insert\\u0020.*|.*delete\\u0020.*|.*update\\u0020.*";

    private static final Map<String, DynamicDataSourceEnum> CACHE_MAP = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        Object[] objects = invocation.getArgs();
        MappedStatement ms = (MappedStatement) objects[0];
        if (!synchronizationActive) {
            DynamicDataSourceEnum dynamicDataSourceEnum;
            if ((dynamicDataSourceEnum = CACHE_MAP.get(ms.getId())) == null) {
                //读方法
                if (ms.getSqlCommandType().equals(SqlCommandType.SELECT)) {
                    //!selectKey 为自增id查询主键(SELECT LAST_INSERT_ID() )方法，使用主库
                    if (ms.getId().contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)) {
                        dynamicDataSourceEnum = DynamicDataSourceEnum.WRITE;
                    } else {
                        BoundSql boundSql = ms.getSqlSource().getBoundSql(objects[1]);
                        String sql = boundSql.getSql().toLowerCase(Locale.CHINA).replaceAll("[\\t\\n\\r]", " ");
                        if (sql.matches(REGEX)) {
                            dynamicDataSourceEnum = DynamicDataSourceEnum.WRITE;
                        } else {
                            dynamicDataSourceEnum = DynamicDataSourceEnum.READ;
                        }
                    }
                } else {
                    dynamicDataSourceEnum = DynamicDataSourceEnum.WRITE;
                }
                CACHE_MAP.put(ms.getId(), dynamicDataSourceEnum);
            }
            logger.debug(" ===>> 方法【" + ms.getId() + "】使用【" + dynamicDataSourceEnum.name() + "】数据源");
            DynamicDataSourceHolder.setDataSource(dynamicDataSourceEnum);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
