export interface ScenarioItemType {
  stepId: string;
  reportId: string;
  name: string; // 步骤名称
  sort: number; // 序号
  stepType: string; // 步骤类型/API/CASE等
  parentId: string; // 父级id
  status: string; // 结果状态 SUCCESS/ERROR
  fakeCode: string; // 误报编号/误报状态独有
  requestName: string; // 请求名称
  requestTime: number; // 请求耗时
  code: string; // 请求响应码
  responseSize: number; // 响应内容大小
  scriptIdentifier: string; // 脚本标识
}
