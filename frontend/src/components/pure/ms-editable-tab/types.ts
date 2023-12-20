export interface TabItem {
  id: string | number;
  label: string;
  closable?: boolean;
  [key: string]: any;
}
