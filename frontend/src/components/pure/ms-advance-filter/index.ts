import { FilterType, OperatorEnum, ViewTypeEnum } from '@/enums/advancedFilterEnum';

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
export const COUNT_GT = { label: 'advanceFilter.operator.count.gt', value: OperatorEnum.COUNT_GT }; // 数量大下
export const COUNT_LT = { label: 'advanceFilter.operator.count.lt', value: OperatorEnum.COUNT_LT }; // 数量小于

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
  [FilterType.SELECT]: COMMON_SELECTION_OPERATORS,
  [FilterType.CASCADER]: COMMON_SELECTION_OPERATORS,
  [FilterType.SELECT_EQUAL]: [EQUAL],
  [FilterType.MEMBER]: COMMON_SELECTION_OPERATORS,
  [FilterType.TAGS_INPUT]: [EMPTY, CONTAINS, NO_CONTAINS, COUNT_LT, COUNT_GT],
  [FilterType.TREE_SELECT]: [BELONG_TO, NOT_BELONG_TO],
  [FilterType.DATE_PICKER]: [BETWEEN, GT, LT, EMPTY, NOT_EMPTY],
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

const baseSelectProps = {
  mode: 'static',
  multiple: true,
  valueKey: 'value',
  labelKey: 'text',
  options: [],
};

export const CustomTypeMaps: Record<string, any> = {
  INPUT: {
    type: 'INPUT',
  },
  CASCADER: {
    type: 'CASCADER',
    propsKey: 'cascaderProps',
    props: { multiple: true, valueKey: 'value', labelKey: 'text', options: [] },
  },
  SELECT: {
    type: 'SELECT',
    propsKey: 'selectProps',
    props: { ...baseSelectProps },
  },
  MULTIPLE_SELECT: {
    type: 'SELECT',
    propsKey: 'selectProps',
    props: { ...baseSelectProps },
  },
  RADIO: {
    type: 'SELECT',
    propsKey: 'selectProps',
    props: { ...baseSelectProps },
  },
  CHECKBOX: {
    type: 'SELECT',
    propsKey: 'selectProps',
    props: { ...baseSelectProps },
  },
  MEMBER: {
    type: 'MEMBER',
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
    type: 'MEMBER',
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
    propsKey: 'dataProps',
    props: {
      showTime: false,
    },
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
  RICH_TEXT: {
    type: 'TEXTAREA',
  },
  MULTIPLE_INPUT: {
    type: 'TAGS_INPUT',
    propsKey: 'numberProps',
    props: {
      precision: 0,
    },
  },
};

// 处理自定义字段
export function getFilterCustomFields(result: any[]) {
  return result.map((item: any) => {
    const FilterTypeKey: keyof typeof FilterType = CustomTypeMaps[item.type].type;
    const formObject = CustomTypeMaps[item.type];
    const { props: formProps } = formObject;
    const currentItem: any = {
      title: item.name ?? item.fieldName,
      dataIndex: item.id ?? item.fieldId,
      type: FilterType[FilterTypeKey],
      customField: true,
      customFieldType: item.type,
    };
    if (formObject.propsKey) {
      if (item.options || item.platformOptionJson) {
        formProps.options = item.options ?? JSON.parse(item.platformOptionJson);
      }
      currentItem[formObject.propsKey] = {
        ...formProps,
      };
    }
    return currentItem;
  });
}

// 全部数据默认显示搜索条件：ID、名称、模块；
export function getAllDataDefaultConditions(viewType: ViewTypeEnum) {
  const conditions = [
    { name: 'num', operator: OperatorEnum.CONTAINS },
    { name: 'name', operator: OperatorEnum.CONTAINS },
    { name: 'moduleId', operator: OperatorEnum.BELONG_TO },
  ];
  if (
    [
      ViewTypeEnum.API_DEFINITION,
      ViewTypeEnum.PLAN_API_CASE,
      ViewTypeEnum.PLAN_API_DEFINITION_DRAWER,
      ViewTypeEnum.PLAN_API_CASE_DRAWER,
    ].includes(viewType)
  ) {
    conditions.push({ name: 'protocol', operator: OperatorEnum.BELONG_TO });
  }
  if ([ViewTypeEnum.BUG, ViewTypeEnum.PLAN_BUG, ViewTypeEnum.PLAN_BUG_DRAWER].includes(viewType)) {
    conditions.push({ name: 'title', operator: OperatorEnum.CONTAINS });
  }
  if ([ViewTypeEnum.API_MOCK].includes(viewType)) {
    conditions.push({ name: 'expectNum', operator: OperatorEnum.CONTAINS });
  }
  if (
    [ViewTypeEnum.PLAN_API_SCENARIO, ViewTypeEnum.PLAN_API_CASE, ViewTypeEnum.PLAN_FUNCTIONAL_CASE].includes(viewType)
  ) {
    conditions.push({ name: 'testPlanCollectionId', operator: OperatorEnum.BELONG_TO });
  }
  return conditions;
}

// 系统视图对应不显示的第一列下拉条件
export const internalViewsHiddenConditionsMap: Record<string, string[]> = {
  my_create: ['createUser'],
  my_review: ['reviewId'],
};
