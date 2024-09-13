package io.metersphere.api.utils;

import io.metersphere.api.parser.jmeter.xstream.MsSaveService;
import io.metersphere.api.parser.ms.MsTestElementParser;
import io.metersphere.plugin.api.spi.AbstractMsProtocolTestElement;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-28  11:46
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class MsTestElementParserTest {

    @Test
    public void testParse() throws Exception {

        String json = "{\"polymorphicName\":\"MsHTTPElement\",\"stepId\":null,\"resourceId\":null,\"projectId\":null,\"name\":\"测试各种前后置\",\"enable\":true,\"children\":[{\"polymorphicName\":\"MsCommonElement\",\"stepId\":null,\"resourceId\":null,\"projectId\":null,\"name\":null,\"enable\":true,\"children\":[],\"parent\":null,\"csvIds\":null,\"preProcessorConfig\":{\"enableGlobal\":true,\"processors\":[{\"processorType\":\"SCRIPT\",\"name\":\"脚本名称\",\"enable\":true,\"projectId\":null,\"stepId\":null,\"script\":\"草草草草草\",\"scriptLanguage\":\"BEANSHELL_JSR233\",\"enableCommonScript\":false,\"commonScriptInfo\":{\"id\":\"\",\"deleted\":false,\"name\":\"\",\"script\":\"\",\"scriptLanguage\":\"BEANSHELL_JSR233\",\"params\":[]},\"valid\":true},{\"processorType\":\"SCRIPT\",\"name\":\"脚本名称\",\"enable\":true,\"projectId\":null,\"stepId\":null,\"script\":\"fkfkfkfkfk\",\"scriptLanguage\":\"BEANSHELL\",\"enableCommonScript\":false,\"commonScriptInfo\":{\"id\":\"\",\"deleted\":false,\"name\":\"\",\"script\":\"\",\"scriptLanguage\":\"BEANSHELL_JSR233\",\"params\":[]},\"valid\":true},{\"processorType\":\"SCRIPT\",\"name\":\"脚本名称\",\"enable\":true,\"projectId\":null,\"stepId\":null,\"script\":\"xxxxxxxxx\",\"scriptLanguage\":\"PYTHON\",\"enableCommonScript\":false,\"commonScriptInfo\":{\"id\":\"\",\"deleted\":false,\"name\":\"\",\"script\":\"\",\"scriptLanguage\":\"BEANSHELL_JSR233\",\"params\":[]},\"valid\":true},{\"processorType\":\"SQL\",\"name\":\"\",\"enable\":true,\"projectId\":null,\"stepId\":null,\"script\":\"select fk from user\",\"queryTimeout\":0,\"resultVariable\":\"\",\"variableNames\":\"fkid\",\"dataSourceId\":\"1ab74c5f-eea2-4d35-9bbc-e8e17e96185e\",\"dataSourceName\":\"DM\",\"extractParams\":[{\"key\":\"fkid\",\"value\":\"fkname\",\"valid\":true,\"notBlankValue\":true}]},{\"processorType\":\"TIME_WAITING\",\"name\":null,\"enable\":true,\"projectId\":null,\"stepId\":null,\"delay\":12138}]},\"postProcessorConfig\":{\"enableGlobal\":true,\"processors\":[{\"processorType\":\"SCRIPT\",\"name\":\"脚本名称\",\"enable\":true,\"projectId\":null,\"stepId\":null,\"script\":\"ssssaaaaa\",\"scriptLanguage\":\"BEANSHELL_JSR233\",\"enableCommonScript\":false,\"commonScriptInfo\":{\"id\":\"\",\"deleted\":false,\"name\":\"\",\"script\":\"\",\"scriptLanguage\":\"BEANSHELL_JSR233\",\"params\":[]},\"valid\":true},{\"processorType\":\"SQL\",\"name\":\"\",\"enable\":true,\"projectId\":null,\"stepId\":null,\"script\":\"xxxx\",\"queryTimeout\":0,\"resultVariable\":\"\",\"variableNames\":\"\",\"dataSourceId\":\"1ab74c5f-eea2-4d35-9bbc-e8e17e96185e\",\"dataSourceName\":\"DM\",\"extractParams\":[]},{\"processorType\":\"SCRIPT\",\"name\":\"脚本名称\",\"enable\":true,\"projectId\":null,\"stepId\":null,\"script\":\"asdasdavvv\",\"scriptLanguage\":\"BEANSHELL\",\"enableCommonScript\":false,\"commonScriptInfo\":{\"id\":\"\",\"deleted\":false,\"name\":\"\",\"script\":\"\",\"scriptLanguage\":\"BEANSHELL_JSR233\",\"params\":[]},\"valid\":true},{\"processorType\":\"EXTRACT\",\"name\":null,\"enable\":true,\"projectId\":null,\"stepId\":null,\"extractors\":[{\"extractType\":\"JSON_PATH\",\"variableName\":\"aaaaaa\",\"variableType\":\"TEMPORARY\",\"expression\":\"\",\"enable\":true,\"resultMatchingRule\":\"RANDOM\",\"resultMatchingRuleNum\":1,\"valid\":false},{\"extractType\":\"X_PATH\",\"variableName\":\"bbbbbb\",\"variableType\":\"TEMPORARY\",\"expression\":\"\",\"enable\":true,\"resultMatchingRule\":\"RANDOM\",\"resultMatchingRuleNum\":1,\"responseFormat\":\"XML\",\"valid\":false},{\"extractType\":\"REGEX\",\"variableName\":\"cccccc\",\"variableType\":\"TEMPORARY\",\"expression\":\"\",\"enable\":true,\"resultMatchingRule\":\"RANDOM\",\"resultMatchingRuleNum\":1,\"expressionMatchingRule\":\"EXPRESSION\",\"extractScope\":\"BODY\",\"valid\":false},{\"extractType\":\"JSON_PATH\",\"variableName\":\"\",\"variableType\":\"TEMPORARY\",\"expression\":\"\",\"enable\":true,\"resultMatchingRule\":\"RANDOM\",\"resultMatchingRuleNum\":1,\"valid\":false}]}]},\"assertionConfig\":{\"enableGlobal\":true,\"assertions\":[]}}],\"parent\":null,\"csvIds\":null,\"customizeRequest\":false,\"customizeRequestEnvEnable\":false,\"path\":\"/api/app/evaAuditPlan/runAsync\",\"method\":\"POST\",\"body\":{\"bodyType\":\"JSON\",\"noneBody\":{},\"formDataBody\":{\"formValues\":[]},\"wwwFormBody\":{\"formValues\":[]},\"jsonBody\":{\"enableJsonSchema\":false,\"jsonValue\":\"{\\n  \\\"id\\\" : 0,\\n  \\\"audit_user_code\\\" : \\\"string\\\",\\n  \\\"audit_user_name\\\" : \\\"string\\\"\\n}\",\"jsonSchema\":{\"id\":null,\"title\":null,\"example\":null,\"type\":\"object\",\"description\":null,\"items\":null,\"properties\":{\"id\":{\"id\":null,\"title\":null,\"example\":null,\"type\":\"integer\",\"description\":\"\",\"items\":null,\"properties\":null,\"additionalProperties\":null,\"required\":null,\"defaultValue\":null,\"pattern\":null,\"maxLength\":null,\"minLength\":null,\"minimum\":null,\"maximum\":null,\"maxItems\":null,\"minItems\":null,\"format\":\"int32\",\"enumValues\":null,\"enable\":true},\"audit_user_code\":{\"id\":null,\"title\":null,\"example\":null,\"type\":\"string\",\"description\":\"审核人\",\"items\":null,\"properties\":null,\"additionalProperties\":null,\"required\":null,\"defaultValue\":null,\"pattern\":null,\"maxLength\":20,\"minLength\":0,\"minimum\":null,\"maximum\":null,\"maxItems\":null,\"minItems\":null,\"format\":\"\",\"enumValues\":null,\"enable\":true},\"audit_user_name\":{\"id\":null,\"title\":null,\"example\":null,\"type\":\"string\",\"description\":\"审核人\",\"items\":null,\"properties\":null,\"additionalProperties\":null,\"required\":null,\"defaultValue\":null,\"pattern\":null,\"maxLength\":50,\"minLength\":0,\"minimum\":null,\"maximum\":null,\"maxItems\":null,\"minItems\":null,\"format\":\"\",\"enumValues\":null,\"enable\":true}},\"additionalProperties\":null,\"required\":null,\"defaultValue\":null,\"pattern\":null,\"maxLength\":null,\"minLength\":null,\"minimum\":null,\"maximum\":null,\"maxItems\":null,\"minItems\":null,\"format\":null,\"enumValues\":null,\"enable\":true}},\"xmlBody\":{\"value\":null},\"rawBody\":{\"value\":null},\"binaryBody\":{\"description\":null,\"file\":null},\"bodyClassByType\":\"io.metersphere.api.dto.request.http.body.JsonBody\",\"bodyDataByType\":{\"enableJsonSchema\":false,\"jsonValue\":\"{\\n  \\\"id\\\" : 0,\\n  \\\"audit_user_code\\\" : \\\"string\\\",\\n  \\\"audit_user_name\\\" : \\\"string\\\"\\n}\",\"jsonSchema\":{\"id\":null,\"title\":null,\"example\":null,\"type\":\"object\",\"description\":null,\"items\":null,\"properties\":{\"id\":{\"id\":null,\"title\":null,\"example\":null,\"type\":\"integer\",\"description\":\"\",\"items\":null,\"properties\":null,\"additionalProperties\":null,\"required\":null,\"defaultValue\":null,\"pattern\":null,\"maxLength\":null,\"minLength\":null,\"minimum\":null,\"maximum\":null,\"maxItems\":null,\"minItems\":null,\"format\":\"int32\",\"enumValues\":null,\"enable\":true},\"audit_user_code\":{\"id\":null,\"title\":null,\"example\":null,\"type\":\"string\",\"description\":\"审核人\",\"items\":null,\"properties\":null,\"additionalProperties\":null,\"required\":null,\"defaultValue\":null,\"pattern\":null,\"maxLength\":20,\"minLength\":0,\"minimum\":null,\"maximum\":null,\"maxItems\":null,\"minItems\":null,\"format\":\"\",\"enumValues\":null,\"enable\":true},\"audit_user_name\":{\"id\":null,\"title\":null,\"example\":null,\"type\":\"string\",\"description\":\"审核人\",\"items\":null,\"properties\":null,\"additionalProperties\":null,\"required\":null,\"defaultValue\":null,\"pattern\":null,\"maxLength\":50,\"minLength\":0,\"minimum\":null,\"maximum\":null,\"maxItems\":null,\"minItems\":null,\"format\":\"\",\"enumValues\":null,\"enable\":true}},\"additionalProperties\":null,\"required\":null,\"defaultValue\":null,\"pattern\":null,\"maxLength\":null,\"minLength\":null,\"minimum\":null,\"maximum\":null,\"maxItems\":null,\"minItems\":null,\"format\":null,\"enumValues\":null,\"enable\":true}}},\"headers\":[{\"key\":\"Content-Type\",\"value\":\"application/json\",\"enable\":true,\"description\":null,\"valid\":true,\"notBlankValue\":true},{\"key\":\"Accept\",\"value\":\"text/plain\",\"enable\":true,\"description\":null,\"valid\":true,\"notBlankValue\":true}],\"rest\":[],\"query\":[],\"otherConfig\":{\"connectTimeout\":60000,\"responseTimeout\":60000,\"certificateAlias\":null,\"followRedirects\":true,\"autoRedirects\":false},\"authConfig\":{\"authType\":\"NONE\",\"basicAuth\":{\"userName\":null,\"password\":null,\"valid\":false},\"digestAuth\":{\"userName\":null,\"password\":null,\"valid\":false},\"httpauthValid\":false},\"moduleId\":\"581796296748088\",\"num\":101788,\"mockNum\":null}";
        AbstractMsTestElement element = ApiDataUtils.parseObject(json, AbstractMsTestElement.class);

        File httpJmx = new File(
                this.getClass().getClassLoader().getResource("file/import/jmeter/post-page.jmx")
                        .getPath()
        );

        Object scriptWrapper = MsSaveService.loadElement(new FileInputStream(httpJmx));
        HashTree hashTree = getHashTree(scriptWrapper);
        MsTestElementParser parser = new MsTestElementParser();
        AbstractMsTestElement msTestElement = parser.parse(hashTree);
        Assertions.assertNotNull(msTestElement);
        List<AbstractMsProtocolTestElement> msHTTPElementList = this.getMsHTTPElement(msTestElement);
        Assertions.assertEquals(msHTTPElementList.size(), 5);

        httpJmx = new File(
                this.getClass().getClassLoader().getResource("file/import/jmeter/single.jmx")
                        .getPath()
        );
        scriptWrapper = MsSaveService.loadElement(new FileInputStream(httpJmx));
        hashTree = getHashTree(scriptWrapper);
        parser = new MsTestElementParser();
        msTestElement = parser.parse(hashTree);
        Assertions.assertNotNull(msTestElement);
        msHTTPElementList = this.getMsHTTPElement(msTestElement);
        Assertions.assertEquals(msHTTPElementList.size(), 1);
    }

    private List<AbstractMsProtocolTestElement> getMsHTTPElement(AbstractMsTestElement msTestElement) {
        List<AbstractMsProtocolTestElement> result = new ArrayList<>();
        if (msTestElement instanceof AbstractMsProtocolTestElement abstractMsProtocolTestElement) {
            result.add(abstractMsProtocolTestElement);
        } else {
            for (AbstractMsTestElement child : msTestElement.getChildren()) {
                result.addAll(this.getMsHTTPElement(child));
            }
        }
        return result;
    }

    private HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        ReflectionUtils.makeAccessible(field);
        return (HashTree) field.get(scriptWrapper);
    }
}
