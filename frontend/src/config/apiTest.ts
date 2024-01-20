import { ConditionType } from '@/models/apiTest/debug';

// 条件操作类型
export type ConditionTypeNameMap = Record<ConditionType, string>;
export const conditionTypeNameMap = {
  script: 'apiTestDebug.script',
  sql: 'apiTestDebug.sql',
  waitTime: 'apiTestDebug.waitTime',
  extract: 'apiTestDebug.extractParameter',
};

export default {};
