export interface JsonSchemaCommon {
  id: string;
  title: string; // 参数名称
  type: string; // 参数类型
  description?: string; // 参数描述
  enable: boolean; // 是否启用
  example: string; // 参数值
  defaultValue: string | number | boolean; // 默认值
  pattern?: string; // 正则表达式
  maxLength?: number;
  minLength?: number;
  minimum?: number;
  maximum?: number;
  minItems?: number;
  maxItems?: number;
  format?: string; // 格式化
}
// json-schema 表格组件的表格项
export interface JsonSchemaTableItem extends JsonSchemaCommon {
  required?: boolean; // 是否必填
  children?: JsonSchemaTableItem[];
  parent?: JsonSchemaTableItem; // 父级
  enumValues?: string; // 参数值的枚举
}
// json-schema 规范的结构子项（表格组件转换后的结构）
export interface JsonSchemaItem extends JsonSchemaCommon {
  items?: JsonSchemaItem[]; // 子级，当 type 为array 时，使用该值
  properties?: Record<string, JsonSchemaItem>; // 子级，当 type 为object 时，使用该值
  required?: string[]; // 必填的字段名
  enumValues?: string[]; // 参数值的枚举
}
// json-schema 规范的结构（表格组件转换后的结构）
export interface JsonSchema {
  type: string;
  properties?: Record<string, JsonSchemaItem>;
  items?: JsonSchemaItem[];
  required?: string[];
  description?: string;
  minItems?: number;
  maxItems?: number;
}
