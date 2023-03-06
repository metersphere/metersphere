package io.metersphere.api.dto;

import io.metersphere.dto.RequestResult;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FakeErrorLibraryDTO {
    private List<String> errorCodeList;
    private RequestResult result;
    private String requestStatus;

    public FakeErrorLibraryDTO() {
        this.errorCodeList = new ArrayList<>();
    }

    public String getErrorCodeStr() {
        if (CollectionUtils.isNotEmpty(this.errorCodeList)) {
            String errorCodeStr = StringUtils.join(this.errorCodeList, ";");
            return errorCodeStr;
        } else {
            return StringUtils.EMPTY;
        }

    }
}
