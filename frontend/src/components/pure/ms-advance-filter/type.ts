/* eslint-disable no-shadow */
export enum BackEndEnum {
  STRING = 'string',
  ARRAY = 'array',
  TIME = 'time',
}

export enum FilterType {
  INPUT = 'Input',
  SELECT = 'Select',
  DATE_PICKER = 'DatePicker',
  RANGE_PICKER = 'RangePicker',
  MUTIPLE_SELECT = 'MutiSelect',
}

export interface FilterFormItem {
  dataIndex?: string; // 对应的row的数据key
  title?: string; // 显示的label 国际化字符串定义在前端
  type?: FilterType; // 类型：Input,Select,DatePicker,RangePicker
  value?: any; // 值 字符串 和 数组
  operator?: string; // 运算符号
  options?: any[]; // 下拉框的选项
  backendType?: BackEndEnum; // 后端类型 string array time
}

export type AccordBelowType = 'all' | 'any';

export interface FilterResult {
  [key: string]: Pick<FilterFormItem, 'value' | 'operator' | 'backendType'> | AccordBelowType;
}

export interface FilterFormProps {
  configList: FilterFormItem[];
}
