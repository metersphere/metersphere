import { useI18n } from '@/hooks/useI18n';

import type { CommonScriptMenu } from '@/models/projectManagement/commonScript';

const { t } = useI18n();

export const SCRIPT_MENU: CommonScriptMenu[] = [
  {
    title: t('project.code_segment.importApiTest'),
    value: 'api_definition',
    command: 'api_definition',
  },
  {
    title: t('project.code_segment.newApiTest'),
    value: 'new_api_request',
    command: 'new_api_request',
  },
  {
    title: t('project.processor.codeTemplateGetVariable'),
    value: 'vars.get("variable_name")',
  },
  {
    title: t('project.processor.codeTemplateSetVariable'),
    value: 'vars.put("variable_name", "variable_value")',
  },
  {
    title: t('project.processor.codeTemplateGetResponseHeader'),
    value: 'prev.getResponseHeaders()',
  },
  {
    title: t('project.processor.codeTemplateGetResponseCode'),
    value: 'prev.getResponseCode()',
  },
  {
    title: t('project.processor.codeTemplateGetResponseResult'),
    value: 'prev.getResponseDataAsString()',
  },
  {
    title: t('project.processor.paramEnvironmentSetGlobalVariable'),
    value: `vars.put(\${__metersphere_env_id}+"key","value"); 
        vars.put("key","value")`,
  },
  {
    title: t('project.processor.insertPublicScript'),
    value: 'custom_function',
    command: 'custom_function',
  },
  {
    title: t('project.processor.terminationTest'),
    value: 'terminal_function',
    command: 'terminal_function',
  },
];

export default {};
