import { FieldRule, SelectOptionData } from '@arco-design/web-vue';

export interface ConditionOptions {
  id: string;
  name: string;
  value: string;
}

// 选项组下拉options
export interface Option {
  value: string;
  label: string;
}

export interface QueryField {
  label: string;
  type: 'a-select' | 'a-input' | 'a-input-number' | 'time-select' | 'a-tree-select';
  value: string;
  field: string;
  rules?: FieldRule[];
  props?: {
    [key: string]: string | number | boolean;
  };
  options?: SelectOptionData[];
}

export interface QueryTemplate {
  searchKey: QueryField;
  operatorCondition: QueryField;
  queryContent: QueryField;
}

export interface OptionsType {
  label: string;
  value: string;
}
export interface OperatorValue {
  value: string; // 如果未设置value初始值，则value初始值为options[0]
  options: OptionsType[]; // 运算符选项
}

export interface SearchKeyType {
  key: string; // 对应字段key
  type: string; // Vue控件名称
  label: string; // 显示名称
  rules?: FieldRule[];
  props?: {
    [key: string]: string | number | boolean;
  };
  operator: OperatorValue;
}
