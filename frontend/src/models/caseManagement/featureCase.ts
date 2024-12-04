import { type MoveMode, TableQueryParams } from '@/models/common';
import { LastReviewResult, StatusType } from '@/enums/caseEnum';

import { ReviewResult } from './caseReview';

export interface ModulesTreeType {
  id: string;
  name: string;
  type: string;
  parentId: string;
  children: ModulesTreeType[];
  attachInfo: Record<string, any>; // 附加信息
  count: number; // 节点资源数量（多数情况下不会随着节点信息返回，视接口而定）
}
// 创建模块
export interface CreateOrUpdateModule {
  projectId: string;
  name: string;
  parentId: string;
}
// 更新模块
export interface UpdateModule {
  id: string;
  name: string;
}

export interface customFieldsItem {
  caseId?: string; // 用例id
  fieldId: string;
  value: string;
  defaultValue?: string;
  [key: string]: any;
}

export interface OptionsField {
  fieldId: string;
  value: string;
  text: string;
  internal: boolean; // 是否是内置
}

export interface CustomAttributes {
  fieldId: string;
  fieldName: string;
  required: boolean;
  apiFieldId: null | undefined | 'string'; // 三方API
  defaultValue: string;
  type: string;
  options: OptionsField[];
}

// 功能用例表
export interface CaseManagementTable {
  id: string;
  num: number;
  moduleId: string; // 模块ID
  projectId: string;
  templateId: string; // 模板ID
  name: string; // 名称
  reviewStatus: StatusType[keyof StatusType]; // 评审状态：未评审/评审中/通过/不通过/重新提审
  tags: any; // 标签（JSON)
  caseEditType: string; // 编辑模式：步骤模式/文本模式
  prerequisite: string; // 前置操作
  pos: number; // 自定义排序，间隔5000
  versionId: string; // 版本ID
  refId: string; // 指向初始版本ID
  lastExecuteResult: string; // 最近的执行结果：未执行/通过/失败/阻塞/跳过
  deleted: boolean; // 是否在回收站：0-否，1-是
  publicCase: boolean; // 是否是公共用例：0-否，1-是
  latest: boolean; // 是否为最新版本：0-否，1-是
  createUser: string;
  updateUser: string;
  deleteUser: string;
  createTime: string;
  updateTime: string;
  deleteTime: string;
  steps: string;
  status: LastReviewResult;
  customFields: customFieldsItem[]; // 自定义字段集合
  [key: string]: any;
}

// 选择类型步骤和预期结果列表
export interface StepList {
  num?: string;
  id: string;
  step: string; // 步骤
  expected: string; // 预期
  showStep: boolean; // 编辑步骤模式
  showExpected: boolean; // 编辑预期模式
  actualResult?: string; // 实际
  executeResult?: string; // 步骤执行结果
  status?: string;
}

export type StepExecutionResult = Pick<StepList, 'actualResult' | 'executeResult'>;

// 关联文件列表
export interface AssociatedList {
  id: string;
  name: string;
  fileType: string; // 文件类型
  projectId: string;
  tags: any;
  description: string;
  moduleName: string; // 模块名称
  originalName: string; // 文件原始名称
  moduleId: string;
  createUser: string;
  createTime: number | string;
  updateUser: string;
  updateTime: number;
  storage: string;
  size: number;
  enable: true;
  refId: string;
  filePath: string; // 文件路径
  [key: string]: any;
}
export interface CreateCaseType {
  request: CaseManagementTable;
  files: File[];
}

export interface FileListQueryParams extends TableQueryParams {
  moduleIds: string[];
  versionId: string;
  projectId: string;
  name: string;
}

export interface DeleteCaseType {
  id: string;
  deleteAll?: boolean;
  projectId: string;
}
export interface BatchDeleteType {
  selectIds: string[];
  projectId: string;
}

// 批量编辑
export interface BatchEditCaseType {
  selectIds: string[];
  projectId: string;
  append: boolean; // 是否追加标签
  tags: string[];
  customField: {
    fieldId: string;
    value: string;
  };
}
// 批量移动
export interface BatchMoveOrCopyType {
  selectIds: string[] | undefined;
  projectId: string;
  moduleId?: string;
  moduleIds: string[];
  selectAll: boolean;
  excludeIds: string[] | undefined;
  condition: Record<string, any>;
}
export type CaseEditType = 'STEP' | 'TEXT';
// 创建或者更新
export interface CreateOrUpdateCase {
  id?: string;
  projectId: string;
  templateId: string;
  name: string;
  prerequisite: string; // prerequisite
  caseEditType: CaseEditType; // 编辑模式：步骤模式/文本模式
  steps: string;
  textDescription: string;
  expectedResult: string; // 预期结果
  description: string;
  publicCase: boolean; // 是否公共用例
  moduleId: string;
  versionId: string;
  tags: string[];
  customFields?: Record<string, any>; // 自定义字段集合
  relateFileMetaIds?: string[]; // 关联文件ID集合
  deleteFileMetaIds?: string[]; //  删除本地上传的文件id
  unLinkFilesIds?: string[]; //  	取消关联的文件id
  [key: string]: any;
}
export interface AttachFileInfo {
  id: string;
  fileId: string;
  fileName: string;
  size: number;
  local: boolean;
  createUser: string;
  createTime: number;
  [key: string]: any;
}

// 详情
export interface DetailCase {
  id: string;
  num?: number;
  moduleId: string;
  moduleName?: string;
  projectId: string;
  templateId?: string;
  name: string;
  reviewStatus: ReviewResult;
  tags: string[];
  caseEditType: string;
  versionId?: string;
  publicCase: boolean;
  latest?: boolean;
  createUser?: string;
  steps: string;
  textDescription: string;
  expectedResult: string;
  prerequisite: string;
  description: string;
  customFields?: Record<string, any>[];
  attachments?: AttachFileInfo[];
  followFlag?: boolean;
  functionalPriority: string;
  [key: string]: any;
}

// 回收站
export interface CaseModuleQueryParams extends TableQueryParams {
  moduleIds: string[];
  projectId: string;
}

export interface TabItemType {
  value: string;
  label: string;
  canHide: boolean;
  isShow: boolean;
}

// 需求
export interface DemandItem {
  id: string;
  caseId: string; // 功能用例ID
  demandId: string; // 需求ID
  demandName: string; // 需求标题
  demandUrl: string; // 需求地址
  demandPlatform: string; // 需求所属平台
  createTime: string;
  updateTime: string;
  createUser: string;
  updateUser: string;
  children: DemandItem[]; // 平台下对应的需求
  [key: string]: any;
}

// 平台需求列表
export interface DemandFormList {
  demandId: string;
  demandName: string;
  demandUrl: string;
}

// 创建需求&编辑需求
export interface CreateOrUpdateDemand {
  id?: string;
  demandPlatform: string;
  demandList?: DemandFormList[];
  caseId?: string;
  [key: string]: any;
}
// 转存文件
export interface OperationFile {
  id?: string;
  projectId: string;
  caseId?: string;
  fileId?: string; // 文件id
  local?: boolean; // 是否是本地
  moduleId?: string; // 文件转存模块id
  [key: string]: any;
}

// 评论列表项
export interface CommentItem {
  id: string;
  caseId: string; // 功能用例ID
  createUser: string; // 评论人
  status: string; // 评审/测试计划执行状态:通过/不通过/重新提审/通过标准变更标记/强制通过标记/强制不通过标记/状态变更标记
  type: string; // 评论类型：用例评论/测试计划用例评论/评审用例评论
  parentId: string; // 父评论ID
  resourceId: string; // 资源ID: 评审ID/测试计划ID
  notifier: string; // 通知人
  replyUser: string; // 回复人
  createTime: string;
  updateTime: string;
  content: string; // 评论内容
  userName: string; // 评论的人名
  replyUserName: string; // 被回复的人名
  replyUserLogo: string; // 被回复的人头像
  userLogo: string; // 评论的人头像
  replies: CommentItem[]; // 该条评论下的所有回复数据
}
// 创建或更新评论
export interface CreateOrUpdate {
  id?: string;
  caseId: string;
  notifier: string;
  replyUser: string;
  parentId: string;
  content: string;
  event: string; // 任务事件(仅评论: ’COMMENT‘; 评论并@: ’AT‘; 回复评论/回复并@: ’REPLAY‘;)
}

export type DemandList = DemandItem[];

// 获取editor富文本语言详情
export interface PreviewImages {
  projectId: string;
  caseId: string;
  fileId?: string;
  fileSource?: string; // 附件(ATTACHMENT)/功能用例详情(CASE_DETAIL)/用例评论(CASE_COMMENT)/评审评论(REVIEW_COMMENT)
  local: boolean;
}

// 导入excel检查
export interface ImportExcelType {
  projectId: string;
  versionId: string;
  cover: boolean;
}

export interface errorMessagesType {
  rowNum: number;
  errMsg: string;
}
export interface ValidateInfo {
  failCount: number;
  successCount: number;
  errorMessages: errorMessagesType[];
}
// 拖拽排序
export interface DragCase {
  projectId: string;
  targetId: string;
  moveMode: 'BEFORE' | 'AFTER' | 'APPEND'[]; // 拖拽类型
  moveId: string;
}
// 变更历史
export interface ChangeHistoryItem {
  id: string;
  projectId: string;
  createTime: string;
  createUser: string;
  sourceId: string;
  type: string;
  module: string;
  refId: string;
  createUserName: string;
  versionName: string;
}

// 取消前后置依赖关系
export interface DeleteDependencyParams {
  id: string;
  caseId: string;
  type: string;
}

export interface ContentTabsMap {
  tabList: TabItemType[];
  backupTabList: TabItemType[];
}
// 脑图删除的模块/用例的集合
export interface FeatureCaseMinderDeleteResourceItem {
  id: string;
  type: string;
}
// 脑图用例操作类型（新增(ADD)/更新(UPDATE)）
export type FeatureCaseMinderActionType = 'ADD' | 'UPDATE';
// 脑图用例编辑模式
export type FeatureCaseMinderEditType = 'STEP' | 'TEXT';
// 脑图新增/修改的模块集合（只记录操作的节点，节点下的子节点不需要记录）
export interface FeatureCaseMinderUpdateModuleItem {
  id: string;
  name: string;
  parentId: string;
  type: FeatureCaseMinderActionType;
  moveMode?: MoveMode;
  targetId?: string;
}
// 脑图新增/修改的文本节点集合
export interface FeatureCaseMinderUpdateTextNodeItem {
  id: string;
  name: string;
  parentId: string;
  type: FeatureCaseMinderActionType;
  moveMode?: MoveMode;
  targetId?: string;
}
export interface CustomField {
  fieldId: string;
  value: string;
}
// 脑图用例步骤描述项
export interface FeatureCaseMinderStepItem {
  id: string;
  num: number;
  desc: string;
  result?: string;
}
// 脑图新增/修改的用例对象集合
export interface FeatureCaseMinderUpdateCaseItem {
  id: string; // 用例id(新增的时候前端传UUid，更新的时候必填)
  templateId: string; // 模板id
  type: FeatureCaseMinderActionType;
  name: string;
  moduleId: string;
  moveMode?: MoveMode; // 移动方式（节点移动或新增时需要）
  targetId?: string;
  prerequisite: string; // 前置操作
  caseEditType: FeatureCaseMinderEditType;
  steps: string;
  textDescription: string; // 文本描述
  expectedResult: string; // 期望结果
  description: string;
  tags: string[];
  customFields: CustomField[];
}
// 脑图
export interface FeatureCaseMinderUpdateParams {
  projectId: string;
  versionId?: string;
  updateCaseList: FeatureCaseMinderUpdateCaseItem[];
  updateModuleList: FeatureCaseMinderUpdateModuleItem[];
  deleteResourceList: FeatureCaseMinderDeleteResourceItem[];
  additionalNodeList: FeatureCaseMinderUpdateTextNodeItem[];
}
