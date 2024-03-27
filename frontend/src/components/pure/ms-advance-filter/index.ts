export { default as FilterForm } from './FilterForm.vue';
export { default as MsAdvanceFilter } from './index.vue';

// const IN = { label: 'advanceFilter.operator.in', value: 'in' };
// const NOT_IN = { label: 'advanceFilter.operator.not_in', value: 'not_in' };
export const LIKE = { label: 'advanceFilter.operator.like', value: 'like' };
export const NOT_LIKE = { label: 'advanceFilter.operator.not_like', value: 'not_like' };
export const GT = { label: 'advanceFilter.operator.gt', value: 'GT' };
export const GE = { label: 'advanceFilter.operator.ge', value: 'GT_OR_EQUALS' };
export const LT = { label: 'advanceFilter.operator.lt', value: 'LT' };
export const LE = { label: 'advanceFilter.operator.le', value: 'LT_OR_EQUALS' };
export const EQUAL = { label: 'advanceFilter.operator.equal', value: 'EQUALS' };
export const NOT_EQUAL = { label: 'advanceFilter.operator.notEqual', value: 'NOT_EQUALS' };
export const BETWEEN = { label: 'advanceFilter.operator.between', value: 'between' };
export const NO_CHECK = { label: 'advanceFilter.operator.no_check', value: 'UNCHECK' };
export const CONTAINS = { label: 'advanceFilter.operator.contains', value: 'CONTAINS' };
export const NO_CONTAINS = { label: 'advanceFilter.operator.not_contains', value: 'NOT_CONTAINS' };
export const START_WITH = { label: 'advanceFilter.operator.start_with', value: 'START_WITH' };
export const END_WITH = { label: 'advanceFilter.operator.end_with', value: 'END_WITH' };
export const EMPTY = { label: 'advanceFilter.operator.empty', value: 'EMPTY' };
export const NOT_EMPTY = { label: 'advanceFilter.operator.not_empty', value: 'NOT_EMPTY' };
export const REGEX = { label: 'advanceFilter.operator.regexp', value: 'REGEX' };
export const LENGTH_EQUAL = { label: 'advanceFilter.operator.length.equal', value: 'LENGTH_EQUALS' };
export const LENGTH_GT = { label: 'advanceFilter.operator.length.gt', value: 'LENGTH_GT' };
export const LENGTH_GE = { label: 'advanceFilter.operator.length.ge', value: 'LENGTH_GT_OR_EQUALS' };
export const LENGTH_LT = { label: 'advanceFilter.operator.length.lt', value: 'LENGTH_LT' };
export const LENGTH_LE = { label: 'advanceFilter.operator.length.le', value: 'LENGTH_LT_OR_EQUALS' };
export const OPERATOR_MAP = {
  string: [LIKE, NOT_LIKE, EQUAL, NOT_EQUAL],
  number: [GT, GE, LT, LE, EQUAL, NOT_EQUAL, BETWEEN],
  date: [GT, GE, LT, LE, EQUAL, NOT_EQUAL, BETWEEN],
  array: [BETWEEN],
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

export const MULTIPLE_OPERATOR_LIST = ['between'];

export function isMutipleOperator(operator: string) {
  return MULTIPLE_OPERATOR_LIST.includes(operator);
}
