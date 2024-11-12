import { TableQueryParams } from '@/models/common';
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

// 查询入参
export interface WorkHomePageDetail extends TableQueryParams {
  dayNumber: number | null;
  startTime: number | null;
  endTime: number | null;
  projectIds: string[];
  handleUsers?: string[];
  organizationId: string;
}

export interface TimeFormParams {
  dayNumber: number | null;
  startTime: number | null;
  endTime: number | null;
}

export interface OverViewOfProject {
  caseCountMap: Record<string, number>; // 模块列表
  projectCountList: {
    id: string;
    name: string;
    count: number[];
  }[]; // 项目列表
  xaxis: string[]; // 横坐标
}

export interface ModuleCardItem {
  label: string | number;
  value: string | number;
  count?: number;
  icon?: string;
  color?: string;
  [key: string]: any;
}

export type StatusStatisticsMapType = Record<
  string,
  {
    name: string;
    count: number;
  }[]
>;

export interface PassRateDataType {
  statusStatisticsMap: StatusStatisticsMapType;
  statusPercentList: {
    status: string; // 状态
    count: number;
    percentValue: string; // 百分比
  }[];
}
