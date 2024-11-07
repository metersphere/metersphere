<template>
  <div>
    <div class="mb-[8px] flex items-center gap-[8px]">
      <a-input v-model:model-value="moduleKeyword" :placeholder="t('apiScenario.quoteTreeSearchTip')" allow-clear />
      <a-tooltip :content="isExpandAll ? t('common.collapseAllSubModule') : t('common.expandAllSubModule')">
        <a-button
          type="outline"
          class="expand-btn arco-btn-outline--secondary"
          @click="() => (isExpandAll = !isExpandAll)"
        >
          <MsIcon v-if="isExpandAll" type="icon-icon_comment_collapse_text_input" />
          <MsIcon v-else type="icon-icon_comment_expand_text_input" />
        </a-button>
      </a-tooltip>
    </div>
    <div class="folder" @click="setActiveFolder('all')">
      <div :class="allFolderClass">
        <slot>
          <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
        </slot>
        <div class="folder-name">{{ folderText }}</div>
        <div class="folder-count">({{ allScenarioCount }})</div>
      </div>
    </div>
    <a-spin class="w-full" :loading="loading">
      <MsTree
        v-model:selected-keys="selectedKeys"
        v-model:checked-keys="checkedKeys"
        v-model:halfCheckedKeys="halfCheckedKeys"
        :data="folderTree"
        :keyword="moduleKeyword"
        :default-expand-all="isExpandAll"
        :expand-all="isExpandAll"
        :empty-text="t('apiScenario.quoteTreeNoData')"
        :virtual-list-props="{
          height: 'calc(100vh - 293px)',
          threshold: 200,
          fixedSize: true,
          buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
        }"
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
          count: 'count',
        }"
        block-node
        title-tooltip-position="left"
        :checkable="!props.singleSelect"
        check-strictly
        @select="handleNodeSelect"
        @check="checkNode"
      >
        <template #title="nodeData">
          <div class="inline-flex w-full gap-[8px]">
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            <div class="ms-tree-node-count ml-[4px] text-[var(--color-text-4)]">
              {{ nodeData.count || 0 }}/{{ nodeData.totalCount }}
            </div>
          </div>
        </template>
        <template #extra="nodeData">
          <MsButton
            v-if="nodeData.children && nodeData.children.length && !props.singleSelect"
            @click="selectCurrent(nodeData, !!checkedKeys.includes(nodeData.id))"
          >
            {{
              checkedKeys.includes(nodeData.id)
                ? t('ms.case.associate.cancelCurrent')
                : t('ms.case.associate.selectCurrent')
            }}
          </MsButton>
        </template>
      </MsTree>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import type { moduleKeysType } from '@/components/business/ms-associate-case/types';
  import useCalculateTreeCount from '@/components/business/ms-associate-case/useCalculateTreeCount';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { getModuleCount, getModuleTreeOnlyModules } from '@/api/modules/api-test/management';
  import {
    getModuleCount as getScenarioModuleCount,
    getModuleTree as getScenarioModuleTree,
  } from '@/api/modules/api-test/scenario';
  import { testPlanAssociateModuleCount } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { mapTree } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';

  const props = withDefaults(
    defineProps<{
      type: 'api' | 'case' | 'scenario';
      projectId: string;
      singleSelect?: boolean;
    }>(),
    {
      type: 'api',
    }
  );
  const emit = defineEmits<{
    (e: 'select', ids: (string | number)[], node: MsTreeNodeData): void;
    (e: 'init', tree: ModuleTreeNode[], moduleCount: Record<string, any>): void;
    (e: 'updateSelectedModules', val: Record<string, Record<string, moduleKeysType>>): void;
    (e: 'check', _checkedKeys: Array<string | number>, checkedNodes: MsTreeNodeData): void;
    (e: 'selectParent', node: MsTreeNodeData, isSelected: boolean): void;
    (e: 'checkAllModule', isCheckedAll: boolean): void;
  }>();

  const { t } = useI18n();

  const moduleKeyword = ref('');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const loading = ref(false);
  const isExpandAll = ref(false);
  const moduleCountMap = ref<Record<string, number>>({});
  const selectedKeys = ref<string[]>([]);

  const checkedKeys = defineModel<(string | number)[]>('checkedKeys', {
    required: true,
  });
  const halfCheckedKeys = defineModel<(string | number)[]>('halfCheckedKeys', {
    required: true,
  });
  const folderText = computed(() => {
    if (props.type === 'api' || props.type === 'case') {
      return t('apiTestManagement.allApi');
    }
    if (props.type === 'scenario') {
      return t('apiScenario.tree.folder.allScenario');
    }
  });
  const allScenarioCount = computed(() => moduleCountMap.value.all || 0);
  const allFolderClass = computed(() =>
    selectedKeys.value[0] === 'all' ? 'folder-text folder-text--active' : 'folder-text'
  );

  function setActiveFolder(id: string) {
    selectedKeys.value = [id];
    emit('select', [], { id, name: folderText.value });
  }
  const { calculateTreeCount } = useCalculateTreeCount();

  /**
   * 初始化模块树
   */
  async function initModules(protocolsParam: string[], type = props.type) {
    try {
      loading.value = true;
      const params = {
        keyword: moduleKeyword.value,
        protocols: protocolsParam,
        projectId: props.projectId,
        moduleIds: [],
      };
      let result: ModuleTreeNode[] = [];
      if (type === 'api' || type === 'case') {
        // case 的模块与 api 的一致
        result = await getModuleTreeOnlyModules(params);
      } else if (type === 'scenario') {
        result = await getScenarioModuleTree(params);
      }
      folderTree.value = calculateTreeCount(result, moduleCountMap.value);
      emit('init', folderTree.value, moduleCountMap.value);
      setActiveFolder('all');
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  async function initModuleCount(protocolsParam: string[], type = props.type) {
    try {
      const params = {
        keyword: moduleKeyword.value,
        protocols: protocolsParam,
        projectId: props.projectId,
        moduleIds: [],
      };
      if (type === 'api') {
        moduleCountMap.value = await getModuleCount(params);
      } else if (type === 'case') {
        moduleCountMap.value = await testPlanAssociateModuleCount(params);
      } else if (type === 'scenario') {
        moduleCountMap.value = await getScenarioModuleCount(params);
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function handleNodeSelect(keys: (string | number)[], node: MsTreeNodeData) {
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });
    emit('select', [keys[0], ...offspringIds], node);
  }

  async function init(protocolsParam: string[], type = props.type) {
    await initModuleCount(protocolsParam, type);
    initModules(protocolsParam, type);
  }

  function checkNode(_checkedKeys: Array<string | number>, checkedNodes: MsTreeNodeData) {
    emit('check', _checkedKeys, checkedNodes);
  }

  function selectCurrent(node: MsTreeNodeData, isSelected: boolean) {
    emit('selectParent', node, isSelected);
  }

  defineExpose({
    init,
  });
</script>

<style lang="less" scoped>
  .folder {
    @apply flex cursor-pointer items-center justify-between;

    padding: 8px 4px 8px 0;
    border-radius: var(--border-radius-small);
    &:hover {
      background-color: rgb(var(--primary-1));
    }
    .folder-text {
      @apply flex flex-1 cursor-pointer items-center;
      .folder-icon {
        margin-right: 4px;
        color: var(--color-text-4);
      }
      .folder-name {
        color: var(--color-text-1);
      }
      .folder-count {
        margin-left: 4px;
        color: var(--color-text-4);
      }
    }
    .folder-text--active {
      .folder-icon,
      .folder-name,
      .folder-count {
        color: rgb(var(--primary-5));
      }
    }
  }
  .expand-btn {
    padding: 8px;
    .arco-icon {
      color: var(--color-text-4);
    }
    &:hover {
      border-color: rgb(var(--primary-5)) !important;
      background-color: rgb(var(--primary-1)) !important;
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
</style>
