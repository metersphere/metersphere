import { Scenario } from '@/models/apiTest/scenario';
import {
  ApiScenarioStatus,
  RequestAssertionCondition,
  ScenarioFailureStrategy,
  ScenarioStepLoopTypeEnum,
  WhileConditionType,
} from '@/enums/apiEnum';

export const defaultStepItemCommon = {
  checked: false,
  expanded: false,
  enable: true,
  children: [],
  config: {
    id: '',
    copyFromStepId: '', // 如果步骤是复制的，这个字段是复制的步骤id
    name: '',
    enable: true,
    polymorphicName: '', // 多态名称，用于后台区分使用的是哪个组件
    // 自定义请求
    customizeRequest: false, // 是否自定义请求
    customizeRequestEnvEnable: false, // 是否启用环境
    // 条件控制器
    value: '', // 变量值
    variable: '', // 变量名
    condition: RequestAssertionCondition.EQUALS, // 条件操作符
    loopType: ScenarioStepLoopTypeEnum.LOOP_COUNT,
    forEachController: {
      loopTime: 0, // 循环间隔时间
      value: '', // 变量值
      variable: '', // 变量名
    },
    msCountController: {
      loops: 0, // 循环次数
    },
    whileController: {
      conditionType: WhileConditionType.CONDITION, // 条件类型
      timeout: 0, // 超时时间
      msWhileScript: {
        scriptValue: '', // 脚本值
      }, // 脚本
      msWhileVariable: {
        condition: RequestAssertionCondition.EQUALS, // 条件操作符
        value: '', // 变量值
        variable: '', // 变量名
      }, // 变量
    },
    waitTime: 0, // 等待时间
  },
  createActionsVisible: false,
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
      enableGlobal: false,
      processors: [],
    },
    postProcessorConfig: {
      enableGlobal: false,
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
  executeTime: 0,
  executeSuccessCount: 0,
  executeFailCount: 0,
  uploadFileIds: [],
  linkFileIds: [],
  // 前端渲染字段
  label: '',
  closable: true,
  isNew: true,
  unSaved: false,
  executeLoading: false, // 执行loading
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
