import i18n from "@/i18n/i18n";
export const SCRIPT_MENU = [
  {
    title: i18n.t('project.code_segment.api_test'),
    children: [
      {
        title: i18n.t('project.code_segment.import_api_test'),
        command: "api_definition",
      },
      {
        title: i18n.t('project.code_segment.new_api_test'),
        command: "new_api_request",
      }
    ]
  },
  {
    title: i18n.t('project.code_segment.custom_value'),
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
    title: i18n.t('project.code_segment.project_env'),
    children: [
      {
        title: i18n.t('api_test.request.processor.param_environment_set_global_variable'),
        value: 'vars.put(${__metersphere_env_id}+"key","value");\n'+'vars.put("key","value")',
      },
    ]
  },
  {
    title: i18n.t('project.code_segment.code_segment'),
    children: [
      {
        title: i18n.t('project.code_segment.insert_segment'),
        command: "custom_function",
      }
    ]
  },
  {
    title: i18n.t('project.code_segment.exception_handle'),
    children: [
      {
        title:  i18n.t('project.code_segment.stop_test'),
        value: 'ctx.getEngine().stopThreadNow(ctx.getThread().getThreadName());'
      },
    ]
  },
  {
    title: i18n.t('project.code_segment.report_handle'),
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
