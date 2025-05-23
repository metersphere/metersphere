export interface ModelAdvancedSetType {
  params: string;
  name: string;
  defaultValue: number;
  enable: boolean;
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
