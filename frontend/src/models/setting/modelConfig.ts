import { ModelBaseTypeEnum } from '@/enums/modelEnum';

export interface ModelAdvancedSetType {
  name: string;
  label: string;
  value: number;
  enable: boolean;
  minValue: number;
  maxValue: number;
}

export interface ModelForm {
  id?: string;
  name: string;
  type: string;
  baseName: string;
  apiUrl: string;
  appKey: string;
  list: ModelAdvancedSetType[];
}

export interface SupplierModelItem {
  value: ModelBaseTypeEnum;
  name: string;
  icon: string;
}
