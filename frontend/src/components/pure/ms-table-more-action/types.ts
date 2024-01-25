export interface ActionsItem {
  label?: string;
  eventTag?: string; // 事件标识
  isDivider?: boolean; // 是否分割线，true 的话只展示分割线，没有其他内容
  danger?: boolean; // 是否危险操作，true 的话会显示红色按钮
  disabled?: boolean; // 是否禁用
  icon?: string; // 按钮图标
  permission?: string[]; // 权限标识
}

export type SelectedValue = string | number | Record<string, any> | undefined;
