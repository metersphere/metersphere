package io.metersphere.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.document.ApiDocumentInfoDTO;
import io.metersphere.api.dto.document.ApiDocumentRequest;
import io.metersphere.api.dto.document.ApiDocumentShareRequest;
import io.metersphere.api.dto.document.ApiDocumentSimpleInfoDTO;
import io.metersphere.api.dto.document.ApiDocumentShareDTO;
import io.metersphere.api.service.APITestService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiDocumentService;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiDocumentShare;
import io.metersphere.base.domain.ApiDocumentShareExample;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.python.antlr.ast.Str;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author song.tianyang
 * @Date 2021/2/5 6:25 下午
 * @Description
 */
@RestController
@RequestMapping(value = "/api/document")
public class ApiDocumentController {
    @Resource
    ApiDocumentService apiDocumentService;
    @Resource
    ApiDefinitionService apiDefinitionService;
    @Resource
    APITestService apiTestService;

    @PostMapping("/selectApiSimpleInfo")
    public List<ApiDocumentInfoDTO> list(@RequestBody ApiDocumentRequest request) {
        List<ApiDocumentInfoDTO> returnList = apiDocumentService.findApiDocumentSimpleInfoByRequest(request);
        return returnList;
    }

    @GetMapping("/selectApiInfoById/{id}")
    public ApiDocumentInfoDTO selectApiInfoById(@PathVariable String id) {
        ApiDefinitionWithBLOBs apiModel = apiDefinitionService.getBLOBs(id);
        ApiDocumentInfoDTO returnDTO = new ApiDocumentInfoDTO();
        try{
            returnDTO = apiDocumentService.conversionModelToDTO(apiModel);
        }catch (Exception e){
            e.printStackTrace();
        }
        returnDTO.setSelectedFlag(true);
        return returnDTO;
    }

    @PostMapping("/generateApiDocumentShareInfo")
    public ApiDocumentShareDTO generateApiDocumentShareInfo(@RequestBody ApiDocumentShareRequest request) {
        ApiDocumentShare apiShare = apiDocumentService.generateApiDocumentShare(request);
        ApiDocumentShareDTO returnDTO = apiDocumentService.conversionApiDocumentShareToDTO(apiShare);
        return returnDTO;
    }

    @GetMapping("/updateJmxByFile")
    public String updateJmxByFile(){
        StringBuilder jmxBuilder = new StringBuilder();
        try {
            String filePath = "/Users/admin/Downloads/成功吧 (20).jmx";
            File file = new File(filePath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            String strLine = null;
            while(null != (strLine = bufferedReader.readLine())){
                if(StringUtils.isNotEmpty(strLine)){
                    jmxBuilder.append(strLine);
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }


        String jmx = apiTestService.updateJmxString(jmxBuilder.toString(),null,false);
        return  jmx;
    }
}
