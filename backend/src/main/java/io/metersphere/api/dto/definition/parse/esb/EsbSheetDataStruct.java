package io.metersphere.api.dto.definition.parse.esb;

import io.metersphere.api.dto.automation.EsbDataStruct;
import io.metersphere.commons.exception.MSException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/3/22 9:42 下午
 * @Description
 */
@Getter
@Setter
public class EsbSheetDataStruct {
    private String serviceName;
    private String serviceDesc;
    private List<EsbDataStruct> requestList = new ArrayList<>();
    private List<EsbDataStruct> responseList = new ArrayList<>();

    //单个接口内也可能有报文头中要增加的数据
    private List<EsbDataStruct> reqHeadList = new ArrayList<>();
    private List<EsbDataStruct> rspHeadList = new ArrayList<>();


    public void setInterfaceInfo(String interfaceCode, String interfaceName, String interfaceDesc) {
        if(StringUtils.isEmpty(interfaceCode) && StringUtils.isEmpty(interfaceName)){
            MSException.throwException("接口的交易码或服务名称不能都为空");
        }
         if(StringUtils.isNotEmpty(interfaceCode)){
             this.serviceName = interfaceCode+":"+interfaceName;
         }else {
             this.serviceName = interfaceName;
         }
         if (this.serviceName.endsWith(":")){
             this.serviceName = this.serviceName.substring(0,this.serviceName.length()-1);
         }
         this.serviceDesc = interfaceDesc;
    }
}
