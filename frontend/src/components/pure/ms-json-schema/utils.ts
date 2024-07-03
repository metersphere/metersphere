import { Message } from '@arco-design/web-vue';

import { useI18n } from '@/hooks/useI18n';
import { getGenerateId } from '@/utils';

import type { JsonSchema, JsonSchemaItem, JsonSchemaTableItem } from './types';

/**
 * 将 json-schema 表格组件的表格项转换为 json-schema 规范的结构
 * @param schemaItem 表格组件项
 * @param isRoot 是否为根节点
 */
export function tableItemToJsonSchema(
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
        const childSchema = tableItemToJsonSchema(child, false);
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
        items: schemaItem.children.map((child) => tableItemToJsonSchema(child, false) as JsonSchemaItem),
      };
    }
  }

  return schema;
}

/**
 * 创建 json-schema 表格组件的表格项
 * @param key 对象/数组/普通子项的 key名
 * @param value 对象/数组/普通子项的值
 * @param parent 父级
 */
function createItem(key: string, value: any, parent?: JsonSchemaTableItem): JsonSchemaTableItem {
  let exampleValue; // 默认情况下，example 值为 undefined
  const itemType = Array.isArray(value) ? 'array' : typeof value;

  // 如果值不是对象或数组，则直接将值作为 example
  if (itemType !== 'object' && itemType !== 'array') {
    exampleValue = typeof value === 'boolean' ? value.toString() : value;
  }

  return {
    id: getGenerateId(),
    title: key,
    type: itemType,
    description: '',
    enable: true,
    required: true,
    defaultValue: '',
    example: exampleValue, // 仅当值不是对象或数组时，才赋予 example 值
    parent,
  };
}
/**
 * 将 json 转换为 json-schema 表格组件的表格项
 * @param json json 字符串或对象或数组
 * @param parent 父级
 */
export function parseJsonToJsonSchemaTableItem(
  json: string | object | Array<any>,
  parent?: JsonSchemaTableItem
): { result: JsonSchemaTableItem[]; ids: Array<string> } {
  if (typeof json === 'string') {
    // 尝试将 json 字符串转换为对象
    try {
      json = JSON.parse(json);
    } catch (error) {
      const { t } = useI18n();
      Message.warning(t('ms.json.schema.illegalJsonConvertFailed'));
      return { result: [], ids: [] };
    }
  }
  if (!parent) {
    // 创建根节点
    const rootItem: JsonSchemaTableItem = {
      id: 'root',
      title: 'root',
      type: Array.isArray(json) ? 'array' : 'object',
      description: '',
      enable: true,
      required: true,
      example: '',
      defaultValue: '',
    };
    const children = parseJsonToJsonSchemaTableItem(json, rootItem);
    rootItem.children = children.result;
    children.ids.push(rootItem.id);
    return { result: [rootItem], ids: children.ids };
  }

  const items: JsonSchemaTableItem[] = [];
  const type = Array.isArray(json) ? 'array' : 'object';
  const ids: Array<string> = [];

  if (type === 'object' || type === 'array') {
    // 遍历对象或数组
    Object.entries(json).forEach(([key, value]) => {
      const item: JsonSchemaTableItem = createItem(key, value, parent);
      if (typeof value === 'object' || Array.isArray(value)) {
        const children = parseJsonToJsonSchemaTableItem(value, item);
        item.children = children.result;
        ids.push(...children.ids);
      } else {
        item.example = typeof value === 'boolean' ? value.toString() : value;
      }
      items.push(item);
      ids.push(item.id);
    });
  }

  return { result: items, ids };
}
