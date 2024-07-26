import { traverseTree } from '@/utils';

import { RequestResult } from '@/models/apiTest/common';
import { type Scenario, type ScenarioStepDetails, ScenarioStepItem } from '@/models/apiTest/scenario';
import { ScenarioExecuteStatus, ScenarioStepType } from '@/enums/apiEnum';

import type { RequestParam } from './common/customApiDrawer.vue';

/**
 * 调试或执行结束后，调用本方法更新步骤的执行状态
 * @param steps 响应式的步骤列表
 * @param stepResponses 步骤的执行结果
 * @param singleStepId 单步骤执行时的步骤ID
 */
export default function updateStepStatus(
  steps: ScenarioStepItem[],
  stepResponses: Record<string | number, RequestResult[]>,
  singleStepId?: string | number
) {
  for (let i = 0; i < steps.length; i++) {
    const node = steps[i];
    if (node.enable || singleStepId === node.uniqueId) {
      // 启用的步骤才计算/如果是单步骤执行，无视顶层步骤的启用状态
      if (
        [
          ScenarioStepType.LOOP_CONTROLLER,
          ScenarioStepType.IF_CONTROLLER,
          ScenarioStepType.ONCE_ONLY_CONTROLLER,
          ScenarioStepType.API_SCENARIO,
        ].includes(node.stepType)
      ) {
        if (!node.executeStatus) {
          // 没有执行状态，说明未参与执行，直接跳过
          // eslint-disable-next-line no-continue
          continue;
        }
        // 逻辑控制器和场景内部可以放入任意步骤，所以它的最终执行结果是根据内部步骤的执行结果来判断的
        let hasNotExecuted = false;
        let hasFailure = false;
        let hasFakeError = false;
        if (!node.children || node.children.length === 0) {
          // 逻辑控制器内无步骤，则直接是未执行
          node.executeStatus = ScenarioExecuteStatus.UN_EXECUTE;
        } else {
          for (let j = 0; j < node.children.length; j++) {
            const childNode = node.children[j];
            updateStepStatus([childNode], stepResponses);
            if (childNode.executeStatus === ScenarioExecuteStatus.FAILED) {
              // 子节点有一个失败，逻辑控制器就是失败
              hasFailure = true;
            } else if (childNode.executeStatus === ScenarioExecuteStatus.FAKE_ERROR) {
              // 子节点有一个误报，逻辑控制器就是误报
              hasFakeError = true;
            } else if (
              childNode.executeStatus &&
              [ScenarioExecuteStatus.EXECUTING, ScenarioExecuteStatus.UN_EXECUTE].includes(childNode.executeStatus)
            ) {
              // 子节点未执行或正在执行，则逻辑控制器也是未执行
              hasNotExecuted = true;
            }
          }
          // 递归完子节点后，判断当前逻辑控制器的状态
          if (hasFailure) {
            node.executeStatus = ScenarioExecuteStatus.FAILED;
          } else if (hasFakeError) {
            node.executeStatus = ScenarioExecuteStatus.FAKE_ERROR;
          } else if (hasNotExecuted) {
            node.executeStatus = ScenarioExecuteStatus.UN_EXECUTE;
          } else {
            node.executeStatus = ScenarioExecuteStatus.SUCCESS;
          }
        }
      } else if (node.stepType === ScenarioStepType.CONSTANT_TIMER) {
        node.executeStatus = stepResponses[node.uniqueId]
          ? ScenarioExecuteStatus.SUCCESS
          : ScenarioExecuteStatus.UN_EXECUTE;
      } else if (node.executeStatus === ScenarioExecuteStatus.EXECUTING) {
        // 非逻辑控制器直接更改本身状态
        if (stepResponses[node.uniqueId] && stepResponses[node.uniqueId].length > 0) {
          // 存在多个请求结果说明是循环控制器下的步骤，需要判断其子步骤的执行结果
          if (
            stepResponses[node.uniqueId].some(
              (report) => !report.isSuccessful && report.status !== ScenarioExecuteStatus.FAKE_ERROR
            )
          ) {
            node.executeStatus = ScenarioExecuteStatus.FAILED;
          } else if (
            stepResponses[node.uniqueId].some((report) => report.status === ScenarioExecuteStatus.FAKE_ERROR)
          ) {
            node.executeStatus = ScenarioExecuteStatus.FAKE_ERROR;
          } else {
            node.executeStatus = ScenarioExecuteStatus.SUCCESS;
          }
        } else {
          node.executeStatus = ScenarioExecuteStatus.UN_EXECUTE;
        }
      }
    }
  }
}

/**
 * 获取场景文件参数
 * @param scenario 场景对象
 */
export function getScenarioFileParams(scenario: Scenario) {
  const linkFileIds = new Set<string>();
  const uploadFileIds = new Set<string>();
  scenario.scenarioConfig.variable.csvVariables.forEach((item) => {
    if (item.file.local) {
      uploadFileIds.add(item.file.fileId);
    } else if (item.file.fileId) {
      linkFileIds.add(item.file.fileId);
    }
  });
  return {
    linkFileIds: Array.from(linkFileIds),
    uploadFileIds: Array.from(uploadFileIds),
  };
}

/**
 * 获取步骤详情参数集合
 * @param details 传入指定的详情映射
 */
export function getStepDetails(steps: ScenarioStepItem[], details: Record<string, ScenarioStepDetails>) {
  const newStepDetails: Record<string, ScenarioStepDetails> = {};
  traverseTree(steps, (step) => {
    const currentDetail = details[step.id] as RequestParam;
    if (
      currentDetail &&
      [ScenarioStepType.API, ScenarioStepType.API_CASE, ScenarioStepType.CUSTOM_REQUEST].includes(step.stepType)
    ) {
      // 接口类型需要处理 json-schema 的循环引用
      newStepDetails[step.id] = {
        ...currentDetail,
        body: {
          ...currentDetail.body,
          jsonBody: {
            ...currentDetail.body?.jsonBody,
            jsonSchema: currentDetail.body?.jsonBody?.jsonSchema,
            jsonSchemaTableData: [], // 原树形结构存在循环引用，这里要去掉以免 axios 序列化失败
          },
        },
      };
    } else {
      newStepDetails[step.id] = details[step.id];
    }
  });
  return newStepDetails;
}
