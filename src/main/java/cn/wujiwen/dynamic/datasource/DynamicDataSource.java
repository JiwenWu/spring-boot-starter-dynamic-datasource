package cn.wujiwen.dynamic.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 *
 * @author wujw
 * @email jiwenwu@outlook.com
 * @date 2021/6/18
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    /**
     * 写数据源
     */
    private Object writeDataSource;
    /**
     * 读数据源
     */
    private Object readDataSource;

    @Override
    public void afterPropertiesSet() {
        if (this.writeDataSource == null) {
            throw new IllegalArgumentException("Property 'writeDataSource' is required");
        }
        setDefaultTargetDataSource(writeDataSource);
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DynamicDataSourceEnum.WRITE.name(), writeDataSource);
        if (readDataSource != null) {
            targetDataSources.put(DynamicDataSourceEnum.READ.name(), readDataSource);
        }
        setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {

        DynamicDataSourceEnum dynamicDataSourceEnum = DynamicDataSourceHolder.getDataSource();

        if (dynamicDataSourceEnum == null
                || dynamicDataSourceEnum == DynamicDataSourceEnum.WRITE) {
            return DynamicDataSourceEnum.WRITE.name();
        }

        return readDataSource != null ? DynamicDataSourceEnum.READ.name() : DynamicDataSourceEnum.WRITE.name();
    }

    public void setWriteDataSource(Object writeDataSource) {
        this.writeDataSource = writeDataSource;
    }

    public void setReadDataSource(Object readDataSource) {
        this.readDataSource = readDataSource;
    }
}
