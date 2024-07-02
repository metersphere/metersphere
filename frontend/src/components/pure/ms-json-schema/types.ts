export interface JsonSchemaItem {
  id: string;
  title: string; // 参数名称
  type: string; // 参数类型
  description: string; // 参数描述
  enable: boolean; // 是否启用
  value: string; // 参数值
  defaultValue: string | number | boolean; // 默认值
  example?: Record<string, any>;
  items?: string; // 子级，当 type 为array 时，使用该值
  properties?: Record<string, any>; // 子级，当 type 为object 时，使用该值
  required?: string[]; // 必填参数 这里的值是参数的title
  pattern?: string; // 正则表达式
  maxLength?: number;
  minLength?: number;
  minimum?: number;
  maximum?: number;
  format?: string; // 格式化
  enumValues?: string; // 参数值的枚举
  // 前端渲染字段
  children?: JsonSchemaItem[];
}
