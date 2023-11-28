<template>
  <div>
    <a-input
      v-model:model-value="moduleKeyword"
      :placeholder="t('caseManagement.caseReview.folderSearchPlaceholder')"
      allow-clear
      class="mb-[16px]"
    />
    <div class="folder">
      <div :class="getFolderClass('all')" @click="setActiveFolder('all')">
        <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
        <div class="folder-name">{{ t('caseManagement.caseReview.allCases') }}</div>
        <div class="folder-count">({{ allFileCount }})</div>
      </div>
    </div>
    <a-divider class="my-[8px]" />
    <a-spin class="min-h-[200px] w-full" :loading="loading">
      <MsTree
        :selected-keys="selectedKeys"
        :data="folderTree"
        :keyword="moduleKeyword"
        :default-expand-all="isExpandAll"
        :expand-all="isExpandAll"
        :empty-text="t('caseManagement.caseReview.noCases')"
        :draggable="false"
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
          <div class="inline-flex w-full">
            <div class="one-line-text w-[calc(100%-32px)] text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">({{ nodeData.count || 0 }})</div>
          </div>
        </template>
      </MsTree>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { getModules } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { mapTree } from '@/utils';

  import { ModuleTreeNode } from '@/models/projectManagement/file';

  const props = defineProps<{
    modulesCount?: Record<string, number>; // 模块数量统计对象
    showType?: string; // 显示类型
    isExpandAll?: boolean; // 是否展开所有节点
  }>();
  const emit = defineEmits(['init', 'folderNodeSelect']);

  const appStore = useAppStore();
  const { t } = useI18n();

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 460px)',
    };
  });

  const activeFolder = ref<string>('all');
  const allFileCount = ref(0);
  const isExpandAll = ref(props.isExpandAll);

  watch(
    () => props.isExpandAll,
    (val) => {
      isExpandAll.value = val;
    }
  );

  function getFolderClass(id: string) {
    return activeFolder.value === id ? 'folder-text folder-text--active' : 'folder-text';
  }

  function setActiveFolder(id: string) {
    activeFolder.value = id;
    emit('folderNodeSelect', [id], []);
  }

  const moduleKeyword = ref('');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const loading = ref(false);

  const selectedKeys = ref<string[]>([]);

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await getModules(appStore.currentProjectId);
      folderTree.value = res;
      if (isSetDefaultKey) {
        selectedKeys.value = [folderTree.value[0].id];
        const offspringIds: string[] = [];
        mapTree(folderTree.value[0].children || [], (e) => {
          offspringIds.push(e.id);
          return e;
        });

        emit('folderNodeSelect', selectedKeys.value, offspringIds);
      }
      emit(
        'init',
        folderTree.value.map((e) => e.name)
      );
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(_selectedKeys: (string | number)[], node: MsTreeNodeData) {
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });

    emit('folderNodeSelect', _selectedKeys, offspringIds);
  }

  onBeforeMount(() => {
    initModules();
  });

  /**
   * 初始化模块文件数量
   */
  watch(
    () => props.modulesCount,
    (obj) => {
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: obj?.[node.id] || 0,
        };
      });
    }
  );

  defineExpose({
    initModules,
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
      @apply flex cursor-pointer items-center;
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
</style>
