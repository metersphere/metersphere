package io.metersphere.plan.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanApiCaseBatchRunRequest extends TestPlanApiCaseBatchRequest implements Serializable {

    private static final long serialVersionUID = 1L;
}
