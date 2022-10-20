package io.metersphere.api.dto.datacount.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoverageDTO {
    public String rateOfCoverage = "0%";
    public long coverage = 0;
    public long notCoverage = 0;
}
