package io.metersphere.sdk.dto.api.task;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.RsaUtils;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;

import java.io.Serial;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * 任务请求参数数据
 */
@Data
public class TaskRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String reportId;
    private String msUrl;
    private String kafkaConfig;
    private String minioConfig;
    private String poolId;

    // TODO 其它执行参数

    // 默认声明对象时获取配置参数
    public void setKafkaConfig(Map<String, String> kafkaConfig) {
        if (MapUtils.isNotEmpty(kafkaConfig)) {
            try {
                this.kafkaConfig = RsaUtils.publicEncrypt(JSON.toJSONString(kafkaConfig), this.getReportId());
            } catch (NoSuchAlgorithmException e) {
                throw new MSException(e);
            }
        }
    }

    public void setMinioConfig(Map<String, String> minioConfig) {
        if (MapUtils.isNotEmpty(minioConfig)) {
            try {
                this.minioConfig = RsaUtils.publicEncrypt(JSON.toJSONString(minioConfig), this.getReportId());
            } catch (NoSuchAlgorithmException e) {
                throw new MSException(e);
            }
        }
    }
}
