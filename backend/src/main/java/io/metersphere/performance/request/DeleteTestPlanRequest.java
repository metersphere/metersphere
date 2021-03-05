package io.metersphere.performance.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteTestPlanRequest extends TestPlanRequest {
    /**
     * 是否强制删除（删除项目时不检查关联关系，强制删除资源）
     */
    private boolean forceDelete = false;
}
