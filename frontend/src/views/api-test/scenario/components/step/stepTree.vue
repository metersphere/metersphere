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
        :field-names="{ title: 'name', key: 'uniqueId', children: 'children' }"
        :virtual-list-props="{
          height: '100%',
          threshold: 200,
          fixedSize: true,
          buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
        }"
        title-class="step-tree-node-title"
        node-highlight-class="step-tree-node-focus"
        action-on-node-click="expand"
        :use-map-data="false"
        disabled-title-tooltip
        checkable
        block-node
        draggable
        hide-switcher
        handle-drop
        @select="(selectedKeys, node) => handleStepSelect(node as ScenarioStepItem)"
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
                  <MsIcon type="icon-icon_split_turn-down_arrow" :size="14" />
                  {{ step.children?.length || 0 }}
                </div>
              </a-tooltip>
              <div class="mr-[8px] flex items-center gap-[8px]">
                <!-- 步骤启用/禁用，完全引用的场景下的子孙步骤不可禁用 -->
                <a-switch
                  v-model:model-value="step.enable"
                  :disabled="step.isRefScenarioStep"
                  size="small"
                  @click.stop="handleStepToggleEnable(step)"
                ></a-switch>
                <!-- 步骤执行 -->
                <MsIcon
                  v-show="!step.isExecuting"
                  v-permission="['PROJECT_API_SCENARIO:READ+EXECUTE']"
                  type="icon-icon_play-round_filled"
                  :size="18"
                  class="cursor-pointer text-[rgb(var(--link-6))]"
                  @click.stop="executeStep(step)"
                />
                <MsIcon
                  v-show="step.isExecuting"
                  v-permission="['PROJECT_API_SCENARIO:READ+EXECUTE']"
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
                  :step-id="step.uniqueId"
                  :disabled="!!step.isQuoteScenarioStep"
                  @quick-input="setQuickInput(step, $event)"
                  @change="handleStepContentChange($event, step)"
                  @click.stop
                />
                <csvTag
                  :step="step"
                  :csv-variables="scenario.scenarioConfig.variable.csvVariables"
                  :disabled="!!step.isQuoteScenarioStep"
                  @remove="(id) => removeCsv(step, id)"
                  @replace="(id) => replaceCsv(step, id)"
                  @remove-deleted="(ids) => removeDeletedCsv(step, ids)"
                />
                <!-- 自定义请求、API、CASE、场景步骤名称 -->
                <template v-if="checkStepIsApi(step)">
                  <apiMethodName v-if="checkStepShowMethod(step)" :method="step.config.method" />
                  <div
                    v-if="step.uniqueId === showStepNameEditInputStepId"
                    class="name-warp absolute left-0 top-[-1px] z-10 w-[450px]"
                    @click.stop
                  >
                    <a-input
                      v-model:model-value="tempStepName"
                      :placeholder="t('apiScenario.pleaseInputStepName')"
                      :max-length="255"
                      size="mini"
                      @press-enter="applyStepNameChange(step)"
                      @blur="applyStepNameChange(step)"
                    />
                  </div>
                  <a-tooltip v-else :content="step.name">
                    <div class="step-name-container">
                      <div class="step-name-text one-line-text font-medium">
                        {{ step.name }}
                      </div>
                      <MsIcon
                        v-if="!step.isQuoteScenarioStep"
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
                    v-if="step.uniqueId === showStepDescEditInputStepId"
                    class="desc-warp absolute left-0 top-[-1px] z-10 w-[450px]"
                  >
                    <a-input
                      v-model:model-value="tempStepDesc"
                      :default-value="step.name || t('apiScenario.pleaseInputStepDesc')"
                      :placeholder="t('apiScenario.pleaseInputStepDesc')"
                      :max-length="255"
                      size="mini"
                      @press-enter="applyStepDescChange(step)"
                      @blur="applyStepDescChange(step)"
                      @click.stop
                    >
                      <template #prefix>
                        <div class="text-[12px] leading-[20px]">{{ t('common.desc') }}</div>
                      </template>
                    </a-input>
                  </div>
                  <a-tooltip :content="step.name" :disabled="!step.name">
                    <div class="step-name-container">
                      <div class="step-name-text one-line-text font-normal">
                        {{ step.name || t('apiScenario.pleaseInputStepDesc') }}
                      </div>
                      <MsIcon
                        v-if="!step.isQuoteScenarioStep"
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
            v-if="!step.isQuoteScenarioStep"
            v-model:selected-keys="selectedKeys"
            v-model:steps="steps"
            v-permission="['PROJECT_API_DEBUG:READ+ADD', 'PROJECT_API_DEFINITION:READ+UPDATE']"
            :step="step"
            @click="() => setFocusNodeKey(step.uniqueId)"
            @other-create="handleOtherCreate"
            @close="() => setFocusNodeKey('')"
            @add-done="handleAddStepDone"
          />
        </template>
        <template #extraEnd="step">
          <responsePopover
            :step="step"
            :step-responses="scenario.stepResponses"
            :final-execute-status="getExecuteStatus(step)"
            @visible-change="handleResponsePopoverVisibleChange"
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
      v-permission="['PROJECT_API_DEBUG:READ+ADD', 'PROJECT_API_DEFINITION:READ+UPDATE']"
      @add-done="handleAddStepDone"
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
      :scenario-id="scenario.id"
      :step-responses="scenario.stepResponses"
      :permission-map="permissionMap"
      :steps="steps"
      @add-step="addCustomApiStep"
      @delete-step="() => deleteStep(activeStep)"
      @apply-step="applyApiStep"
      @stop-debug="() => handleStopExecute(activeStep)"
      @execute="handleApiExecute"
      @replace="handleReplaceStep"
    />
    <customCaseDrawer
      v-model:visible="customCaseDrawerVisible"
      :active-step="activeStep"
      :request="currentStepDetail as unknown as RequestParam"
      :scenario-id="scenario.id"
      :file-params="currentStepFileParams"
      :steps="steps"
      :step-responses="scenario.stepResponses"
      :permission-map="permissionMap"
      @apply-step="applyApiStep"
      @delete-step="() => deleteStep(activeStep)"
      @stop-debug="() => handleStopExecute(activeStep)"
      @execute="(request, executeType) => handleApiExecute((request as unknown as RequestParam), executeType)"
      @replace="handleReplaceStep"
    />
    <importApiDrawer
      v-if="importApiDrawerVisible"
      v-model:visible="importApiDrawerVisible"
      :scenario-id="scenario.id"
      @copy="handleImportApiApply('copy', $event)"
      @quote="handleImportApiApply('quote', $event)"
    />
    <scriptOperationDrawer
      v-model:visible="scriptOperationDrawerVisible"
      :detail="currentStepDetail as unknown as ExecuteConditionProcessor"
      :step="activeStep"
      :name="activeStep?.name"
      :step-responses="scenario.stepResponses"
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
      class="ms-modal-form"
      body-class="!overflow-hidden !p-0"
      :width="680"
      title-align="start"
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
        <a-form-item label="" class="hidden-item">
          <div class="flex items-center gap-[8px]">
            <a-switch
              v-model:model-value="scenarioConfigForm.useOriginScenarioParam"
              class="ml-[6px]"
              size="small"
            ></a-switch>
            {{ t('apiScenario.sourceScenarioParams') }}
          </div>
        </a-form-item>
        <a-form-item v-show="scenarioConfigForm.useOriginScenarioParam" class="hidden-item">
          <a-radio-group v-model:model-value="scenarioConfigForm.useOriginScenarioParamPreferential">
            <a-radio :value="true">
              <div class="flex items-center gap-[4px]">
                {{ t('apiScenario.sourceScenario') }}
                <a-tooltip :content="t('apiScenario.sourceScenarioTip')" position="right">
                  <icon-question-circle
                    class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                    size="16"
                  />
                </a-tooltip>
              </div>
            </a-radio>
            <a-radio :value="false">
              <div class="flex items-center gap-[4px]">
                {{ t('apiScenario.currentScenario') }}
                <a-tooltip :content="t('apiScenario.currentScenarioTip')" position="right">
                  <icon-question-circle
                    class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
                    size="16"
                  />
                </a-tooltip>
              </div>
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="" class="hidden-item !mb-0">
          <div class="flex items-center gap-[8px]">
            <a-switch
              v-model:model-value="scenarioConfigForm.enableScenarioEnv"
              class="ml-[6px]"
              size="small"
            ></a-switch>
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
        <div class="flex items-center justify-end">
          <!-- <div class="flex items-center">
            <div class="text-[var(--color-text-4)]">
              {{ t('apiScenario.valuePriority') }}
            </div>
            <div v-if="scenarioConfigParamTip" class="text-[var(--color-text-1)]">
              {{ scenarioConfigParamTip }}
            </div>
          </div> -->
          <div class="flex items-center gap-[12px]">
            <a-button type="secondary" @click="cancelScenarioConfig">{{ t('common.cancel') }}</a-button>
            <a-button type="primary" @click="saveScenarioConfig">{{ t('common.confirm') }}</a-button>
          </div>
        </div>
      </template>
    </a-modal>
    <saveAsApiModal
      v-if="tempApiDetail"
      v-model:visible="saveNewApiModalVisible"
      :detail="tempApiDetail"
      @close="() => (tempApiDetail = undefined)"
    ></saveAsApiModal>
    <a-modal
      v-model:visible="saveCaseModalVisible"
      :title="t('apiTestManagement.saveAsCase')"
      :ok-loading="saveCaseLoading"
      class="ms-modal-form"
      title-align="start"
      body-class="!p-0"
      @before-ok="saveAsCase"
      @cancel="handleSaveCaseCancel"
    >
      <a-form ref="saveCaseModalFormRef" :model="saveCaseModalForm" layout="vertical">
        <a-form-item
          field="name"
          :label="t('case.caseName')"
          :rules="[{ required: true, message: t('case.caseNameRequired') }]"
          asterisk-position="end"
        >
          <a-input
            v-model:model-value="saveCaseModalForm.name"
            :placeholder="t('case.caseNamePlaceholder')"
            :max-length="255"
          />
        </a-form-item>
        <a-form-item field="priority" :label="t('case.caseLevel')">
          <a-select v-model:model-value="saveCaseModalForm.priority" :options="casePriorityOptions"></a-select>
        </a-form-item>
        <a-form-item field="status" :label="t('common.status')">
          <a-select v-model:model-value="saveCaseModalForm.status">
            <a-option v-for="item in caseStatusOptions" :key="item.value" :value="item.value">
              {{ t(item.label) }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="tags" :label="t('common.tag')">
          <MsTagsInput
            v-model:model-value="saveCaseModalForm.tags"
            placeholder="common.tagsInputPlaceholder"
            allow-clear
            unique-value
            retain-input-value
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <quoteCsvDrawer
      v-model:visible="csvDrawerVisible"
      :csv-variables="scenario.scenarioConfig.variable.csvVariables"
      :exclude-keys="activeStep?.csvIds"
      :is-single="!!replaceCsvId"
      @confirm="handleQuoteCsvConfirm"
    />
  </div>
</template>

<script setup lang="ts">
  import { useEventListener } from '@vueuse/core';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { parseTableDataToJsonSchema } from '@/components/pure/ms-json-schema/utils';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import { ImportData } from '../common/importApiDrawer/index.vue';
  import quoteCsvDrawer from '../common/quoteCsvDrawer.vue';
  import stepType from '../common/stepType/stepType.vue';
  import createStepActions from './createAction/createStepActions.vue';
  import stepInsertStepTrigger from './createAction/stepInsertStepTrigger.vue';
  import conditionContent from './stepNodeComposition/conditionContent.vue';
  import csvTag from './stepNodeComposition/csvTag.vue';
  import loopControlContent from './stepNodeComposition/loopContent.vue';
  import quoteContent from './stepNodeComposition/quoteContent.vue';
  import waitTimeContent from './stepNodeComposition/waitTimeContent.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import { RequestParam as CaseRequestParam } from '@/views/api-test/components/requestComposition/index.vue';
  import saveAsApiModal from '@/views/api-test/components/saveAsApiModal.vue';

  import { addCase, getDefinitionDetail } from '@/api/modules/api-test/management';
  import { scenarioCopyStepFiles } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { deleteNode, findNodeByKey, getGenerateId, insertNodes, mapTree, TreeNode } from '@/utils';

  import {
    ExecuteApiRequestFullParams,
    ExecuteConditionProcessor,
    ExecutePluginRequestParams,
  } from '@/models/apiTest/common';
  import { AddApiCaseParams } from '@/models/apiTest/management';
  import {
    CreateStepAction,
    Scenario,
    ScenarioStepDetails,
    ScenarioStepFileParams,
    ScenarioStepItem,
  } from '@/models/apiTest/scenario';
  import {
    RequestCaseStatus,
    ScenarioAddStepActionType,
    ScenarioExecuteStatus,
    ScenarioStepRefType,
    ScenarioStepType,
  } from '@/enums/apiEnum';

  import type { RequestParam } from '../common/customApiDrawer.vue';
  import useCreateActions from './createAction/useCreateActions';
  import useStepExecute from './useStepExecute';
  import useStepNodeEdit from './useStepNodeEdit';
  import useStepOperation from './useStepOperation';
  import { casePriorityOptions, caseStatusOptions } from '@/views/api-test/components/config';
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
    (e: 'stepAdd'): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

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
  const selectedKeys = defineModel<(string | number)[]>('selectedKeys', {
    required: true,
  }); // 没啥用，目前用来展示选中样式
  const isPriorityLocalExec = inject<Ref<boolean>>('isPriorityLocalExec');
  const localExecuteUrl = inject<Ref<string>>('localExecuteUrl');

  const permissionMap = {
    execute: 'PROJECT_API_SCENARIO:READ+EXECUTE',
  };
  const loading = ref(false);
  const treeRef = ref<InstanceType<typeof MsTree>>();
  const focusStepKey = ref<string | number>(''); // 聚焦的key
  const activeStep = ref<ScenarioStepItem>(); // 用于弹窗配置时记录当前操作的步骤节点
  const activeStepByCreate = ref<ScenarioStepItem | undefined>(); // 用于抽屉操作创建步骤时记录当前操作的步骤节点

  const { executeStep, handleApiExecute, handleStopExecute } = useStepExecute({
    scenario,
    steps,
    stepDetails,
    activeStep,
    isPriorityLocalExec,
    localExecuteUrl,
  });

  function setFocusNodeKey(id: string | number) {
    focusStepKey.value = id || '';
  }

  function checkStepIsApi(step: ScenarioStepItem) {
    return [ScenarioStepType.API, ScenarioStepType.API_CASE, ScenarioStepType.CUSTOM_REQUEST].includes(step.stepType);
  }

  function getExecuteStatus(step: ScenarioStepItem) {
    if (scenario.value.stepResponses && scenario.value.stepResponses[step.uniqueId]) {
      if (
        scenario.value.stepResponses[step.uniqueId].some((report) => report.status === ScenarioExecuteStatus.FAKE_ERROR)
      ) {
        return ScenarioExecuteStatus.FAKE_ERROR;
      }
      // 有一次失败就是失败
      if (scenario.value.stepResponses[step.uniqueId].some((report) => !report.isSuccessful)) {
        return ScenarioExecuteStatus.FAILED;
      }
      return ScenarioExecuteStatus.SUCCESS;
    }
    return step.executeStatus;
  }

  function handleResponsePopoverVisibleChange(visible: boolean, step: ScenarioStepItem) {
    if (visible) {
      setFocusNodeKey(step.uniqueId);
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
    if (parent && selectedKeys.value.includes(parent.uniqueId)) {
      // 添加子节点时，当前节点已选中，则需要把新节点也需要选中（因为父级选中子级也会展示选中状态）
      selectedKeys.value.push(step.uniqueId);
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
    if ((node as ScenarioStepItem).isQuoteScenarioStep) {
      return [];
    }
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
    if (_stepType.isQuoteApi || _stepType.isCopyApi) {
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
    if ((node as ScenarioStepItem).stepType === ScenarioStepType.LOOP_CONTROLLER) {
      const arr = [...stepMoreActions];
      arr.splice(1, 0, {
        label: 'apiScenario.quoteCsv',
        eventTag: 'quoteCsv',
      });
      return arr;
    }
    return stepMoreActions;
  }

  const showScenarioConfig = ref(false);

  /**
   * 处理api、case、场景步骤名称编辑
   */
  const showStepNameEditInputStepId = ref<string | number>('');
  const tempStepName = ref('');
  /**
   * 处理非 api、case、场景步骤名称编辑
   */
  const showStepDescEditInputStepId = ref<string | number>('');
  const tempStepDesc = ref('');
  const importApiDrawerVisible = ref(false);
  const customCaseDrawerVisible = ref(false);
  const customApiDrawerVisible = ref(false);
  const scriptOperationDrawerVisible = ref(false);

  const { handleStepExpand, handleStepSelect, deleteStep, handleDrop, getStepDetail } = useStepOperation({
    scenario,
    steps,
    stepDetails,
    activeStep,
    selectedKeys,
    customApiDrawerVisible,
    customCaseDrawerVisible,
    scriptOperationDrawerVisible,
    loading,
  });

  const showQuickInput = ref(false);
  const quickInputParamValue = ref<any>('');
  const quickInputDataKey = ref('');

  const {
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
  } = useStepNodeEdit({
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
  });

  const saveNewApiModalVisible = ref(false);
  const tempApiDetail = ref<RequestParam>();

  const saveCaseModalVisible = ref(false);
  const saveCaseLoading = ref(false);
  const saveCaseModalForm = ref({
    name: '',
    priority: 'P0',
    status: RequestCaseStatus.PROCESSING,
    tags: [],
  });
  const saveCaseModalFormRef = ref<FormInstance>();

  function handleSaveCaseCancel() {
    saveCaseModalForm.value = {
      name: '',
      priority: 'P0',
      status: RequestCaseStatus.PROCESSING,
      tags: [],
    };
    saveCaseModalVisible.value = false;
    activeStep.value = undefined;
  }

  function saveAsCase(done: (closed: boolean) => void) {
    saveCaseModalFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          if (activeStep.value) {
            saveCaseLoading.value = true;
            let detail = stepDetails.value[activeStep.value.id] as
              | ExecuteApiRequestFullParams
              | ExecutePluginRequestParams;
            if (!detail) {
              // 如果步骤没有查看过详情，则需要手动加载一次
              if (
                (stepDetails.value[activeStep.value.id] === undefined &&
                  activeStep.value.copyFromStepId &&
                  !activeStep.value.isNew) ||
                (stepDetails.value[activeStep.value.id] === undefined && !activeStep.value.isNew)
              ) {
                // 详情映射中没有对应数据，初始化步骤详情（复制的步骤没有加载详情前就被复制，打开复制后的步骤就初始化被复制步骤的详情）
                await getStepDetail(activeStep.value);
                detail = stepDetails.value[activeStep.value.id] as
                  | ExecuteApiRequestFullParams
                  | ExecutePluginRequestParams;
              } else {
                const apiDetail = await getDefinitionDetail(activeStep.value.resourceId || '');
                detail = {
                  ...apiDetail.request,
                  ...apiDetail,
                };
              }
            }
            const fileParams = scenario.value.stepFileParam[activeStep.value.id];
            const params: AddApiCaseParams = {
              projectId: appStore.currentProjectId,
              environmentId: appStore.currentEnvConfig?.id || '',
              apiDefinitionId: activeStep.value.resourceId || '',
              request: detail,
              ...saveCaseModalForm.value,
              uploadFileIds: fileParams?.uploadFileIds || [],
              linkFileIds: fileParams?.linkFileIds || [],
            };
            await addCase(params);
            done(true);
            Message.success(t('common.saveSuccess'));
            handleSaveCaseCancel();
            saveCaseLoading.value = false;
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          done(false);
        } finally {
          handleSaveCaseCancel();
          saveCaseLoading.value = false;
        }
      } else {
        saveCaseLoading.value = false;
        done(false);
      }
    });
  }

  const csvDrawerVisible = ref(false);
  const replaceCsvId = ref('');
  function removeCsv(step: ScenarioStepItem, id?: string) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
    if (id && realStep) {
      realStep.csvIds = realStep.csvIds.filter((item: string) => item !== id);
      scenario.value.unSaved = true;
    }
  }

  function replaceCsv(step: ScenarioStepItem, id?: string) {
    csvDrawerVisible.value = true;
    activeStep.value = step;
    replaceCsvId.value = id || '';
  }

  function removeDeletedCsv(step: ScenarioStepItem, ids: string[]) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
    if (realStep) {
      realStep.csvIds = realStep.csvIds.filter((item: string) => !ids.includes(item));
      scenario.value.unSaved = true;
    }
  }

  function handleQuoteCsvConfirm(keys: string[]) {
    if (activeStep.value) {
      const realStep = findNodeByKey<ScenarioStepItem>(steps.value, activeStep.value.uniqueId, 'uniqueId');
      if (!!replaceCsvId.value && realStep !== null) {
        const index = realStep.csvIds.findIndex((item: string) => item === replaceCsvId.value);
        if (!realStep.csvIds) {
          realStep.csvIds = [];
        }
        realStep.csvIds.splice(index, 1, keys[0]);
      } else if (realStep !== null) {
        realStep.csvIds = [...(realStep.csvIds || []), ...keys];
      }
      scenario.value.unSaved = true;
    }
  }

  /**
   * 复制步骤
   * @param node 复制的节点
   */
  async function copyStep(node: MsTreeNodeData) {
    loading.value = true;
    try {
      const id = getGenerateId();
      const stepDetail = stepDetails.value[node.id];
      const { isQuoteScenario } = getStepType(node as ScenarioStepItem);
      let { copyFromStepId } = node;
      if (stepDetail || node.isNew !== true || !node.copyFromStepId) {
        // 如果复制的步骤查看过详情，则复制来源直接取它的 id
        // 如果复制的步骤没有查看过详情，且是新建的步骤，则取它本身的 id
        copyFromStepId = node.id;
      }
      let parseRequestBodyResult: Record<string, any> = {
        uploadFileIds: [],
        linkFileIds: [],
        deleteFileIds: [], // 存储对比已保存的文件后，需要删除的文件 id 集合
        unLinkFileIds: [], // 存储对比已保存的文件后，需要取消关联的文件 id 集合
      };
      let newFileRes;
      if (node.config.protocol === 'HTTP' && (stepDetail as RequestParam)?.body) {
        if (node.copyFromStepId || node.refType === ScenarioStepRefType.COPY) {
          // 复制的步骤需要复制文件
          const fileIds = parseRequestBodyFiles((stepDetail as RequestParam).body, [], [], []).uploadFileIds;
          if (fileIds.length > 0) {
            newFileRes = await scenarioCopyStepFiles({
              copyFromStepId,
              resourceId: node.resourceId,
              stepType: node.stepType,
              refType: node.refType,
              isTempFile: !!stepDetail, // 复制未保存的步骤时 true
              fileIds,
            });
          }
          parseRequestBodyFiles((stepDetail as RequestParam).body, [], [], [], newFileRes);
        } else {
          parseRequestBodyResult = parseRequestBodyFiles((stepDetail as RequestParam).body, [], [], [], newFileRes); // 解析请求体中的文件，将详情中的文件 id 集合收集，更新时以判断文件是否删除以及是否新上传的文件
        }
      }
      if (stepDetail) {
        // 如果复制的步骤还有详情数据，则也复制详情数据
        stepDetails.value[id] = cloneDeep({
          ...stepDetail,
          stepId: id,
          uniqueId: id,
          ...parseRequestBodyResult,
        });
      }
      insertNodes<ScenarioStepItem>(
        steps.value,
        node.uniqueId,
        {
          ...cloneDeep(
            mapTree<ScenarioStepItem>(node, (childNode) => {
              const childId = getGenerateId();
              const childStepDetail = stepDetails.value[childNode.id];
              let childCopyFromStepId = childNode.id;
              if (childStepDetail) {
                // 如果复制的步骤下子步骤还有详情数据，则也复制详情数据
                stepDetails.value[childId] = cloneDeep(childStepDetail);
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
                uniqueId: childId,
              };
            })[0]
          ),
          name: `copy_${node.name}`.substring(0, 255),
          copyFromStepId,
          sort: node.sort + 1,
          isNew: true,
          id,
          uniqueId: id,
        },
        'after',
        selectedIfNeed,
        'uniqueId'
      );
      scenario.value.unSaved = true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  async function handleStepMoreActionSelect(item: ActionsItem, node: MsTreeNodeData) {
    switch (item.eventTag) {
      case 'copy':
        copyStep(node);
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
        openModal({
          type: 'error',
          title: t('common.tip'),
          content:
            node.children && node.children.length > 0
              ? t('apiScenario.deleteStepConfirmWithChildren')
              : t('apiScenario.deleteStepConfirm', { name: node.name }),
          okText: t('common.confirmDelete'),
          cancelText: t('common.cancel'),
          okButtonProps: {
            status: 'danger',
          },
          maskClosable: false,
          onBeforeOk: async () => {
            deleteNode(steps.value, node.uniqueId, 'uniqueId');
            delete stepDetails.value[node.id];
            scenario.value.unSaved = true;
          },
          hideCancel: false,
        });
        break;
      case 'saveAsApi':
        if (!stepDetails.value[node.id]) {
          // 详情映射中没有对应数据，初始化步骤详情（复制的步骤没有加载详情前就被复制，打开复制后的步骤就初始化被复制步骤的详情）
          await getStepDetail(node as ScenarioStepItem);
        }
        const detail = stepDetails.value[node.id] as RequestParam;
        const fileParams = scenario.value.stepFileParam[node.id];
        tempApiDetail.value = {
          ...detail,
          customizeRequest: false, // 另存为新接口时，不是自定义请求
          uploadFileIds: fileParams?.uploadFileIds || [],
          linkFileIds: fileParams?.linkFileIds || [],
        };
        saveNewApiModalVisible.value = true;
        break;
      case 'saveAsCase':
        activeStep.value = node as ScenarioStepItem;
        saveCaseModalVisible.value = true;
        break;
      case 'quoteCsv':
        activeStep.value = node as ScenarioStepItem;
        replaceCsvId.value = '';
        csvDrawerVisible.value = true;
        break;
      default:
        break;
    }
  }

  function checkAll(val: boolean) {
    treeRef.value?.checkAll(val);
  }

  const activeCreateAction = ref<CreateStepAction>(); // 用于抽屉操作创建步骤时记录当前插入类型
  const currentStepDetail = computed<ScenarioStepDetails | undefined>(() => {
    if (activeStep.value) {
      return stepDetails.value[activeStep.value.id];
    }
    return undefined;
  });
  const currentStepFileParams = computed<ScenarioStepFileParams | undefined>(() => {
    if (activeStep.value) {
      return scenario.value.stepFileParam[activeStep.value.id];
    }
    return undefined;
  });

  function handleAddStepDone(newStep: ScenarioStepItem) {
    selectedKeys.value = [newStep.uniqueId]; // 选中新添加的步骤
    emit('stepAdd');
    scenario.value.unSaved = true;
  }

  function handleReplaceStep(newStep: ScenarioStepItem) {
    if (activeStep.value) {
      // 替换步骤，删除原本的详情数据
      delete scenario.value.stepResponses[activeStep.value.uniqueId];
      delete scenario.value.stepFileParam[activeStep.value.id];
      delete stepDetails.value[activeStep.value.id];
      const realStep = findNodeByKey<ScenarioStepItem>(steps.value, activeStep.value.uniqueId, 'uniqueId');
      if (realStep) {
        // 将旧步骤替换为新步骤
        if (realStep.parent?.children) {
          // 如果被替换的步骤是子孙步骤，则需要在父级的 children 中替换
          const index = realStep.parent.children.findIndex(
            (item: ScenarioStepItem) => item.uniqueId === realStep.uniqueId
          );
          realStep.parent.children.splice(index, 1, newStep);
        } else {
          // 如果被替换的步骤是第一层级步骤，则直接替换
          const index = steps.value.findIndex((item) => item.uniqueId === realStep.uniqueId);
          steps.value.splice(index, 1, newStep);
        }
      }
      activeStep.value = newStep;
    }
    Message.success(t('apiScenario.replaceSuccess'));
    scenario.value.unSaved = true;
    if (newStep.stepType === ScenarioStepType.API_SCENARIO) {
      customCaseDrawerVisible.value = false;
      customApiDrawerVisible.value = false;
    } else {
      customCaseDrawerVisible.value = false;
      customApiDrawerVisible.value = false;
      nextTick(() => {
        // 等待抽屉关闭后再打开新的抽屉
        handleStepSelect(newStep);
      });
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
    activeStepByCreate.value = step;
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
    if (activeStepByCreate.value && activeCreateAction.value) {
      switch (activeCreateAction.value) {
        case 'inside':
          sort = activeStepByCreate.value.children ? activeStepByCreate.value.children.length : 0;
          break;
        case 'before':
          sort = activeStepByCreate.value.sort;
          break;
        case 'after':
          sort = activeStepByCreate.value.sort + 1;
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
    insertSteps.forEach((step) => {
      scenario.value.stepFileParam[step.id] = {
        linkFileIds: [],
        uploadFileIds: [],
        deleteFileIds: [],
        unLinkFileIds: [],
      };
    });
    if (activeStepByCreate.value && activeCreateAction.value) {
      handleCreateSteps(
        activeStepByCreate.value,
        insertSteps,
        steps.value,
        activeCreateAction.value,
        selectedKeys.value
      );
    } else {
      steps.value = steps.value.concat(insertSteps);
    }
    emit('stepAdd');
    scenario.value.unSaved = true;
  }

  /**
   * 添加自定义 API 步骤
   */
  function addCustomApiStep(request: RequestParam) {
    request.isNew = false;
    stepDetails.value[request.stepId] = request;
    scenario.value.stepFileParam[request.stepId] = {
      linkFileIds: request.linkFileIds,
      uploadFileIds: request.uploadFileIds,
      deleteFileIds: request.deleteFileIds,
      unLinkFileIds: request.unLinkFileIds,
    };
    emit('updateResource', request.uploadFileIds, request.linkFileIds);
    if (activeStepByCreate.value && activeCreateAction.value) {
      handleCreateStep(
        {
          stepType: ScenarioStepType.CUSTOM_REQUEST,
          name: request.stepName || request.name || t('apiScenario.customApi'),
          config: {
            protocol: request.protocol,
            method: request.method,
          },
          id: request.stepId,
          uniqueId: request.stepId,
          projectId: appStore.currentProjectId,
          refType: ScenarioStepRefType.DIRECT,
        },
        activeStepByCreate.value,
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
        uniqueId: request.stepId,
        sort: steps.value.length + 1,
        stepType: ScenarioStepType.CUSTOM_REQUEST,
        refType: ScenarioStepRefType.DIRECT,
        name: request.name || t('apiScenario.customApi'),
        projectId: appStore.currentProjectId,
      });
    }
    selectedKeys.value = [request.stepId]; // 选中新添加的步骤
    emit('stepAdd');
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
      const realStep = findNodeByKey<ScenarioStepItem>(steps.value, activeStep.value.uniqueId, 'uniqueId');
      if (realStep) {
        const _stepType = getStepType(realStep as ScenarioStepItem);
        if (_stepType.isQuoteCase && !realStep.isQuoteScenarioStep) {
          realStep.name = request.stepName || request.name;
          stepDetails.value[realStep.id] = request; // 为了设置一次正确的polymorphicName
          activeStep.value = undefined;
          return;
        }
      }
      if (realStep && !realStep.isQuoteScenarioStep) {
        request.isNew = false;
        stepDetails.value[realStep.id] = {
          ...request,
          body: {
            ...request.body,
            jsonBody: {
              ...request.body?.jsonBody,
              jsonSchema: request.body?.jsonBody?.jsonSchemaTableData
                ? parseTableDataToJsonSchema(request.body?.jsonBody?.jsonSchemaTableData?.[0])
                : undefined,
            },
          },
        };
        scenario.value.stepFileParam[realStep?.id] = {
          linkFileIds: request.linkFileIds,
          uploadFileIds: request.uploadFileIds,
          deleteFileIds: request.deleteFileIds,
          unLinkFileIds: request.unLinkFileIds,
        };
        realStep.config = {
          ...realStep.config,
          method: request.method,
        };
        realStep.name = request.stepName || request.name;
        emit('updateResource', request.uploadFileIds, request.linkFileIds);
      }
      activeStep.value = undefined;
    }
  }

  /**
   * 添加脚本操作步骤
   */
  function addScriptStep(name: string, scriptProcessor: ExecuteConditionProcessor) {
    const id = getGenerateId();
    stepDetails.value[id] = cloneDeep(scriptProcessor);
    if (activeStepByCreate.value && activeCreateAction.value) {
      handleCreateStep(
        {
          id,
          uniqueId: id,
          refType: ScenarioStepRefType.DIRECT,
          stepType: ScenarioStepType.SCRIPT,
          name,
          projectId: appStore.currentProjectId,
        },
        activeStepByCreate.value,
        steps.value,
        activeCreateAction.value,
        selectedKeys.value
      );
    } else {
      steps.value.push({
        ...cloneDeep(defaultStepItemCommon),
        id,
        uniqueId: id,
        sort: steps.value.length + 1,
        stepType: ScenarioStepType.SCRIPT,
        refType: ScenarioStepRefType.DIRECT,
        name,
        projectId: appStore.currentProjectId,
      });
    }
    selectedKeys.value = [id]; // 选中新添加的步骤
    emit('stepAdd');
    scenario.value.unSaved = true;
  }

  function saveScriptStep(name: string, scriptProcessor: ExecuteConditionProcessor, unSaved = false) {
    if (activeStep.value && !activeStep.value.isQuoteScenarioStep) {
      // 引用的场景步骤不需要存储详情
      stepDetails.value[activeStep.value.id] = cloneDeep(scriptProcessor);
      activeStep.value.name = name;
      activeStep.value = undefined;
      if (unSaved) {
        scenario.value.unSaved = true;
      }
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
    padding: 4px;
    border: 1px dashed rgb(var(--primary-3));
    color: rgb(var(--primary-5));
    background-color: var(--color-text-fff);
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
        .step-name-text {
          max-width: calc(100% - 244px) !important;
        }
      }
    }
    .arco-tree-node-title {
      @apply !cursor-pointer;

      padding: 8px 4px;
      background-color: var(--color-text-fff);
      &:hover {
        background-color: var(--color-text-n9) !important;
      }
      .step-node-content {
        @apply flex w-full flex-1 flex-nowrap items-center;

        gap: 8px;
      }
      .step-name-container {
        @apply flex flex-1 items-center overflow-hidden;

        margin-right: 16px;
        &:hover {
          .edit-script-name-icon {
            @apply visible;
          }
        }
        .step-name-text {
          margin-right: 4px;
          max-width: calc(100% - 170px);
          color: var(--color-text-1);
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
  :deep(.step-tree-node-title) {
    @apply w-full;
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
  :deep(.value-input) {
    .input-suffix-icon {
      width: 12px;
      height: 12px;
      @apply cursor-pointer;
    }
    .arco-input-suffix {
      opacity: 0;
    }
    &:hover {
      .arco-input-suffix {
        color: rgb(var(--primary-5));
        opacity: 1;
      }
    }
  }
</style>
