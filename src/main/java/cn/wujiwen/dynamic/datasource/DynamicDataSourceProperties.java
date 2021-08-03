package cn.wujiwen.dynamic.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Desc:
 *
 * @author wujw
 * @email jiwenwu@outlook.com
 * @date 2021/6/18
 */
@ConfigurationProperties(prefix = "mybatis-dynamic")
public class DynamicDataSourceProperties {

    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

}
