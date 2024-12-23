import { RequestResult } from '@/models/apiTest/common';
import type { RequestMethods } from '@/enums/apiEnum';
import type { ExecuteResultEnum, ExecuteStatusEnum } from '@/enums/taskCenter';

export interface LegendData {
  label: string;
  value: string;
  rote: number;
  count: number;
  class: string;
}

export interface AssertionItem {
  name: string;
  content: string;
  script: string;
  message: string;
  pass: boolean;
}
// 响应结果
export interface ResponseResult {
  responseCode: string;
  responseMessage: string;
  responseTime: number;
  latency: number;
  responseSize: number;
  headers: string;
  body: string;
  contentType: string;
  vars: string;
  imageUrl: string;
  socketInitTime: number;
  dnsLookupTime: number;
  tcpHandshakeTime: number;
  sslHandshakeTime: number;
  transferStartTime: number;
  downloadTime: number;
  bodySize: number;
  headerSize: number;
  assertions: AssertionItem[];
}

export type resultType = RequestResult & ResponseResult;

export interface StepContent {
  resourceId: string;
  projectId: string;
  stepId: string;
  threadName: string;
  name: string;
  url: string;
  requestSize: number;
  startTime: number;
  endTime: number;
  error: number;
  headers: string;
  cookies: string;
  body: string;
  status: string;
  method: RequestMethods | string;
  assertionTotal: number;
  passAssertionsTotal: number;
  subRequestResults: ResponseResult[];
  responseResult: ResponseResult;
  isSuccessful: boolean;
  fakeErrorCode: string;
  scriptIdentifier: string;
  [key: string]: any;
}

export type StepContentType = StepContent & RequestResult;
// 步骤详情
export interface ReportStepDetailItem {
  id: string;
  reportId: string;
  stepId: string;
  status: ExecuteStatusEnum;
  fakeCode: string;
  requestName: string;
  requestTime: number;
  code: string;
  responseSize: number;
  scriptIdentifier: string;
  content: StepContentType;
  [key: string]: any;
}

export type ReportStepDetail = Partial<ReportStepDetailItem>;

// 步骤
export interface ScenarioItemType {
  stepId: string;
  reportId: string;
  name: string; // 步骤名称
  sort: number; // 临时序号
  index: number; // 序号
  stepType: string; // 步骤类型/API/CASE等
  parentId: string; // 父级id
  status: string; // 结果状态 SUCCESS/ERROR
  fakeCode: string; // 误报编号/误报状态独有
  requestName: string; // 请求名称
  requestTime: number; // 请求耗时
  code: string; // 请求响应码
  responseSize: number; // 响应内容大小
  scriptIdentifier: string; // 脚本标识
  fold: boolean; // 是否展示折叠
  expanded: boolean; // 是否展开折叠树节点
  children: ScenarioItemType[];
  level?: number;
  stepDetail: ReportStepDetailItem;
  stepChildren?: ScenarioItemType[]; // 步骤子步骤
}

export type ScenarioDetailItem = Partial<ScenarioItemType>;
// 报告场景的详情
export interface ReportDetail {
  id: string;
  name: string; // 报告名称
  testPlanId: string;
  createUser: string;
  createTime?: number;
  deleteTime: number;
  deleteUser: string;
  deleted: boolean;
  updateUser: string;
  updateTime: number;
  startTime: number; // 开始时间/同创建时间一致
  endTime: number; //  结束时间/报告执行完成
  requestDuration: number; // 请求总耗时
  status: ExecuteStatusEnum; // 报告状态/SUCCESS/ERROR
  execStatus?: ExecuteStatusEnum; // 报告状态/SUCCESS/ERROR
  triggerMode: string; // 触发方式
  runMode: string; // 执行模式
  poolId: string; // 资源池
  poolName: string; // 资源池名称
  versionId: string;
  integrated: boolean; // 是否是集成报告
  projectId: string;
  environmentId: string; // 环境id
  environmentName: string; // 环境名称
  errorCount: number; // 失败数
  fakeErrorCount: number; // 误报数
  pendingCount: number; // 未执行数
  successCount: number; // 成功数
  assertionCount: number; // 总断言数
  assertionSuccessCount: number; // 成功断言数
  requestErrorRate: string; // 请求失败率
  requestPendingRate: string; // 请求未执行率
  requestFakeErrorRate: string; // 请求误报率
  requestPassRate: string; // 请求通过率
  assertionPassRate: string; // 断言通过率
  scriptIdentifier: string; // 脚本标识
  children: ScenarioItemType[]; // 步骤列表
  stepTotal: number; // 步骤总数
  console: string; // 控制台
  taskOrigin?: string; // 任务来源
  taskOriginName?: string; // 任务来源名称
  result?: ExecuteResultEnum;
  resourcePoolNode?: string; // 资源池节点
  threadId?: string; // 线程ID
  [key: string]: any;
}

export type ReportDetailPartial = Partial<ScenarioItemType>;

// 获取分享id
export interface GetShareId {
  reportId: string;
  projectId: string;
}
// 用例任务报告详情
export interface TaskReportDetail {
  id: string;
  name: string; // 报告名称
  createUser: string;
  createTime: number;
  startTime: number; // 开始时间/同创建时间一致
  endTime: number; //  结束时间/报告执行完成
  status: ExecuteStatusEnum; // 报告状态
  poolId: string; // 资源池
  resourcePoolName: string; // 资源池名称
  integrated: boolean; // 是否是集成报告
  environmentId: string; // 环境id
  environmentName: string; // 环境名称
  taskOrigin: string; // 任务来源
  taskOriginName: string; // 任务来源名称
  result: ExecuteResultEnum;
  resourcePoolNode: string; // 资源池节点
  threadId: string; // 线程ID
  apiReportDetailDTOList: ReportStepDetailItem[];
}
