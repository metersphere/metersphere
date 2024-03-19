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
        :field-names="{ title: 'name', key: 'id', children: 'children' }"
        :virtual-list-props="{
          height: '100%',
          threshold: 20,
          fixedSize: true,
          buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
        }"
        title-class="step-tree-node-title"
        node-highlight-background-color="var(--color-text-n9)"
        action-on-node-click="expand"
        disabled-title-tooltip
        checkable
        block-node
        draggable
        @select="handleStepSelect"
        @expand="handleStepExpand"
        @more-actions-close="() => setFocusNodeKey('')"
        @more-action-select="handleStepMoreActionSelect"
      >
        <template #title="step">
          <div class="flex w-full items-center gap-[8px]">
            <!-- 步骤序号 -->
            <div
              class="flex h-[16px] min-w-[16px] items-center justify-center rounded-full bg-[var(--color-text-brand)] px-[2px] !text-white"
            >
              {{ step.order }}
            </div>
            <div class="step-node-content">
              <!-- 步骤展开折叠按钮 -->
              <a-tooltip
                v-if="step.children?.length > 0"
                :content="t('apiScenario.expandStepTip', { count: step.children.length })"
              >
                <div class="flex cursor-pointer items-center gap-[2px] text-[var(--color-text-1)]">
                  <MsIcon
                    :type="step.expanded ? 'icon-icon_split_turn-down_arrow' : 'icon-icon_split-turn-down-left'"
                    :size="14"
                  />
                  {{ step.children?.length || 0 }}
                </div>
              </a-tooltip>
              <div class="mr-[8px] flex items-center gap-[8px]">
                <!-- 步骤启用/禁用 -->
                <a-switch
                  :default-checked="step.enabled"
                  size="small"
                  @click.stop="step.enabled = !step.enabled"
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
              <stepType :type="step.type" />
              <!-- 步骤整体内容 -->
              <div class="relative flex flex-1 items-center gap-[4px]">
                <!-- 步骤差异内容，按步骤类型展示不同组件 -->
                <component :is="getStepContent(step)" :data="getStepContentData(step)" />
                <!-- API、CASE、场景步骤名称 -->
                <template v-if="checkStepIsApi(step)">
                  <apiMethodName v-if="checkStepShowMethod(step)" :method="step.method" />
                  <div
                    v-if="step.id === showStepNameEditInputStepId"
                    class="absolute left-0 top-[-2px] z-10 w-[calc(100%-24px)]"
                    @click.stop
                  >
                    <a-input
                      :id="step.id"
                      v-model:model-value="tempStepName"
                      :placeholder="t('apiScenario.pleaseInputStepName')"
                      :max-length="255"
                      size="small"
                      @press-enter="applyStepChange(step)"
                      @blur="applyStepChange(step)"
                    />
                  </div>
                  <a-tooltip :content="step.name">
                    <div class="step-name-container">
                      <div class="one-line-text mr-[4px] max-w-[250px] font-medium text-[var(--color-text-1)]">
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
              </div>
            </div>
          </div>
        </template>
        <template #extra="step">
          <a-trigger
            trigger="click"
            class="arco-trigger-menu absolute"
            content-class="w-[160px]"
            position="br"
            @popup-visible-change="handleActionTriggerChange($event, step)"
          >
            <MsButton
              :id="step.id"
              type="icon"
              class="ms-tree-node-extra__btn !mr-[4px]"
              @click="setFocusNodeKey(step.id)"
            >
              <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
            </MsButton>
            <template #content>
              <actionDropdown
                v-model:visible="step.actionDropdownVisible"
                position="br"
                class="scenario-action-dropdown"
                :popup-translate="[-7, -10]"
                @select="(val) => handleActionSelect(val as ScenarioAddStepActionType, step)"
              >
                <span></span>
              </actionDropdown>
              <div class="arco-trigger-menu-inner">
                <div
                  :class="[
                    'arco-trigger-menu-item !mx-0 !w-full',
                    activeAction === 'addChildStep' ? 'step-tree-active-action' : '',
                  ]"
                  @click="handleTriggerActionClick(step, 'addChildStep')"
                >
                  <icon-plus size="12" />
                  {{ t('apiScenario.addChildStep') }}
                </div>
                <div
                  :class="[
                    'arco-trigger-menu-item !mx-0 !w-full',
                    activeAction === 'insertBefore' ? 'step-tree-active-action' : '',
                  ]"
                  @click="handleTriggerActionClick(step, 'insertBefore')"
                >
                  <icon-left size="12" />
                  {{ t('apiScenario.insertBefore') }}
                </div>
                <div
                  :class="[
                    'arco-trigger-menu-item !mx-0 !w-full',
                    activeAction === 'insertAfter' ? 'step-tree-active-action' : '',
                  ]"
                  @click="handleTriggerActionClick(step, 'insertAfter')"
                >
                  <icon-left size="12" />
                  {{ t('apiScenario.insertAfter') }}
                </div>
              </div>
            </template>
          </a-trigger>
        </template>
        <template #extraEnd="step">
          <executeStatus v-if="step.status" :status="step.status" size="small" />
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
    <actionDropdown
      class="scenario-action-dropdown"
      @select="(val) => handleActionSelect(val as ScenarioAddStepActionType)"
    >
      <a-button type="dashed" class="add-step-btn" long>
        <div class="flex items-center gap-[8px]">
          <icon-plus />
          {{ t('apiScenario.addStep') }}
        </div>
      </a-button>
    </actionDropdown>
    <importApiDrawer v-model:visible="importApiDrawerVisible" />
    <!-- todo  执行、上传文件、转存文件等需要传入相关方法； 当前场景环境使用的是假数据； add-step暂时只是将数据传递到当前组件的customDemoStep对象中，用于再次打开的时候测试编辑功能  -->
    <customApiDrawer
      v-model:visible="customApiDrawerVisible"
      :env-detail-item="{ id: 'demp-id-112233', projectId: '123456', name: 'demo环境' }"
      :request="customDemoStep"
      @add-step="addCustomApiStep"
    />
    <scriptOperationDrawer v-model:visible="scriptOperationDrawerVisible" />
  </div>
</template>

<script setup lang="ts">
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import { MsTreeExpandedData, MsTreeNodeData } from '@/components/business/ms-tree/types';
  import customApiDrawer from '../common/customApiDrawer.vue';
  import executeStatus from '../common/executeStatus.vue';
  import importApiDrawer from '../common/importApiDrawer/index.vue';
  import scriptOperationDrawer from '../common/scriptOperationDrawer.vue';
  import stepType from '../common/stepType.vue';
  import actionDropdown from './actionDropdown.vue';
  import conditionContent from './stepNodeComposition/conditionContent.vue';
  import customApiContent from './stepNodeComposition/customApiContent.vue';
  import loopControlContent from './stepNodeComposition/loopContent.vue';
  import onlyOnceControlContent from './stepNodeComposition/onlyOnceContent.vue';
  import quoteContent from './stepNodeComposition/quoteContent.vue';
  import waitTimeContent from './stepNodeComposition/waitTimeContent.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { deleteNode, findNodeByKey, getGenerateId, insertNode, mapTree } from '@/utils';

  import { CustomApiStep } from '@/models/apiTest/scenario';
  import { RequestMethods, ScenarioAddStepActionType, ScenarioExecuteStatus, ScenarioStepType } from '@/enums/apiEnum';

  import { defaultStepItemCommon } from '../config';

  export interface ScenarioStepItem {
    id: string | number;
    order: number;
    enabled: boolean; // 是否启用
    type: ScenarioStepType;
    name: string;
    description: string;
    method?: RequestMethods;
    status?: ScenarioExecuteStatus;
    num?: number; // 详情或者引用的类型才有
    // 引用类型专有字段
    belongProjectId?: string;
    belongProjectName?: string;
    children?: ScenarioStepItem[];
    // 页面渲染以及交互需要字段
    // renderId: string; // 渲染id
    checked: boolean; // 是否选中
    expanded: boolean; // 是否展开
    actionDropdownVisible?: boolean; // 是否展示操作下拉
  }

  const props = defineProps<{
    stepKeyword: string;
    expandAll?: boolean;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const steps = defineModel<ScenarioStepItem[]>('steps', {
    required: true,
  });
  const checkedKeys = defineModel<string[]>('checkedKeys', {
    required: true,
  });

  const selectedKeys = ref<string[]>([]); // 没啥用，目前用来展示选中样式
  const loading = ref(false);
  const treeRef = ref<InstanceType<typeof MsTree>>();
  const focusStepKey = ref<string>(''); // 聚焦的key

  function getStepContent(step: ScenarioStepItem) {
    switch (step.type) {
      case ScenarioStepType.QUOTE_API:
      case ScenarioStepType.QUOTE_CASE:
      case ScenarioStepType.QUOTE_SCENARIO:
        return quoteContent;
      case ScenarioStepType.CUSTOM_API:
        return customApiContent;
      case ScenarioStepType.LOOP_CONTROL:
        return loopControlContent;
      case ScenarioStepType.CONDITION_CONTROL:
        return conditionContent;
      case ScenarioStepType.ONLY_ONCE_CONTROL:
        return onlyOnceControlContent;
      case ScenarioStepType.WAIT_TIME:
        return waitTimeContent;
      default:
        return '';
    }
  }

  function getStepContentData(step: ScenarioStepItem) {
    switch (step.type) {
      case ScenarioStepType.QUOTE_API:
      case ScenarioStepType.QUOTE_CASE:
      case ScenarioStepType.QUOTE_SCENARIO:
        return {
          id: step.id,
          belongProjectId: step.belongProjectId,
          belongProjectName: step.belongProjectName,
          num: step.num,
          name: step.name,
          type: step.type,
          currentProjectId: appStore.currentProjectId,
        };
      case ScenarioStepType.CUSTOM_API:
        return {};
      case ScenarioStepType.LOOP_CONTROL:
        return {};
      case ScenarioStepType.CONDITION_CONTROL:
        return {};
      case ScenarioStepType.ONLY_ONCE_CONTROL:
        return {};
      case ScenarioStepType.WAIT_TIME:
        return {};
      default:
        return '';
    }
  }

  function setFocusNodeKey(id: string) {
    focusStepKey.value = id || '';
  }

  function checkStepIsApi(step: ScenarioStepItem) {
    return [
      ScenarioStepType.QUOTE_API,
      ScenarioStepType.COPY_API,
      ScenarioStepType.QUOTE_CASE,
      ScenarioStepType.COPY_CASE,
      ScenarioStepType.CUSTOM_API,
    ].includes(step.type);
  }

  function checkStepShowMethod(step: ScenarioStepItem) {
    return [
      ScenarioStepType.QUOTE_API,
      ScenarioStepType.COPY_API,
      ScenarioStepType.QUOTE_CASE,
      ScenarioStepType.COPY_CASE,
      ScenarioStepType.CUSTOM_API,
      ScenarioStepType.QUOTE_SCENARIO,
      ScenarioStepType.COPY_SCENARIO,
    ].includes(step.type);
  }

  const activeAction = ref('');
  function handleTriggerActionClick(step: ScenarioStepItem, action: string) {
    step.actionDropdownVisible = true;
    activeAction.value = action;
  }

  function handleActionTriggerChange(val: boolean, step: ScenarioStepItem) {
    if (!val) {
      activeAction.value = '';
      step.actionDropdownVisible = false;
      setFocusNodeKey('');
    }
  }

  const stepMoreActions: ActionsItem[] = [
    {
      label: 'common.execute',
      eventTag: 'execute',
    },
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
  function handleStepMoreActionSelect(item: ActionsItem, node: MsTreeNodeData) {
    switch (item.eventTag) {
      case 'execute':
        console.log('执行步骤', node);
        break;
      case 'copy':
        const id = getGenerateId();
        insertNode<ScenarioStepItem>(
          steps.value,
          node.id,
          {
            ...cloneDeep(
              mapTree<ScenarioStepItem>(node, (childNode) => {
                const childId = getGenerateId();
                if (selectedKeys.value.includes(node.id)) {
                  // 复制一个已选中的节点，需要把新节点也需要选中（因为父级选中子级也会展示选中状态）
                  selectedKeys.value.push(childId);
                }
                return {
                  ...childNode,
                  id: childId,
                };
              })[0]
            ),
            name: `copy-${node.name}`,
            id,
          },
          'after',
          'id'
        );
        if (selectedKeys.value.includes(node.id)) {
          // 复制一个已选中的节点，需要把新节点也需要选中（因为父级选中子级也会展示选中状态）
          selectedKeys.value.push(id);
        }
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
      const input = treeRef.value?.$el.querySelector('.arco-input') as HTMLInputElement;
      input?.focus();
    });
  }

  function applyStepChange(step: ScenarioStepItem) {
    const realStep = findNodeByKey<ScenarioStepItem>(steps.value, step.id, 'id');
    if (realStep) {
      realStep.name = tempStepName.value;
    }
    showStepNameEditInputStepId.value = '';
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

  function handleStepSelect(_selectedKeys: Array<string | number>, node: MsTreeNodeData) {
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });
    selectedKeys.value = [node.id, ...offspringIds];
  }

  function executeStep(node: MsTreeNodeData) {
    console.log('执行步骤', node);
  }

  const importApiDrawerVisible = ref(false);
  const customApiDrawerVisible = ref(false);
  const scriptOperationDrawerVisible = ref(false);

  function handleActionSelect(val: ScenarioAddStepActionType, step?: ScenarioStepItem) {
    switch (val) {
      case ScenarioAddStepActionType.IMPORT_SYSTEM_API:
        importApiDrawerVisible.value = true;
        break;
      case ScenarioAddStepActionType.CUSTOM_API:
        customApiDrawerVisible.value = true;
        break;
      case ScenarioAddStepActionType.LOOP_CONTROL:
        steps.value.push({
          ...defaultStepItemCommon,
          id: Date.now(),
          order: steps.value.length + 1,
          type: ScenarioStepType.LOOP_CONTROL,
          name: '循环控制',
          description: '循环控制描述',
        });
        break;
      case ScenarioAddStepActionType.CONDITION_CONTROL:
        steps.value.push({
          ...defaultStepItemCommon,
          id: Date.now(),
          order: steps.value.length + 1,
          type: ScenarioStepType.CONDITION_CONTROL,
          name: '条件控制',
          description: '条件控制描述',
        });
        break;
      case ScenarioAddStepActionType.ONLY_ONCE_CONTROL:
        steps.value.push({
          ...defaultStepItemCommon,
          id: Date.now(),
          order: steps.value.length + 1,
          type: ScenarioStepType.ONLY_ONCE_CONTROL,
          name: '仅执行一次',
          description: '仅执行一次描述',
        });
        break;
      case ScenarioAddStepActionType.SCRIPT_OPERATION:
        scriptOperationDrawerVisible.value = true;
        break;
      case ScenarioAddStepActionType.WAIT_TIME:
        steps.value.push({
          ...defaultStepItemCommon,
          id: Date.now(),
          order: steps.value.length + 1,
          type: ScenarioStepType.WAIT_TIME,
          name: '等待时间',
          description: '等待时间描述',
        });
        break;
      default:
        break;
    }
    if (step) {
      document.getElementById(step.id.toString())?.click();
    }
  }

  const customDemoStep = ref<CustomApiStep>();

  function addCustomApiStep(step: CustomApiStep) {
    // todo: 添加自定义api步骤
    customDemoStep.value = { ...step };
  }

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
      @apply !cursor-pointer;

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
      &[draggable='true']:hover {
        .step-node-content {
          padding-left: 20px;
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
      @apply ml-0;

      top: 13px;
      left: 24px;
      width: 16px;
      height: 16px;
      .arco-icon {
        font-size: 16px !important;
      }
    }
    .ms-tree-node-extra {
      gap: 4px;
      background-color: var(--color-text-n9) !important;
    }
  }
</style>
