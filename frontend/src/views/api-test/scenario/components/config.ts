import { useI18n } from '@/hooks/useI18n';

import { type CsvVariable, Scenario, ScenarioStepConfig } from '@/models/apiTest/scenario';
import {
  ApiScenarioStatus,
  RequestAssertionCondition,
  ScenarioFailureStrategy,
  ScenarioStepLoopTypeEnum,
  WhileConditionType,
} from '@/enums/apiEnum';

const { t } = useI18n();

// 场景状态选项
export const scenarioStatusOptions = [
  { label: t('apiTestManagement.processing'), value: ApiScenarioStatus.UNDERWAY },
  { label: t('apiTestManagement.deprecate'), value: ApiScenarioStatus.DEPRECATED },
  { label: t('apiTestManagement.done'), value: ApiScenarioStatus.COMPLETED },
];

// 循环控制器
export const defaultLoopController = {
  loopType: ScenarioStepLoopTypeEnum.LOOP_COUNT,
  forEachController: {
    loopTime: 0, // 循环间隔时间
    value: '', // 变量值
    variable: '', // 变量名
  },
  msCountController: {
    loops: '1', // 循环次数
    loopTime: 0, // 循环间隔时间
  },
  whileController: {
    conditionType: WhileConditionType.CONDITION, // 条件类型
    timeout: 3000, // 超时时间
    msWhileScript: {
      scriptValue: '', // 脚本值
    }, // 脚本
    msWhileVariable: {
      condition: RequestAssertionCondition.EQUALS, // 条件操作符
      value: '', // 变量值
      variable: '', // 变量名
    }, // 变量
  },
};

// 自定义请求
export const defaultCustomApiConfig = {
  customizeRequest: false, // 是否自定义请求
  customizeRequestEnvEnable: false, // 是否启用环境
};

// 条件控制器
export const defaultConditionController = {
  value: '', // 变量值
  variable: '', // 变量名
  condition: RequestAssertionCondition.EQUALS, // 条件操作符
};

// 条件控制器
export const defaultTimeController = {
  delay: 1000, // 等待时间
};

// 场景配置
export const defaultScenarioStepConfig: ScenarioStepConfig = {
  enableScenarioEnv: false,
  useOriginScenarioParamPreferential: true,
  useOriginScenarioParam: false,
};

export const defaultStepItemCommon = {
  checked: false,
  expanded: false,
  enable: true,
  children: [],
  copyFromStepId: '', // 如果步骤是复制的，这个字段是复制的步骤id
  isNew: true, // 是否新建的步骤
  config: {
    id: '',
    name: '',
    enable: true,
  },
  createActionsVisible: false,
  responsePopoverVisible: false,
  isExecuting: false,
  executeStatus: undefined,
  isRefScenarioStep: false,
  isQuoteScenarioStep: false,
  csvIds: [],
};

export const defaultScenario: Scenario = {
  name: '',
  moduleId: '',
  priority: 'P0',
  status: ApiScenarioStatus.UNDERWAY,
  tags: [],
  projectId: '',
  description: '',
  grouped: false,
  environmentId: '',
  scenarioConfig: {
    variable: {
      commonVariables: [],
      csvVariables: [],
    },
    preProcessorConfig: {
      enableGlobal: true,
      processors: [],
    },
    postProcessorConfig: {
      enableGlobal: true,
      processors: [],
    },
    assertionConfig: {
      assertions: [],
    },
    otherConfig: {
      enableGlobalCookie: true,
      enableCookieShare: false,
      enableStepWait: false,
      stepWaitTime: 1000,
      failureStrategy: ScenarioFailureStrategy.CONTINUE,
    },
  },
  steps: [],
  stepDetails: {},
  stepFileParam: {},
  fileParam: {
    linkFileIds: [],
    uploadFileIds: [],
  },
  executeTime: 0,
  executeSuccessCount: 0,
  executeFailCount: 0,
  executeFakeErrorCount: 0,
  uploadFileIds: [],
  linkFileIds: [],
  reportId: '',
  // 前端渲染字段
  label: '',
  closable: true,
  isNew: true,
  unSaved: false,
  executeLoading: false, // 执行loading
  isDebug: false,
  stepResponses: {},
  errorMessageInfo: {},
};

export const conditionOptions = [
  {
    value: RequestAssertionCondition.EQUALS,
    label: 'apiScenario.equal',
  },
  {
    value: RequestAssertionCondition.NOT_EQUALS,
    label: 'apiScenario.notEqualTo',
  },
  {
    value: RequestAssertionCondition.GT,
    label: 'apiScenario.greater',
  },
  {
    value: RequestAssertionCondition.LT,
    label: 'apiScenario.less',
  },
  {
    value: RequestAssertionCondition.GT_OR_EQUALS,
    label: 'apiScenario.greaterOrEqual',
  },
  {
    value: RequestAssertionCondition.LT_OR_EQUALS,
    label: 'apiScenario.lessOrEqual',
  },
  {
    value: RequestAssertionCondition.CONTAINS,
    label: 'apiScenario.include',
  },
  {
    value: RequestAssertionCondition.NOT_CONTAINS,
    label: 'apiScenario.notInclude',
  },
  {
    value: RequestAssertionCondition.EMPTY,
    label: 'apiScenario.null',
  },
  {
    value: RequestAssertionCondition.NOT_EMPTY,
    label: 'apiScenario.notNull',
  },
];

// 场景-常规参数默认值
export const defaultNormalParamItem = {
  key: '',
  paramType: 'CONSTANT',
  value: '',
  description: '',
  tags: [],
  enable: true,
};

// 场景-csv参数默认值
export const defaultCsvParamItem: CsvVariable = {
  id: '',
  scenarioId: '',
  name: '',
  scope: 'SCENARIO',
  enable: false,
  encoding: 'UTF-8',
  random: false,
  variableNames: '',
  ignoreFirstLine: false,
  delimiter: ',',
  allowQuotedData: false,
  recycleOnEof: true,
  stopThreadOnEof: false,
  settingVisible: false,
  file: {
    fileId: '',
    fileName: '',
    local: false,
    fileAlias: '',
    delete: false,
  },
};
