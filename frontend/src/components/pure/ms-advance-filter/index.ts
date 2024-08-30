import { FilterType, OperatorEnum } from '@/enums/advancedFilterEnum';

export { default as MsAdvanceFilter } from './index.vue';

export const BELONG_TO = { label: 'advanceFilter.operator.belongTo', value: OperatorEnum.BELONG_TO }; // 属于
export const NOT_BELONG_TO = { label: 'advanceFilter.operator.notBelongTo', value: OperatorEnum.NOT_BELONG_TO }; // 不属于
export const GT = { label: 'advanceFilter.operator.gt', value: OperatorEnum.GT }; // 大于
export const GE = { label: 'advanceFilter.operator.ge', value: 'GT_OR_EQUALS' }; // 大于等于
export const LT = { label: 'advanceFilter.operator.lt', value: OperatorEnum.LT }; // 小于
export const LE = { label: 'advanceFilter.operator.le', value: 'LT_OR_EQUALS' }; // 小于等于
export const EQUAL = { label: 'advanceFilter.operator.equal', value: OperatorEnum.EQUAL }; // 等于
export const NOT_EQUAL = { label: 'advanceFilter.operator.notEqual', value: OperatorEnum.NOT_EQUAL }; // 不等于
export const BETWEEN = { label: 'advanceFilter.operator.between', value: OperatorEnum.BETWEEN }; // 介于
export const COUNT_GT = { label: 'advanceFilter.operator.length.gt', value: OperatorEnum.COUNT_GT }; // 数量大下
export const COUNT_LT = { label: 'advanceFilter.operator.length.lt', value: OperatorEnum.COUNT_LT }; // 数量小于

export const EMPTY = { label: 'advanceFilter.operator.empty', value: OperatorEnum.EMPTY }; // 为空
export const NOT_EMPTY = { label: 'advanceFilter.operator.not_empty', value: OperatorEnum.NOT_EMPTY }; // 不为空
export const NO_CHECK = { label: 'advanceFilter.operator.no_check', value: 'UNCHECK' }; // 不校验
export const CONTAINS = { label: 'advanceFilter.operator.contains', value: OperatorEnum.CONTAINS }; // 包含
export const NO_CONTAINS = { label: 'advanceFilter.operator.not_contains', value: OperatorEnum.NOT_CONTAINS }; // 不包含
export const START_WITH = { label: 'advanceFilter.operator.start_with', value: 'START_WITH' }; // 以...开始
export const END_WITH = { label: 'advanceFilter.operator.end_with', value: 'END_WITH' }; // 以...结束
export const REGEX = { label: 'advanceFilter.operator.regexp', value: 'REGEX' }; // 正则匹配
export const LENGTH_EQUAL = { label: 'advanceFilter.operator.length.equal', value: 'LENGTH_EQUALS' }; // 长度等于
export const LENGTH_GT = { label: 'advanceFilter.operator.length.gt', value: 'LENGTH_GT' }; // 长度大于
export const LENGTH_GE = { label: 'advanceFilter.operator.length.ge', value: 'LENGTH_GT_OR_EQUALS' }; // 长度大于等于
export const LENGTH_LT = { label: 'advanceFilter.operator.length.lt', value: 'LENGTH_LT' }; // 长度小于
export const LENGTH_LE = { label: 'advanceFilter.operator.length.le', value: 'LENGTH_LT_OR_EQUALS' }; // 长度小于等于

const COMMON_TEXT_OPERATORS = [CONTAINS, NO_CONTAINS, EMPTY, NOT_EMPTY, EQUAL, NOT_EQUAL];
const COMMON_SELECTION_OPERATORS = [BELONG_TO, NOT_BELONG_TO, EMPTY, NOT_EMPTY];

export const operatorOptionsMap: Record<string, { value: string; label: string }[]> = {
  [FilterType.INPUT]: COMMON_TEXT_OPERATORS,
  [FilterType.TEXTAREA]: COMMON_TEXT_OPERATORS,
  [FilterType.NUMBER]: [GT, LT, EQUAL, EMPTY, NOT_EMPTY],
  [FilterType.RADIO]: COMMON_SELECTION_OPERATORS,
  [FilterType.CHECKBOX]: COMMON_SELECTION_OPERATORS,
  [FilterType.SELECT]: COMMON_SELECTION_OPERATORS,
  [FilterType.TAGS_INPUT]: [EMPTY, CONTAINS, NO_CONTAINS, COUNT_LT, COUNT_GT],
  [FilterType.TREE_SELECT]: [BELONG_TO, NOT_BELONG_TO],
  [FilterType.DATE_PICKER]: [BETWEEN, EQUAL, EMPTY, NOT_EMPTY],
};

export const timeSelectOptions = [GE, LE];

export const statusCodeOptions = [
  NO_CHECK,
  EQUAL,
  NOT_EQUAL,
  GT,
  GE,
  LT,
  LE,
  CONTAINS,
  NO_CONTAINS,
  START_WITH,
  END_WITH,
  EMPTY,
  NOT_EMPTY,
  REGEX,
  LENGTH_GT,
  LENGTH_GE,
  LENGTH_LT,
  LENGTH_LE,
  LENGTH_EQUAL,
];

export const CustomTypeMaps: Record<string, any> = {
  INPUT: {
    type: 'INPUT',
  },
  SELECT: {
    type: 'SELECT',
    propsKey: 'selectProps',
    props: {
      mode: 'static',
      valueKey: 'value',
      labelKey: 'text',
      options: [],
    },
  },
  MULTIPLE_SELECT: {
    type: 'SELECT',
    propsKey: 'selectProps',
    props: {
      mode: 'static',
      multiple: true,
      valueKey: 'value',
      labelKey: 'text',
      options: [],
    },
  },
  RADIO: {
    type: 'RADIO',
    propsKey: 'radioProps',
    props: {
      options: [],
      valueKey: 'value',
      labelKey: 'text',
    },
  },
  CHECKBOX: {
    type: 'CHECKBOX',
    propsKey: 'checkProps',
    props: {
      options: [],
      valueKey: 'value',
      labelKey: 'text',
    },
  },
  MEMBER: {
    type: 'SELECT',
    propsKey: 'selectProps',
    props: {
      mode: 'remote',
      valueKey: 'value',
      labelKey: 'text',
      remoteFunc: '',
      remoteFieldsMap: {
        id: 'value',
        value: 'value',
        label: 'text',
      },
    },
  },
  MULTIPLE_MEMBER: {
    type: 'SELECT',
    propsKey: 'selectProps',
    props: {
      mode: 'remote',
      multiple: true,
      valueKey: 'value',
      labelKey: 'text',
      remoteFunc: '',
      remoteFieldsMap: {
        id: 'value',
        value: 'value',
        label: 'text',
      },
    },
  },
  DATE: {
    type: 'DATE_PICKER',
  },
  DATETIME: {
    type: 'DATE_PICKER',
    operator: 'between',
  },
  FLOAT: {
    type: 'NUMBER',
  },
  INT: {
    type: 'NUMBER',
    propsKey: 'numberProps',
    props: {
      precision: 0,
    },
  },
  TEXTAREA: {
    type: 'TEXTAREA',
  },
  MULTIPLE_INPUT: {
    type: 'TAGS_INPUT',
  },
};

export const defaultFormModelList = [
  {
    dataIndex: 'id',
    title: 'caseManagement.featureCase.tableColumnID',
    type: FilterType.INPUT,
    operator: OperatorEnum.CONTAINS,
    value: '',
  },
  {
    dataIndex: 'name',
    label: 'common.name',
    type: FilterType.INPUT,
    operator: OperatorEnum.CONTAINS,
    value: '',
  },
  {
    dataIndex: 'moduleId',
    label: 'common.belongModule',
    type: FilterType.TREE_SELECT,
    operator: OperatorEnum.BELONG_TO,
    value: '',
  },
];
