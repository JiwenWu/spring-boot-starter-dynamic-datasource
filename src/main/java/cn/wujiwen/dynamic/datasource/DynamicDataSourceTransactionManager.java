package cn.wujiwen.dynamic.datasource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;

/**
 * Desc:
 *
 * @author wujw
 * @email jiwenwu@outlook.com
 * @date 2021/6/18
 */
public class DynamicDataSourceTransactionManager extends DataSourceTransactionManager {
    /**
     * 只读事务到读库，读写事务到写库
     *
     * @param transaction
     * @param definition
     */
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        //设置数据源
        boolean readOnly = definition.isReadOnly();
        if (readOnly) {
            DynamicDataSourceHolder.setDataSource(DynamicDataSourceEnum.READ);
        } else {
            DynamicDataSourceHolder.setDataSource(DynamicDataSourceEnum.WRITE);
        }
        super.doBegin(transaction, definition);
    }

    /**
     * 清理本地线程的数据源
     *
     * @param transaction
     */
    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        super.doCleanupAfterCompletion(transaction);
        DynamicDataSourceHolder.clearDataSource();
    }
}
