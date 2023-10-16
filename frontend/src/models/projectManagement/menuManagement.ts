export interface MenuTableConfigItem {
  [key: string]: any;
}
export interface MenuTableListItem {
  module: string;
  moduleEnable: boolean;
  moduleDesc?: string;
  children?: MenuTableConfigItem[];
  type?: string;
}

export interface MenuTableListParams {
  projectId: string;
  type?: string;
  typeValue?: boolean | string;
}
export interface PoolOption {
  id: string;
  name: string;
}

export type SelectValue =
  | string
  | number
  | boolean
  | Record<string, any>
  | (string | number | boolean | Record<string, any>)[];
