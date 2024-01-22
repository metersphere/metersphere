export interface TabItem {
  id: string | number;
  label: string;
  closable?: boolean;
  unSaved?: boolean; // 未保存
  [key: string]: any;
}
