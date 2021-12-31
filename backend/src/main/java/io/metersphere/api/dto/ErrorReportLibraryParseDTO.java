package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ErrorReportLibraryParseDTO {
    public List<String> errorCodeList;

    public ErrorReportLibraryParseDTO() {
        this.errorCodeList = new ArrayList<>();
    }

    public String getErrorCodeStr(){
        if(CollectionUtils.isNotEmpty(this.errorCodeList)){
            String errorCodeStr = StringUtils.join(this.errorCodeList,";");
            //控制字符串长度
            if(errorCodeStr.length() > 10){
                return errorCodeStr.substring(0,10)+"...";
            }else {
                return errorCodeStr;
            }
        }else {
            return "";
        }

    }
}
