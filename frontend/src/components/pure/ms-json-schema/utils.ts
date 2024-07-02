import type { JsonSchema, JsonSchemaItem, JsonSchemaTableItem } from './types';

/**
 * 将 json-schema 表格组件的表格项转换为 json-schema 规范的结构
 * @param schemaItem 表格组件项
 * @param isRoot 是否为根节点
 */
export function convertToJsonSchema(
  schemaItem: JsonSchemaTableItem,
  isRoot: boolean = true
): JsonSchema | JsonSchemaItem {
  let schema: JsonSchema | JsonSchemaItem = { type: schemaItem.type };

  // 对于 null 类型，只设置 type 和 enable 属性
  if (schemaItem.type === 'null') {
    return {
      type: 'null',
      enable: schemaItem.enable,
    };
  }

  if (!isRoot) {
    // 使用解构赋值和剩余参数来拷贝对象，同时排除 children、required、parent、id、title 属性
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { children, required, parent, id, title, ...copiedObject } = schemaItem;
    // 使用\n分割enumValues字符串，得到枚举值数组
    const enumArray = (copiedObject.enumValues?.split('\n') || []).filter((value) => value.trim() !== '');
    schema = {
      ...schema,
      ...copiedObject,
      enumValues: enumArray.length > 0 ? enumArray : undefined,
    };
  }

  if (schemaItem.children && schemaItem.children.length > 0) {
    if (schemaItem.type === 'object') {
      schema = {
        type: 'object',
        enable: schemaItem.enable,
        properties: {},
        required: [],
      };
      schemaItem.children.forEach((child) => {
        const childSchema = convertToJsonSchema(child, false);
        schema.properties![child.title] = childSchema as JsonSchemaItem;
        if (child.required) {
          schema.required!.push(child.title);
        }
      });
      if (schema.required!.length === 0) {
        delete schema.required;
      }
    } else if (schemaItem.type === 'array') {
      schema = {
        type: 'array',
        enable: schemaItem.enable,
        items: schemaItem.children.map((child) => convertToJsonSchema(child, false) as JsonSchemaItem),
      };
    }
  }

  return schema;
}
export default {};
