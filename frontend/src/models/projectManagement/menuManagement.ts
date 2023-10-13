import { MenuEnum } from '@/enums/commonEnum';

export interface MenuTableConfigItem {
  [key: string]: any;
}
export interface MenuTableListItem {
  module: string;
  moduleEnable: boolean;
  moduleDesc?: string;
  children?: MenuTableConfigItem[];
}

export interface MenuTableListParams {
  projectId: string;
  type?: MenuEnum;
  typeValue?: boolean;
}
export interface PoolOption {
  id: string;
  name: string;
}
