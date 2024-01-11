import type { MsSearchSelectProps, RadioProps } from '@/components/business/ms-select';

import type { CascaderOption, TreeNodeData } from '@arco-design/web-vue';
import type { VirtualListProps } from '@arco-design/web-vue/es/_components/virtual-list-v2/interface';
import type { TreeSelectProps } from '@arco-design/web-vue/es/tree-select/interface';

export type CascaderModelValue = string | number | Record<string, any> | (string | number | Record<string, any>)[];

export interface MsCascaderProps {
  modelValue: CascaderModelValue;
  options: CascaderOption[];
  mode?: 'MS' | 'native'; // MS的多选、原生;这里的多选指的是出了一级以外的多选，一级是顶级分类选项只能单选。原生模式使用 arco-design 的 cascader 组件，只加了getOptionComputedStyle
  prefix?: string; // 输入框前缀
  levelTop?: string[]; // 顶级选项，多选时则必传
  level?: string; // 顶级选项，该级别为单选选项
  multiple?: boolean; // 是否多选
  strictly?: boolean; // 是否严格模式
  virtualListProps?: VirtualListProps; // 传入开启虚拟滚动
  panelWidth?: number; // 下拉框宽度，默认为 150px
  placeholder?: string;
  loading?: boolean;
  optionSize?: 'small' | 'default';
  pathMode?: boolean; // 是否开启路径模式
  valueKey?: string;
  labelKey?: string; // 传入自定义的 labelKey
}

/* eslint-disable no-shadow */
export enum BackEndEnum {
  STRING = 'string',
  ARRAY = 'array',
  TIME = 'time',
  NUMBER = 'number',
}

export enum FilterType {
  INPUT = 'Input',
  NUMBER = 'Number',
  SELECT = 'Select',
  DATE_PICKER = 'DatePicker',
  CASCADER = 'Cascader',
  TAGS_INPUT = 'TagsInput',
  TREE_SELECT = 'TreeSelect',
  TEXTAREA = 'textArea',
  RADIO = 'radio',
  CHECKBOX = 'checkbox',
  JIRAKEY = 'JIRAKEY',
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
  radioProps?: Partial<RadioProps>;
  checkProps?: Partial<RadioProps>;
}

export type AccordBelowType = 'AND' | 'OR';

export interface CombineItem {
  [key: string]: Pick<FilterFormItem, 'value' | 'operator' | 'backendType'>;
}

export interface FilterResult {
  // 匹配模式 所有/任一
  accordBelow: AccordBelowType;
  // 高级搜索
  combine: CombineItem;
}

export interface FilterFormProps {
  configList: FilterFormItem[];
}
