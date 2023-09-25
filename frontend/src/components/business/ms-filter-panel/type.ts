import { SelectOptionData } from '@arco-design/web-vue';

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
  props?: {
    placeholder: string;
    [key: string]: string | number | boolean;
  };
  options?: SelectOptionData[];
}

export interface QueryTemplate {
  searchKey: QueryField;
  operatorCondition: QueryField;
  queryContent: QueryField;
}
