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
                  :step-id="step.uniqueId"
                  :disabled="!!step.isQuoteScenarioStep"
                  @quick-input="setQuickInput(step, $event)"
                  @change="handleStepContentChange($event, step)"
                  @click.stop
                />
                <!-- 自定义请求、API、CASE、场景步骤名称 -->
                <template v-if="checkStepIsApi(step)">
                  <apiMethodName v-if="checkStepShowMethod(step)" :method="step.config.method" />
                  <div
                    v-if="step.uniqueId === showStepNameEditInputStepId"
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
                        } font-normal text-[var(--color-text-1)]`"
                      >
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
            @click="setFocusNodeKey(step.uniqueId)"
            @other-create="handleOtherCreate"
            @close="setFocusNodeKey('')"
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
      @delete-step="deleteStep(activeStep)"
      @apply-step="applyApiStep"
      @stop-debug="handleStopExecute(activeStep)"
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
      @delete-step="deleteStep(activeStep)"
      @stop-debug="handleStopExecute(activeStep)"
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
    <a-modal
      v-model:visible="saveNewApiModalVisible"
      :title="t('common.save')"
      class="ms-modal-form"
      title-align="start"
      body-class="!p-0"
    >
      <a-form ref="saveModalFormRef" :model="saveModalForm" layout="vertical">
        <a-form-item
          field="name"
          :label="t('apiTestDebug.requestName')"
          :rules="[{ required: true, message: t('apiTestDebug.requestNameRequired') }]"
          asterisk-position="end"
        >
          <a-input
            v-model:model-value="saveModalForm.name"
            :max-length="255"
            :placeholder="t('apiTestDebug.requestNamePlaceholder')"
          />
        </a-form-item>
        <a-form-item
          v-if="activeStep?.config.protocol === 'HTTP'"
          field="path"
          :label="t('apiTestDebug.requestUrl')"
          :rules="[{ required: true, message: t('apiTestDebug.requestUrlRequired') }]"
          asterisk-position="end"
        >
          <a-input
            v-model:model-value="saveModalForm.path"
            :max-length="255"
            :placeholder="t('apiTestDebug.commonPlaceholder')"
          />
        </a-form-item>
        <a-form-item :label="t('apiTestDebug.requestModule')" class="mb-0">
          <a-tree-select
            v-model:modelValue="saveModalForm.moduleId"
            :data="apiModuleTree"
            :field-names="{ title: 'name', key: 'id', children: 'children' }"
            :tree-props="{
              virtualListProps: {
                height: 200,
                threshold: 200,
              },
            }"
            allow-search
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-[4px]">
            <a-checkbox v-model:model-value="saveModalForm.saveApiAsCase"></a-checkbox>
            {{ t('apiScenario.syncSaveAsCase') }}
          </div>
          <div class="flex items-center gap-[12px]">
            <a-button type="secondary" :disabled="saveLoading" @click="handleSaveApiCancel">
              {{ t('common.cancel') }}
            </a-button>
            <a-button type="primary" :loading="saveLoading" @click="handleSaveApi">{{ t('common.confirm') }}</a-button>
          </div>
        </div>
      </template>
    </a-modal>
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
  </div>
</template>

<script setup lang="ts">
  import { useEventListener } from '@vueuse/core';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import { MsTreeExpandedData, MsTreeNodeData } from '@/components/business/ms-tree/types';
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
  import {
    addCase,
    addDefinition,
    getDefinitionDetail,
    getModuleTreeOnlyModules,
  } from '@/api/modules/api-test/management';
  import { debugScenario, getScenarioDetail, getScenarioStep } from '@/api/modules/api-test/scenario';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
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

  import {
    ExecuteApiRequestFullParams,
    ExecuteConditionProcessor,
    ExecutePluginRequestParams,
  } from '@/models/apiTest/common';
  import { AddApiCaseParams } from '@/models/apiTest/management';
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
    RequestCaseStatus,
    RequestDefinitionStatus,
    ScenarioAddStepActionType,
    ScenarioExecuteStatus,
    ScenarioStepRefType,
    ScenarioStepType,
  } from '@/enums/apiEnum';

  import type { RequestParam } from '../common/customApiDrawer.vue';
  import updateStepStatus from '../utils';
  import useCreateActions from './createAction/useCreateActions';
  import { casePriorityOptions, caseStatusOptions, defaultResponseItem } from '@/views/api-test/components/config';
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
  const currentEnvConfig = inject<Ref<EnvConfig>>('currentEnvConfig');

  const permissionMap = {
    execute: 'PROJECT_API_SCENARIO:READ+EXECUTE',
  };
  const loading = ref(false);
  const treeRef = ref<InstanceType<typeof MsTree>>();
  const focusStepKey = ref<string | number>(''); // 聚焦的key
  const activeStep = ref<ScenarioStepItem>(); // 用于抽屉操作创建步骤、弹窗配置时记录当前操作的步骤节点

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
    if ((node as ScenarioStepItem).isQuoteScenarioStep) {
      return [];
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
    return stepMoreActions;
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
  const showScenarioConfig = ref(false);
  const scenarioConfigParamTip = computed(() => {
    if (!scenarioConfigForm.value.useOriginScenarioParam && !scenarioConfigForm.value.enableScenarioEnv) {
      // 非使用原场景参数-非选择源场景环境
      return t('apiScenario.notSource');
    }
    if (!scenarioConfigForm.value.useOriginScenarioParam && scenarioConfigForm.value.enableScenarioEnv) {
      // 非使用原场景参数-选择源场景环境
      return t('apiScenario.notSourceParamAndSourceEnv');
    }
    if (
      scenarioConfigForm.value.useOriginScenarioParam &&
      scenarioConfigForm.value.useOriginScenarioParamPreferential &&
      !scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用原场景参数-优先使用原场景参数
      return t('apiScenario.sourceParamAndSource');
    }
    if (
      scenarioConfigForm.value.useOriginScenarioParam &&
      scenarioConfigForm.value.useOriginScenarioParamPreferential &&
      scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用原场景参数-优先使用原场景参数-选择源场景环境
      return t('apiScenario.sourceParamAndSourceEnv');
    }
    if (
      scenarioConfigForm.value.useOriginScenarioParam &&
      !scenarioConfigForm.value.useOriginScenarioParamPreferential &&
      !scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用原场景参数-优先使用当前场景参数
      return t('apiScenario.currentParamAndSource');
    }
    if (
      scenarioConfigForm.value.useOriginScenarioParam &&
      !scenarioConfigForm.value.useOriginScenarioParamPreferential &&
      scenarioConfigForm.value.enableScenarioEnv
    ) {
      // 使用原场景参数-优先使用当前场景参数-选择源场景环境
      return t('apiScenario.currentParamAndSourceEnv');
    }
  });

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

  const apiModuleTree = ref<MsTreeNodeData[]>([]);
  async function initApiModuleTree(protocol: string) {
    try {
      apiModuleTree.value = await getModuleTreeOnlyModules({
        keyword: '',
        protocol,
        projectId: appStore.currentProjectId,
        moduleIds: [],
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  }

  const saveNewApiModalVisible = ref(false);
  const saveModalForm = ref({
    name: '',
    path: '',
    moduleId: 'root',
    saveApiAsCase: false,
  });
  const saveModalFormRef = ref<FormInstance>();
  const saveLoading = ref(false);

  async function saveApiAsCase(id: string) {
    if (activeStep.value) {
      const detail = stepDetails.value[activeStep.value.id] as RequestParam;
      const fileParams = scenario.value.stepFileParam[activeStep.value.id];
      const url = new URL(saveModalForm.value.path);
      const path = url.pathname + url.search + url.hash;
      const params: AddApiCaseParams = {
        name: saveModalForm.value.name,
        projectId: appStore.currentProjectId,
        environmentId: currentEnvConfig?.value.id || '',
        apiDefinitionId: id,
        request: {
          ...detail,
          url: path,
        },
        priority: 'P0',
        status: RequestCaseStatus.PROCESSING,
        tags: [],
        uploadFileIds: fileParams?.uploadFileIds || [],
        linkFileIds: fileParams?.linkFileIds || [],
      };
      await addCase(params);
    }
  }

  /**
   * 保存请求
   * @param isSaveCase 是否需要保存用例
   */
  async function realSaveAsApi() {
    try {
      saveLoading.value = true;
      if (activeStep.value) {
        let url;
        let path = '';
        try {
          url = new URL(saveModalForm.value.path);
          path = url.pathname + url.search + url.hash;
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          path = saveModalForm.value.path;
        }
        const detail = stepDetails.value[activeStep.value.id] as RequestParam;
        const fileParams = scenario.value.stepFileParam[activeStep.value.id];
        const res = await addDefinition({
          ...saveModalForm.value,
          path,
          projectId: appStore.currentProjectId,
          tags: [],
          description: '',
          status: RequestDefinitionStatus.PROCESSING,
          customFields: [],
          versionId: '',
          environmentId: currentEnvConfig?.value.id || '',
          request: {
            ...detail,
            url: path,
            path,
          },
          uploadFileIds: fileParams?.uploadFileIds || [],
          linkFileIds: fileParams?.linkFileIds || [],
          response: [defaultResponseItem],
          method: detail?.method,
          protocol: detail?.protocol,
        });
        if (saveModalForm.value.saveApiAsCase) {
          await saveApiAsCase(res.id);
        }
        Message.success(t('common.saveSuccess'));
        saveNewApiModalVisible.value = false;
        saveLoading.value = false;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      saveLoading.value = false;
    }
  }

  function handleSaveApiCancel() {
    saveModalFormRef.value?.resetFields();
    saveNewApiModalVisible.value = false;
  }

  function handleSaveApi() {
    saveModalFormRef.value?.validate(async (errors) => {
      if (!errors) {
        await realSaveAsApi();
        handleSaveApiCancel();
      }
    });
  }

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
              environmentId: currentEnvConfig?.value.id || '',
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
          node.uniqueId,
          {
            ...cloneDeep(
              mapTree<ScenarioStepItem>(node, (childNode) => {
                const childId = getGenerateId();
                const childStepDetail = stepDetails.value[childNode.id];
                const childStepFileParam = scenario.value.stepFileParam[childNode.id];
                let childCopyFromStepId = childNode.id;
                if (childStepDetail) {
                  // 如果复制的步骤下子步骤还有详情数据，则也复制详情数据
                  stepDetails.value[childId] = cloneDeep(childStepDetail);
                }
                if (childStepFileParam) {
                  // 如果复制的步骤下子步骤还有详情数据，则也复制详情数据
                  scenario.value.stepFileParam[childNode.id] = cloneDeep(childStepFileParam);
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
            copyFromStepId: stepDetail ? node.id : node.copyFromStepId,
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
            scenario.value.unSaved = true;
          },
          hideCancel: false,
        });
        break;
      case 'saveAsApi':
        activeStep.value = node as ScenarioStepItem;
        initApiModuleTree((stepDetails.value[node.id] as RequestParam)?.protocol);
        saveModalForm.value.path = (stepDetails.value[node.id] as RequestParam)?.url;
        saveNewApiModalVisible.value = true;
        break;
      case 'saveAsCase':
        activeStep.value = node as ScenarioStepItem;
        saveCaseModalVisible.value = true;
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

  function applyStepNameChange(step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
    if (realStep) {
      realStep.name = tempStepName.value || realStep.name;
      realStep.draggable = true; // 编辑完恢复拖拽
    }
    showStepNameEditInputStepId.value = '';
    scenario.value.unSaved = !!tempStepName.value;
  }

  /**
   * 处理非 api、case、场景步骤名称编辑
   */
  const showStepDescEditInputStepId = ref<string | number>('');
  const tempStepDesc = ref('');
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

  function applyStepDescChange(step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
    if (realStep) {
      realStep.name = tempStepDesc.value || realStep.name;
      realStep.draggable = true; // 编辑完恢复拖拽
    }
    showStepDescEditInputStepId.value = '';
    scenario.value.unSaved = !!tempStepDesc.value;
  }

  function handleStepContentChange($event, step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.uniqueId, 'uniqueId');
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
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, data.node?.uniqueId, 'uniqueId');
    if (realStep) {
      realStep.expanded = !realStep.expanded;
    }
  }

  function handleStepToggleEnable(data: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, data.uniqueId, 'uniqueId');
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

  function handleAddStepDone(newStep: ScenarioStepItem) {
    selectedKeys.value = [newStep.uniqueId]; // 选中新添加的步骤
    emit('stepAdd');
    scenario.value.unSaved = true;
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
      offspringIds.push(e.uniqueId);
      return e;
    });
    selectedKeys.value = [step.uniqueId, ...offspringIds];
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

  /**
   * 开启websocket监听，接收执行结果
   */
  function debugSocket(step: ScenarioStepItem, _scenario: Scenario, reportId: string | number) {
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
          data.taskResult.requestResults.forEach((result) => {
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
          updateStepStatus([step], _scenario.stepResponses, step.uniqueId);
        }
      }
    });
  }

  async function realExecute(
    executeParams: Pick<ApiScenarioDebugRequest, 'steps' | 'stepDetails' | 'reportId' | 'stepFileParam'>
  ) {
    const [currentStep] = executeParams.steps;
    try {
      currentStep.isExecuting = true;
      currentStep.executeStatus = ScenarioExecuteStatus.EXECUTING;
      debugSocket(currentStep, scenario.value, executeParams.reportId); // 开启websocket
      const res = await debugScenario({
        id: scenario.value.id || '',
        grouped: false,
        environmentId: currentEnvConfig?.value?.id || '',
        projectId: appStore.currentProjectId,
        scenarioConfig: scenario.value.scenarioConfig,
        frontendDebug: scenario.value.executeType === 'localExec',
        ...executeParams,
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
      const _stepDetails = {};
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
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, request.stepId, 'uniqueId');
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
        uniqueId: request.stepId,
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
          const index = realStep.parent.children.findIndex((item) => item.uniqueId === realStep.uniqueId);
          realStep.parent.children.splice(index, 1, newStep);
        } else {
          // 如果被替换的步骤是第一层级步骤，则直接替换
          const index = steps.value.findIndex((item) => item.uniqueId === realStep.uniqueId);
          steps.value.splice(index, 1, newStep);
        }
      }
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
        handleStepSelect([newStep.uniqueId], newStep);
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
    if (activeStep.value && activeCreateAction.value) {
      handleCreateStep(
        {
          stepType: ScenarioStepType.CUSTOM_REQUEST,
          name: request.name || t('apiScenario.customApi'),
          config: {
            protocol: request.protocol,
            method: request.method,
          },
          id: request.stepId,
          uniqueId: request.stepId,
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
    if (activeStep.value) {
      const _stepType = getStepType(activeStep.value);
      if (_stepType.isQuoteCase || activeStep.value.isQuoteScenarioStep) {
        // 引用的 case 和引用的场景步骤都不可更改
        stepDetails.value[activeStep.value.id] = request; // 为了设置一次正确的polymorphicName
        return;
      }
    }
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
      activeStep.value.config = {
        ...activeStep.value.config,
        method: request.method,
      };
      activeStep.value.name = request.name;
      emit('updateResource', request.uploadFileIds, request.linkFileIds);
      activeStep.value = undefined;
    }
  }

  /**
   * 删除
   */
  function deleteStep(step?: ScenarioStepItem) {
    if (step) {
      openModal({
        type: 'error',
        title: t('common.tip'),
        content: t('apiScenario.deleteStepConfirm', { name: step.name }),
        okText: t('common.confirmDelete'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        maskClosable: false,
        onBeforeOk: async () => {
          customCaseDrawerVisible.value = false;
          customApiDrawerVisible.value = false;
          deleteNode(steps.value, step.uniqueId, 'uniqueId');
          activeStep.value = undefined;
          scenario.value.unSaved = true;
          Message.success(t('common.deleteSuccess'));
        },
        hideCancel: false,
      });
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
          uniqueId: id,
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
    if (activeStep.value) {
      stepDetails.value[activeStep.value.id] = cloneDeep(scriptProcessor);
      activeStep.value.name = name;
      activeStep.value = undefined;
      if (unSaved) {
        scenario.value.unSaved = true;
      }
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
      mapTree(cloneDeep(dragNode.children || []), (e) => {
        offspringIds.push(e.uniqueId);
        return e;
      });
      const stepIdAndOffspringIds = [dragNode.uniqueId, ...offspringIds];
      if (dropPosition === 0) {
        // 拖拽到节点内
        if (selectedKeys.value.includes(dropNode.uniqueId)) {
          // 释放位置的节点已选中，则需要把拖动的节点及其子孙节点也需要选中（因为父级选中子级也会展示选中状态）
          selectedKeys.value = selectedKeys.value.concat(stepIdAndOffspringIds);
        }
      } else if (dropNode.parent && selectedKeys.value.includes(dropNode.parent.uniqueId)) {
        // 释放位置的节点的父节点已选中，则需要把拖动的节点及其子孙节点也需要选中（因为父级选中子级也会展示选中状态）
        selectedKeys.value = selectedKeys.value.concat(stepIdAndOffspringIds);
      } else if (dragNode.parent && selectedKeys.value.includes(dragNode.parent.uniqueId)) {
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
      const dragResult = handleTreeDragDrop(steps.value, dragNode, dropNode, dropPosition, 'uniqueId');
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
