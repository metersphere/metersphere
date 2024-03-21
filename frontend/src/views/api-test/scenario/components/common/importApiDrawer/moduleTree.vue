<template>
  <div>
    <div class="mb-[12px] flex items-center gap-[8px]">
      <a-input v-model:model-value="moduleKeyword" :placeholder="t('apiScenario.quoteTreeSearchTip')" allow-clear />
      <a-tooltip :content="isExpandAll ? t('apiScenario.collapseAll') : t('apiScenario.expandAllStep')">
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
    <a-spin class="w-full" :loading="loading">
      <MsTree
        v-model:selected-keys="selectedKeys"
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
        @select="handleNodeSelect"
      >
        <template #title="nodeData">
          <div class="inline-flex w-full">
            <div class="one-line-text w-[calc(100%-32px)] text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">({{ moduleCountMap[nodeData.id] || 0 }})</div>
          </div>
        </template>
      </MsTree>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { getModuleCount, getModuleTreeOnlyModules } from '@/api/modules/api-test/management';
  import {
    getModuleCount as getScenarioModuleCount,
    getModuleTree as getScenarioModuleTree,
  } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import { mapTree } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';

  const props = withDefaults(
    defineProps<{
      type: 'api' | 'case' | 'scenario';
      protocol: string;
      projectId: string;
    }>(),
    {
      type: 'api',
    }
  );
  const emit = defineEmits<{
    (e: 'select', ids: (string | number)[], node: MsTreeNodeData): void;
  }>();

  const { t } = useI18n();

  const moduleKeyword = ref('');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const loading = ref(false);
  const isExpandAll = ref(false);
  const moduleCountMap = ref<Record<string, number>>({});
  const selectedKeys = ref<string[]>([]);

  /**
   * 初始化模块树
   */
  async function initModules(type = props.type) {
    try {
      loading.value = true;
      const params = {
        keyword: moduleKeyword.value,
        protocol: props.protocol,
        projectId: props.projectId,
        moduleIds: [],
      };
      if (type === 'api' || type === 'case') {
        // case 的模块与 api 的一致
        folderTree.value = await getModuleTreeOnlyModules(params);
      } else if (type === 'scenario') {
        folderTree.value = await getScenarioModuleTree(params);
      }
      selectedKeys.value = [folderTree.value[0]?.id];
      emit('select', [folderTree.value[0]?.id], folderTree.value[0]);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  async function initModuleCount(type = props.type) {
    try {
      const params = {
        keyword: moduleKeyword.value,
        protocol: props.protocol,
        projectId: props.projectId,
        moduleIds: [],
      };
      if (type === 'api' || type === 'case') {
        // case 的模块与 api 的一致
        moduleCountMap.value = await getModuleCount(params);
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

  function init(type = props.type) {
    initModules(type);
    initModuleCount(type);
  }

  defineExpose({
    init,
  });
</script>

<style lang="less" scoped>
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
