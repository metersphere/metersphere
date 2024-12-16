package io.metersphere.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusPercentDTO {

    @Schema(description =  "状态")
    private String status;

    @Schema(description =  "数量")
    private Integer count;

    @Schema(description =  "百分比")
    private String percentValue;


    public void setPercentValue(String percentValue) {
        int i = percentValue.indexOf(".");
        if (i > 0) {
            String substring = percentValue.substring(i);
            if (StringUtils.equalsIgnoreCase(substring, ".00%")) {
                percentValue = percentValue.substring(0, i)+"%";
            }
        }
        this.percentValue = percentValue;
    }
}
