// @ts-nocheck
import type { BubbleListItemProps } from 'vue-element-plus-x/types/BubbleList';

import type { AiChatContentRoleTypeEnum } from '@/enums/aiEnums';

export interface AiChatPrams {
  prompt: string;
  chatModelId: string;
  conversationId: string;
  organizationId: string;
}

export interface AiChatResponse {
  id: string;
  title: string;
  createTime: number;
  createUser: string;
}

export interface AiChatListItem {
  id: string;
  title: string;
  createTime: number;
  createUser: string;
  [key: string]: any; // 前端属性
}

export interface AiChatContentItem extends Partial<BubbleListItemProps> {
  id: string;
  conversationId: string;
  type: AiChatContentRoleTypeEnum;
  timestamp: number;
  content: string;
  [key: string]: any; // 前端属性
}

export interface AiModelConfig {
  id: string;
  name: string;
}

export interface CaseTemplateConfig {
  caseEditType: 'STEP' | 'TEXT';
  caseName: boolean;
  preCondition: boolean;
  caseSteps: boolean;
  expectedResult: boolean;
  remark: boolean;
}

export interface CaseDesignConfig {
  normal: boolean;
  abnormal: boolean;
  equivalenceClassPartitioning: boolean;
  boundaryValueAnalysis: boolean;
  decisionTableTesting: boolean;
  causeEffectGraphing: boolean;
  orthogonalExperimentMethod: boolean;
  scenarioMethod: boolean;
  scenarioMethodDescription: string;
}

export interface CaseAiChatConfig {
  designConfig: CaseDesignConfig;
  templateConfig: CaseTemplateConfig;
}

export interface CaseAiBatchSaveParams extends AiChatPrams {
  projectId: string | number; // 项目 ID
  moduleId: string | number; // 模块 ID
  templateId: string | number; // 模板 ID
}

export interface AiCaseStepDescription {
  id: string;
  num: number;
  desc: string;
  result: string;
}
export interface AiCaseTransformResult {
  name: string;
  description: string;
  prerequisite: string;
  stepDescription: AiCaseStepDescription[];
  textDescription: string;
  expectedResult: string;
  caseEditType: string;
}

export interface ApiAiChatConfig {
  normal: boolean;
  abnormal: boolean;
  caseName: boolean;
  requestParams: boolean;
  preScript: boolean;
  postScript: boolean;
  assertion: boolean;
}

export interface ApiAiChatParams {
  prompt: string;
  chatModelId: string;
  conversationId: string;
  organizationId: string;
  apiDefinitionId: string | number; // 接口定义 id
}

export interface ApiCaseAiBatchSaveParams extends ApiAiChatParams {
  projectId: string | number; // 项目 ID
}

export interface ApiCaseAiBatchSaveResponse {
  errorCount: number; // 错误数量
  successCount: number; // 成功数量
  errorDetail: string; // 错误详情
}
