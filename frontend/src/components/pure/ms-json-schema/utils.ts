import { Message } from '@arco-design/web-vue';

import { useI18n } from '@/hooks/useI18n';
import { getGenerateId } from '@/utils';

import type { JsonSchema, JsonSchemaItem, JsonSchemaTableItem } from './types';

/**
 * 将 json-schema 表格组件的表格项转换为 json-schema 规范的结构
 * @param schemaItem 表格组件项
 * @param isRoot 是否为根节点
 */
export function parseTableDataToJsonSchema(
  schemaItem?: JsonSchemaTableItem,
  isRoot: boolean = true
): JsonSchema | JsonSchemaItem | undefined {
  try {
    if (!schemaItem || !schemaItem.title) return undefined;
    let schema: JsonSchema | JsonSchemaItem = { type: schemaItem.type };

    // 对于 null 类型，只设置 type、enable、description 属性
    if (schemaItem.type === 'null') {
      return {
        type: 'null',
        enable: schemaItem.enable,
        description: schemaItem.description,
      };
    }

    if (!isRoot) {
      // 非根节点，组装普通节点信息
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

    if (schemaItem.children) {
      if (schemaItem.type === 'object') {
        // 对象类型
        schema = {
          type: 'object',
          enable: schemaItem.enable,
          properties: {},
          required: [],
          description: schemaItem.description,
        };
        schemaItem.children.forEach((child) => {
          const childSchema = parseTableDataToJsonSchema(child, false);
          schema.properties![child.title] = childSchema as JsonSchemaItem;
          if (child.required) {
            schema.required!.push(child.title);
          }
        });
        if (schema.required!.length === 0) {
          delete schema.required;
        }
      } else if (schemaItem.type === 'array') {
        // 数组类型
        schema = {
          type: 'array',
          enable: schemaItem.enable,
          items: schemaItem.children.map((child) => parseTableDataToJsonSchema(child, false) as JsonSchemaItem),
          minItems: schemaItem.minItems,
          maxItems: schemaItem.maxItems,
          description: schemaItem.description,
        };
      }
    }

    return schema;
  } catch (error) {
    // eslint-disable-next-line no-console
    console.log(error);
    return undefined;
  }
}

/**
 * 创建 json-schema 表格组件的表格项
 * @param key 对象/数组/普通子项的 key名
 * @param value 对象/数组/普通子项的值
 * @param parent 父级
 */
function createItem(key: string, value: any, parent?: JsonSchemaTableItem): JsonSchemaTableItem {
  let exampleValue; // 默认情况下，example 值为 undefined
  let itemType = Array.isArray(value) ? 'array' : typeof value;
  if (value === null) {
    itemType = 'null';
  }

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
    required: false,
    defaultValue: '',
    example: exampleValue, // 仅当值不是对象或数组时，才赋予 example 值
    parent,
  };
}

/**
 * 将 json 转换为 json-schema 表格组件的数据结构
 * @param json json 字符串或对象或数组
 * @param parent 父级
 */
export function parseJsonToJsonSchemaTableData(
  json: string | object | Array<any> | null,
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
      required: false,
      example: '',
      defaultValue: '',
    };
    const children = parseJsonToJsonSchemaTableData(json, rootItem);
    rootItem.children = children.result;
    children.ids.push(rootItem.id);
    return { result: [rootItem], ids: children.ids };
  }

  const items: JsonSchemaTableItem[] = [];
  const type = Array.isArray(json) ? 'array' : 'object';
  const ids: Array<string> = [];

  if ((type === 'object' || type === 'array') && json !== null) {
    // 遍历对象或数组
    Object.entries(json).forEach(([key, value]) => {
      const item: JsonSchemaTableItem = createItem(key, value, parent);
      if ((typeof value === 'object' && value !== null) || Array.isArray(value)) {
        const children = parseJsonToJsonSchemaTableData(value, item);
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

/**
 * 将 json-schema 规范的结构转换为 json-schema 表格组件的数据结构
 * @param schema json-schema 规范的结构/字符串
 */
export function parseSchemaToJsonSchemaTableData(schema: string | JsonSchema): {
  result: JsonSchemaTableItem[];
  ids: Array<string>;
} {
  const ids: Array<string> = ['root'];
  if (typeof schema === 'string') {
    // 尝试将 json 字符串转换为对象
    try {
      schema = JSON.parse(schema);
    } catch (error) {
      const { t } = useI18n();
      Message.warning(t('ms.json.schema.illegalJsonConvertFailed'));
      return { result: [], ids: [] };
    }
  }
  const parseNode = (
    node: JsonSchema | JsonSchemaItem,
    parent?: JsonSchemaTableItem,
    requiredFields?: string[]
  ): JsonSchemaTableItem => {
    let item: JsonSchemaTableItem;

    if (!parent) {
      // 根节点
      item = {
        id: 'root',
        title: 'root',
        type: node.type,
        description: node.description,
        enable: true,
        required: false,
        example: '',
        defaultValue: '',
        maxItems: node.maxItems,
        minItems: node.minItems,
      };
    } else {
      // 子孙节点
      // 剔除不需要的属性 properties、items
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { properties, items, ...copiedObj } = node;
      const itemId = getGenerateId();
      item = {
        ...(copiedObj as JsonSchemaItem),
        id: itemId,
        required: requiredFields?.includes((node as JsonSchemaItem).title),
        enumValues: (node as JsonSchemaItem).enumValues?.join('\n'),
        parent,
      };
      if ((node as JsonSchemaItem).enable === true || (node as JsonSchemaItem).enable === undefined) {
        // 如果enable为true或者undefined，则设置为选中
        ids.push(itemId);
      }
    }

    // 检查当前节点的required属性是否存在且为数组类型，然后传递当前节点的required属性给子节点判断是否必填
    const newRequiredFields = Array.isArray(node.required) ? node.required : undefined;

    if ((node.type === 'object' && node.properties) || (node.type === 'array' && node.items)) {
      const children = node.type === 'object' ? node.properties : node.items;
      item.children = Object.entries(children || []).map(([key, childNode]) =>
        parseNode({ ...childNode, title: key }, item, newRequiredFields)
      );
    }
    return item;
  };

  const result = [parseNode(schema as JsonSchema)];
  return { result, ids };
}
