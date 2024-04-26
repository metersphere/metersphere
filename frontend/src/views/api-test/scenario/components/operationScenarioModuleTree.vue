<template>
  <div>
    <div class="mb-[8px] flex items-center gap-[8px]">
      <a-input
        v-model:model-value="moduleKeyword"
        :placeholder="t('apiScenario.tree.selectorPlaceholder')"
        allow-clear
      />
    </div>
    <a-spin class="w-full" :loading="loading">
      <MsTree
        v-model:focus-node-key="focusNodeKey"
        v-model:selected-keys="selectedKeys"
        :data="folderTree"
        :keyword="moduleKeyword"
        :default-expand-all="isExpandAll"
        :expand-all="isExpandAll"
        :empty-text="t('apiScenario.tree.noMatchModule')"
        :virtual-list-props="virtualListProps"
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
          count: 'count',
        }"
        block-node
        title-tooltip-position="left"
        @select="folderNodeSelect"
      >
        <template #title="nodeData">
          <div :id="nodeData.id" class="inline-flex w-full gap-[8px]">
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            <div class="ms-tree-node-count ml-[4px] text-[var(--color-text-brand)]">{{ nodeData.count || 0 }}</div>
          </div>
        </template>
      </MsTree>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';

  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { getModuleCount, getModuleTree } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { mapTree } from '@/utils';

  import { ApiScenarioGetModuleParams } from '@/models/apiTest/scenario';
  import { ModuleTreeNode } from '@/models/common';

  const props = withDefaults(
    defineProps<{
      isExpandAll?: boolean; // 是否展开所有节点
    }>(),
    {}
  );
  const emit = defineEmits(['init', 'newScenario', 'import', 'folderNodeSelect', 'clickScenario', 'changeProtocol']);

  const appStore = useAppStore();
  const { t } = useI18n();

  const virtualListProps = computed(() => {
    return {
      height: '40vh',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });

  const moduleKeyword = ref('');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const focusNodeKey = ref<string | number>('');
  const selectedKeys = ref<Array<string | number>>([]);
  const loading = ref(false);

  const modulesCount = ref<Record<string, number>>({});
  const isExpandAll = ref(props.isExpandAll);

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await getModuleTree({
        keyword: moduleKeyword.value,
        projectId: appStore.currentProjectId,
        moduleIds: [],
      });
      const nodePathObj: Record<string, any> = {};
      folderTree.value = mapTree<ModuleTreeNode>(res, (e, fullPath) => {
        // 拼接当前节点的完整路径
        nodePathObj[e.id] = {
          path: e.path,
          fullPath,
        };
        return {
          ...e,
          hideMoreAction: e.id === 'root',
        };
      });
      if (isSetDefaultKey) {
        selectedKeys.value = [folderTree.value[0].id];
      }
      emit('init', folderTree.value, nodePathObj);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => props.isExpandAll,
    (val) => {
      isExpandAll.value = val;
    }
  );

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(_selectedKeys: (string | number)[], node: MsTreeNodeData) {
    emit('folderNodeSelect', node);
  }

  async function initModuleCount(params: ApiScenarioGetModuleParams) {
    try {
      const res = await getModuleCount(params);
      modulesCount.value = res;
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: res[node.id] || 0,
          disabled: false,
        };
      });
    } catch (error) {
      console.log(error);
    }
  }
  onBeforeMount(async () => {
    await initModules();
    await initModuleCount({
      projectId: appStore.currentProjectId,
    });
  });
</script>

<style lang="less" scoped>
  .folder {
    @apply flex cursor-pointer items-center justify-between;

    padding: 8px 4px;
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
  :deep(#root ~ .arco-tree-node-drag-icon) {
    @apply hidden;
  }
</style>
