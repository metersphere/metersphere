package io.metersphere.api.service.definition;

import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.dto.definition.ApiAIResponse;
import io.metersphere.api.dto.definition.ApiGenerateInfo;
import io.metersphere.api.dto.definition.ApiTestCaseAIRequest;
import io.metersphere.api.mapper.ApiDefinitionBlobMapper;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.request.ai.ModelSourceDTO;
import io.metersphere.system.service.AiChatBaseService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestCaseAIService {

    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;
    @Resource
    AiChatBaseService aiChatBaseService;


    public String generateApiTestCase(ApiTestCaseAIRequest request, ModelSourceDTO module) {
        ApiDefinitionBlob blob = apiDefinitionBlobMapper.selectByPrimaryKey(request.getApiDefinitionId());
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(new String(blob.getRequest()), AbstractMsTestElement.class);

        String prompt = request.getPrompt() + "\n" + "以下是接口的定义的json格式数据,根据接口定义生成接口用例:\n" +
                JSON.toJSONString(BeanUtils.copyBean(new ApiAIResponse(), msTestElement));
        request.setPrompt(prompt);

        return aiChatBaseService.chatWithMemory(request, module)
                .content();
    }

    public String chat(ApiTestCaseAIRequest request, String userId) {
        ModelSourceDTO module = aiChatBaseService.getModule(request, userId);

        String prompt = "下面一段话中是否需要生成用例？需要生成几条用例？\n" + request.getPrompt();
        ApiGenerateInfo apiGenerateInfo = aiChatBaseService.chat(prompt, module)
                .entity(ApiGenerateInfo.class);

        if (BooleanUtils.isTrue(apiGenerateInfo.getGenerateCase())) {
            // 判断对话是否是需要生成用例
            return generateApiTestCase(request, module);
        } else {
            return aiChatBaseService.chatWithMemory(request, module).content();
        }
    }
}
