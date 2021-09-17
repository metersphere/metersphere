import i18n from "@/i18n/i18n";
export const SCRIPT_MENU = [
  {
    title: 'API测试',
    children: [
      {
        title: '从API定义导入',
        command: "api_definition",
      },
      {
        title: '新API测试[JSON]',
        command: "new_api_request",
      }
    ]
  },
  {
    title: '自定义变量',
    children: [
      {
        title: i18n.t('api_test.request.processor.code_template_get_variable'),
        value: 'vars.get("variable_name")',
      },
      {
        title: i18n.t('api_test.request.processor.code_template_set_variable'),
        value: 'vars.put("variable_name", "variable_value")',
      },
      {
        title: i18n.t('api_test.request.processor.code_template_get_response_header'),
        value: 'prev.getResponseHeaders()',
      },
      {
        title: i18n.t('api_test.request.processor.code_template_get_response_code'),
        value: 'prev.getResponseCode()',
      },
      {
        title: i18n.t('api_test.request.processor.code_template_get_response_result'),
        value: 'prev.getResponseDataAsString()',
      },
    ]
  },
  {
    title: '项目环境',
    children: [
      {
        title: i18n.t('api_test.request.processor.param_environment_set_global_variable'),
        value: 'vars.put(${__metersphere_env_id}+"key","value");\n'+'vars.put("key","value")',
      },
    ]
  },
  {
    title: '自定义函数',
    children: [
      {
        title: "插入自定义函数",
        command: "custom_function",
      }
    ]
  },
  {
    title: '异常处理',
    children: [
      {
        title: "终止测试",
        value: 'ctx.getEngine().stopThreadNow(ctx.getThread().getThreadName())'
      },
    ]
  },
  {
    title: '报文处理',
    children: [
      {
        title: i18n.t('api_test.request.processor.code_add_report_length'),
        value: 'String report = ctx.getCurrentSampler().getRequestData();\n' +
          'if(report!=null){\n' +
          '    //补足8位长度，前置补0\n' +
          '    String reportlengthStr = String.format("%08d",report.length());\n' +
          '    report = reportlengthStr+report;\n' +
          '    ctx.getCurrentSampler().setRequestData(report);\n' +
          '}',
      },
      {
        title: i18n.t('api_test.request.processor.code_hide_report_length'),
        value: '//Get response data\n' +
          'String returnData = prev.getResponseDataAsString();\n' +
          'if(returnData!=null&&returnData.length()>8){\n' +
          '//remove 8 report length \n' +
          '    String subStringData = returnData.substring(8,returnData.length());\n' +
          '    if(subStringData.startsWith("<")){\n' +
          '        returnData = subStringData;\n' +
          '        prev.setResponseData(returnData);\n' +
          '    }\n' +
          '}',
      },
    ]
  }
]
