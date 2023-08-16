export interface ActionsItem {
  label?: string;
  eventTag?: string; // 事件标识
  isDivider?: boolean; // 是否分割线，true 的话只展示分割线，没有其他内容
  danger?: boolean; // 是否危险操作，true 的话会显示红色按钮
}

export type SelectedValue = string | number | Record<string, any> | undefined;
