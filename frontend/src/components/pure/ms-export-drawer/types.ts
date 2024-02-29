export interface MsExportDrawerColumns {
  [key: string]: string;
}
export interface MsExportDrawerMap {
  systemColumns?: MsExportDrawerColumns;
  customColumns?: MsExportDrawerColumns;
  otherColumns?: MsExportDrawerColumns;
}
export interface MsExportDrawerOption {
  text: string;
  key: string;
  columnType: string;
  [key: string]: any;
}
