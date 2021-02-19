package io.metersphere.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiDocumentInfoDTO;
import io.metersphere.api.dto.ApiDocumentRequest;
import io.metersphere.api.dto.ApiDocumentSimpleInfoDTO;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiDocumentService;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.python.antlr.ast.Str;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    @PostMapping("/selectApiSimpleInfo")
    public List<ApiDocumentSimpleInfoDTO> list(@RequestBody ApiDocumentRequest request) {
        List<ApiDocumentSimpleInfoDTO> returnList = apiDocumentService.findApiDocumentSimpleInfoByRequest(request);
        return returnList;
    }

    @GetMapping("/selectApiInfoById/{id}")
    public ApiDocumentInfoDTO selectApiInfoById(@PathVariable String id) {
        ApiDefinitionWithBLOBs apiModel = apiDefinitionService.getBLOBs(id);
        ApiDocumentInfoDTO returnDTO = apiDocumentService.conversionModelToDTO(apiModel);
        return returnDTO;
    }
}
