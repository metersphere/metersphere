import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

import { localExecuteApiDebug } from '@/api/modules/api-test/common';
import { debugScenario } from '@/api/modules/api-test/scenario';
import { getSocket } from '@/api/modules/project-management/commonScript';
import { useI18n } from '@/hooks/useI18n';
import useAppStore from '@/store/modules/app';
import { findNodeByKey, getGenerateId, mapTree, traverseTree } from '@/utils';

import type { RequestResult } from '@/models/apiTest/common';
import type {
  ApiScenarioDebugRequest,
  Scenario,
  ScenarioStepDetails,
  ScenarioStepItem,
} from '@/models/apiTest/scenario';
import { ScenarioExecuteStatus, ScenarioStepRefType, ScenarioStepType } from '@/enums/apiEnum';

import type { RequestParam } from '../common/customApiDrawer.vue';
import updateStepStatus, { getScenarioFileParams, getStepDetails } from '../utils';

/**
 * 步骤执行逻辑
 */
export default function useStepExecute({
  scenario,
  steps,
  stepDetails,
  activeStep,
  isPriorityLocalExec,
  localExecuteUrl,
}: {
  scenario: Ref<Scenario>;
  steps: Ref<ScenarioStepItem[]>;
  stepDetails: Ref<Record<string, ScenarioStepDetails>>;
  activeStep: Ref<ScenarioStepItem | undefined>;
  isPriorityLocalExec: Ref<boolean> | undefined;
  localExecuteUrl: Ref<string> | undefined;
}) {
  const appStore = useAppStore();
  const websocketMap: Record<string | number, WebSocket> = {};

  /**
   * 开启websocket监听，接收执行结果
   */
  function debugSocket(step: ScenarioStepItem, _scenario: Scenario, reportId: string | number) {
    return new Promise((resolve) => {
      websocketMap[reportId] = getSocket(
        reportId || '',
        scenario.value.executeType === 'localExec' ? '/ws/debug' : '',
        scenario.value.executeType === 'localExec' ? localExecuteUrl?.value : ''
      );
      websocketMap[reportId].addEventListener('message', (event) => {
        const data = JSON.parse(event.data);
        if (data.msgType === 'EXEC_RESULT') {
          if (step.reportId === data.reportId) {
            // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
            data.taskResult.requestResults.forEach((result: RequestResult) => {
              if (_scenario.stepResponses[result.stepId] === undefined) {
                _scenario.stepResponses[result.stepId] = [];
              }
              _scenario.stepResponses[result.stepId].push({
                ...result,
                console: data.taskResult.console,
              });
            });
          }
        } else if (data.msgType === 'EXEC_END') {
          // 执行结束，关闭websocket
          websocketMap[reportId]?.close();
          if (step.reportId === data.reportId) {
            step.isExecuting = false;
            if (_scenario.stepResponses[step.uniqueId] === undefined) {
              _scenario.stepResponses[step.uniqueId] = [];
            }
            updateStepStatus([step], _scenario.stepResponses, step.uniqueId);
          }
        }
      });
      websocketMap[reportId].addEventListener('open', () => {
        resolve(true);
      });
    });
  }

  async function realExecute(
    executeParams: Pick<ApiScenarioDebugRequest, 'steps' | 'stepDetails' | 'reportId' | 'stepFileParam'>
  ) {
    const [currentStep] = executeParams.steps;
    try {
      currentStep.isExecuting = true;
      currentStep.executeStatus = ScenarioExecuteStatus.EXECUTING;
      await debugSocket(currentStep, scenario.value, executeParams.reportId); // 开启websocket
      const res = await debugScenario({
        id: scenario.value.id || '',
        grouped: false,
        environmentId: appStore.currentEnvConfig?.id || '',
        projectId: appStore.currentProjectId,
        scenarioConfig: scenario.value.scenarioConfig,
        frontendDebug: scenario.value.executeType === 'localExec',
        fileParam: {
          ...getScenarioFileParams(scenario.value),
        },
        ...executeParams,
        stepDetails: getStepDetails(executeParams.steps, executeParams.stepDetails),
        steps: mapTree(executeParams.steps, (node) => {
          return {
            ...node,
            enable: node.uniqueId === currentStep.uniqueId || node.enable, // 单步骤执行，则临时无视顶层启用禁用状态
            parent: null, // 原树形结构存在循环引用，这里要去掉以免 axios 序列化失败
          };
        }),
      });
      if (scenario.value.executeType === 'localExec' && localExecuteUrl?.value) {
        await localExecuteApiDebug(localExecuteUrl.value, res);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      websocketMap[executeParams.reportId].close();
      currentStep.isExecuting = false;
      updateStepStatus([currentStep], scenario.value.stepResponses, currentStep.uniqueId);
    }
  }

  /**
   * 单个步骤执行调试
   */
  function executeStep(node: MsTreeNodeData) {
    if (node.isExecuting) {
      return;
    }
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, node.uniqueId, 'uniqueId');
    if (realStep) {
      realStep.reportId = getGenerateId();
      const _stepDetails: Record<string, any> = {};
      const stepFileParam = scenario.value.stepFileParam[realStep.id];
      traverseTree(
        realStep,
        (step) => {
          if (step.enable || step.uniqueId === realStep.uniqueId) {
            // 启用的步骤才执行；如果点击的是禁用步骤也执行，但是禁用的子步骤不执行
            _stepDetails[step.id] = stepDetails.value[step.id];
            step.executeStatus = ScenarioExecuteStatus.EXECUTING;
          } else {
            step.executeStatus = undefined;
          }
          delete scenario.value.stepResponses[step.uniqueId]; // 先移除上一次的执行结果
        },
        (step) => {
          // 当前步骤是启用的情或是在禁用的步骤上点击执行，才需要继续递归子孙步骤；否则无需向下递归
          return step.enable || step.uniqueId === realStep.uniqueId;
        }
      );
      scenario.value.executeType = isPriorityLocalExec?.value ? 'localExec' : 'serverExec';
      realExecute({
        steps: [realStep as ScenarioStepItem],
        stepDetails: _stepDetails,
        reportId: realStep.reportId,
        stepFileParam: {
          [realStep.id]: stepFileParam,
        },
      });
    }
  }

  /**
   * 处理 api 详情抽屉的执行动作
   * @param request 抽屉内的请求参数
   * @param executeType 执行类型
   */
  function handleApiExecute(request: RequestParam, executeType?: 'localExec' | 'serverExec') {
    const { t } = useI18n();
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, request.uniqueId || request.stepId, 'uniqueId');
    if (realStep) {
      delete scenario.value.stepResponses[realStep.uniqueId]; // 先移除上一次的执行结果
      realStep.reportId = getGenerateId();
      realStep.executeStatus = ScenarioExecuteStatus.EXECUTING;
      request.executeLoading = true;
      scenario.value.executeType = executeType;
      realExecute({
        steps: [realStep as ScenarioStepItem],
        stepDetails: {
          [realStep.id]: request,
        },
        reportId: realStep.reportId,
        stepFileParam: {
          [realStep.uniqueId]: {
            uploadFileIds: request.uploadFileIds || [],
            linkFileIds: request.linkFileIds || [],
          },
        },
      });
    } else {
      // 步骤列表找不到该步骤，说明是新建的自定义请求还未保存，则临时创建一个步骤进行调试（不保存步骤信息）
      delete scenario.value.stepResponses[request.uniqueId || request.stepId]; // 先移除上一次的执行结果
      const reportId = getGenerateId();
      request.executeLoading = true;
      activeStep.value = {
        id: request.stepId,
        name: t('apiScenario.customApi'),
        stepType: ScenarioStepType.CUSTOM_REQUEST,
        refType: ScenarioStepRefType.DIRECT,
        sort: 1,
        enable: true,
        isNew: true,
        config: {},
        projectId: appStore.currentProjectId,
        isExecuting: false,
        reportId,
        uniqueId: request.uniqueId,
      };
      realExecute({
        steps: [activeStep.value],
        stepDetails: {
          [request.stepId]: request,
        },
        reportId,
        stepFileParam: {
          [request.stepId]: {
            uploadFileIds: request.uploadFileIds || [],
            linkFileIds: request.linkFileIds || [],
          },
        },
      });
    }
  }

  async function handleStopExecute(step?: ScenarioStepItem) {
    if (step?.reportId) {
      const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
      websocketMap[step.reportId].close();
      if (realStep) {
        realStep.isExecuting = false;
        updateStepStatus([realStep as ScenarioStepItem], scenario.value.stepResponses, realStep.uniqueId);
      }
    }
  }

  return {
    executeStep,
    handleApiExecute,
    handleStopExecute,
  };
}
