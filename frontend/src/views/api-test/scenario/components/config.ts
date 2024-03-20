import { ScenarioStepLoopType, ScenarioStepLoopWhileType } from '@/models/apiTest/scenario';

export const defaultStepItemCommon = {
  checked: false,
  expanded: false,
  enabled: true,
  children: [],
  loopNum: 0,
  loopType: 'num' as ScenarioStepLoopType,
  loopSpace: 0,
  variableName: '',
  variablePrefix: '',
  loopWhileType: 'condition' as ScenarioStepLoopWhileType,
  variableVal: '',
  condition: 'equal',
  overTime: 0,
  expression: '',
  waitTime: 0,
  description: '',
  createActionsVisible: false,
};

export const conditionOptions = [
  {
    value: 'equal',
    label: 'apiScenario.equal',
  },
  {
    value: 'notEqualTo',
    label: 'apiScenario.notEqualTo',
  },
  {
    value: 'greater',
    label: 'apiScenario.greater',
  },
  {
    value: 'less',
    label: 'apiScenario.less',
  },
  {
    value: 'greaterOrEqual',
    label: 'apiScenario.greaterOrEqual',
  },
  {
    value: 'lessOrEqual',
    label: 'apiScenario.lessOrEqual',
  },
  {
    value: 'include',
    label: 'apiScenario.include',
  },
  {
    value: 'notInclude',
    label: 'apiScenario.notInclude',
  },
  {
    value: 'null',
    label: 'apiScenario.null',
  },
  {
    value: 'notNull',
    label: 'apiScenario.notNull',
  },
];
