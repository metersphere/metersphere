// 条件操作类型
export type ConditionType = 'script' | 'sql' | 'waitTime' | 'extract';
// 表达式类型
export type ExpressionType = 'regular' | 'JSONPath' | 'XPath';
// 表达式配置
export interface ExpressionConfig {
  expression: string;
  expressionType?: ExpressionType;
  regexpMatchRule?: 'expression' | 'group'; // 正则表达式匹配规则
  resultMatchRule?: 'random' | 'specify' | 'all'; // 结果匹配规则
  specifyMatchNum?: number; // 指定匹配下标
  xmlMatchContentType?: 'xml' | 'html'; // 响应内容格式
}
// 响应时间信息
export interface ResponseTiming {
  ready: number;
  socketInit: number;
  dnsQuery: number;
  tcpHandshake: number;
  sslHandshake: number;
  waitingTTFB: number;
  downloadContent: number;
  deal: number;
  total: number;
}
