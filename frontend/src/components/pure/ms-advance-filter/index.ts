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
const NOT_EQUAL = { label: 'advanceFilter.operator.notEqual', value: 'notEqual' };
const BETWEEN = { label: 'advanceFilter.operator.between', value: 'between' };

export const OPERATOR_MAP = {
  string: [LIKE, NOT_LIKE, IN, NOT_IN, EQUAL, NOT_EQUAL],
  number: [GT, GE, LT, LE, EQUAL, NOT_EQUAL, BETWEEN],
  date: [GT, GE, LT, LE, EQUAL, NOT_EQUAL, BETWEEN],
  array: [IN, NOT_IN],
};
