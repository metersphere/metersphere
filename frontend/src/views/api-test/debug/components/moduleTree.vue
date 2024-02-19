<template>
  <div class="flex h-full flex-col p-[24px]">
    <div class="mb-[8px] flex items-center gap-[8px]">
      <a-input v-model:model-value="moduleKeyword" :placeholder="t('apiTestDebug.searchTip')" allow-clear />
      <a-dropdown @select="handleSelect">
        <a-button type="primary">{{ t('apiTestDebug.newApi') }}</a-button>
        <template #content>
          <a-doption value="newApi">{{ t('apiTestDebug.newApi') }}</a-doption>
          <a-doption value="import">{{ t('apiTestDebug.importApi') }}</a-doption>
        </template>
      </a-dropdown>
    </div>
    <div class="folder">
      <div class="folder-text">
        <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
        <div class="folder-name">{{ t('apiTestDebug.allRequest') }}</div>
        <div class="folder-count">({{ allFileCount }})</div>
      </div>
      <div class="ml-auto flex items-center">
        <a-tooltip :content="isExpandAll ? t('common.collapseAll') : t('common.expandAll')">
          <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" @click="changeExpand">
            <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
          </MsButton>
        </a-tooltip>
        <popConfirm mode="add" :all-names="rootModulesName" parent-id="NONE" @add-finish="initModules">
          <MsButton type="icon" class="!mr-0 p-[2px]">
            <MsIcon
              type="icon-icon_create_planarity"
              size="18"
              class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
            />
          </MsButton>
        </popConfirm>
      </div>
    </div>
    <a-divider class="my-[8px]" />
    <a-spin class="h-[calc(100%-98px)] w-full" :loading="loading">
      <MsTree
        v-model:focus-node-key="focusNodeKey"
        :data="folderTree"
        :keyword="moduleKeyword"
        :node-more-actions="folderMoreActions"
        :default-expand-all="isExpandAll"
        :expand-all="isExpandAll"
        :empty-text="t('apiTestDebug.noMatchModule')"
        :virtual-list-props="{
          height: '100%',
        }"
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
          count: 'count',
        }"
        :selectable="false"
        block-node
        title-tooltip-position="left"
        @more-action-select="handleFolderMoreSelect"
        @more-actions-close="moreActionsClose"
        @drop="handleDrop"
      >
        <template #title="nodeData">
          <div
            v-if="nodeData.type === 'API'"
            class="inline-flex w-full cursor-pointer gap-[4px]"
            @click="emit('clickApiNode', nodeData)"
          >
            <apiMethodName :method="nodeData.attachInfo?.method || nodeData.attachInfo?.protocol" />
            <div class="one-line-text w-[calc(100%-32px)] text-[var(--color-text-1)]">{{ nodeData.name }}</div>
          </div>
          <div v-else class="inline-flex w-full">
            <div class="one-line-text w-[calc(100%-32px)] text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            <div class="ml-[4px] text-[var(--color-text-4)]">({{ nodeData.count || 0 }})</div>
          </div>
        </template>
        <template #extra="nodeData">
          <!-- 默认模块的 id 是root，默认模块不可编辑、不可添加子模块；API不可添加子模块 -->
          <popConfirm
            v-if="nodeData.id !== 'root' && nodeData.type !== 'API'"
            mode="add"
            :all-names="(nodeData.children || []).map((e: ModuleTreeNode) => e.name || '')"
            :parent-id="nodeData.id"
            @close="resetFocusNodeKey"
            @add-finish="() => initModules()"
          >
            <MsButton type="icon" size="mini" class="ms-tree-node-extra__btn !mr-0" @click="setFocusNodeKey(nodeData)">
              <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
            </MsButton>
          </popConfirm>
          <popConfirm
            v-if="nodeData.id !== 'root'"
            mode="rename"
            :parent-id="nodeData.id"
            :node-id="nodeData.id"
            :field-config="{ field: renameFolderTitle }"
            :all-names="(nodeData.children || []).map((e: ModuleTreeNode) => e.name || '')"
            :node-type="nodeData.type"
            @close="resetFocusNodeKey"
            @rename-finish="handleRenameFinish"
          >
            <span :id="`renameSpan${nodeData.id}`" class="relative"></span>
          </popConfirm>
        </template>
      </MsTree>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import popConfirm from '@/views/api-test/components/popConfirm.vue';

  import {
    deleteDebug,
    deleteDebugModule,
    getDebugModuleCount,
    getDebugModules,
    moveDebugModule,
  } from '@/api/modules/api-test/debug';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { mapTree } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    isExpandAll?: boolean; // 是否展开所有节点
  }>();
  const emit = defineEmits(['init', 'clickApiNode', 'newApi', 'import', 'renameFinish']);

  const { t } = useI18n();
  const { openModal } = useModal();

  function handleSelect(value: string | number | Record<string, any> | undefined) {
    switch (value) {
      case 'newApi':
        emit('newApi');
        break;
      case 'import':
        emit('import');
        break;

      default:
        break;
    }
  }

  const isExpandAll = ref(props.isExpandAll);
  const rootModulesName = ref<string[]>([]); // 根模块名称列表

  watch(
    () => props.isExpandAll,
    (val) => {
      isExpandAll.value = val;
    }
  );

  function changeExpand() {
    isExpandAll.value = !isExpandAll.value;
  }

  const moduleKeyword = ref('');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const focusNodeKey = ref<string | number>('');
  const loading = ref(false);

  function setFocusNodeKey(node: MsTreeNodeData) {
    focusNodeKey.value = node.id || '';
  }

  const folderMoreActions: ActionsItem[] = [
    {
      label: 'common.rename',
      eventTag: 'rename',
    },
    {
      label: 'common.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];
  const renamePopVisible = ref(false);

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules() {
    try {
      loading.value = true;
      const res = await getDebugModules();
      folderTree.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: e.id === 'root',
        };
      });
      rootModulesName.value = folderTree.value.map((e) => e.name || '');
      emit('init', folderTree.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const modulesCount = ref<Record<string, number>>({});
  const allFileCount = computed(() => modulesCount.value.all || 0);
  async function initModuleCount() {
    try {
      const res = await getDebugModuleCount({
        keyword: moduleKeyword.value,
      });
      modulesCount.value = res;
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: res[node.id] || 0,
        };
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  /**
   * 删除文件夹
   * @param node 节点信息
   */
  function deleteFolder(node: MsTreeNodeData) {
    openModal({
      type: 'error',
      title: t('apiTestDebug.deleteFolderTipTitle', { name: node.name }),
      content: t('apiTestDebug.deleteFolderTipContent'),
      okText: t('apiTestDebug.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteDebugModule(node.id);
          Message.success(t('apiTestDebug.deleteSuccess'));
          initModules();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const renameFolderTitle = ref(''); // 重命名的文件夹名称

  function resetFocusNodeKey() {
    focusNodeKey.value = '';
    renamePopVisible.value = false;
    renameFolderTitle.value = '';
  }

  /**
   * 删除接口调试
   * @param node 节点信息
   */
  function deleteApiDebug(node: MsTreeNodeData) {
    openModal({
      type: 'error',
      title: t('apiTestDebug.deleteDebugTipTitle', { name: node.name }),
      content: t('apiTestDebug.deleteDebugTipContent'),
      okText: t('apiTestDebug.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteDebug(node.id);
          Message.success(t('apiTestDebug.deleteSuccess'));
          initModules();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 处理树节点更多按钮事件
   * @param item
   */
  function handleFolderMoreSelect(item: ActionsItem, node: MsTreeNodeData) {
    switch (item.eventTag) {
      case 'delete':
        if (node.type === 'MODULE') {
          deleteFolder(node);
        } else {
          deleteApiDebug(node);
        }
        resetFocusNodeKey();
        break;
      case 'rename':
        renameFolderTitle.value = node.name || '';
        renamePopVisible.value = true;
        document.querySelector(`#renameSpan${node.id}`)?.dispatchEvent(new Event('click'));
        break;
      default:
        break;
    }
  }

  /**
   * 处理文件夹树节点拖拽事件
   * @param tree 树数据
   * @param dragNode 拖拽节点
   * @param dropNode 释放节点
   * @param dropPosition 释放位置
   */
  async function handleDrop(
    tree: MsTreeNodeData[],
    dragNode: MsTreeNodeData,
    dropNode: MsTreeNodeData,
    dropPosition: number
  ) {
    try {
      loading.value = true;
      await moveDebugModule({
        dragNodeId: dragNode.id as string,
        dropNodeId: dropNode.id || '',
        dropPosition,
      });
      Message.success(t('apiTestDebug.moduleMoveSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
      await initModules();
      initModuleCount();
    }
  }

  function moreActionsClose() {
    if (!renamePopVisible.value) {
      // 当下拉菜单关闭时，若不是触发重命名气泡显示，则清空聚焦节点 key
      resetFocusNodeKey();
    }
  }

  function handleRenameFinish(newName: string, id: string) {
    initModules();
    emit('renameFinish', newName, id);
  }

  onBeforeMount(async () => {
    await initModules();
    initModuleCount();
  });

  defineExpose({
    initModules,
    initModuleCount,
  });
</script>

<style lang="less" scoped>
  .folder {
    @apply flex items-center justify-between;

    padding: 8px 4px;
    border-radius: var(--border-radius-small);
    &:hover {
      background-color: rgb(var(--primary-1));
    }
    .folder-text {
      @apply flex items-center;
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
