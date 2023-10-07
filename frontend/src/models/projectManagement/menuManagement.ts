import { MenuEnum } from '@/enums/commonEnum';

export interface MenuTableListItem {
  module: string;
  moduleEnable: boolean;
  moduleDesc?: string;
  children?: MenuTableListItem[];
}

export interface MenuTableListParams {
  projectId: string;
  type: MenuEnum;
  typeValue: boolean;
}
