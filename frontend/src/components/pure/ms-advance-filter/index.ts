export { default as FilterForm } from './FilterForm.vue';
export { default as MsAdvanceFilter } from './index.vue';

const IN = { label: 'advanceFilter.operator.in', value: 'in' };
const NOT_IN = { label: 'advanceFilter.operator.not_in', value: 'not_in' };
const LIKE = { label: 'advanceFilter.operator.like', value: 'like' };
const NOT_LIKE = { label: 'advanceFilter.operator.not_like', value: 'not_like' };
const GT = { label: 'advanceFilter.operator.gt', value: 'gt' };
const GE = { label: 'advanceFilter.operator.ge', value: 'ge' };
const LT = { label: 'advanceFilter.operator.lt', value: 'lt' };
const LE = { label: 'advanceFilter.operator.le', value: 'le' };
const EQUAL = { label: 'advanceFilter.operator.equal', value: 'equal' };
const NOT_EQUAL = { label: 'advanceFilter.operator.notEqual', value: 'not_equal' };
const BETWEEN = { label: 'advanceFilter.operator.between', value: 'between' };

export const OPERATOR_MAP = {
  string: [LIKE, NOT_LIKE, IN, NOT_IN, EQUAL, NOT_EQUAL],
  number: [GT, GE, LT, LE, EQUAL, NOT_EQUAL, BETWEEN],
  date: [GT, GE, LT, LE, EQUAL, NOT_EQUAL, BETWEEN],
  array: [IN, NOT_IN],
};

export const timeSelectOptions = [GE, LE];

export const statusCodeOptions = [
  { label: 'ms.assertion.noValidation', value: 'none' },
  IN,
  NOT_IN,
  EQUAL,
  NOT_EQUAL,
  GT,
  GE,
  LT,
  LE,
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

export const MULTIPLE_OPERATOR_LIST = ['in', 'not_in', 'between'];

export function isMutipleOperator(operator: string) {
  return MULTIPLE_OPERATOR_LIST.includes(operator);
}
