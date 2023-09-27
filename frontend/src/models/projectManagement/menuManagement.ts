import { MenuEnum } from '@/enums/commonEnum';

export interface MenuTableListItem {
  module: string;
  moduleEnable: boolean;
  moduleDesc?: string;
}

export interface MenuTableListParams {
  projectId: string;
  type: MenuEnum;
  typeValue: boolean;
}
