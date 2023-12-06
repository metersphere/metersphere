import { MsCascaderProps } from '@/components/business/ms-cascader/index.vue';
import type { MsSearchSelectProps } from '@/components/business/ms-select';

import type { CascaderOption, TreeNodeData } from '@arco-design/web-vue';
import type { TreeSelectProps } from '@arco-design/web-vue/es/tree-select/interface';

/* eslint-disable no-shadow */
export enum BackEndEnum {
  STRING = 'string',
  ARRAY = 'array',
  TIME = 'time',
}

export enum FilterType {
  INPUT = 'Input',
  NUMBER = 'Number',
  SELECT = 'Select',
  DATE_PICKER = 'DatePicker',
  CASCADER = 'Cascader',
  TAGS_INPUT = 'TagsInput',
  TREE_SELECT = 'TreeSelect',
}

export interface FilterFormItem {
  dataIndex?: string; // 对应的row的数据key
  title?: string; // 显示的label 国际化字符串定义在前端
  type: FilterType; // 类型：Input,Select,DatePicker,RangePicker
  value?: any; // 值 字符串 和 数组
  operator?: string; // 运算符号
  cascaderOptions?: CascaderOption[]; // 级联选择的选项
  backendType?: BackEndEnum; // 后端类型 string array time
  selectProps?: Partial<MsSearchSelectProps>; // select的props, 参考 MsSelect
  cascaderProps?: Partial<MsCascaderProps>; // cascader的props, 参考 MsCascader
  treeSelectData?: TreeNodeData[];
  treeSelectProps?: Partial<TreeSelectProps>;
}

export type AccordBelowType = 'all' | 'any';

export interface FilterResult {
  [key: string]: Pick<FilterFormItem, 'value' | 'operator' | 'backendType'> | AccordBelowType;
}

export interface FilterFormProps {
  configList: FilterFormItem[];
}
