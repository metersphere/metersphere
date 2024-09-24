<template>
  <div class="flex h-full flex-col p-[12px_16px]">
    <div class="mb-[8px] flex items-center gap-[8px]">
      <a-input v-model:model-value="moduleKeyword" :placeholder="t('apiTestDebug.searchTip')" allow-clear />
      <a-button v-permission="['PROJECT_API_DEBUG:READ+ADD']" type="primary" @click="emit('newApi')">
        {{ t('apiTestDebug.newApi') }}
      </a-button>
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
        <popConfirm
          v-if="hasAnyPermission(['PROJECT_API_DEBUG:READ+ADD'])"
          mode="add"
          :all-names="rootModulesName"
          parent-id="NONE"
          :add-module-api="addDebugModule"
          @add-finish="handleAddFinish"
        >
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
    <a-spin class="max-h-[calc(100%-98px)] w-full" :loading="loading">
      <MsTree
        v-model:selected-keys="selectedKeys"
        v-model:focus-node-key="focusNodeKey"
        :data="folderTree"
        :keyword="moduleKeyword"
        :node-more-actions="folderMoreActions"
        :default-expand-all="isExpandAll"
        :expand-all="isExpandAll"
        :empty-text="t('apiTestDebug.noMatchModule')"
        :virtual-list-props="{
          height: '100%',
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
        :draggable="hasAnyPermission(['PROJECT_API_DEBUG:READ+UPDATE'])"
        :selectable="nodeSelectable"
        block-node
        :allow-drop="allowDrop"
        @more-action-select="handleFolderMoreSelect"
        @more-actions-close="moreActionsClose"
        @drop="handleDrop"
        @select="
          (keys, node) => {
            if (node.type === 'API') {
              emit('clickApiNode', node);
            }
          }
        "
      >
        <template #title="nodeData">
          <div v-if="nodeData.type === 'API'" class="inline-flex w-full cursor-pointer gap-[4px]">
            <apiMethodName :method="nodeData.attachInfo?.method || nodeData.attachInfo?.protocol" />
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
          </div>
          <div v-else class="inline-flex w-full gap-[8px]">
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            <div class="ms-tree-node-count ml-[4px] text-[var(--color-text-brand)]">{{ nodeData.count || 0 }}</div>
          </div>
        </template>
        <template #extra="nodeData">
          <popConfirm
            v-if="nodeData.id !== 'root' && hasAnyPermission(['PROJECT_API_DEBUG:READ+UPDATE'])"
            mode="rename"
            :parent-id="nodeData.id"
            :node-id="nodeData.id"
            :field-config="{ field: renameFolderTitle }"
            :all-names="(nodeData.parent? nodeData.parent.children || [] : folderTree).filter((e: ModuleTreeNode) => e.id !== nodeData.id).map((e: ModuleTreeNode) => e.name || '')"
            :node-type="nodeData.type"
            :update-module-api="updateDebugModule"
            :update-api-node-api="updateDebug"
            @close="resetFocusNodeKey"
            @rename-finish="handleRenameFinish"
          >
            <span :id="`renameSpan${nodeData.id}`" class="relative"></span>
          </popConfirm>
          <!-- 默认模块的 id 是root，默认模块不可编辑、不可添加子模块；API不可添加子模块 -->
          <popConfirm
            v-if="nodeData.id !== 'root' && nodeData.type !== 'API' && hasAnyPermission(['PROJECT_API_DEBUG:READ+ADD'])"
            mode="add"
            :all-names="(nodeData.children || []).map((e: ModuleTreeNode) => e.name || '')"
            :parent-id="nodeData.id"
            :add-module-api="addDebugModule"
            @close="resetFocusNodeKey"
            @add-finish="handleAddFinish"
          >
            <MsButton type="icon" size="mini" class="ms-tree-node-extra__btn !mr-0" @click="setFocusNodeKey(nodeData)">
              <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
            </MsButton>
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
    addDebugModule,
    deleteDebug,
    deleteDebugModule,
    dragDebug,
    getDebugModuleCount,
    getDebugModules,
    moveDebugModule,
    updateDebug,
    updateDebugModule,
  } from '@/api/modules/api-test/debug';
  import { dropPositionMap } from '@/config/common';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { characterLimit, mapTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    isExpandAll?: boolean; // 是否展开所有节点
    activeNodeId?: string | number; // 当前选中节点 id
  }>();
  const emit = defineEmits(['init', 'clickApiNode', 'newApi', 'import', 'updateApiNode', 'deleteFinish']);

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

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
  const selectedKeys = ref<(string | number)[]>([]);
  const focusNodeKey = ref<string | number>('');
  const loading = ref(false);

  watch(
    () => props.activeNodeId,
    (val) => {
      if (val) {
        selectedKeys.value = [val];
      }
    }
  );

  function nodeSelectable(node: MsTreeNodeData) {
    // 只有 api 节点可选中
    return node.type === 'API';
  }

  function setFocusNodeKey(node: MsTreeNodeData) {
    focusNodeKey.value = node.id || '';
  }

  const folderMoreActions: ActionsItem[] = [
    {
      label: 'common.rename',
      eventTag: 'rename',
      permission: ['PROJECT_API_DEBUG:READ+UPDATE'],
    },
    {
      label: 'common.delete',
      eventTag: 'delete',
      danger: true,
      permission: ['PROJECT_API_DEBUG:READ+DELETE'],
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
        keyword: '',
      });
      modulesCount.value = res;
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: res[node.id] || 0,
          draggable: node.id !== 'root',
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
      title: t('apiTestDebug.deleteFolderTipTitle', { name: characterLimit(node.name) }),
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
          emit('deleteFinish', node);
          await initModules();
          initModuleCount();
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
      title: t('apiTestDebug.deleteDebugTipTitle', { name: characterLimit(node.name) }),
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
          emit('deleteFinish', node);
          await initModules();
          initModuleCount();
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

  function allowDrop(dropNode: MsTreeNodeData, dropPosition: number, dragNode?: MsTreeNodeData | null) {
    if (dropNode.type === 'API' && dropPosition === 0) {
      // API节点不可添加子节点
      return false;
    }
    if (dropNode.type === 'MODULE' && dragNode?.type === 'API' && dropPosition !== 0) {
      // API节点不移动到模块的前后位置
      document.querySelector('.arco-tree-node-title-draggable::before')?.setAttribute('style', 'display: none');
      return false;
    }
    return true;
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
      if (dragNode.id === 'root' || (dragNode.type === 'MODULE' && dropNode.id === 'root')) {
        // 根节点不可拖拽；模块不可拖拽到根节点
        return;
      }
      loading.value = true;
      if (dragNode.type === 'MODULE') {
        await moveDebugModule({
          dragNodeId: dragNode.id as string,
          dropNodeId: dropNode.id || '',
          dropPosition,
        });
      } else {
        await dragDebug({
          projectId: appStore.currentProjectId,
          moveMode: dropPositionMap[dropPosition],
          moveId: dragNode.id,
          targetId: dropNode.type === 'MODULE' ? dragNode.id : dropNode.id,
          moduleId: dropNode.type === 'API' ? dropNode.parentId : dropNode.id, // 释放节点是 API，则传入它所属模块id；模块的话直接是模块id
        });
        emit('updateApiNode', { ...dragNode, moduleId: dropNode.type === 'API' ? dropNode.parentId : dropNode.id });
      }
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

  async function handleAddFinish() {
    await initModules();
    initModuleCount();
  }

  function moreActionsClose() {
    if (!renamePopVisible.value) {
      // 当下拉菜单关闭时，若不是触发重命名气泡显示，则清空聚焦节点 key
      resetFocusNodeKey();
    }
  }

  async function handleRenameFinish(newName: string, id: string) {
    emit('updateApiNode', { name: newName, id });
    await initModules();
    initModuleCount();
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
