<template>
  <div class="h-full">
    <div class="folder" @click="setActiveFolder('all')">
      <div :class="allFolderClass">
        <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
        <div class="folder-name">{{ t('apiScenario.tree.folder.allScenario') }}</div>
        <div class="folder-count">({{ allScenarioCount }})</div>
      </div>
      <div class="ml-auto flex items-center">
        <a-tooltip :content="isExpandAll ? t('common.collapseAll') : t('common.expandAll')">
          <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" @click="changeExpand">
            <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
          </MsButton>
        </a-tooltip>
      </div>
    </div>
    <div class="mb-[8px] flex items-center gap-[8px]">
      <a-input
        v-model:model-value="moduleKeyword"
        :placeholder="t('apiScenario.tree.selectorPlaceholder')"
        allow-clear
      />
    </div>
    <a-spin class="w-full" :style="{ height: `calc(100vh - 248px)` }" :loading="loading">
      <MsTree
        v-model:focus-node-key="focusNodeKey"
        v-model:selected-keys="selectedKeys"
        :data="folderTree"
        :keyword="moduleKeyword"
        :node-more-actions="folderMoreActions"
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

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { getTrashModuleCount, getTrashModuleTree } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
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
  const { openModal } = useModal();

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    switch (value) {
      case 'newScenario':
        emit('newScenario');
        break;
      case 'import':
        emit('import');
        break;
      case 'addModule':
        document.querySelector('#addModulePopSpan')?.dispatchEvent(new Event('click'));
        break;
      default:
        break;
    }
  }

  const virtualListProps = computed(() => {
    return {
      height: '100%',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });

  const moduleKeyword = ref('');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const focusNodeKey = ref<string | number>('');
  const selectedKeys = ref<Array<string | number>>([]);
  const allFolderClass = computed(() =>
    selectedKeys.value[0] === 'all' ? 'folder-text folder-text--active' : 'folder-text'
  );
  const loading = ref(false);

  const folderMoreActions: ActionsItem[] = [
    {
      label: 'common.rename',
      eventTag: 'rename',
    },
    {
      isDivider: true,
    },
    {
      label: 'common.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const modulesCount = ref<Record<string, number>>({});
  const allScenarioCount = computed(() => modulesCount.value.all || 0);
  const isExpandAll = ref(props.isExpandAll);

  function setActiveFolder(id: string) {
    selectedKeys.value = [id];
    emit('folderNodeSelect', selectedKeys.value, []);
  }

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await getTrashModuleTree({
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

  function changeExpand() {
    isExpandAll.value = !isExpandAll.value;
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

  async function initModuleCount(params: ApiScenarioGetModuleParams) {
    try {
      const res = await getTrashModuleCount(params);
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
  defineExpose({
    initModuleCount,
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
