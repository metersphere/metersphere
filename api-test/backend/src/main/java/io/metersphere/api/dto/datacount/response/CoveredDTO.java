package io.metersphere.api.dto.datacount.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoveredDTO {
    public String rateOfCovered = "0%";
    public long covered = 0;
    public long notCovered = 0;
}
