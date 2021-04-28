package io.metersphere.api.controller;

import io.metersphere.api.dto.document.ApiDocumentInfoDTO;
import io.metersphere.api.dto.document.ApiDocumentRequest;
import io.metersphere.api.dto.document.ApiDocumentShareDTO;
import io.metersphere.api.dto.document.ApiDocumentShareRequest;
import io.metersphere.api.service.APITestService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiDocumentService;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiDocumentShare;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @PostMapping("/selectApiInfoByParam")
    public List<ApiDocumentInfoDTO> selectApiInfoByParam(@RequestBody ApiDocumentRequest request) {
        List<ApiDocumentInfoDTO> returnList = new ArrayList<>();
        if(request.getApiIdList() != null){
            //要根据ids的顺序进行返回排序
            List<ApiDefinitionWithBLOBs> apiModels = apiDefinitionService.getBLOBs(request.getApiIdList());
            Map<String,ApiDefinitionWithBLOBs> apiModelMaps = apiModels.stream().collect(Collectors.toMap(ApiDefinitionWithBLOBs :: getId,a->a,(k1,k2)->k1));
            for(String id : request.getApiIdList()){
                ApiDefinitionWithBLOBs model = apiModelMaps.get(id);
                if(model == null){
                    model = new ApiDefinitionWithBLOBs();
                    model.setId(id);
                    model.setName(id);
                }
                ApiDocumentInfoDTO returnDTO = apiDocumentService.conversionModelToDTO(model);
                returnList.add(returnDTO);
            }
        }
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
}
