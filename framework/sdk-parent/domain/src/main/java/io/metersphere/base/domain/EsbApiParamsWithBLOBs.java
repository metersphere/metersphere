package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EsbApiParamsWithBLOBs extends EsbApiParams implements Serializable {
    private String dataStruct;

    private String frontedScript;

    private String responseDataStruct;

    private String backedScript;

    private static final long serialVersionUID = 1L;
}