package io.metersphere.base.domain.ext;

import io.metersphere.base.domain.CustomFieldTestCase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
/**
 * 自定义字段关联表的统一操作类
 * 字段跟 CustomFieldIssues， CustomFieldTestCase 一样
 * 考虑到表中数据量较大，按类型拆分成多个表
 */
public class CustomFieldResource extends CustomFieldTestCase implements Serializable {
    private static final long serialVersionUID = 1L;
}
