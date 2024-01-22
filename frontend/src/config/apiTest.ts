import { ConditionType } from '@/models/apiTest/debug';

// 条件操作类型
export type ConditionTypeNameMap = Record<ConditionType, string>;
export const conditionTypeNameMap = {
  script: 'apiTestDebug.script',
  sql: 'apiTestDebug.sql',
  waitTime: 'apiTestDebug.waitTime',
  extract: 'apiTestDebug.extractParameter',
};
// 代码字符集
export const codeCharset = ['UTF-8', 'UTF-16', 'GBK', 'GB2312', 'ISO-8859-1', 'Shift_JIS', 'ASCII', 'BIG5', 'KOI8-R'];
