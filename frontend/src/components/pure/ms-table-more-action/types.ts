export interface ActionsItem {
  label?: string;
  eventTag?: string;
  isDivider?: boolean;
  danger?: boolean;
}

export type SelectedValue = string | number | Record<string, any> | undefined;
