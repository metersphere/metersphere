import { MenuEnum } from '@/enums/commonEnum';

export interface MenuTableConfigItem {
  projectId: string;
  type: string;
  typeValue: string;
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
