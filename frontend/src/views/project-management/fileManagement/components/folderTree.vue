<template>
  <a-input
    v-model:model-value="moduleKeyword"
    :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
    allow-clear
    class="mb-[16px]"
    :max-length="255"
  ></a-input>
  <a-spin class="min-h-[400px] w-full" :loading="loading">
    <MsTree
      v-model:focus-node-key="focusNodeKey"
      :selected-keys="props.selectedKeys"
      :data="folderTree"
      :keyword="moduleKeyword"
      :node-more-actions="folderMoreActions"
      :expand-all="props.isExpandAll"
      :empty-text="t('project.fileManagement.noFolder')"
      :draggable="!props.isModal && hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+UPDATE'])"
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
      @more-action-select="handleFolderMoreSelect"
      @more-actions-close="moreActionsClose"
      @drop="handleDrop"
    >
      <template #title="nodeData">
        <div class="inline-flex w-full gap-[8px]">
          <div class="one-line-text text-[var(--color-text-1)]">{{ nodeData.name }}</div>
          <div v-if="!props.isModal" class="ms-tree-node-count ml-auto text-[var(--color-text-brand)]">
            {{ nodeData.count || 0 }}
          </div>
        </div>
      </template>
      <template v-if="!props.isModal" #extra="nodeData">
        <!-- 默认模块的 id 是root，默认模块不可编辑、不可添加子模块 -->
        <popConfirm
          v-if="nodeData.id !== 'root' && hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+ADD'])"
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
          v-if="nodeData.id !== 'root' && hasAnyPermission(['PROJECT_FILE_MANAGEMENT:READ+UPDATE'])"
          mode="rename"
          :parent-id="nodeData.id"
          :node-id="nodeData.id"
          :field-config="{ field: renameFolderTitle }"
          :all-names="(nodeData.children || []).map((e: ModuleTreeNode) => e.name || '')"
          @close="resetFocusNodeKey"
          @rename-finish="() => initModules()"
        >
          <span :id="`renameSpan${nodeData.id}`" class="relative"></span>
        </popConfirm>
      </template>
    </MsTree>
  </a-spin>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';
  import { useVModel } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import popConfirm from './popConfirm.vue';

  import { deleteModule, getModules, moveModule } from '@/api/modules/project-management/fileManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { mapTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    isExpandAll: boolean;
    activeFolder?: string; // 当前选中的文件夹，弹窗模式下需要使用
    selectedKeys: Array<string | number>; // 选中的节点 key
    isModal?: boolean; // 是否是弹窗模式
    modulesCount?: Record<string, number>; // 模块数量统计对象
    showType?: string; // 显示类型
  }>();
  const emit = defineEmits(['update:selectedKeys', 'init', 'folderNodeSelect']);

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const virtualListProps = computed(() => {
    if (props.isModal) {
      return {
        height: 'calc(60vh - 190px)',
        threshold: 200,
        fixedSize: true,
        buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
      };
    }
    return {
      height: 'calc(100vh - 292px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });
  const moduleKeyword = ref('');
  const folderTree = ref<ModuleTreeNode[]>([]);
  const focusNodeKey = ref<string | number>('');
  const loading = ref(false);

  function setFocusNodeKey(node: MsTreeNodeData) {
    focusNodeKey.value = node.id || '';
  }

  const folderMoreActions: ActionsItem[] = [
    {
      label: 'project.fileManagement.rename',
      eventTag: 'rename',
      permission: ['PROJECT_FILE_MANAGEMENT:READ+UPDATE'],
    },
    {
      label: 'project.fileManagement.delete',
      eventTag: 'delete',
      danger: true,
      permission: ['PROJECT_FILE_MANAGEMENT:READ+DELETE'],
    },
  ];
  const renamePopVisible = ref(false);

  const selectedKeys = useVModel(props, 'selectedKeys', emit);

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await getModules(appStore.currentProjectId);
      folderTree.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: e.id === 'root',
          draggable: e.id !== 'root' && !props.isModal,
          disabled: e.id === props.activeFolder && props.isModal,
          count: props.modulesCount?.[e.id] || 0,
        };
      });
      if (isSetDefaultKey) {
        selectedKeys.value = [folderTree.value[0].id];
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
   * 删除文件夹
   * @param node 节点信息
   */
  function deleteFolder(node: MsTreeNodeData) {
    openModal({
      type: 'error',
      title: t('project.fileManagement.deleteFolderTipTitle', { name: node.name }),
      content: t('project.fileManagement.deleteFolderTipContent'),
      okText: t('project.fileManagement.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteModule(node.id);
          Message.success(t('project.fileManagement.deleteSuccess'));
          initModules(selectedKeys.value[0] === node.id);
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

  /**
   * 处理树节点更多按钮事件
   * @param item
   */
  function handleFolderMoreSelect(item: ActionsItem, node: MsTreeNodeData) {
    switch (item.eventTag) {
      case 'delete':
        deleteFolder(node);
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
      await moveModule({
        dragNodeId: dragNode.id as string,
        dropNodeId: dropNode.id || '',
        dropPosition,
      });
      Message.success(t('project.fileManagement.moduleMoveSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
      initModules();
    }
  }

  function moreActionsClose() {
    if (!renamePopVisible.value) {
      // 当下拉菜单关闭时，若不是触发重命名气泡显示，则清空聚焦节点 key
      resetFocusNodeKey();
    }
  }

  watch(
    () => props.showType,
    (val) => {
      if (val === 'Module') {
        initModules();
      }
    },
    {
      immediate: true,
    }
  );

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

  onBeforeMount(() => {
    if (props.isModal) {
      initModules();
    }
  });

  defineExpose({
    initModules,
  });
</script>

<style lang="less" scoped></style>
