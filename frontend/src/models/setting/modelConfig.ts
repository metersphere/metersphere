import { ModelBaseTypeEnum, ModelOwnerTypeTypeEnum, ModelPermissionTypeEnum, ModelTypeEnum } from '@/enums/modelEnum';

import { TableQueryParams } from '../common';

export interface ModelAdvancedSetType {
  name: string;
  label: string;
  value: number;
  enable: boolean;
  minValue: number;
  maxValue: number;
}

export interface ModelFormConfigParams {
  id?: string;
  name: string;
  type: ModelTypeEnum; // 模型类型（大语言/视觉/音频）
  providerName: ModelBaseTypeEnum; // 模型供应商名称
  permissionType: ModelPermissionTypeEnum; // 模型类型（公有/私有）
  status: boolean; // 模型链接状态
  owner: string; // 模型拥有者
  ownerType: ModelOwnerTypeTypeEnum; // 模型拥有者类型（个人/系统）
  baseName: string; // 基础模型名称
  appKey: string;
  apiUrl: string;
  advSettingDTOList: ModelAdvancedSetType[]; // 模型高级设置参数配置
}

export interface SupplierModelItem {
  value: ModelBaseTypeEnum;
  name: string;
  icon: string;
}

export interface ModelConfigItem extends ModelFormConfigParams {
  id: string;
  createUserName: string;
}

export type ModelConfigNameItem = Pick<ModelConfigItem, 'id' | 'name'>;

export interface GetModelConfigListQueryParams extends TableQueryParams {
  owner: string; // 模型拥有者
  providerName: ModelBaseTypeEnum; // 供应商
}
