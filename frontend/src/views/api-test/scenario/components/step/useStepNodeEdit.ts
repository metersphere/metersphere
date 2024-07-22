import { Message } from '@arco-design/web-vue';

import MsTree from '@/components/business/ms-tree/index.vue';

import { getScenarioDetail } from '@/api/modules/api-test/scenario';
import { useI18n } from '@/hooks/useI18n';
import { findNodeByKey, getGenerateId, mapTree } from '@/utils';

import type { Scenario, ScenarioStepConfig, ScenarioStepDetail, ScenarioStepItem } from '@/models/apiTest/scenario';
import { ScenarioStepRefType } from '@/enums/apiEnum';

/**
 * 处理步骤节点信息更改
 */
export default function useStepNodeEdit({
  steps,
  scenario,
  activeStep,
  quickInputDataKey,
  quickInputParamValue,
  showQuickInput,
  treeRef,
  tempStepDesc,
  showStepDescEditInputStepId,
  tempStepName,
  showStepNameEditInputStepId,
  loading,
  selectedKeys,
  showScenarioConfig,
}: {
  steps: Ref<ScenarioStepItem[]>;
  scenario: Ref<Scenario>;
  activeStep: Ref<ScenarioStepItem | undefined>;
  quickInputDataKey: Ref<string>;
  quickInputParamValue: Ref<any>;
  showQuickInput: Ref<boolean>;
  treeRef: Ref<InstanceType<typeof MsTree> | undefined>;
  tempStepDesc: Ref<string>;
  showStepDescEditInputStepId: Ref<string | number>;
  tempStepName: Ref<string>;
  showStepNameEditInputStepId: Ref<string | number>;
  loading: Ref<boolean>;
  selectedKeys: Ref<Array<string | number>>;
  showScenarioConfig: Ref<boolean>;
}) {
  const { t } = useI18n();

  /**
   * 打开快速输入
   * @param dataKey 快速输入的数据 key
   */
  function setQuickInput(step: ScenarioStepItem, dataKey: keyof ScenarioStepDetail) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
    if (realStep) {
      activeStep.value = realStep as ScenarioStepItem;
    }
    quickInputDataKey.value = dataKey;
    quickInputParamValue.value = step.config?.[dataKey] || '';
    if (quickInputDataKey.value === 'msWhileVariableValue' && activeStep.value?.config.whileController) {
      quickInputParamValue.value = activeStep.value.config.whileController.msWhileVariable.value;
    } else if (quickInputDataKey.value === 'msWhileVariableScriptValue' && activeStep.value?.config.whileController) {
      quickInputParamValue.value = activeStep.value.config.whileController.msWhileScript.scriptValue;
    } else if (quickInputDataKey.value === 'conditionValue' && activeStep.value?.config) {
      quickInputParamValue.value = activeStep.value.config.value || '';
    }
    showQuickInput.value = true;
  }

  function clearQuickInput() {
    activeStep.value = undefined;
    quickInputParamValue.value = '';
    quickInputDataKey.value = '';
  }

  /**
   * 应用快速输入
   */
  function applyQuickInput() {
    if (activeStep.value) {
      if (quickInputDataKey.value === 'msWhileVariableValue' && activeStep.value.config.whileController) {
        activeStep.value.config.whileController.msWhileVariable.value = quickInputParamValue.value;
      } else if (quickInputDataKey.value === 'msWhileVariableScriptValue' && activeStep.value.config.whileController) {
        activeStep.value.config.whileController.msWhileScript.scriptValue = quickInputParamValue.value;
      } else if (quickInputDataKey.value === 'conditionValue' && activeStep.value.config) {
        activeStep.value.config.value = quickInputParamValue.value;
      }
      showQuickInput.value = false;
      clearQuickInput();
      scenario.value.unSaved = true;
    }
  }

  /**
   * 步骤描述编辑按钮点击
   */
  function handleStepDescClick(step: ScenarioStepItem) {
    tempStepDesc.value = step.name;
    showStepDescEditInputStepId.value = step.uniqueId;
    nextTick(() => {
      // 等待输入框渲染完成后聚焦
      const input = treeRef.value?.$el.querySelector('.desc-warp .arco-input-wrapper .arco-input') as HTMLInputElement;
      input?.focus();
    });
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
    if (realStep) {
      realStep.draggable = false; // 编辑时禁止拖拽
    }
  }

  /**
   * 应用步骤描述更改
   */
  function applyStepDescChange(step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
    if (realStep) {
      realStep.name = tempStepDesc.value || realStep.name;
      realStep.draggable = true; // 编辑完恢复拖拽
    }
    showStepDescEditInputStepId.value = '';
    scenario.value.unSaved = !!tempStepDesc.value;
  }

  /**
   * 步骤内容编辑
   * @param $event 编辑内容对象信息
   */
  function handleStepContentChange($event: Record<string, any>, step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
    if (realStep) {
      Object.keys($event).forEach((key) => {
        realStep.config[key] = $event[key];
      });
      scenario.value.unSaved = true;
    }
  }

  /**
   * 步骤启用禁用切换
   */
  function handleStepToggleEnable(data: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, data.uniqueId, 'uniqueId');
    if (realStep) {
      realStep.enable = !realStep.enable;
      scenario.value.unSaved = true;
    }
  }

  /**
   * 步骤名称编辑按钮点击事件
   */
  function handleStepNameClick(step: ScenarioStepItem) {
    tempStepName.value = step.name;
    showStepNameEditInputStepId.value = step.uniqueId;
    nextTick(() => {
      // 等待输入框渲染完成后聚焦
      const input = treeRef.value?.$el.querySelector('.name-warp .arco-input-wrapper .arco-input') as HTMLInputElement;
      input?.focus();
    });
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
    if (realStep) {
      realStep.draggable = false; // 编辑时禁止拖拽
    }
  }

  /**
   * 应用步骤名称更改
   */
  function applyStepNameChange(step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
    if (realStep) {
      realStep.name = tempStepName.value || realStep.name;
      realStep.draggable = true; // 编辑完恢复拖拽
    }
    showStepNameEditInputStepId.value = '';
    scenario.value.unSaved = !!tempStepName.value;
  }

  const scenarioConfigForm = ref<
    ScenarioStepConfig & {
      refType: ScenarioStepRefType;
    }
  >({
    refType: ScenarioStepRefType.REF,
    enableScenarioEnv: false,
    useOriginScenarioParamPreferential: true,
    useOriginScenarioParam: false,
  });
  // const scenarioConfigParamTip = computed(() => {
  //   if (!scenarioConfigForm.value.useOriginScenarioParam && !scenarioConfigForm.value.enableScenarioEnv) {
  //     // 非使用原场景参数-非选择源场景环境
  //     return t('apiScenario.notSource');
  //   }
  //   if (!scenarioConfigForm.value.useOriginScenarioParam && scenarioConfigForm.value.enableScenarioEnv) {
  //     // 非使用原场景参数-选择源场景环境
  //     return t('apiScenario.notSourceParamAndSourceEnv');
  //   }
  //   if (
  //     scenarioConfigForm.value.useOriginScenarioParam &&
  //     scenarioConfigForm.value.useOriginScenarioParamPreferential &&
  //     !scenarioConfigForm.value.enableScenarioEnv
  //   ) {
  //     // 使用原场景参数-优先使用原场景参数
  //     return t('apiScenario.sourceParamAndSource');
  //   }
  //   if (
  //     scenarioConfigForm.value.useOriginScenarioParam &&
  //     scenarioConfigForm.value.useOriginScenarioParamPreferential &&
  //     scenarioConfigForm.value.enableScenarioEnv
  //   ) {
  //     // 使用原场景参数-优先使用原场景参数-选择源场景环境
  //     return t('apiScenario.sourceParamAndSourceEnv');
  //   }
  //   if (
  //     scenarioConfigForm.value.useOriginScenarioParam &&
  //     !scenarioConfigForm.value.useOriginScenarioParamPreferential &&
  //     !scenarioConfigForm.value.enableScenarioEnv
  //   ) {
  //     // 使用原场景参数-优先使用当前场景参数
  //     return t('apiScenario.currentParamAndSource');
  //   }
  //   if (
  //     scenarioConfigForm.value.useOriginScenarioParam &&
  //     !scenarioConfigForm.value.useOriginScenarioParamPreferential &&
  //     scenarioConfigForm.value.enableScenarioEnv
  //   ) {
  //     // 使用原场景参数-优先使用当前场景参数-选择源场景环境
  //     return t('apiScenario.currentParamAndSourceEnv');
  //   }
  // });

  // 关闭场景配置弹窗
  function cancelScenarioConfig() {
    showScenarioConfig.value = false;
    scenarioConfigForm.value = {
      refType: ScenarioStepRefType.REF,
      enableScenarioEnv: false,
      useOriginScenarioParamPreferential: true,
      useOriginScenarioParam: false,
    };
  }

  /**
   * 刷新引用场景的步骤数据
   */
  async function refreshScenarioStepInfo(step: ScenarioStepItem, id: string | number) {
    try {
      loading.value = true;
      const res = await getScenarioDetail(id);
      if (step.children) {
        step.children = mapTree(res.steps || [], (child) => {
          child.uniqueId = getGenerateId();
          child.isQuoteScenarioStep = true; // 标记为引用场景下的子步骤
          child.isRefScenarioStep = true; // 标记为完全引用场景
          child.draggable = false; // 引用场景下的任何步骤不可拖拽
          if (selectedKeys.value.includes(step.uniqueId) && !selectedKeys.value.includes(child.uniqueId)) {
            // 如果有新增的子步骤，且当前步骤被选中，则这个新增的子步骤也要选中
            selectedKeys.value.push(child.uniqueId);
          }
          return child;
        }) as ScenarioStepItem[];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  // 应用场景配置
  async function saveScenarioConfig() {
    if (activeStep.value) {
      const realStep = findNodeByKey<ScenarioStepItem>(steps.value, activeStep.value.uniqueId, 'uniqueId');
      if (realStep) {
        realStep.refType = scenarioConfigForm.value.refType; // 更新场景引用类型
        realStep.config = {
          ...realStep.config,
          ...scenarioConfigForm.value,
        };
        if (scenarioConfigForm.value.refType === ScenarioStepRefType.REF) {
          // 更新子孙步骤完全引用
          await refreshScenarioStepInfo(realStep as ScenarioStepItem, realStep.resourceId);
        } else {
          realStep.children = mapTree<ScenarioStepItem>(realStep.children || [], (child) => {
            // 更新子孙步骤-步骤引用
            child.isRefScenarioStep = false;
            return child;
          });
        }
        Message.success(t('apiScenario.setSuccess'));
        scenario.value.unSaved = true;
        cancelScenarioConfig();
      }
    }
  }

  return {
    setQuickInput,
    clearQuickInput,
    applyQuickInput,
    handleStepDescClick,
    applyStepDescChange,
    handleStepContentChange,
    handleStepToggleEnable,
    handleStepNameClick,
    applyStepNameChange,
    saveScenarioConfig,
    cancelScenarioConfig,
    scenarioConfigForm,
  };
}
