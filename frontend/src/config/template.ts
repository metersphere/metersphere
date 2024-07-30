import type { defaultBugField, defaultCaseField } from '@/models/setting/template';

export const defaultTemplateCaseDetail: defaultCaseField = {
  name: '',
  prerequisite: '', // 前置条件
  caseEditType: '', // 编辑模式：步骤模式/文本模式
  steps: '',
  textDescription: '', // 文本描述
  expectedResult: '', // 预期结果
  description: '',
};

export const defaultTemplateBugDetail: defaultBugField = {
  title: '',
  description: '',
  descriptionFileIds: [],
};

export default {};
