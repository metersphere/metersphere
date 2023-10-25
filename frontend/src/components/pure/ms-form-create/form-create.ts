import { FormRule } from '@form-create/arco-design';

// 表单字段使用
export const INPUT = {
  type: 'input',
  title: '',
  field: 'fieldName',
  value: '',
  props: {
    placeholder: '请输入',
  },
};
export const SELECT = {
  type: 'SearchSelect',
  field: 'fieldName',
  title: '',
  value: '',
  options: [],
  props: {
    multiple: false,
    placeholder: '请选择',
    options: [],
  },
};

export const MULTIPLE_SELECT = {
  type: 'SearchSelect',
  field: 'fieldName',
  title: '',
  value: [],
  options: [],
  props: {
    multiple: true,
    placeholder: '请选择',
    options: [],
  },
};

export const RADIO = {
  type: 'radio',
  field: 'fieldName',
  title: '',
  value: '',
  options: [],
};

export const CHECKBOX = {
  type: 'checkbox',
  field: 'fieldName',
  title: '',
  value: [],
  options: [],
};

export const MEMBER = {
  type: 'SearchSelect',
  field: 'fieldName',
  title: '',
  value: '',
  options: [],
  props: {
    multiple: false,
    placeholder: '请选择',
  },
};

export const MULTIPLE_MEMBER = {
  type: 'SearchSelect',
  field: 'fieldName',
  title: '',
  value: '',
  options: [],
  props: {
    multiple: true,
    placeholder: '请选择',
    options: [],
  },
};

export const DATE = {
  type: 'DatePicker',
  field: 'fieldName',
  title: '',
  value: '',
  props: {
    'placeholder': '请选择',
    'format': 'YYYY/MM/DD',
    'show-time': false,
  },
};

export const DATETIME = {
  type: 'DatePicker',
  field: 'fieldName',
  title: '',
  value: '',
  props: {
    'placeholder': '请选择',
    'format': 'YYYY/MM/DD HH:mm:ss',
    'show-time': true,
  },
};

export const FLOAT = {
  type: 'InputNumber',
  field: 'fieldName',
  title: '',
  value: 0,
  props: {
    placeholder: '请输入',
  },
};

export const INT = {
  type: 'InputNumber',
  field: 'fieldName',
  title: '',
  value: 0,
  props: {
    precision: 0,
    placeholder: '请输入',
  },
};

export const MULTIPLE_INPUT = {
  type: 'a-input-tag',
  field: 'fieldName',
  title: '',
  value: [],
  props: {
    placeholder: '请选择',
  },
};

export const TEXTAREA = {
  type: 'a-textarea',
  field: 'fieldName',
  title: '',
  value: '',
  props: {
    'placeholder': '请输入',
    'auto-size': {
      minRows: 1,
      maxRows: 3,
    },
  },
};

export const FieldTypeFormRules: Record<string, FormRule> = {
  INPUT,
  SELECT,
  MULTIPLE_SELECT,
  RADIO,
  CHECKBOX,
  MEMBER,
  MULTIPLE_MEMBER,
  DATE,
  DATETIME,
  INT,
  FLOAT,
  MULTIPLE_INPUT,
  TEXTAREA,
};
