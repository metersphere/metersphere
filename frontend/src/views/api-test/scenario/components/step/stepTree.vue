<template>
  <div class="flex flex-col gap-[16px]">
    <MsTree
      ref="treeRef"
      v-model:checked-keys="checkedKeys"
      v-model:focus-node-key="focusStepKey"
      :data="props.steps"
      :node-more-actions="stepMoreActions"
      :field-names="{ title: 'name', key: 'id', children: 'children' }"
      :selectable="false"
      disabled-title-tooltip
      checkable
      block-node
      draggable
    >
      <template #title="step">
        <div class="flex items-center gap-[8px]">
          <div
            class="flex h-[16px] min-w-[16px] items-center justify-center rounded-full bg-[var(--color-text-brand)] px-[2px] !text-white"
          >
            {{ step.order }}
          </div>
          <div class="step-node-first">
            <div
              v-show="step.children?.length > 0"
              class="flex cursor-pointer items-center gap-[2px] text-[var(--color-text-1)]"
              @click.stop="toggleNodeExpand(step)"
            >
              <MsIcon
                :type="step.expanded ? 'icon-icon_split_turn-down_arrow' : 'icon-icon_split-turn-down-left'"
                :size="14"
              />
              {{ step.children?.length || 0 }}
            </div>
            <div class="text-[var(--color-text-1)]">{{ step.name }}</div>
          </div>
        </div>
      </template>
      <template #extra="step">
        <MsButton :id="step.key" type="icon" class="ms-tree-node-extra__btn !mr-[4px]" @click="setFocusNodeKey(step)">
          <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
        </MsButton>
      </template>
      <template #extraEnd="step">
        <executeStatus :status="step.status" size="small" />
      </template>
    </MsTree>
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
  </div>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import executeStatus from '../common/executeStatus.vue';
  import importApiDrawer from '../common/importApiDrawer/index.vue';
  import stepType from '../common/stepType.vue';
  import actionDropdown from './actionDropdown.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { ScenarioAddStepActionType, ScenarioExecuteStatus, ScenarioStepType } from '@/enums/apiEnum';

  export interface ScenarioStepItem {
    id: string | number;
    order: number;
    checked: boolean;
    type: ScenarioStepType;
    name: string;
    description: string;
    status: ScenarioExecuteStatus;
    children?: ScenarioStepItem[];
  }

  const props = defineProps<{
    steps: ScenarioStepItem[];
  }>();

  const { t } = useI18n();

  const checkedKeys = defineModel<string[]>('checkedKeys', {
    required: true,
  });
  const treeRef = ref<InstanceType<typeof MsTree>>();
  const focusStepKey = ref<string>(''); // 聚焦的key
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

  function setFocusNodeKey(node: MsTreeNodeData) {
    focusStepKey.value = node.id || '';
  }

  function toggleNodeExpand(node: MsTreeNodeData) {
    if (node.id) {
      treeRef.value?.expandNode(node.id, !node.expanded);
    }
  }

  function checkAll(val: boolean) {
    treeRef.value?.checkAll(val);
  }

  const importApiDrawerVisible = ref(false);

  function handleActionSelect(val: ScenarioAddStepActionType) {
    switch (val) {
      case ScenarioAddStepActionType.IMPORT_SYSTEM_API:
        importApiDrawerVisible.value = true;
        break;
      case ScenarioAddStepActionType.CUSTOM_API:
        console.log('自定义API');
        break;
      case ScenarioAddStepActionType.LOOP_CONTROL:
        console.log('循环控制');
        break;
      case ScenarioAddStepActionType.CONDITION_CONTROL:
        console.log('条件控制');
        break;
      case ScenarioAddStepActionType.ONLY_ONCE_CONTROL:
        console.log('仅执行一次');
        break;
      case ScenarioAddStepActionType.SCRIPT_OPERATION:
        console.log('脚本操作');
        break;
      case ScenarioAddStepActionType.WAIT_TIME:
        console.log('等待时间');
        break;
      default:
        break;
    }
  }

  defineExpose({
    checkAll,
  });
</script>

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
  // 循环生成树的左边距样式
  .loop-levels(@index, @max) when (@index <= @max) {
    :deep(.arco-tree-node[data-level='@{index}']) {
      margin-left: @index * 32px;
    }
    .loop-levels(@index + 1, @max); // 下个层级
  }
  .loop-levels(0, 99); // 最大层级
  :deep(.arco-tree-node) {
    padding: 7px 8px;
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
      &:hover {
        background-color: var(--color-text-n9) !important;
      }
      .step-node-first {
        @apply flex items-center;

        gap: 8px;
      }
      &[draggable='true']:hover {
        .step-node-first {
          padding-left: 20px;
        }
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

      top: 6px;
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
