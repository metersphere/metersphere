<template>
  <div class="flex h-full flex-col gap-[16px]">
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
                <!-- 步骤启用/禁用 -->
                <a-switch
                  :default-checked="step.enable"
                  size="small"
                  @click.stop="handleStepToggleEnable(step)"
                ></a-switch>
                <!-- 步骤执行 -->
                <MsIcon
                  type="icon-icon_play-round_filled"
                  :size="18"
                  class="cursor-pointer text-[rgb(var(--link-6))]"
                  @click.stop="executeStep(step)"
                />
              </div>
              <!-- 步骤类型 -->
              <stepType :step="step" />
              <!-- 步骤整体内容 -->
              <div class="relative flex flex-1 items-center gap-[4px]">
                <!-- 步骤差异内容，按步骤类型展示不同组件 -->
                <component
                  :is="getStepContent(step)"
                  :data="step.config"
                  :step-id="step.id"
                  @quick-input="setQuickInput(step, $event)"
                  @change="handleStepContentChange($event, step)"
                  @click.stop
                />
                <!-- API、CASE、场景步骤名称 -->
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
                      <div class="one-line-text mr-[4px] max-w-[150px] font-medium text-[var(--color-text-1)]">
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
          />
        </template>
        <template #extraEnd="step">
          <executeStatus v-if="step.executeStatus" :status="step.executeStatus" size="small" />
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
    <createStepActions v-model:selected-keys="selectedKeys" v-model:steps="steps" @other-create="handleOtherCreate">
      <a-button type="dashed" class="add-step-btn" long>
        <div class="flex items-center gap-[8px]">
          <icon-plus />
          {{ t('apiScenario.addStep') }}
        </div>
      </a-button>
    </createStepActions>
    <customApiDrawer
      v-model:visible="customApiDrawerVisible"
      :env-detail-item="{ id: 'demp-id-112233', projectId: '123456', name: 'demo环境' }"
      :request="currentStepDetail"
      :step="activeStep"
      @add-step="addCustomApiStep"
      @apply-step="applyApiStep"
    />
    <customCaseDrawer
      v-model:visible="customCaseDrawerVisible"
      :active-step="activeStep"
      :request="currentStepDetail"
      @apply-step="applyApiStep"
      @delete-step="deleteCaseStep"
    />
    <importApiDrawer
      v-if="importApiDrawerVisible"
      v-model:visible="importApiDrawerVisible"
      @copy="handleImportApiApply('copy', $event)"
      @quote="handleImportApiApply('quote', $event)"
    />
    <scriptOperationDrawer
      v-if="scriptOperationDrawerVisible"
      v-model:visible="scriptOperationDrawerVisible"
      :script="currentStepDetail"
      :name="activeStep?.name"
      @save="addScriptStep"
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
  import customApiContent from './stepNodeComposition/customApiContent.vue';
  import loopControlContent from './stepNodeComposition/loopContent.vue';
  import quoteContent from './stepNodeComposition/quoteContent.vue';
  import waitTimeContent from './stepNodeComposition/waitTimeContent.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import { RequestParam as CaseRequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import { getScenarioStep } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import {
    deleteNode,
    findNodeByKey,
    getGenerateId,
    handleTreeDragDrop,
    insertNodes,
    mapTree,
    TreeNode,
  } from '@/utils';

  import { ExecuteConditionProcessor } from '@/models/apiTest/common';
  import { CreateStepAction, ScenarioStepItem } from '@/models/apiTest/scenario';
  import { ScenarioAddStepActionType, ScenarioStepRefType, ScenarioStepType } from '@/enums/apiEnum';

  import type { RequestParam } from '../common/customApiDrawer.vue';
  import useCreateActions from './createAction/useCreateActions';
  import getStepType from '@/views/api-test/scenario/components/common/stepType/utils';
  import { defaultStepItemCommon } from '@/views/api-test/scenario/components/config';

  // 非首屏渲染必要组件，异步加载
  const MsCodeEditor = defineAsyncComponent(() => import('@/components/pure/ms-code-editor/index.vue'));
  const customApiDrawer = defineAsyncComponent(() => import('../common/customApiDrawer.vue'));
  const customCaseDrawer = defineAsyncComponent(() => import('../common/customCaseDrawer.vue'));
  const importApiDrawer = defineAsyncComponent(() => import('../common/importApiDrawer/index.vue'));
  const scriptOperationDrawer = defineAsyncComponent(() => import('../common/scriptOperationDrawer.vue'));

  const props = defineProps<{
    stepKeyword: string;
    expandAll?: boolean;
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
  const stepDetails = defineModel<Record<string, any>>('stepDetails', {
    required: true,
  });

  const selectedKeys = ref<(string | number)[]>([]); // 没啥用，目前用来展示选中样式
  const loading = ref(false);
  const treeRef = ref<InstanceType<typeof MsTree>>();
  const focusStepKey = ref<string>(''); // 聚焦的key

  /**
   * 根据步骤类型获取步骤内容组件
   */
  function getStepContent(step: ScenarioStepItem) {
    const _stepType = getStepType(step);
    if (_stepType.isQuoteApi || _stepType.isQuoteCase || _stepType.isQuoteScenario) {
      return quoteContent;
    }
    switch (step.stepType) {
      case ScenarioStepType.CUSTOM_REQUEST:
        return customApiContent;
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

  function setFocusNodeKey(id: string) {
    focusStepKey.value = id || '';
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
  function checkedIfNeed(step: TreeNode<ScenarioStepItem>, parent?: TreeNode<ScenarioStepItem>) {
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

  function handleStepMoreActionSelect(item: ActionsItem, node: MsTreeNodeData) {
    switch (item.eventTag) {
      case 'copy':
        const id = getGenerateId();
        insertNodes<ScenarioStepItem>(
          steps.value,
          node.id,
          {
            ...cloneDeep(
              mapTree<ScenarioStepItem>(node, (childNode) => {
                return {
                  ...childNode,
                  id: getGenerateId(), // TODO:引用类型额外需要一个复制来源 ID
                };
              })[0]
            ),
            name: `copy-${node.name}`,
            sort: node.sort + 1,
            id,
          },
          'after',
          checkedIfNeed,
          'id'
        );
        break;
      case 'config':
        console.log('config', node);
        break;
      case 'delete':
        deleteNode(steps.value, node.id, 'id');
        break;
      default:
        break;
    }
  }

  function checkAll(val: boolean) {
    treeRef.value?.checkAll(val);
  }

  /**
   * 处理步骤名称编辑
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
  }

  /**
   * 处理步骤名称编辑
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
  }

  function handleStepContentChange($event, step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.id, 'id');
    if (realStep) {
      Object.keys($event).forEach((key) => {
        realStep.config[key] = $event[key];
      });
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
    }
  }

  const importApiDrawerVisible = ref(false);
  const customCaseDrawerVisible = ref(false);
  const customApiDrawerVisible = ref(false);
  const scriptOperationDrawerVisible = ref(false);
  const activeStep = ref<ScenarioStepItem>(); // 用于抽屉操作创建步骤时记录当前操作的步骤节点
  const activeCreateAction = ref<CreateStepAction>(); // 用于抽屉操作创建步骤时记录当前插入类型
  const currentStepDetail = computed<any>(() => {
    // TODO: 步骤详情类型
    if (activeStep.value) {
      return stepDetails.value[activeStep.value.id];
    }
    return undefined;
  });

  async function getStepDetail(step: ScenarioStepItem) {
    try {
      appStore.showLoading();
      const res = await getScenarioStep(step.id);
      stepDetails.value[step.id] = {
        ...res,
        protocol: step.config.protocol,
        method: step.config.method,
      };
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
      if (stepDetails.value[step.id] === undefined) {
        // 详情映射中没有加载过该 api 详情，说明是初次查看详情，引用的 api 不需要在这里加载详情
        await getStepDetail(step);
      }
      customApiDrawerVisible.value = true;
    } else if (step.stepType === ScenarioStepType.API_CASE) {
      activeStep.value = step;
      customCaseDrawerVisible.value = true;
    } else if (step.stepType === ScenarioStepType.SCRIPT) {
      activeStep.value = step;
      scriptOperationDrawerVisible.value = true;
    }
  }

  function executeStep(node: MsTreeNodeData) {
    console.log('执行步骤', node);
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
  }

  /**
   * 添加自定义 API 步骤
   */
  function addCustomApiStep(request: RequestParam) {
    request.isNew = false;
    stepDetails.value[request.id] = request;
    if (activeStep.value && activeCreateAction.value) {
      handleCreateStep(
        {
          stepType: ScenarioStepType.CUSTOM_REQUEST,
          name: t('apiScenario.customApi'),
          method: request.method,
          id: request.id,
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
          customizeRequest: true,
          customizeRequestEnvEnable: request.customizeRequestEnvEnable,
          protocol: request.protocol,
          method: request.method,
        },
        id: request.id,
        sort: steps.value.length + 1,
        stepType: ScenarioStepType.CUSTOM_REQUEST,
        refType: ScenarioStepRefType.DIRECT,
        name: t('apiScenario.customApi'),
        projectId: appStore.currentProjectId,
      });
    }
  }

  /**
   * API 详情抽屉关闭时应用更改
   */
  function applyApiStep(request: RequestParam | CaseRequestParam) {
    if (activeStep.value) {
      request.isNew = false;
      stepDetails.value[activeStep.value?.id] = request;
      activeStep.value = undefined;
    }
  }

  /**
   * 删除
   */
  function deleteCaseStep() {
    if (activeStep.value) {
      customCaseDrawerVisible.value = false;
      steps.value = steps.value.filter((item) => item.id !== activeStep.value?.id);
      delete stepDetails.value[activeStep.value?.id];
      activeStep.value = undefined;
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
  }

  /**
   * 释放允许拖拽步骤到释放的节点内
   * @param dropNode 释放节点
   */
  function isAllowDropInside(dropNode: MsTreeNodeData) {
    return [
      ScenarioStepType.LOOP_CONTROLLER,
      ScenarioStepType.IF_CONTROLLER,
      ScenarioStepType.ONCE_ONLY_CONTROLLER,
    ].includes(dropNode.stepType);
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

      padding: 12px 4px;
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
</style>
