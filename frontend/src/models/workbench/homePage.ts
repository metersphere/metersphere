import { WorkCardEnum } from '@/enums/workbenchEnum';

// 配置卡片列表
export interface WorkConfigItem {
  label: string;
  value: string;
  description?: string;
  img?: string;
  expand?: boolean;
}

export interface childrenWorkConfigItem extends WorkConfigItem {
  value: WorkCardEnum;
}

export interface WorkConfigCard extends WorkConfigItem {
  children: childrenWorkConfigItem[];
}

export interface SelectedCardItem {
  label: string;
  id: string; // 唯一id
  key: WorkCardEnum;
  fullScreen: boolean; // 是否全屏
  isDisabledHalfScreen: boolean; // 是否禁用半屏幕
  pos?: number; // 排序
  projectIds: string[];
  handleUsers: string[];
}
