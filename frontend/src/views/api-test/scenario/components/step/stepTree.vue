<template>
  <div class="flex h-full flex-col gap-[8px]">
    <a-spin class="max-h-[calc(100%-46px)] w-full" :loading="loading">
      <MsTree
        ref="treeRef"
        v-model:selected-keys="selectedKeys"
        v-model:checked-keys="checkedKeys"
        v-model:focus-node-key="focusStepKey"
        v-model:data="steps"
        :keyword="props.stepKeyword"
        :expand-all="props.expandAll"
        :node-more-actions="stepMoreActions"
        :filter-more-action-func="setStepMoreAction"
        :field-names="{ title: 'name', key: 'id', children: 'children' }"
        :virtual-list-props="{
          height: '100%',
          threshold: 20,
          fixedSize: true,
          buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
        }"
        title-class="step-tree-node-title"
        node-highlight-class="step-tree-node-focus"
        action-on-node-click="expand"
        disabled-title-tooltip
        checkable
        block-node
        draggable
        @select="(selectedKeys, node) => handleStepSelect(selectedKeys, node as ScenarioStepItem)"
        @expand="handleStepExpand"
        @more-actions-close="() => setFocusNodeKey('')"
        @more-action-select="handleStepMoreActionSelect"
        @drop="handleDrop"
      >
        <template #title="step">
          <div class="flex w-full items-center gap-[8px]">
            <!-- 步骤序号 -->
            <div
              class="flex h-[16px] min-w-[16px] items-center justify-center rounded-full bg-[var(--color-text-brand)] px-[2px] !text-white"
            >
              {{ step.sort }}
            </div>
            <div class="step-node-content">
              <!-- 步骤展开折叠按钮 -->
              <a-tooltip
                v-if="step.children?.length > 0"
                :content="
                  t(step.expanded ? 'apiScenario.collapseStepTip' : 'apiScenario.expandStepTip', {
                    count: step.children.length,
                  })
                "
              >
                <div class="flex cursor-pointer items-center gap-[2px] text-[var(--color-text-1)]">
                  <MsIcon
                    :type="step.expanded ? 'icon-icon_split-turn-down-left' : 'icon-icon_split_turn-down_arrow'"
                    :size="14"
                  />
                  {{ step.children?.length || 0 }}
                </div>
              </a-tooltip>
              <div class="mr-[8px] flex items-center gap-[8px]">
                <!-- 步骤启用/禁用，完全引用的场景下的子孙步骤不可禁用 -->
                <a-switch
                  v-show="step.isRefScenarioStep !== true"
                  v-model:model-value="step.enable"
                  size="small"
                  @click.stop="handleStepToggleEnable(step)"
                ></a-switch>
                <!-- 步骤执行 -->
                <MsIcon
                  v-show="!step.isExecuting"
                  type="icon-icon_play-round_filled"
                  :size="18"
                  class="cursor-pointer text-[rgb(var(--link-6))]"
                  @click.stop="executeStep(step)"
                />
                <MsIcon
                  v-show="step.isExecuting"
                  type="icon-icon_stop"
                  :size="20"
                  class="cursor-pointer text-[rgb(var(--link-6))]"
                  @click.stop="handleStopExecute(step)"
                />
              </div>
              <!-- 步骤类型 -->
              <stepType :step="step" />
              <!-- 步骤整体内容 -->
              <div class="relative flex flex-1 items-center gap-[4px]">
                <!-- 步骤差异内容，按步骤类型展示不同组件 -->
                <component
                  :is="getStepContent(step)"
                  :data="checkStepIsApi(step) || step.stepType === ScenarioStepType.API_SCENARIO ? step : step.config"
                  :step-id="step.id"
                  @quick-input="setQuickInput(step, $event)"
                  @change="handleStepContentChange($event, step)"
                  @click.stop
                />
                <!-- 自定义请求、API、CASE、场景步骤名称 -->
                <template v-if="checkStepIsApi(step)">
                  <apiMethodName v-if="checkStepShowMethod(step)" :method="step.config.method" />
                  <div
                    v-if="step.id === showStepNameEditInputStepId"
                    class="name-warp absolute left-0 top-[-2px] z-10 w-[calc(100%-24px)]"
                    @click.stop
                  >
                    <a-input
                      v-model:model-value="tempStepName"
                      :placeholder="t('apiScenario.pleaseInputStepName')"
                      :max-length="255"
                      size="small"
                      @press-enter="applyStepNameChange(step)"
                      @blur="applyStepNameChange(step)"
                    />
                  </div>
                  <a-tooltip :content="step.name">
                    <div class="step-name-container">
                      <div class="one-line-text mr-[4px] max-w-[350px] font-medium text-[var(--color-text-1)]">
                        {{ step.name }}
                      </div>
                      <MsIcon
                        type="icon-icon_edit_outlined"
                        class="edit-script-name-icon"
                        @click.stop="handleStepNameClick(step)"
                      />
                    </div>
                  </a-tooltip>
                </template>
                <!-- 其他步骤描述 -->
                <template v-else>
                  <div
                    v-if="step.id === showStepDescEditInputStepId"
                    class="desc-warp absolute left-0 top-[-2px] z-10 w-[calc(100%-24px)]"
                  >
                    <a-input
                      v-model:model-value="tempStepDesc"
                      :default-value="step.name || t('apiScenario.pleaseInputStepDesc')"
                      :placeholder="t('apiScenario.pleaseInputStepDesc')"
                      :max-length="255"
                      size="small"
                      @press-enter="applyStepDescChange(step)"
                      @blur="applyStepDescChange(step)"
                      @click.stop
                    >
                      <template #prefix>
                        {{ t('common.desc') }}
                      </template>
                    </a-input>
                  </div>
                  <a-tooltip :content="step.name" :disabled="!step.name">
                    <div class="step-name-container">
                      <div
                        :class="`one-line-text mr-[4px] ${
                          step.stepType === ScenarioStepType.ONCE_ONLY_CONTROLLER ? 'max-w-[750px]' : 'max-w-[150px]'
                        } font-normal text-[var(--color-text-4)]`"
                      >
                        {{ step.name || t('apiScenario.pleaseInputStepDesc') }}
                      </div>
                      <MsIcon
                        type="icon-icon_edit_outlined"
                        class="edit-script-name-icon"
                        @click.stop="handleStepDescClick(step)"
                      />
                    </div>
                  </a-tooltip>
                </template>
              </div>
            </div>
          </div>
        </template>
        <template #extra="step">
          <stepInsertStepTrigger
            v-model:selected-keys="selectedKeys"
            v-model:steps="steps"
            :step="step"
            @click="setFocusNodeKey(step.id)"
            @other-create="handleOtherCreate"
            @close="setFocusNodeKey('')"
            @add-done="scenario.unSaved = true"
          />
        </template>
        <template #extraEnd="step">
          <responsePopover
            v-if="
              ![
                ScenarioStepType.LOOP_CONTROLLER,
                ScenarioStepType.IF_CONTROLLER,
                ScenarioStepType.ONCE_ONLY_CONTROLLER,
                ScenarioStepType.CONSTANT_TIMER,
              ].includes(step.stepType) &&
              (getExecuteStatus(step) === ScenarioExecuteStatus.SUCCESS ||
                getExecuteStatus(step) === ScenarioExecuteStatus.FAILED)
            "
            :step="step"
            :step-responses="scenario.stepResponses"
            @visible-change="handleResponsePopoverVisibleChange"
          />
          <executeStatus
            v-else-if="step.executeStatus"
            :status="getExecuteStatus(step)"
            size="small"
            class="ml-[4px]"
          />
        </template>
        <template v-if="steps.length === 0 && stepKeyword.trim() !== ''" #empty>
          <div
            class="rounded-[var(--border-radius-small)] bg-[var(--color-fill-1)] p-[8px] text-center text-[12px] leading-[16px] text-[var(--color-text-4)]"
          >
            {{ t('apiScenario.noMatchStep') }}
          </div>
        </template>
      </MsTree>
    </a-spin>
    <createStepActions
      v-model:selected-keys="selectedKeys"
      v-model:steps="steps"
      @add-done="scenario.unSaved = true"
      @other-create="handleOtherCreate"
    >
      <a-button type="dashed" class="add-step-btn" long>
        <div class="flex items-center gap-[8px]">
          <icon-plus />
          {{ t('apiScenario.addStep') }}
        </div>
      </a-button>
    </createStepActions>
    <customApiDrawer
      v-model:visible="customApiDrawerVisible"
      :request="currentStepDetail as unknown as RequestParam"
      :file-params="currentStepFileParams"
      :step="activeStep"
      :step-responses="scenario.stepResponses"
      @add-step="addCustomApiStep"
      @apply-step="applyApiStep"
      @stop-debug="handleStopExecute(activeStep)"
      @execute="handleApiExecute"
    />
    <customCaseDrawer
      v-model:visible="customCaseDrawerVisible"
      :active-step="activeStep"
      :request="currentStepDetail as unknown as RequestParam"
      :file-params="currentStepFileParams"
      :step-responses="scenario.stepResponses"
      @apply-step="applyApiStep"
      @delete-step="deleteCaseStep(activeStep)"
      @stop-debug="handleStopExecute(activeStep)"
      @execute="(request, executeType) => handleApiExecute((request as unknown as RequestParam), executeType)"
    />
    <importApiDrawer
      v-if="importApiDrawerVisible"
      v-model:visible="importApiDrawerVisible"
      @copy="handleImportApiApply('copy', $event)"
      @quote="handleImportApiApply('quote', $event)"
    />
    <scriptOperationDrawer
      v-model:visible="scriptOperationDrawerVisible"
      :detail="currentStepDetail as unknown as ExecuteConditionProcessor"
      :step="activeStep"
      :name="activeStep?.name"
      @add="addScriptStep"
      @save="saveScriptStep"
    />
    <a-modal
      v-model:visible="showQuickInput"
      :title="quickInputDataKey ? t(`apiScenario.${quickInputDataKey}`) : ''"
      :ok-text="t('apiTestDebug.apply')"
      :ok-button-props="{ disabled: !quickInputParamValue || quickInputParamValue.trim() === '' }"
      class="ms-modal-form"
      body-class="!p-0"
      :width="680"
      title-align="start"
      @ok="applyQuickInput"
      @close="clearQuickInput"
    >
      <MsCodeEditor
        v-if="showQuickInput"
        v-model:model-value="quickInputParamValue"
        theme="MS-text"
        height="300px"
        :show-full-screen="false"
      >
        <template #rightTitle>
          <div class="flex justify-between">
            <div class="text-[var(--color-text-4)]">
              {{ t('apiTestDebug.quickInputParamsTip') }}
            </div>
          </div>
        </template>
      </MsCodeEditor>
    </a-modal>
    <a-modal
      v-model:visible="showScenarioConfig"
      :title="t('apiScenario.scenarioConfig')"
      :ok-text="t('common.confirm')"
      class="ms-modal-form"
      body-class="!overflow-hidden !p-0"
      :width="680"
      title-align="start"
      @ok="applyQuickInput"
    >
      <a-form :model="scenarioConfigForm" layout="vertical" class="ms-form">
        <a-form-item>
          <template #label>
            <div class="flex items-center gap-[4px]">
              {{ t('apiScenario.quoteMode') }}
              <a-tooltip position="right">
                <icon-question-circle
                  class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                  size="16"
                />
                <template #content>
                  <div>{{ t('apiScenario.fullQuoteTip') }}</div>
                  <div>{{ t('apiScenario.stepQuoteTip') }}</div>
                </template>
              </a-tooltip>
            </div>
          </template>
          <a-radio-group v-model:model-value="scenarioConfigForm.refType">
            <a-radio :value="ScenarioStepRefType.REF">{{ t('apiScenario.fullQuote') }}</a-radio>
            <a-radio :value="ScenarioStepRefType.PARTIAL_REF">{{ t('apiScenario.stepQuote') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :label="t('apiScenario.runRule')">
          <a-radio-group v-model:model-value="scenarioConfigForm.useCurrentScenarioParam" type="button">
            <a-radio :value="true">{{ t('apiScenario.currentScenario') }}</a-radio>
            <a-radio :value="false">{{ t('apiScenario.sourceScenario') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :label="t('apiScenario.currentScenarioTip')">
          <a-radio-group v-model:model-value="scenarioConfigForm.useBothScenarioParam">
            <a-radio :value="false">{{ t('apiScenario.empty') }}</a-radio>
            <a-radio :value="true">
              {{
                t(
                  scenarioConfigForm.useCurrentScenarioParam
                    ? 'apiScenario.sourceScenarioParams'
                    : 'apiScenario.currentScenarioParams'
                )
              }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="" class="hidden-item !mb-0">
          <div class="flex items-center gap-[8px]">
            <a-checkbox v-model:model-value="scenarioConfigForm.enableScenarioEnv" class="ml-[6px]"></a-checkbox>
            <div class="flex items-center gap-[4px]">
              {{ t('apiScenario.sourceScenarioEnv') }}
              <a-tooltip :content="t('apiScenario.sourceScenarioEnvTip')" position="right">
                <icon-question-circle
                  class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                  size="16"
                />
              </a-tooltip>
            </div>
          </div>
        </a-form-item>
      </a-form>
      <template #footer>
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <div class="text-[var(--color-text-4)]">
              {{ t('apiScenario.valuePriority') }}
            </div>
            <div v-if="scenarioConfigParamTip" class="text-[var(--color-text-1)]">
              {{ scenarioConfigParamTip }}
            </div>
          </div>
          <div class="flex items-center gap-[12px]">
            <a-button type="secondary" @click="cancelScenarioConfig">{{ t('common.cancel') }}</a-button>
            <a-button type="primary" @click="saveScenarioConfig">{{ t('common.confirm') }}</a-button>
          </div>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { useEventListener } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import { MsTreeExpandedData, MsTreeNodeData } from '@/components/business/ms-tree/types';
  import executeStatus from '../common/executeStatus.vue';
  import { ImportData } from '../common/importApiDrawer/index.vue';
  import stepType from '../common/stepType/stepType.vue';
  import createStepActions from './createAction/createStepActions.vue';
  import stepInsertStepTrigger from './createAction/stepInsertStepTrigger.vue';
  import conditionContent from './stepNodeComposition/conditionContent.vue';
  import loopControlContent from './stepNodeComposition/loopContent.vue';
  import quoteContent from './stepNodeComposition/quoteContent.vue';
  import waitTimeContent from './stepNodeComposition/waitTimeContent.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import { RequestParam as CaseRequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import { localExecuteApiDebug } from '@/api/modules/api-test/common';
  import { debugScenario, getScenarioStep } from '@/api/modules/api-test/scenario';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import {
    deleteNode,
    findNodeByKey,
    getGenerateId,
    handleTreeDragDrop,
    insertNodes,
    mapTree,
    traverseTree,
    TreeNode,
  } from '@/utils';

  import { ExecuteConditionProcessor } from '@/models/apiTest/common';
  import {
    ApiScenarioDebugRequest,
    CreateStepAction,
    Scenario,
    ScenarioStepConfig,
    ScenarioStepDetails,
    ScenarioStepFileParams,
    ScenarioStepItem,
  } from '@/models/apiTest/scenario';
  import { EnvConfig } from '@/models/projectManagement/environmental';
  import {
    ScenarioAddStepActionType,
    ScenarioExecuteStatus,
    ScenarioStepRefType,
    ScenarioStepType,
  } from '@/enums/apiEnum';

  import type { RequestParam } from '../common/customApiDrawer.vue';
  import updateStepStatus from '../utils';
  import useCreateActions from './createAction/useCreateActions';
  import { parseRequestBodyFiles } from '@/views/api-test/components/utils';
  import getStepType from '@/views/api-test/scenario/components/common/stepType/utils';
  import { defaultStepItemCommon } from '@/views/api-test/scenario/components/config';

  // 非首屏渲染必要组件，异步加载
  const MsCodeEditor = defineAsyncComponent(() => import('@/components/pure/ms-code-editor/index.vue'));
  const customApiDrawer = defineAsyncComponent(() => import('../common/customApiDrawer.vue'));
  const customCaseDrawer = defineAsyncComponent(() => import('../common/customCaseDrawer.vue'));
  const importApiDrawer = defineAsyncComponent(() => import('../common/importApiDrawer/index.vue'));
  const scriptOperationDrawer = defineAsyncComponent(() => import('../common/scriptOperationDrawer.vue'));
  const responsePopover = defineAsyncComponent(() => import('../common/responsePopover.vue'));

  const props = defineProps<{
    stepKeyword: string;
    expandAll?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'updateResource', uploadFileIds: string[], linkFileIds: string[]): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const steps = defineModel<ScenarioStepItem[]>('steps', {
    required: true,
  });
  const checkedKeys = defineModel<(string | number)[]>('checkedKeys', {
    required: true,
  });
  // 步骤详情映射，存储部分抽屉展示详情的数据
  const stepDetails = defineModel<Record<string, ScenarioStepDetails>>('stepDetails', {
    required: true,
  });
  const scenario = defineModel<Scenario>('scenario', {
    required: true,
  });
  const isPriorityLocalExec = inject<Ref<boolean>>('isPriorityLocalExec');
  const localExecuteUrl = inject<Ref<string>>('localExecuteUrl');
  const currentEnvConfig = inject<Ref<EnvConfig>>('currentEnvConfig');

  const selectedKeys = ref<(string | number)[]>([]); // 没啥用，目前用来展示选中样式
  const loading = ref(false);
  const treeRef = ref<InstanceType<typeof MsTree>>();
  const focusStepKey = ref<string | number>(''); // 聚焦的key
  const activeStep = ref<ScenarioStepItem>(); // 用于抽屉操作创建步骤、弹窗配置时记录当前操作的步骤节点

  function setFocusNodeKey(id: string | number) {
    focusStepKey.value = id || '';
  }

  function getExecuteStatus(step: ScenarioStepItem) {
    if (scenario.value.stepResponses && scenario.value.stepResponses[step.id]) {
      // 有一次失败就是失败
      return scenario.value.stepResponses[step.id].some((report) => !report.isSuccessful)
        ? ScenarioExecuteStatus.FAILED
        : ScenarioExecuteStatus.SUCCESS;
    }
    return step.executeStatus;
  }

  function handleResponsePopoverVisibleChange(visible: boolean, step: ScenarioStepItem) {
    if (visible) {
      setFocusNodeKey(step.id);
    } else {
      setFocusNodeKey('');
    }
  }

  /**
   * 根据步骤类型获取步骤内容组件
   */
  function getStepContent(step: ScenarioStepItem) {
    switch (step.stepType) {
      case ScenarioStepType.API:
      case ScenarioStepType.API_CASE:
      case ScenarioStepType.API_SCENARIO:
      case ScenarioStepType.CUSTOM_REQUEST:
        return quoteContent;
      case ScenarioStepType.LOOP_CONTROLLER:
        return loopControlContent;
      case ScenarioStepType.IF_CONTROLLER:
        return conditionContent;
      case ScenarioStepType.CONSTANT_TIMER:
        return waitTimeContent;
      default:
        return () => null;
    }
  }

  function checkStepIsApi(step: ScenarioStepItem) {
    return [ScenarioStepType.API, ScenarioStepType.API_CASE, ScenarioStepType.CUSTOM_REQUEST].includes(step.stepType);
  }

  function checkStepShowMethod(step: ScenarioStepItem) {
    return [
      ScenarioStepType.API,
      ScenarioStepType.API_CASE,
      ScenarioStepType.CUSTOM_REQUEST,
      ScenarioStepType.API_SCENARIO,
    ].includes(step.stepType);
  }

  /**
   * 增加步骤时判断父节点是否选中，如果选中则需要把新节点也选中
   */
  function selectedIfNeed(step: TreeNode<ScenarioStepItem>, parent?: TreeNode<ScenarioStepItem>) {
    if (parent && selectedKeys.value.includes(parent.id)) {
      // 添加子节点时，当前节点已选中，则需要把新节点也需要选中（因为父级选中子级也会展示选中状态）
      selectedKeys.value.push(step.id);
    }
  }

  const stepMoreActions: ActionsItem[] = [
    {
      label: 'common.copy',
      eventTag: 'copy',
    },
    {
      label: 'common.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  function setStepMoreAction(items: ActionsItem[], node: MsTreeNodeData) {
    const _stepType = getStepType(node as ScenarioStepItem);
    if ((node as ScenarioStepItem).stepType === ScenarioStepType.CUSTOM_REQUEST) {
      // 自定义请求
      return [
        {
          label: 'common.copy',
          eventTag: 'copy',
        },
        {
          label: 'apiScenario.saveAsApi',
          eventTag: 'saveAsApi',
        },
        {
          label: 'common.delete',
          eventTag: 'delete',
          danger: true,
        },
      ];
    }
    if (_stepType.isQuoteScenario) {
      return [
        {
          label: 'common.copy',
          eventTag: 'copy',
        },
        {
          label: 'apiScenario.scenarioConfig',
          eventTag: 'config',
        },
        {
          label: 'common.delete',
          eventTag: 'delete',
          danger: true,
        },
      ];
    }
    if (_stepType.isQuoteCase) {
      return [
        {
          label: 'common.copy',
          eventTag: 'copy',
        },
        {
          label: 'apiTestManagement.saveAsCase',
          eventTag: 'saveAsCase',
        },
        {
          label: 'common.delete',
          eventTag: 'delete',
          danger: true,
        },
      ];
    }
    return stepMoreActions;
  }

  const scenarioConfigForm = ref<
    ScenarioStepConfig & {
      refType: ScenarioStepRefType;
    }
  >({
    refType: ScenarioStepRefType.REF,
    enableScenarioEnv: false,
    useBothScenarioParam: false,
    useCurrentScenarioParam: true,
  });
  const showScenarioConfig = ref(false);
  const scenarioConfigParamTip = computed(() => {
    if (
      scenarioConfigForm.value.useCurrentScenarioParam &&
      !scenarioConfigForm.value.useBothScenarioParam &&
      !scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用当前场景参数-空值
      return t('apiScenario.currentScenarioAndNull');
    }
    if (
      scenarioConfigForm.value.useCurrentScenarioParam &&
      !scenarioConfigForm.value.useBothScenarioParam &&
      scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用当前场景参数-空值-且选择源场景环境
      return t('apiScenario.currentScenarioAndNullAndSourceEnv');
    }
    if (
      scenarioConfigForm.value.useCurrentScenarioParam &&
      scenarioConfigForm.value.useBothScenarioParam &&
      !scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用当前场景参数-原场景参数
      return t('apiScenario.currentScenarioAndSourceScenario');
    }
    if (
      scenarioConfigForm.value.useCurrentScenarioParam &&
      scenarioConfigForm.value.useBothScenarioParam &&
      scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用当前场景参数-原场景参数-且选择源场景环境
      return t('apiScenario.currentScenarioAndSourceScenarioAndSourceEnv');
    }
    if (
      !scenarioConfigForm.value.useCurrentScenarioParam &&
      !scenarioConfigForm.value.useBothScenarioParam &&
      !scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用原场景参数-空值
      return t('apiScenario.sourceScenarioAndNull');
    }
    if (
      !scenarioConfigForm.value.useCurrentScenarioParam &&
      !scenarioConfigForm.value.useBothScenarioParam &&
      scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用原场景参数-空值-且选择源场景环境
      return t('apiScenario.sourceScenarioAndNullAndSourceEnv');
    }
    if (
      !scenarioConfigForm.value.useCurrentScenarioParam &&
      scenarioConfigForm.value.useBothScenarioParam &&
      !scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用原场景参数-当前场景参数
      return t('apiScenario.sourceScenarioAndCurrentScenario');
    }
    if (
      !scenarioConfigForm.value.useCurrentScenarioParam &&
      scenarioConfigForm.value.useBothScenarioParam &&
      scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用原场景参数-当前场景参数-且选择源场景环境
      return t('apiScenario.sourceScenarioAndCurrentScenarioAndSourceEnv');
    }
  });

  // 关闭场景配置弹窗
  function cancelScenarioConfig() {
    showScenarioConfig.value = false;
    scenarioConfigForm.value = {
      refType: ScenarioStepRefType.REF,
      enableScenarioEnv: false,
      useBothScenarioParam: false,
      useCurrentScenarioParam: true,
    };
  }

  // 应用场景配置
  function saveScenarioConfig() {
    if (activeStep.value) {
      const realStep = findNodeByKey<ScenarioStepItem>(steps.value, activeStep.value.id, 'id');
      if (realStep) {
        realStep.refType = scenarioConfigForm.value.refType; // 更新场景引用类型
        realStep.config = {
          ...realStep.config,
          ...scenarioConfigForm.value,
        };
        realStep.children = mapTree<ScenarioStepItem>(realStep.children || [], (child) => {
          // 更新子孙步骤是否完全引用
          if (scenarioConfigForm.value.refType === ScenarioStepRefType.REF) {
            child.isRefScenarioStep = true;
            child.enable = true;
          } else {
            child.isRefScenarioStep = false;
          }
          return child;
        });
        Message.success(t('apiScenario.setSuccess'));
        scenario.value.unSaved = true;
        cancelScenarioConfig();
      }
    }
  }

  function handleStepMoreActionSelect(item: ActionsItem, node: MsTreeNodeData) {
    switch (item.eventTag) {
      case 'copy':
        const id = getGenerateId();
        const stepDetail = stepDetails.value[node.id];
        const stepFileParam = scenario.value.stepFileParam[node.id];
        const { isQuoteScenario } = getStepType(node as ScenarioStepItem);
        if (stepDetail) {
          // 如果复制的步骤还有详情数据，则也复制详情数据
          stepDetails.value[id] = cloneDeep(stepDetail);
        }
        if (stepFileParam) {
          // 如果复制的步骤还有详情数据，则也复制详情数据
          scenario.value.stepFileParam[id] = cloneDeep(stepFileParam);
        }
        insertNodes<ScenarioStepItem>(
          steps.value,
          node.id,
          {
            ...cloneDeep(
              mapTree<ScenarioStepItem>(node, (childNode) => {
                const childId = getGenerateId();
                const childStepDetail = stepDetails.value[node.id];
                const childStepFileParam = scenario.value.stepFileParam[node.id];
                let childCopyFromStepId = childNode.id;
                if (childStepDetail) {
                  // 如果复制的步骤下子步骤还有详情数据，则也复制详情数据
                  stepDetails.value[childId] = cloneDeep(childStepDetail);
                }
                if (childStepFileParam) {
                  // 如果复制的步骤还有详情数据，则也复制详情数据
                  scenario.value.stepFileParam[id] = cloneDeep(childStepFileParam);
                }
                if (!isQuoteScenario) {
                  // 非引用场景才处理复制来源 id
                  if (childStepDetail || (childNode.isNew && childNode.stepRefType === ScenarioStepRefType.REF)) {
                    // 如果子步骤查看过详情，则复制来源直接取它的 id
                    // 如果子步骤没有查看过详情，且是新建的步骤，且子步骤是引用的步骤，则还是取它本身的 id
                    childCopyFromStepId = childNode.id;
                  } else if (childNode.isNew && childNode.stepRefType === ScenarioStepRefType.COPY) {
                    // 如果子步骤没有查看过详情，且是新建的步骤，且子步骤是复制的步骤，则取它的来源 id
                    childCopyFromStepId = childNode.copyFromStepId;
                  }
                }
                return {
                  ...cloneDeep(childNode),
                  executeStatus: undefined,
                  copyFromStepId: childCopyFromStepId,
                  id: childId,
                };
              })[0]
            ),
            name: `copy-${node.name}`,
            copyFromStepId: node.id,
            sort: node.sort + 1,
            isNew: true,
            id,
          },
          'after',
          selectedIfNeed,
          'id'
        );
        scenario.value.unSaved = true;
        break;
      case 'config':
        activeStep.value = node as ScenarioStepItem;
        scenarioConfigForm.value = {
          refType: node.refType,
          ...node.config,
        };
        showScenarioConfig.value = true;
        break;
      case 'delete':
        deleteNode(steps.value, node.id, 'id');
        scenario.value.unSaved = true;
        break;
      default:
        break;
    }
  }

  function checkAll(val: boolean) {
    treeRef.value?.checkAll(val);
  }

  /**
   * 处理api、case、场景步骤名称编辑
   */
  const showStepNameEditInputStepId = ref<string | number>('');
  const tempStepName = ref('');
  function handleStepNameClick(step: ScenarioStepItem) {
    tempStepName.value = step.name;
    showStepNameEditInputStepId.value = step.id;
    nextTick(() => {
      // 等待输入框渲染完成后聚焦
      const input = treeRef.value?.$el.querySelector('.name-warp .arco-input-wrapper .arco-input') as HTMLInputElement;
      input?.focus();
    });
  }

  function applyStepNameChange(step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.id, 'id');
    if (realStep) {
      realStep.name = tempStepName.value;
    }
    showStepNameEditInputStepId.value = '';
    scenario.value.unSaved = true;
  }

  /**
   * 处理非 api、case、场景步骤名称编辑
   */
  const showStepDescEditInputStepId = ref<string | number>('');
  const tempStepDesc = ref('');
  function handleStepDescClick(step: ScenarioStepItem) {
    tempStepDesc.value = step.name;
    showStepDescEditInputStepId.value = step.id;
    nextTick(() => {
      // 等待输入框渲染完成后聚焦
      const input = treeRef.value?.$el.querySelector('.desc-warp .arco-input-wrapper .arco-input') as HTMLInputElement;
      input?.focus();
    });
  }

  function applyStepDescChange(step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.id, 'id');
    if (realStep) {
      realStep.name = tempStepDesc.value;
    }
    showStepDescEditInputStepId.value = '';
    scenario.value.unSaved = true;
  }

  function handleStepContentChange($event, step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.id, 'id');
    if (realStep) {
      Object.keys($event).forEach((key) => {
        realStep.config[key] = $event[key];
      });
      scenario.value.unSaved = true;
    }
  }

  /**
   * 处理步骤展开折叠
   */
  function handleStepExpand(data: MsTreeExpandedData) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, data.node?.id, 'id');
    if (realStep) {
      realStep.expanded = !realStep.expanded;
    }
  }

  function handleStepToggleEnable(data: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, data.id, 'id');
    if (realStep) {
      realStep.enable = !realStep.enable;
      scenario.value.unSaved = true;
    }
  }

  const importApiDrawerVisible = ref(false);
  const customCaseDrawerVisible = ref(false);
  const customApiDrawerVisible = ref(false);
  const scriptOperationDrawerVisible = ref(false);
  const activeCreateAction = ref<CreateStepAction>(); // 用于抽屉操作创建步骤时记录当前插入类型
  const currentStepDetail = computed<ScenarioStepDetails | undefined>(() => {
    if (activeStep.value) {
      return stepDetails.value[activeStep.value.id];
    }
  });
  const currentStepFileParams = computed<ScenarioStepFileParams | undefined>(() => {
    if (activeStep.value) {
      return scenario.value.stepFileParam[activeStep.value.id];
    }
  });

  async function getStepDetail(step: ScenarioStepItem) {
    try {
      appStore.showLoading();
      const res = await getScenarioStep(step.copyFromStepId || step.id);
      let parseRequestBodyResult;
      if (step.config.protocol === 'HTTP' && res.body) {
        parseRequestBodyResult = parseRequestBodyFiles(res.body); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
      }
      stepDetails.value[step.id] = {
        ...res,
        stepId: step.id,
        protocol: step.config.protocol,
        method: step.config.method,
        ...parseRequestBodyResult,
      };
      scenario.value.stepFileParam[step.id] = parseRequestBodyResult;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
    }
  }

  /**
   * 处理步骤选中事件
   * @param _selectedKeys 选中的 key集合
   * @param step 点击的步骤节点
   */
  async function handleStepSelect(_selectedKeys: Array<string | number>, step: ScenarioStepItem) {
    const _stepType = getStepType(step);
    const offspringIds: string[] = [];
    mapTree(step.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });
    selectedKeys.value = [step.id, ...offspringIds];
    if (_stepType.isCopyApi || _stepType.isQuoteApi || step.stepType === ScenarioStepType.CUSTOM_REQUEST) {
      // 复制 api、引用 api、自定义 api打开抽屉
      activeStep.value = step;
      if (
        (stepDetails.value[step.id] === undefined && step.copyFromStepId) ||
        (stepDetails.value[step.id] === undefined && !step.isNew)
      ) {
        // 查看场景详情时，详情映射中没有对应数据，初始化步骤详情（复制的步骤没有加载详情前就被复制，打开复制后的步骤就初始化被复制步骤的详情）
        await getStepDetail(step);
      }
      customApiDrawerVisible.value = true;
    } else if (step.stepType === ScenarioStepType.API_CASE) {
      activeStep.value = step;
      if (
        _stepType.isCopyCase &&
        ((stepDetails.value[step.id] === undefined && step.copyFromStepId) ||
          (stepDetails.value[step.id] === undefined && !step.isNew))
      ) {
        // 只有复制的 case 需要查看步骤详情，引用的无法更改所以不需要在此初始化详情
        // 查看场景详情时，详情映射中没有对应数据，初始化步骤详情（复制的步骤没有加载详情前就被复制，打开复制后的步骤就初始化被复制步骤的详情）
        await getStepDetail(step);
      }
      customCaseDrawerVisible.value = true;
    } else if (step.stepType === ScenarioStepType.SCRIPT) {
      activeStep.value = step;
      if (
        (stepDetails.value[step.id] === undefined && step.copyFromStepId) ||
        (stepDetails.value[step.id] === undefined && !step.isNew)
      ) {
        // 查看场景详情时，详情映射中没有对应数据，初始化步骤详情（复制的步骤没有加载详情前就被复制，打开复制后的步骤就初始化被复制步骤的详情）
        await getStepDetail(step);
      }
      scriptOperationDrawerVisible.value = true;
    }
  }

  const websocketMap: Record<string | number, WebSocket> = {};
  let temporaryStepReportMap = {}; // 缓存websocket返回的报告内容，避免执行接口后切换场景tab导致报告丢失

  watch(
    () => scenario.value.id,
    () => {
      const stepKeys = Object.keys(temporaryStepReportMap);
      if (stepKeys.length > 0) {
        stepKeys.forEach((key) => {
          const report = temporaryStepReportMap[key];
          scenario.value.stepResponses[report.stepId] = temporaryStepReportMap[key];
        });
        temporaryStepReportMap = {};
        updateStepStatus(steps.value, scenario.value.stepResponses);
      }
    }
  );

  /**
   * 开启websocket监听，接收执行结果
   */
  function debugSocket(step: ScenarioStepItem, reportId: string | number, executeType?: 'localExec' | 'serverExec') {
    websocketMap[reportId] = getSocket(
      reportId || '',
      executeType === 'localExec' ? '/ws/debug' : '',
      executeType === 'localExec' ? localExecuteUrl?.value : ''
    );
    websocketMap[reportId].addEventListener('message', (event) => {
      const data = JSON.parse(event.data);
      if (data.msgType === 'EXEC_RESULT') {
        if (step.reportId === data.reportId) {
          // 判断当前查看的tab是否是当前返回的报告的tab，是的话直接赋值
          data.taskResult.requestResults.forEach((result) => {
            if (scenario.value.stepResponses[result.stepId] === undefined) {
              scenario.value.stepResponses[result.stepId] = [];
            }
            scenario.value.stepResponses[result.stepId].push({
              ...result,
              console: data.taskResult.console,
            });
          });
        } else {
          // 不是则需要把报告缓存起来，等切换到对应的tab再赋值
          data.taskResult.requestResults.forEach((result) => {
            if (step.reportId) {
              if (temporaryStepReportMap[step.reportId] === undefined) {
                temporaryStepReportMap[step.reportId] = [];
              }
              temporaryStepReportMap[step.reportId].push({
                ...result,
                console: data.taskResult.console,
              });
            }
          });
        }
      } else if (data.msgType === 'EXEC_END') {
        // 执行结束，关闭websocket
        websocketMap[reportId].close();
        if (step.reportId === data.reportId) {
          step.isExecuting = false;
          updateStepStatus([step], scenario.value.stepResponses);
        }
      }
    });
  }

  async function realExecute(
    executeParams: Pick<
      ApiScenarioDebugRequest,
      'steps' | 'stepDetails' | 'reportId' | 'uploadFileIds' | 'linkFileIds'
    >,
    executeType?: 'localExec' | 'serverExec'
  ) {
    const [currentStep] = executeParams.steps;
    try {
      currentStep.isExecuting = true;
      currentStep.executeStatus = ScenarioExecuteStatus.EXECUTING;
      debugSocket(currentStep, executeParams.reportId, executeType); // 开启websocket
      const res = await debugScenario({
        id: scenario.value.id || '',
        grouped: false,
        environmentId: currentEnvConfig?.value.id || '',
        projectId: appStore.currentProjectId,
        scenarioConfig: scenario.value.scenarioConfig,
        frontendDebug: executeType === 'localExec',
        ...executeParams,
        steps: mapTree(executeParams.steps, (node) => {
          return {
            ...node,
            parent: null, // 原树形结构存在循环引用，这里要去掉以免 axios 序列化失败
          };
        }),
      });
      if (executeType === 'localExec' && localExecuteUrl?.value) {
        await localExecuteApiDebug(localExecuteUrl.value, res);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      websocketMap[executeParams.reportId].close();
      currentStep.isExecuting = false;
    }
  }

  /**
   * 单个步骤执行调试
   */
  function executeStep(node: MsTreeNodeData) {
    if (node.isExecuting) {
      return;
    }
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, node.id, 'id');
    if (realStep) {
      realStep.reportId = getGenerateId();
      const _stepDetails = {};
      const stepFileParam = scenario.value.stepFileParam[realStep.id];
      traverseTree(realStep, (step) => {
        _stepDetails[step.id] = stepDetails.value[step.id];
        step.executeStatus = ScenarioExecuteStatus.EXECUTING;
        delete scenario.value.stepResponses[step.id]; // 先移除上一次的执行结果
        return step;
      });
      realExecute(
        {
          steps: [realStep as ScenarioStepItem],
          stepDetails: _stepDetails,
          reportId: realStep.reportId,
          uploadFileIds: stepFileParam?.uploadFileIds || [],
          linkFileIds: stepFileParam?.linkFileIds || [],
        },
        isPriorityLocalExec?.value ? 'localExec' : 'serverExec'
      );
    }
  }

  /**
   * 处理 api 详情抽屉的执行动作
   * @param request 抽屉内的请求参数
   * @param executeType 执行类型
   */
  function handleApiExecute(request: RequestParam, executeType?: 'localExec' | 'serverExec') {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, request.stepId, 'id');
    if (realStep) {
      delete scenario.value.stepResponses[realStep.id]; // 先移除上一次的执行结果
      realStep.reportId = getGenerateId();
      realStep.executeStatus = ScenarioExecuteStatus.EXECUTING;
      request.executeLoading = true;
      realExecute(
        {
          steps: [realStep as ScenarioStepItem],
          stepDetails: {
            [realStep.id]: request,
          },
          reportId: realStep.reportId,
          uploadFileIds: request.uploadFileIds || [],
          linkFileIds: request.linkFileIds || [],
        },
        executeType
      );
    } else {
      // 步骤列表找不到该步骤，说明是新建的自定义请求还未保存，则临时创建一个步骤进行调试（不保存步骤信息）
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
      };
      realExecute(
        {
          steps: [activeStep.value],
          stepDetails: {
            [request.stepId]: request,
          },
          reportId,
          uploadFileIds: request.uploadFileIds || [],
          linkFileIds: request.linkFileIds || [],
        },
        executeType
      );
    }
  }

  function handleStopExecute(step?: ScenarioStepItem) {
    if (step?.reportId) {
      websocketMap[step.reportId].close();
      step.isExecuting = false;
      step.executeStatus = undefined;
    }
  }

  /**
   * 处理抽屉资源类型步骤创建动作
   */
  function handleOtherCreate(
    type:
      | ScenarioAddStepActionType.IMPORT_SYSTEM_API
      | ScenarioAddStepActionType.CUSTOM_API
      | ScenarioAddStepActionType.SCRIPT_OPERATION,
    step?: ScenarioStepItem,
    _activeCreateAction?: CreateStepAction
  ) {
    activeStep.value = step;
    activeCreateAction.value = _activeCreateAction;
    switch (type) {
      case ScenarioAddStepActionType.IMPORT_SYSTEM_API:
        importApiDrawerVisible.value = true;
        break;
      case ScenarioAddStepActionType.CUSTOM_API:
        customApiDrawerVisible.value = true;
        break;
      case ScenarioAddStepActionType.SCRIPT_OPERATION:
        scriptOperationDrawerVisible.value = true;
        break;
      default:
        break;
    }
  }

  const { handleCreateStep, handleCreateSteps, buildInsertStepInfos } = useCreateActions();

  /**
   * 处理导入系统请求
   * @param type 导入类型
   * @param data 导入数据
   */
  function handleImportApiApply(type: 'copy' | 'quote', data: ImportData) {
    let sort = steps.value.length + 1;
    if (activeStep.value && activeCreateAction.value) {
      switch (activeCreateAction.value) {
        case 'inside':
          sort = activeStep.value.children ? activeStep.value.children.length : 0;
          break;
        case 'before':
          sort = activeStep.value.sort;
          break;
        case 'after':
          sort = activeStep.value.sort + 1;
          break;
        default:
          break;
      }
    }
    const refType = type === 'copy' ? ScenarioStepRefType.COPY : ScenarioStepRefType.REF;
    const insertApiSteps = buildInsertStepInfos(
      data.api,
      ScenarioStepType.API,
      refType,
      sort,
      appStore.currentProjectId
    );
    const insertCaseSteps = buildInsertStepInfos(
      data.case,
      ScenarioStepType.API_CASE,
      refType,
      sort + insertApiSteps.length,
      appStore.currentProjectId
    );
    const insertScenarioSteps = buildInsertStepInfos(
      data.scenario,
      ScenarioStepType.API_SCENARIO,
      refType,
      sort + insertApiSteps.length + insertCaseSteps.length,
      appStore.currentProjectId
    );
    const insertSteps = insertApiSteps.concat(insertCaseSteps).concat(insertScenarioSteps);
    if (activeStep.value && activeCreateAction.value) {
      handleCreateSteps(activeStep.value, insertSteps, steps.value, activeCreateAction.value, selectedKeys.value);
    } else {
      steps.value = steps.value.concat(insertSteps);
    }
    scenario.value.unSaved = true;
  }

  /**
   * 添加自定义 API 步骤
   */
  function addCustomApiStep(request: RequestParam) {
    request.isNew = false;
    stepDetails.value[request.stepId] = {
      ...request,
      customizeRequest: true,
      customizeRequestEnvEnable: request.customizeRequestEnvEnable,
    };
    scenario.value.stepFileParam[request.stepId] = {
      linkFileIds: request.linkFileIds,
      uploadFileIds: request.uploadFileIds,
      deleteFileIds: request.deleteFileIds,
      unLinkFileIds: request.unLinkFileIds,
    };
    emit('updateResource', request.uploadFileIds, request.linkFileIds);
    if (activeStep.value && activeCreateAction.value) {
      handleCreateStep(
        {
          stepType: ScenarioStepType.CUSTOM_REQUEST,
          name: t('apiScenario.customApi'),
          config: {
            protocol: request.protocol,
            method: request.method,
          },
          id: request.stepId,
          projectId: appStore.currentProjectId,
        },
        activeStep.value,
        steps.value,
        activeCreateAction.value,
        selectedKeys.value
      );
    } else {
      steps.value.push({
        ...cloneDeep(defaultStepItemCommon),
        config: {
          protocol: request.protocol,
          method: request.method,
        },
        id: request.stepId,
        sort: steps.value.length + 1,
        stepType: ScenarioStepType.CUSTOM_REQUEST,
        refType: ScenarioStepRefType.DIRECT,
        name: t('apiScenario.customApi'),
        projectId: appStore.currentProjectId,
      });
    }
    scenario.value.unSaved = true;
  }

  /**
   * API 详情抽屉关闭时应用更改
   */
  function applyApiStep(request: RequestParam | CaseRequestParam) {
    if (request.unSaved) {
      scenario.value.unSaved = true;
    }
    if (activeStep.value) {
      request.isNew = false;
      stepDetails.value[activeStep.value.id] = request;
      scenario.value.stepFileParam[activeStep.value?.id] = {
        linkFileIds: request.linkFileIds,
        uploadFileIds: request.uploadFileIds,
        deleteFileIds: request.deleteFileIds,
        unLinkFileIds: request.unLinkFileIds,
      };
      emit('updateResource', request.uploadFileIds, request.linkFileIds);
      activeStep.value = undefined;
    }
  }

  /**
   * 删除
   */
  function deleteCaseStep(step?: ScenarioStepItem) {
    if (step) {
      customCaseDrawerVisible.value = false;
      deleteNode(steps.value, step.id, 'id');
      activeStep.value = undefined;
      scenario.value.unSaved = true;
    }
  }

  /**
   * 添加脚本操作步骤
   */
  function addScriptStep(name: string, scriptProcessor: ExecuteConditionProcessor) {
    const id = getGenerateId();
    stepDetails.value[id] = cloneDeep(scriptProcessor);
    if (activeStep.value && activeCreateAction.value) {
      handleCreateStep(
        {
          id,
          refType: ScenarioStepRefType.DIRECT,
          stepType: ScenarioStepType.SCRIPT,
          name,
          projectId: appStore.currentProjectId,
        },
        activeStep.value,
        steps.value,
        activeCreateAction.value,
        selectedKeys.value
      );
    } else {
      steps.value.push({
        ...cloneDeep(defaultStepItemCommon),
        id,
        sort: steps.value.length + 1,
        stepType: ScenarioStepType.SCRIPT,
        refType: ScenarioStepRefType.DIRECT,
        name,
        projectId: appStore.currentProjectId,
      });
    }
    scenario.value.unSaved = true;
  }

  function saveScriptStep(name: string, scriptProcessor: ExecuteConditionProcessor) {
    if (activeStep.value) {
      stepDetails.value[activeStep.value.id] = cloneDeep(scriptProcessor);
      activeStep.value.name = name;
      activeStep.value = undefined;
    }
  }

  /**
   * 释放允许拖拽步骤到释放的节点内
   * @param dropNode 释放节点
   */
  function isAllowDropInside(dropNode: MsTreeNodeData) {
    return (
      // 逻辑控制器内可以拖拽任意类型的步骤
      [
        ScenarioStepType.LOOP_CONTROLLER,
        ScenarioStepType.IF_CONTROLLER,
        ScenarioStepType.ONCE_ONLY_CONTROLLER,
      ].includes(dropNode.stepType) ||
      // 复制的场景内可以释放任意类型的步骤
      (dropNode.stepType === ScenarioStepType.API_SCENARIO && dropNode.refType === ScenarioStepRefType.COPY)
    );
  }

  /**
   * 处理步骤节点拖拽事件
   * @param tree 树数据
   * @param dragNode 拖拽节点
   * @param dropNode 释放节点
   * @param dropPosition 释放位置（取值：-1，,0，,1。 -1：dropNodeId节点之前。 0:dropNodeId节点内。 1：dropNodeId节点后）
   */
  function handleDrop(
    tree: MsTreeNodeData[],
    dragNode: MsTreeNodeData,
    dropNode: MsTreeNodeData,
    dropPosition: number
  ) {
    try {
      if (dropPosition === 0 && !isAllowDropInside(dropNode)) {
        // Message.error(t('apiScenario.notAllowDropInside')); TODO:不允许释放提示
        return;
      }
      loading.value = true;
      const offspringIds: string[] = [];
      mapTree(dragNode.children || [], (e) => {
        offspringIds.push(e.id);
        return e;
      });
      const stepIdAndOffspringIds = [dragNode.id, ...offspringIds];
      if (dropPosition === 0) {
        // 拖拽到节点内
        if (selectedKeys.value.includes(dropNode.id)) {
          // 释放位置的节点已选中，则需要把拖动的节点及其子孙节点也需要选中（因为父级选中子级也会展示选中状态）
          selectedKeys.value = selectedKeys.value.concat(stepIdAndOffspringIds);
        }
      } else if (dropNode.parent && selectedKeys.value.includes(dropNode.parent.id)) {
        // 释放位置的节点的父节点已选中，则需要把拖动的节点及其子孙节点也需要选中（因为父级选中子级也会展示选中状态）
        selectedKeys.value = selectedKeys.value.concat(stepIdAndOffspringIds);
      } else if (dragNode.parent && selectedKeys.value.includes(dragNode.parent.id)) {
        // 如果被拖动的节点的父节点在选中的节点中，则需要把被拖动的节点及其子孙节点从选中的节点中移除
        selectedKeys.value = selectedKeys.value.filter((e) => {
          for (let i = 0; i < stepIdAndOffspringIds.length; i++) {
            const id = stepIdAndOffspringIds[i];
            if (e === id) {
              stepIdAndOffspringIds.splice(i, 1);
              return false;
            }
          }
          return true;
        });
      }
      const dragResult = handleTreeDragDrop(steps.value, dragNode, dropNode, dropPosition, 'id');
      if (dragResult) {
        Message.success(t('common.moveSuccess'));
        scenario.value.unSaved = true;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      nextTick(() => {
        loading.value = false;
      });
    }
  }

  const showQuickInput = ref(false);
  const quickInputParamValue = ref('');
  const quickInputDataKey = ref('');

  function setQuickInput(step: ScenarioStepItem, dataKey: string) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.id, 'id');
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

  const dbClick = ref({
    e: null as MouseEvent | null,
    timeStamp: 0,
  });

  onMounted(() => {
    useEventListener(treeRef.value?.$el, 'dblclick', (e) => {
      dbClick.value.e = e;
      dbClick.value.timeStamp = Date.now();
    });
  });

  // 向子孙组件提供属性
  provide('dbClick', readonly(dbClick));

  defineExpose({
    checkAll,
  });
</script>

<style lang="less">
  .step-tree-active-action {
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
</style>

<style lang="less" scoped>
  .add-step-btn {
    @apply bg-white;

    padding: 4px;
    border: 1px dashed rgb(var(--primary-3));
    color: rgb(var(--primary-5));
    &:hover,
    &:focus {
      border: 1px dashed rgb(var(--primary-5));
      color: rgb(var(--primary-5));
      background-color: rgb(var(--primary-1));
    }
  }
  // 循环生成树的左边距样式 TODO:transform性能更高以及保留步骤完整宽度，需要加横向滚动
  .loop-levels(@index, @max) when (@index <= @max) {
    :deep(.arco-tree-node[data-level='@{index}']) {
      margin-left: @index * 32px;
    }
    .loop-levels(@index + 1, @max); // 下个层级
  }
  .loop-levels(0, 99); // 最大层级
  :deep(.arco-tree-node) {
    padding: 0 8px;
    min-width: 1000px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-medium) !important;
    &:not(:first-child) {
      margin-top: 4px;
    }
    &:hover {
      background-color: var(--color-text-n9) !important;
      .arco-tree-node-title {
        background-color: var(--color-text-n9) !important;
      }
    }
    .arco-tree-node-title {
      @apply !cursor-pointer bg-white;

      padding: 8px 4px;
      &:hover {
        background-color: var(--color-text-n9) !important;
      }
      .step-node-content {
        @apply flex w-full flex-1 items-center;

        gap: 8px;
        margin-right: 6px;
      }
      .step-name-container {
        @apply flex items-center;

        margin-right: 16px;
        &:hover {
          .edit-script-name-icon {
            @apply visible;
          }
        }
        .edit-script-name-icon {
          @apply invisible cursor-pointer;

          color: rgb(var(--primary-5));
        }
      }
      .arco-tree-node-title-text {
        @apply flex-1;
      }
    }
    .arco-tree-node-indent {
      @apply hidden;
    }
    .arco-tree-node-switcher {
      @apply hidden;
    }
    .arco-tree-node-drag-icon {
      @apply hidden;
    }
    .ms-tree-node-extra {
      gap: 4px;
      background-color: var(--color-text-n9) !important;
    }
  }
  :deep(.arco-tree-node-selected) {
    .arco-tree-node-title {
      .step-tree-node-title {
        font-weight: 400;
        color: var(--color-text-1);
      }
    }
  }
  :deep(.step-tree-node-focus) {
    background-color: var(--color-text-n9) !important;
    .arco-tree-node-title {
      background-color: var(--color-text-n9) !important;
    }
    .ms-tree-node-extra {
      @apply !visible !w-auto;
    }
  }
  .ms-form {
    :deep(.arco-form-item-wrapper-col),
    :deep(.arco-form-item-content) {
      min-height: auto;
    }
  }
</style>
