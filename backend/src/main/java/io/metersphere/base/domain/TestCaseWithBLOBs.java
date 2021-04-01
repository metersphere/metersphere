package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TestCaseWithBLOBs extends TestCase implements Serializable {
    private String remark;

    private String steps;   //与TestCaseExcelData里的属性名不一致，BeanUtils.copyBean()复制不了值，需要手动赋值

    private static final long serialVersionUID = 1L;
}