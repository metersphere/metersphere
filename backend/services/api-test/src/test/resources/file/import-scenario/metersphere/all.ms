{
    "organizationId": "717345437786112",
    "projectId": "997050905108480",
    "exportScenarioList":
    [
        {
            "id": "950871430496256",
            "name": "场景复制：复制引用场景二人组",
            "priority": "P0",
            "status": "UNDERWAY",
            "stepTotal": 2,
            "requestPassRate": "0",
            "lastReportStatus": "",
            "lastReportId": null,
            "num": 100003,
            "deleted": false,
            "pos": 14798848,
            "versionId": "997050905108527",
            "refId": "950871430496256",
            "latest": true,
            "projectId": "997050905108480",
            "moduleId": "661562424147968",
            "description": "",
            "tags":
            [],
            "grouped": false,
            "environmentId": "997050905108481",
            "createUser": "724938939965440",
            "createTime": 1729236148336,
            "deleteTime": null,
            "deleteUser": null,
            "updateUser": "724938939965440",
            "updateTime": 1729236175161,
            "modulePath": "/正经场景模块",
            "scenarioConfig":
            {
                "variable":
                {
                    "commonVariables":
                    [],
                    "csvVariables":
                    []
                },
                "preProcessorConfig":
                {
                    "enableGlobal": true,
                    "processors":
                    []
                },
                "postProcessorConfig":
                {
                    "enableGlobal": true,
                    "processors":
                    []
                },
                "assertionConfig":
                {
                    "assertions":
                    []
                },
                "otherConfig":
                {
                    "enableGlobalCookie": true,
                    "enableCookieShare": false,
                    "enableStepWait": false,
                    "failureStrategy": "CONTINUE"
                }
            },
            "steps":
            []
        },
        {
            "id": "951661704478720",
            "name": "场景引用：复制引用场景二人组",
            "priority": "P0",
            "status": "UNDERWAY",
            "stepTotal": 2,
            "requestPassRate": "0",
            "lastReportStatus": "",
            "lastReportId": null,
            "num": 100004,
            "deleted": false,
            "pos": 14802944,
            "versionId": "997050905108527",
            "refId": "951661704478720",
            "latest": true,
            "projectId": "997050905108480",
            "moduleId": "661562424147968",
            "description": "",
            "tags":
            [],
            "grouped": false,
            "environmentId": "997050905108481",
            "createUser": "724938939965440",
            "createTime": 1729236194110,
            "deleteTime": null,
            "deleteUser": null,
            "updateUser": "724938939965440",
            "updateTime": 1729236194110,
            "modulePath": "/正经场景模块",
            "scenarioConfig":
            {
                "variable":
                {
                    "commonVariables":
                    [],
                    "csvVariables":
                    []
                },
                "preProcessorConfig":
                {
                    "enableGlobal": true,
                    "processors":
                    []
                },
                "postProcessorConfig":
                {
                    "enableGlobal": true,
                    "processors":
                    []
                },
                "assertionConfig":
                {
                    "assertions":
                    []
                },
                "otherConfig":
                {
                    "enableGlobalCookie": true,
                    "enableCookieShare": false,
                    "enableStepWait": false,
                    "failureStrategy": "CONTINUE"
                }
            },
            "steps":
            []
        }
    ],
    "apiScenarioCsvList":
    [],
    "scenarioStepList":
    [
        {
            "id": "172923614497700000",
            "enable": true,
            "resourceId": "949101903970304",
            "originProjectId": "997050905108480",
            "stepType": "API_SCENARIO",
            "refType": "COPY",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "enableScenarioEnv": false,
                "useOriginScenarioParamPreferential": true,
                "useOriginScenarioParam": false
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "场景引用复制接口定义",
            "resourceNum": "100001",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "950871430496256",
            "sort": 1,
            "parentId": null
        },
        {
            "id": "172923614497700001",
            "enable": true,
            "resourceId": "933073076830208",
            "originProjectId": "997050905108480",
            "stepType": "API",
            "refType": "COPY",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "protocol": "HTTP",
                "method": "GET"
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "可以正常执行的获取tapd-复制",
            "resourceNum": "100003",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "950871430496256",
            "sort": 1,
            "parentId": "172923614497700000"
        },
        {
            "id": "172923614497700002",
            "enable": true,
            "resourceId": "933073076830208",
            "originProjectId": "997050905108480",
            "stepType": "API",
            "refType": "REF",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "protocol": "HTTP",
                "method": "GET"
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "可以正常执行的获取tapd-引用",
            "resourceNum": "100003",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "950871430496256",
            "sort": 2,
            "parentId": "172923614497700000"
        },
        {
            "id": "172923614497800000",
            "enable": true,
            "resourceId": "950029616906240",
            "originProjectId": "997050905108480",
            "stepType": "API_SCENARIO",
            "refType": "COPY",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "enableScenarioEnv": false,
                "useOriginScenarioParamPreferential": true,
                "useOriginScenarioParam": false
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "场景引用复制接口用例",
            "resourceNum": "100002",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "950871430496256",
            "sort": 2,
            "parentId": null
        },
        {
            "id": "172923614497800001",
            "enable": true,
            "resourceId": "933725911859200",
            "originProjectId": "997050905108480",
            "stepType": "API_CASE",
            "refType": "COPY",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "protocol": "HTTP",
                "method": "GET"
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "复制的用例",
            "resourceNum": "100003001",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "950871430496256",
            "sort": 1,
            "parentId": "172923614497800000"
        },
        {
            "id": "172923614497800002",
            "enable": true,
            "resourceId": "933725911859200",
            "originProjectId": "997050905108480",
            "stepType": "API_CASE",
            "refType": "REF",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "protocol": "HTTP",
                "method": "GET"
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "引用的用例",
            "resourceNum": "100003001",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "950871430496256",
            "sort": 2,
            "parentId": "172923614497800000"
        },
        {
            "id": "172923619228400000",
            "enable": true,
            "resourceId": "949101903970304",
            "originProjectId": "997050905108480",
            "stepType": "API_SCENARIO",
            "refType": "REF",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "enableScenarioEnv": false,
                "useOriginScenarioParamPreferential": true,
                "useOriginScenarioParam": false
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "场景引用复制接口定义",
            "resourceNum": "100001",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "951661704478720",
            "sort": 1,
            "parentId": null
        },
        {
            "id": "172923619228400003",
            "enable": true,
            "resourceId": "950029616906240",
            "originProjectId": "997050905108480",
            "stepType": "API_SCENARIO",
            "refType": "REF",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "enableScenarioEnv": false,
                "useOriginScenarioParamPreferential": true,
                "useOriginScenarioParam": false
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "场景引用复制接口用例",
            "resourceNum": "100002",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "951661704478720",
            "sort": 2,
            "parentId": null
        },
        {
            "id": "172923599832400000",
            "enable": true,
            "resourceId": "933073076830208",
            "originProjectId": "997050905108480",
            "stepType": "API",
            "refType": "COPY",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "protocol": "HTTP",
                "method": "GET"
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "可以正常执行的获取tapd-复制",
            "resourceNum": "100003",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "949101903970304",
            "sort": 1,
            "parentId": null
        },
        {
            "id": "172923601542900000",
            "enable": true,
            "resourceId": "933073076830208",
            "originProjectId": "997050905108480",
            "stepType": "API",
            "refType": "REF",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "protocol": "HTTP",
                "method": "GET"
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "可以正常执行的获取tapd-引用",
            "resourceNum": "100003",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "949101903970304",
            "sort": 2,
            "parentId": null
        },
        {
            "id": "172923607239900000",
            "enable": true,
            "resourceId": "933725911859200",
            "originProjectId": "997050905108480",
            "stepType": "API_CASE",
            "refType": "COPY",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "protocol": "HTTP",
                "method": "GET"
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "复制的用例",
            "resourceNum": "100003001",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "950029616906240",
            "sort": 1,
            "parentId": null
        },
        {
            "id": "172923609009900000",
            "enable": true,
            "resourceId": "933725911859200",
            "originProjectId": "997050905108480",
            "stepType": "API_CASE",
            "refType": "REF",
            "config":
            {
                "id": "",
                "name": "",
                "enable": true,
                "protocol": "HTTP",
                "method": "GET"
            },
            "csvIds": null,
            "projectId": "997050905108480",
            "name": "引用的用例",
            "resourceNum": "100003001",
            "versionId": null,
            "children": null,
            "uniqueId": null,
            "scenarioId": "950029616906240",
            "sort": 2,
            "parentId": null
        }
    ],
    "scenarioStepBlobMap":
    {
        "172923599832400000": "{\"polymorphicName\":\"MsHTTPElement\",\"stepId\":null,\"resourceId\":null,\"projectId\":null,\"name\":\"https://api.tapd.cn/stories?workspace_id=55049933&id=1155049933001012963\",\"enable\":true,\"children\":[{\"polymorphicName\":\"MsCommonElement\",\"stepId\":null,\"resourceId\":null,\"projectId\":null,\"name\":null,\"enable\":true,\"children\":null,\"parent\":null,\"csvIds\":null,\"preProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]},\"postProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]},\"assertionConfig\":{\"enableGlobal\":true,\"assertions\":[]}}],\"parent\":null,\"csvIds\":null,\"customizeRequest\":false,\"customizeRequestEnvEnable\":false,\"path\":\"/stories\",\"method\":\"GET\",\"body\":{\"bodyType\":\"NONE\",\"noneBody\":{},\"formDataBody\":{\"formValues\":[]},\"wwwFormBody\":{\"formValues\":[]},\"jsonBody\":{\"enableJsonSchema\":false,\"jsonValue\":null,\"jsonSchema\":null},\"xmlBody\":{\"value\":null},\"rawBody\":{\"value\":null},\"binaryBody\":{\"description\":null,\"file\":null},\"bodyClassByType\":\"io.metersphere.api.dto.request.http.body.NoneBody\",\"bodyDataByType\":{}},\"headers\":[],\"rest\":[],\"query\":[{\"key\":\"workspace_id\",\"value\":\"55049933\",\"enable\":true,\"description\":\"\",\"paramType\":\"string\",\"required\":false,\"minLength\":null,\"maxLength\":null,\"encode\":false,\"valid\":true,\"notBlankValue\":true},{\"key\":\"id\",\"value\":\"1155049933001012963\",\"enable\":true,\"description\":\"\",\"paramType\":\"string\",\"required\":false,\"minLength\":null,\"maxLength\":null,\"encode\":false,\"valid\":true,\"notBlankValue\":true}],\"otherConfig\":{\"connectTimeout\":60000,\"responseTimeout\":60000,\"certificateAlias\":null,\"followRedirects\":false,\"autoRedirects\":true},\"authConfig\":{\"authType\":\"BASIC\",\"basicAuth\":{\"userName\":\"oOjrikkm\",\"password\":\"9CD9AB02-7497-0B85-2C4F-45CC3EA763F8\",\"valid\":true},\"digestAuth\":{\"userName\":null,\"password\":null,\"valid\":false},\"httpauthValid\":true},\"moduleId\":null,\"num\":null,\"mockNum\":null}",
        "172923614497800001": "{\"authConfig\":{\"authType\":\"BASIC\",\"basicAuth\":{\"userName\":\"oOjrikkm\",\"password\":\"9CD9AB02-7497-0B85-2C4F-45CC3EA763F8\"},\"digestAuth\":{\"userName\":\"\",\"password\":\"\"}},\"body\":{\"bodyType\":\"NONE\",\"formDataBody\":{\"formValues\":[]},\"wwwFormBody\":{\"formValues\":[]},\"jsonBody\":{\"jsonValue\":\"\",\"enableJsonSchema\":true},\"xmlBody\":{\"value\":\"\"},\"rawBody\":{\"value\":\"\"},\"binaryBody\":{\"description\":\"\",\"sendAsBody\":false}},\"headers\":[],\"method\":\"GET\",\"otherConfig\":{\"connectTimeout\":60000,\"responseTimeout\":60000,\"certificateAlias\":\"\",\"followRedirects\":true,\"autoRedirects\":false},\"path\":\"/stories\",\"query\":[{\"id\":\"172059506570200000\",\"enable\":true,\"key\":\"workspace_id\",\"value\":\"55049933\",\"paramType\":\"string\",\"description\":\"\",\"required\":false,\"encode\":false},{\"id\":\"172059506675100000\",\"enable\":true,\"key\":\"id\",\"value\":\"1155049933001012963\",\"paramType\":\"string\",\"description\":\"\",\"required\":false,\"encode\":false}],\"rest\":[],\"url\":\"/stories\",\"polymorphicName\":\"MsHTTPElement\",\"name\":\"正常执行的但是断言失败的\",\"children\":[{\"polymorphicName\":\"MsCommonElement\",\"assertionConfig\":{\"enableGlobal\":true,\"assertions\":[{\"name\":\"响应体\",\"assertionType\":\"RESPONSE_BODY\",\"id\":\"1720595131649\",\"enable\":true,\"assertionBodyType\":\"JSON_PATH\",\"jsonPathAssertion\":{\"assertions\":[{\"id\":\"172059513165900000\",\"enable\":true,\"variableName\":\"\",\"variableType\":\"TEMPORARY\",\"extractScope\":\"BODY\",\"expression\":\"$.data[0].Story.id\",\"condition\":\"EQUALS\",\"extractType\":\"JSON_PATH\",\"expressionMatchingRule\":\"EXPRESSION\",\"resultMatchingRule\":\"RANDOM\",\"resultMatchingRuleNum\":1,\"responseFormat\":\"XML\",\"moreSettingPopoverVisible\":false,\"expectedValue\":\"aaaavbbb\"}]},\"xpathAssertion\":{\"responseFormat\":\"XML\",\"assertions\":[]},\"regexAssertion\":{\"assertions\":[]},\"bodyAssertionDataByType\":{\"assertions\":[]}}]},\"postProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]},\"preProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]}}]}",
        "172923601542900000": "{\"polymorphicName\":\"MsHTTPElement\",\"stepId\":null,\"resourceId\":null,\"projectId\":null,\"name\":\"https://api.tapd.cn/stories?workspace_id=55049933&id=1155049933001012963\",\"enable\":true,\"children\":[{\"polymorphicName\":\"MsCommonElement\",\"stepId\":null,\"resourceId\":null,\"projectId\":null,\"name\":null,\"enable\":true,\"children\":null,\"parent\":null,\"csvIds\":null,\"preProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]},\"postProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]},\"assertionConfig\":{\"enableGlobal\":true,\"assertions\":[]}}],\"parent\":null,\"csvIds\":null,\"customizeRequest\":false,\"customizeRequestEnvEnable\":false,\"path\":\"/stories\",\"method\":\"GET\",\"body\":{\"bodyType\":\"NONE\",\"noneBody\":{},\"formDataBody\":{\"formValues\":[]},\"wwwFormBody\":{\"formValues\":[]},\"jsonBody\":{\"enableJsonSchema\":false,\"jsonValue\":null,\"jsonSchema\":null},\"xmlBody\":{\"value\":null},\"rawBody\":{\"value\":null},\"binaryBody\":{\"description\":null,\"file\":null},\"bodyClassByType\":\"io.metersphere.api.dto.request.http.body.NoneBody\",\"bodyDataByType\":{}},\"headers\":[],\"rest\":[],\"query\":[{\"key\":\"workspace_id\",\"value\":\"55049933\",\"enable\":true,\"description\":\"\",\"paramType\":\"string\",\"required\":false,\"minLength\":null,\"maxLength\":null,\"encode\":false,\"valid\":true,\"notBlankValue\":true},{\"key\":\"id\",\"value\":\"1155049933001012963\",\"enable\":true,\"description\":\"\",\"paramType\":\"string\",\"required\":false,\"minLength\":null,\"maxLength\":null,\"encode\":false,\"valid\":true,\"notBlankValue\":true}],\"otherConfig\":{\"connectTimeout\":60000,\"responseTimeout\":60000,\"certificateAlias\":null,\"followRedirects\":false,\"autoRedirects\":true},\"authConfig\":{\"authType\":\"BASIC\",\"basicAuth\":{\"userName\":\"oOjrikkm\",\"password\":\"9CD9AB02-7497-0B85-2C4F-45CC3EA763F8\",\"valid\":true},\"digestAuth\":{\"userName\":null,\"password\":null,\"valid\":false},\"httpauthValid\":true},\"moduleId\":null,\"num\":null,\"mockNum\":null}",
        "172923607239900000": "{\"authConfig\":{\"authType\":\"BASIC\",\"basicAuth\":{\"userName\":\"oOjrikkm\",\"password\":\"9CD9AB02-7497-0B85-2C4F-45CC3EA763F8\"},\"digestAuth\":{\"userName\":\"\",\"password\":\"\"}},\"body\":{\"bodyType\":\"NONE\",\"formDataBody\":{\"formValues\":[]},\"wwwFormBody\":{\"formValues\":[]},\"jsonBody\":{\"jsonValue\":\"\",\"enableJsonSchema\":true},\"xmlBody\":{\"value\":\"\"},\"rawBody\":{\"value\":\"\"},\"binaryBody\":{\"description\":\"\",\"sendAsBody\":false}},\"headers\":[],\"method\":\"GET\",\"otherConfig\":{\"connectTimeout\":60000,\"responseTimeout\":60000,\"certificateAlias\":\"\",\"followRedirects\":true,\"autoRedirects\":false},\"path\":\"/stories\",\"query\":[{\"id\":\"172059506570200000\",\"enable\":true,\"key\":\"workspace_id\",\"value\":\"55049933\",\"paramType\":\"string\",\"description\":\"\",\"required\":false,\"encode\":false},{\"id\":\"172059506675100000\",\"enable\":true,\"key\":\"id\",\"value\":\"1155049933001012963\",\"paramType\":\"string\",\"description\":\"\",\"required\":false,\"encode\":false}],\"rest\":[],\"url\":\"/stories\",\"polymorphicName\":\"MsHTTPElement\",\"name\":\"正常执行的但是断言失败的\",\"children\":[{\"polymorphicName\":\"MsCommonElement\",\"assertionConfig\":{\"enableGlobal\":true,\"assertions\":[{\"name\":\"响应体\",\"assertionType\":\"RESPONSE_BODY\",\"id\":\"1720595131649\",\"enable\":true,\"assertionBodyType\":\"JSON_PATH\",\"jsonPathAssertion\":{\"assertions\":[{\"id\":\"172059513165900000\",\"enable\":true,\"variableName\":\"\",\"variableType\":\"TEMPORARY\",\"extractScope\":\"BODY\",\"expression\":\"$.data[0].Story.id\",\"condition\":\"EQUALS\",\"extractType\":\"JSON_PATH\",\"expressionMatchingRule\":\"EXPRESSION\",\"resultMatchingRule\":\"RANDOM\",\"resultMatchingRuleNum\":1,\"responseFormat\":\"XML\",\"moreSettingPopoverVisible\":false,\"expectedValue\":\"aaaavbbb\"}]},\"xpathAssertion\":{\"responseFormat\":\"XML\",\"assertions\":[]},\"regexAssertion\":{\"assertions\":[]},\"bodyAssertionDataByType\":{\"assertions\":[]}}]},\"postProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]},\"preProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]}}]}",
        "172923614497700002": "{\"polymorphicName\":\"MsHTTPElement\",\"stepId\":null,\"resourceId\":null,\"projectId\":null,\"name\":\"https://api.tapd.cn/stories?workspace_id=55049933&id=1155049933001012963\",\"enable\":true,\"children\":[{\"polymorphicName\":\"MsCommonElement\",\"stepId\":null,\"resourceId\":null,\"projectId\":null,\"name\":null,\"enable\":true,\"children\":null,\"parent\":null,\"csvIds\":null,\"preProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]},\"postProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]},\"assertionConfig\":{\"enableGlobal\":true,\"assertions\":[]}}],\"parent\":null,\"csvIds\":null,\"customizeRequest\":false,\"customizeRequestEnvEnable\":false,\"path\":\"/stories\",\"method\":\"GET\",\"body\":{\"bodyType\":\"NONE\",\"noneBody\":{},\"formDataBody\":{\"formValues\":[]},\"wwwFormBody\":{\"formValues\":[]},\"jsonBody\":{\"enableJsonSchema\":false,\"jsonValue\":null,\"jsonSchema\":null},\"xmlBody\":{\"value\":null},\"rawBody\":{\"value\":null},\"binaryBody\":{\"description\":null,\"file\":null},\"bodyClassByType\":\"io.metersphere.api.dto.request.http.body.NoneBody\",\"bodyDataByType\":{}},\"headers\":[],\"rest\":[],\"query\":[{\"key\":\"workspace_id\",\"value\":\"55049933\",\"enable\":true,\"description\":\"\",\"paramType\":\"string\",\"required\":false,\"minLength\":null,\"maxLength\":null,\"encode\":false,\"valid\":true,\"notBlankValue\":true},{\"key\":\"id\",\"value\":\"1155049933001012963\",\"enable\":true,\"description\":\"\",\"paramType\":\"string\",\"required\":false,\"minLength\":null,\"maxLength\":null,\"encode\":false,\"valid\":true,\"notBlankValue\":true}],\"otherConfig\":{\"connectTimeout\":60000,\"responseTimeout\":60000,\"certificateAlias\":null,\"followRedirects\":false,\"autoRedirects\":true},\"authConfig\":{\"authType\":\"BASIC\",\"basicAuth\":{\"userName\":\"oOjrikkm\",\"password\":\"9CD9AB02-7497-0B85-2C4F-45CC3EA763F8\",\"valid\":true},\"digestAuth\":{\"userName\":null,\"password\":null,\"valid\":false},\"httpauthValid\":true},\"moduleId\":null,\"num\":null,\"mockNum\":null}",
        "172923614497700001": "{\"polymorphicName\":\"MsHTTPElement\",\"stepId\":null,\"resourceId\":null,\"projectId\":null,\"name\":\"https://api.tapd.cn/stories?workspace_id=55049933&id=1155049933001012963\",\"enable\":true,\"children\":[{\"polymorphicName\":\"MsCommonElement\",\"stepId\":null,\"resourceId\":null,\"projectId\":null,\"name\":null,\"enable\":true,\"children\":null,\"parent\":null,\"csvIds\":null,\"preProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]},\"postProcessorConfig\":{\"enableGlobal\":true,\"processors\":[]},\"assertionConfig\":{\"enableGlobal\":true,\"assertions\":[]}}],\"parent\":null,\"csvIds\":null,\"customizeRequest\":false,\"customizeRequestEnvEnable\":false,\"path\":\"/stories\",\"method\":\"GET\",\"body\":{\"bodyType\":\"NONE\",\"noneBody\":{},\"formDataBody\":{\"formValues\":[]},\"wwwFormBody\":{\"formValues\":[]},\"jsonBody\":{\"enableJsonSchema\":false,\"jsonValue\":null,\"jsonSchema\":null},\"xmlBody\":{\"value\":null},\"rawBody\":{\"value\":null},\"binaryBody\":{\"description\":null,\"file\":null},\"bodyClassByType\":\"io.metersphere.api.dto.request.http.body.NoneBody\",\"bodyDataByType\":{}},\"headers\":[],\"rest\":[],\"query\":[{\"key\":\"workspace_id\",\"value\":\"55049933\",\"enable\":true,\"description\":\"\",\"paramType\":\"string\",\"required\":false,\"minLength\":null,\"maxLength\":null,\"encode\":false,\"valid\":true,\"notBlankValue\":true},{\"key\":\"id\",\"value\":\"1155049933001012963\",\"enable\":true,\"description\":\"\",\"paramType\":\"string\",\"required\":false,\"minLength\":null,\"maxLength\":null,\"encode\":false,\"valid\":true,\"notBlankValue\":true}],\"otherConfig\":{\"connectTimeout\":60000,\"responseTimeout\":60000,\"certificateAlias\":null,\"followRedirects\":false,\"autoRedirects\":true},\"authConfig\":{\"authType\":\"BASIC\",\"basicAuth\":{\"userName\":\"oOjrikkm\",\"password\":\"9CD9AB02-7497-0B85-2C4F-45CC3EA763F8\",\"valid\":true},\"digestAuth\":{\"userName\":null,\"password\":null,\"valid\":false},\"httpauthValid\":true},\"moduleId\":null,\"num\":null,\"mockNum\":null}"
    },
    "relatedApiDefinitions":
    [
        {
            "id": null,
            "name": "可以正常执行的获取tapd",
            "protocol": "HTTP",
            "method": "GET",
            "path": "/stories",
            "status": "PROCESSING",
            "num": null,
            "tags":
            [],
            "pos": 12288,
            "projectId": null,
            "moduleId": "root",
            "latest": null,
            "versionId": null,
            "refId": null,
            "description": "",
            "createTime": null,
            "createUser": null,
            "updateTime": null,
            "updateUser": null,
            "deleteUser": null,
            "deleteTime": null,
            "deleted": null,
            "request":
            {
                "polymorphicName": "MsHTTPElement",
                "stepId": null,
                "resourceId": null,
                "projectId": null,
                "name": "https://api.tapd.cn/stories?workspace_id=55049933&id=1155049933001012963",
                "enable": true,
                "children":
                [
                    {
                        "polymorphicName": "MsCommonElement",
                        "stepId": null,
                        "resourceId": null,
                        "projectId": null,
                        "name": null,
                        "enable": true,
                        "children": null,
                        "parent": null,
                        "csvIds": null,
                        "preProcessorConfig":
                        {
                            "enableGlobal": true,
                            "processors":
                            []
                        },
                        "postProcessorConfig":
                        {
                            "enableGlobal": true,
                            "processors":
                            []
                        },
                        "assertionConfig":
                        {
                            "enableGlobal": true,
                            "assertions":
                            []
                        }
                    }
                ],
                "parent": null,
                "csvIds": null,
                "customizeRequest": false,
                "customizeRequestEnvEnable": false,
                "path": "/stories",
                "method": "GET",
                "body":
                {
                    "bodyType": "NONE",
                    "noneBody":
                    {},
                    "formDataBody":
                    {
                        "formValues":
                        []
                    },
                    "wwwFormBody":
                    {
                        "formValues":
                        []
                    },
                    "jsonBody":
                    {
                        "enableJsonSchema": false,
                        "jsonValue": null,
                        "jsonSchema": null
                    },
                    "xmlBody":
                    {
                        "value": null
                    },
                    "rawBody":
                    {
                        "value": null
                    },
                    "binaryBody":
                    {
                        "description": null,
                        "file": null
                    },
                    "bodyClassByType": "io.metersphere.api.dto.request.http.body.NoneBody",
                    "bodyDataByType":
                    {}
                },
                "headers":
                [],
                "rest":
                [],
                "query":
                [
                    {
                        "key": "workspace_id",
                        "value": "55049933",
                        "enable": true,
                        "description": "",
                        "paramType": "string",
                        "required": false,
                        "minLength": null,
                        "maxLength": null,
                        "encode": false,
                        "valid": true,
                        "notBlankValue": true
                    },
                    {
                        "key": "id",
                        "value": "1155049933001012963",
                        "enable": true,
                        "description": "",
                        "paramType": "string",
                        "required": false,
                        "minLength": null,
                        "maxLength": null,
                        "encode": false,
                        "valid": true,
                        "notBlankValue": true
                    }
                ],
                "otherConfig":
                {
                    "connectTimeout": 60000,
                    "responseTimeout": 60000,
                    "certificateAlias": null,
                    "followRedirects": false,
                    "autoRedirects": true
                },
                "authConfig":
                {
                    "authType": "BASIC",
                    "basicAuth":
                    {
                        "userName": "oOjrikkm",
                        "password": "9CD9AB02-7497-0B85-2C4F-45CC3EA763F8",
                        "valid": true
                    },
                    "digestAuth":
                    {
                        "userName": null,
                        "password": null,
                        "valid": false
                    },
                    "httpauthValid": true
                },
                "moduleId": null,
                "num": null,
                "mockNum": null
            },
            "response":
            [],
            "modulePath": "未规划接口",
            "apiTestCaseList":
            [],
            "apiMockList":
            []
        }
    ],
    "relatedApiTestCaseList":
    [
        {
            "id": null,
            "name": "正常执行的但是断言失败的",
            "priority": "P0",
            "num": null,
            "status": "PROCESSING",
            "lastReportStatus": null,
            "lastReportId": null,
            "projectId": null,
            "apiDefinitionId": null,
            "apiDefinitionNum": null,
            "apiDefinitionName": null,
            "environmentId": null,
            "createTime": null,
            "createUser": null,
            "updateTime": null,
            "updateUser": null,
            "deleteTime": null,
            "deleteUser": null,
            "follow": null,
            "method": null,
            "path": null,
            "environmentName": null,
            "createName": null,
            "updateName": null,
            "deleteName": null,
            "tags":
            [],
            "passRate": null,
            "request":
            {
                "polymorphicName": "MsHTTPElement",
                "stepId": null,
                "resourceId": null,
                "projectId": null,
                "name": "正常执行的但是断言失败的",
                "enable": true,
                "children":
                [
                    {
                        "polymorphicName": "MsCommonElement",
                        "stepId": null,
                        "resourceId": null,
                        "projectId": null,
                        "name": null,
                        "enable": true,
                        "children":
                        [],
                        "parent": null,
                        "csvIds": null,
                        "preProcessorConfig":
                        {
                            "enableGlobal": true,
                            "processors":
                            []
                        },
                        "postProcessorConfig":
                        {
                            "enableGlobal": true,
                            "processors":
                            []
                        },
                        "assertionConfig":
                        {
                            "enableGlobal": true,
                            "assertions":
                            [
                                {
                                    "assertionType": "RESPONSE_BODY",
                                    "enable": true,
                                    "name": "响应体",
                                    "id": "1720595131649",
                                    "projectId": null,
                                    "assertionBodyType": "JSON_PATH",
                                    "jsonPathAssertion":
                                    {
                                        "assertions":
                                        [
                                            {
                                                "enable": true,
                                                "expression": "$.data[0].Story.id",
                                                "condition": "EQUALS",
                                                "expectedValue": "aaaavbbb",
                                                "valid": true
                                            }
                                        ]
                                    },
                                    "xpathAssertion":
                                    {
                                        "responseFormat": "XML",
                                        "assertions":
                                        []
                                    },
                                    "documentAssertion": null,
                                    "regexAssertion":
                                    {
                                        "assertions":
                                        []
                                    },
                                    "bodyAssertionClassByType": "io.metersphere.project.api.assertion.body.MsJSONPathAssertion",
                                    "bodyAssertionDataByType":
                                    {
                                        "assertions":
                                        [
                                            {
                                                "enable": true,
                                                "expression": "$.data[0].Story.id",
                                                "condition": "EQUALS",
                                                "expectedValue": "aaaavbbb",
                                                "valid": true
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    }
                ],
                "parent": null,
                "csvIds": null,
                "customizeRequest": false,
                "customizeRequestEnvEnable": false,
                "path": "/stories",
                "method": "GET",
                "body":
                {
                    "bodyType": "NONE",
                    "noneBody": null,
                    "formDataBody":
                    {
                        "formValues":
                        []
                    },
                    "wwwFormBody":
                    {
                        "formValues":
                        []
                    },
                    "jsonBody":
                    {
                        "enableJsonSchema": true,
                        "jsonValue": "",
                        "jsonSchema": null
                    },
                    "xmlBody":
                    {
                        "value": ""
                    },
                    "rawBody":
                    {
                        "value": ""
                    },
                    "binaryBody":
                    {
                        "description": "",
                        "file": null
                    },
                    "bodyClassByType": "io.metersphere.api.dto.request.http.body.NoneBody",
                    "bodyDataByType": null
                },
                "headers":
                [],
                "rest":
                [],
                "query":
                [
                    {
                        "key": "workspace_id",
                        "value": "55049933",
                        "enable": true,
                        "description": "",
                        "paramType": "string",
                        "required": false,
                        "minLength": null,
                        "maxLength": null,
                        "encode": false,
                        "valid": true,
                        "notBlankValue": true
                    },
                    {
                        "key": "id",
                        "value": "1155049933001012963",
                        "enable": true,
                        "description": "",
                        "paramType": "string",
                        "required": false,
                        "minLength": null,
                        "maxLength": null,
                        "encode": false,
                        "valid": true,
                        "notBlankValue": true
                    }
                ],
                "otherConfig":
                {
                    "connectTimeout": 60000,
                    "responseTimeout": 60000,
                    "certificateAlias": "",
                    "followRedirects": true,
                    "autoRedirects": false
                },
                "authConfig":
                {
                    "authType": "BASIC",
                    "basicAuth":
                    {
                        "userName": "oOjrikkm",
                        "password": "9CD9AB02-7497-0B85-2C4F-45CC3EA763F8",
                        "valid": true
                    },
                    "digestAuth":
                    {
                        "userName": "",
                        "password": "",
                        "valid": false
                    },
                    "httpauthValid": true
                },
                "moduleId": null,
                "num": null,
                "mockNum": null
            },
            "modulePath": null,
            "moduleId": null,
            "protocol": null,
            "apiChange": null,
            "inconsistentWithApi": null,
            "ignoreApiDiff": null,
            "ignoreApiChange": null
        }
    ],
    "relatedScenarioList":
    [
        {
            "id": "949101903970304",
            "name": "场景引用复制接口定义",
            "priority": "P0",
            "status": "UNDERWAY",
            "stepTotal": 2,
            "requestPassRate": "0",
            "lastReportStatus": "",
            "lastReportId": null,
            "num": 100001,
            "deleted": false,
            "pos": 14790656,
            "versionId": "997050905108527",
            "refId": "949101903970304",
            "latest": true,
            "projectId": "997050905108480",
            "moduleId": "661562424147968",
            "description": "",
            "tags":
            [],
            "grouped": false,
            "environmentId": "997050905108481",
            "createUser": "724938939965440",
            "createTime": 1729236045861,
            "deleteTime": null,
            "deleteUser": null,
            "updateUser": "724938939965440",
            "updateTime": 1729236045861,
            "modulePath": "/正经场景模块",
            "scenarioConfig":
            {
                "variable":
                {
                    "commonVariables":
                    [],
                    "csvVariables":
                    []
                },
                "preProcessorConfig":
                {
                    "enableGlobal": true,
                    "processors":
                    []
                },
                "postProcessorConfig":
                {
                    "enableGlobal": true,
                    "processors":
                    []
                },
                "assertionConfig":
                {
                    "assertions":
                    []
                },
                "otherConfig":
                {
                    "enableGlobalCookie": true,
                    "enableCookieShare": false,
                    "enableStepWait": false,
                    "failureStrategy": "CONTINUE"
                }
            },
            "steps":
            []
        },
        {
            "id": "950029616906240",
            "name": "场景引用复制接口用例",
            "priority": "P0",
            "status": "UNDERWAY",
            "stepTotal": 2,
            "requestPassRate": "0",
            "lastReportStatus": "",
            "lastReportId": null,
            "num": 100002,
            "deleted": false,
            "pos": 14794752,
            "versionId": "997050905108527",
            "refId": "950029616906240",
            "latest": true,
            "projectId": "997050905108480",
            "moduleId": "661562424147968",
            "description": "",
            "tags":
            [],
            "grouped": false,
            "environmentId": "997050905108481",
            "createUser": "724938939965440",
            "createTime": 1729236099525,
            "deleteTime": null,
            "deleteUser": null,
            "updateUser": "724938939965440",
            "updateTime": 1729236099525,
            "modulePath": "/正经场景模块",
            "scenarioConfig":
            {
                "variable":
                {
                    "commonVariables":
                    [],
                    "csvVariables":
                    []
                },
                "preProcessorConfig":
                {
                    "enableGlobal": true,
                    "processors":
                    []
                },
                "postProcessorConfig":
                {
                    "enableGlobal": true,
                    "processors":
                    []
                },
                "assertionConfig":
                {
                    "assertions":
                    []
                },
                "otherConfig":
                {
                    "enableGlobalCookie": true,
                    "enableCookieShare": false,
                    "enableStepWait": false,
                    "failureStrategy": "CONTINUE"
                }
            },
            "steps":
            []
        }
    ]
}