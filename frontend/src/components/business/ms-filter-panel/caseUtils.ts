import { OPERATORS } from './operator';

// eslint-disable-next-line no-shadow
export enum CaseKeyEnum {
  NAME = 'name',
  UPDATE_TIME = 'updateTime',
  MODULES = 'modules',
  CREATE_TIME = 'createTime',
  CREATOR = 'creator',
  TAGS = 'tags',
  REVIEW_RESULT = 'reviewResults',
  FOLLOW_PEOPLE = 'followPeople',
  ASSOCIATED_REQUIREMENTS = 'associated_requirements',
  CASE_LEVEL = 'caseLevel',
  CASE_STATUS = 'caseStatus',
  PRINCIPAL = 'principal', // 责任人
}

// 名称
export const NAME = {
  key: CaseKeyEnum.NAME, // 对应字段key
  type: 'a-input', // Vue控件名称
  label: '显示名称', // 显示名称
  operator: {
    value: OPERATORS.LIKE.value, // 如果未设置value初始值，则value初始值为options[0]
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE], // 运算符选项
  },
};

// 标签
export const TAGS = {
  key: CaseKeyEnum.TAGS,
  type: 'a-input',
  label: '标签',
  operator: {
    value: OPERATORS.LIKE.value,
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE],
  },
};

// 所属模块
export const MODULE = {
  key: 'module',
  type: 'a-tree-select',
  label: '所属模块',
  operator: {
    value: OPERATORS.LIKE.value,
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE],
  },
};

// 创建时间
export const CREATE_TIME = {
  key: CaseKeyEnum.CREATE_TIME,
  type: 'time-select', // 时间选择器
  label: '创建时间',
  props: {},
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.LT],
  },
};

// 更新时间
export const UPDATE_TIME = {
  key: CaseKeyEnum.UPDATE_TIME,
  type: 'time-select',
  label: '更新时间',
  props: {},
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.LT],
  },
};
// 功能用例所需要列表
export const TEST_PLAN_TEST_CASE = [NAME, TAGS, MODULE, CREATE_TIME, UPDATE_TIME];

export default {};
