package io.metersphere.api.controller;

import io.metersphere.api.dto.share.ApiDocumentInfoDTO;
import io.metersphere.api.dto.share.ApiDocumentRequest;
import io.metersphere.api.dto.share.ApiDocumentShareRequest;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ShareInfoService;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ShareInfo;
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
@RequestMapping(value = "/share/info")
public class ShareInfoController {
    @Resource
    ShareInfoService shareInfoService;
    @Resource
    ApiDefinitionService apiDefinitionService;

    @PostMapping("/selectApiSimpleInfo")
    public List<ApiDocumentInfoDTO> list(@RequestBody ApiDocumentRequest request) {
        List<ApiDocumentInfoDTO> returnList = shareInfoService.findApiDocumentSimpleInfoByRequest(request);
        return returnList;
    }

    @GetMapping("/get/{id}")
    public ShareInfo get(@PathVariable String id) {
        return shareInfoService.get(id);
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
                ApiDocumentInfoDTO returnDTO = shareInfoService.conversionModelToDTO(model);
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
            returnDTO = shareInfoService.conversionModelToDTO(apiModel);
        }catch (Exception e){
            e.printStackTrace();
        }
        returnDTO.setSelectedFlag(true);
        return returnDTO;
    }

    @PostMapping("/generateApiDocumentShareInfo")
    public ShareInfoDTO generateApiDocumentShareInfo(@RequestBody ApiDocumentShareRequest request) {
        ShareInfo apiShare = shareInfoService.generateApiDocumentShareInfo(request);
        ShareInfoDTO returnDTO = shareInfoService.conversionShareInfoToDTO(apiShare);
        return returnDTO;
    }

    @PostMapping("/generateShareInfo")
    public ShareInfoDTO generateShareInfo(@RequestBody ShareInfo request) {
        ShareInfo apiShare = shareInfoService.generateShareInfo(request);
        ShareInfoDTO returnDTO = shareInfoService.conversionShareInfoToDTO(apiShare);
        return returnDTO;
    }
}
