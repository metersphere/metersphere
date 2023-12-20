// mock 参数配置项输入组选项
export interface MockParamInputGroupOptionItem {
  label: string;
  value: string;
}
// mock 参数配置项输入组项类型
export type MockParamInputGroupItemType = 'select' | 'input' | 'number' | 'date' | 'radio' | 'inputAppendSelect';
// mock 参数配置项输入组
export interface MockParamInputGroupItem {
  type: MockParamInputGroupItemType;
  label?: string;
  value?: string | number;
  placeholder?: string;
  options?: MockParamInputGroupOptionItem[];
  inputValue?: string;
  selectValue?: string;
}
// mock 参数配置项
export interface MockParamItem {
  label: string;
  value: string;
  desc?: string;
  inputGroup?: MockParamInputGroupItem[];
}
