import { FieldRule } from '@arco-design/web-vue';

export type FormItemType = 'input' | 'select' | 'inputNumber';
export type FormMode = 'create' | 'edit';
export type ValueType = 'Array' | 'string';
// 自定义检验器，为了传入动态渲染的表单项下标
export interface CustomValidator {
  notRepeat?: boolean;
}

export interface FormItemModel {
  filed: string;
  type: FormItemType;
  rules?: (FieldRule & CustomValidator)[];
  label?: string;
  placeholder?: string;
  min?: number;
  max?: number;
  maxLength?: number;
}

declare const _default: import('vue').DefineComponent<
  {
    models: FormItemModel[];
    formMode: FormMode;
    addText: string;
    maxHeight?: string;
    defaultVals?: Record<string, string[] | string>; // 当外层是编辑状态时，可传入已填充的数据
  },
  unknown,
  import('vue').ComponentOptionsMixin,
  import('vue').ComponentOptionsMixin,
  {
    formValidate: (cb: (res?: Record<string, any>) => void, isSubmit = true) => void;
    getFormResult: <T>() => T[];
  }
>;

export declare type MsBatchFormInstance = InstanceType<typeof _default>;
