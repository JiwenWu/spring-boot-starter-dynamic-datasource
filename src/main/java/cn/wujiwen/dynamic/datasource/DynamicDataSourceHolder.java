package cn.wujiwen.dynamic.datasource;

/**
 * Desc:
 *
 * @author wujw
 * @email jiwenwu@outlook.com
 * @date 2021/6/18
 */
public class DynamicDataSourceHolder {
    private static final ThreadLocal<DynamicDataSourceEnum> HOLDER = new ThreadLocal<>();


    private DynamicDataSourceHolder(){
    }

    public static void setDataSource(DynamicDataSourceEnum dynamicDataSourceEnum) {
        HOLDER.set(dynamicDataSourceEnum);
    }

    public static DynamicDataSourceEnum getDataSource() {
        return HOLDER.get();
    }

    public static void clearDataSource() {
        HOLDER.remove();
    }
}
