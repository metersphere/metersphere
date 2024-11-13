<template>
  <div>
    <div class="mb-[16px] flex justify-between">
      <a-input
        v-model:model-value="moduleKeyword"
        :placeholder="t('caseManagement.caseReview.folderSearchPlaceholder')"
        allow-clear
        :max-length="255"
      />
      <a-button
        v-if="!props.isModal && hasAnyPermission(['CASE_REVIEW:READ+ADD'])"
        class="ml-2"
        type="primary"
        @click="emit('create')"
      >
        {{ t('common.newCreate') }}
      </a-button>
    </div>
    <MsFolderAll
      v-if="!props.isModal"
      v-model:isExpandAll="isExpandAll"
      :active-folder="activeFolder"
      :folder-name="t('caseManagement.caseReview.allReviews')"
      :all-count="allFileCount"
      @set-active-folder="setActiveFolder"
    >
      <template #expandRight>
        <popConfirm
          v-if="hasAnyPermission(['CASE_REVIEW:READ+UPDATE'])"
          mode="add"
          :all-names="rootModulesName"
          parent-id="NONE"
          @add-finish="() => initModules()"
        >
          <MsButton type="icon" class="!mr-0 p-[2px]">
            <MsIcon
              type="icon-icon_create_planarity"
              size="18"
              class="text-[rgb(var(--primary-5))] hover:text-[rgb(var(--primary-4))]"
            />
          </MsButton>
        </popConfirm>
      </template>
    </MsFolderAll>
    <a-spin class="min-h-[400px] w-full" :loading="loading">
      <MsTree
        v-model:focus-node-key="focusNodeKey"
        v-model:selected-keys="selectedKeys"
        :data="folderTree"
        :keyword="moduleKeyword"
        :node-more-actions="folderMoreActions"
        :default-expand-all="isExpandAll"
        :expand-all="isExpandAll"
        :empty-text="t('caseManagement.caseReview.noReviews')"
        :draggable="!props.isModal && hasAnyPermission(['CASE_REVIEW:READ+UPDATE'])"
        :virtual-list-props="virtualListProps"
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
          count: 'count',
        }"
        block-node
        title-tooltip-position="top"
        @select="folderNodeSelect"
        @more-action-select="handleFolderMoreSelect"
        @more-actions-close="moreActionsClose"
        @drop="handleDrop"
      >
        <template #title="nodeData">
          <div class="inline-flex w-full gap-[8px]">
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
            <div v-if="!props.isModal" class="ms-tree-node-count ml-[4px] text-[var(--color-text-brand)]">
              {{ nodeData.count || 0 }}
            </div>
          </div>
        </template>
        <template v-if="!props.isModal" #extra="nodeData">
          <!-- 默认模块的 id 是root，默认模块不可编辑、不可添加子模块 -->
          <popConfirm
            v-if="nodeData.id !== 'root' && hasAnyPermission(['CASE_REVIEW:READ+ADD'])"
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
            v-if="nodeData.id !== 'root' && hasAnyPermission(['CASE_REVIEW:READ+UPDATE'])"
            mode="rename"
            :parent-id="nodeData.id"
            :node-id="nodeData.id"
            :field-config="{ field: renameFolderTitle }"
            :all-names="(nodeData.children || []).map((e: ModuleTreeNode) => e.name || '')"
            @close="resetFocusNodeKey"
            @rename-finish="initModules"
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
  import MsFolderAll from '@/components/business/ms-folder-all/index.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import popConfirm from './popConfirm.vue';

  import { deleteReviewModule, getReviewModules, moveReviewModule } from '@/api/modules/case-management/caseReview';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { characterLimit, mapTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    isModal?: boolean; // 是否是弹窗模式
    modulesCount?: Record<string, number>; // 模块数量统计对象
    isExpandAll?: boolean; // 是否展开所有节点
  }>();
  const emit = defineEmits<{
    (e: 'init', data: ModuleTreeNode[], nodePathObj: Record<string, any>): void;
    (e: 'folderNodeSelect', selectedKeys: string[], offspringIds: string[]): void;
    (e: 'create'): void;
    (e: 'nodeDelete'): void;
    (e: 'nodeDrop'): void;
  }>();

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
      height: 'calc(100vh - 218px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });

  const activeFolder = ref<string>('all');
  const allFileCount = ref(0);
  const isExpandAll = ref(props.isExpandAll);
  const rootModulesName = ref<string[]>([]); // 根模块名称列表
  const selectedKeys = ref<string[]>([]);

  watch(
    () => props.isExpandAll,
    (val) => {
      isExpandAll.value = val;
    }
  );

  function setActiveFolder(id: string) {
    activeFolder.value = id;
    if (id === 'all') {
      selectedKeys.value = [];
    }
    emit('folderNodeSelect', [id], []);
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
      permission: ['CASE_REVIEW:READ+UPDATE'],
    },
    {
      label: 'common.delete',
      eventTag: 'delete',
      danger: true,
      permission: ['CASE_REVIEW:READ+DELETE'],
    },
  ];
  const renamePopVisible = ref(false);

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await getReviewModules(appStore.currentProjectId);
      const nodePathObj: Record<string, any> = {};
      folderTree.value = mapTree<ModuleTreeNode>(res, (e, fullPath) => {
        // 拼接当前节点的完整路径
        nodePathObj[e.id] = {
          path: e.path,
          fullPath,
        };
        return {
          ...e,
          hideMoreAction: e.id === 'root' || props.isModal,
          draggable: e.id !== 'root' && !props.isModal,
          disabled: e.id === activeFolder.value && props.isModal,
          count: props.modulesCount?.[e.id] || 0, // 避免模块数量先初始化完成了，数量没更新
        };
      });
      if (isSetDefaultKey) {
        selectedKeys.value = [folderTree.value[0].id];
        const offspringIds: string[] = [];
        mapTree(folderTree.value[0].children || [], (e) => {
          offspringIds.push(e.id);
          return e;
        });

        emit('folderNodeSelect', selectedKeys.value, offspringIds);
      }
      emit('init', folderTree.value, nodePathObj);
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
      title: t('caseManagement.caseReview.deleteFolderTipTitle', { name: characterLimit(node.name) }),
      content: t('caseManagement.caseReview.deleteFolderTipContent'),
      okText: t('caseManagement.caseReview.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteReviewModule(node.id);
          Message.success(t('caseManagement.caseReview.deleteSuccess'));
          initModules(selectedKeys.value[0] === node.id);
          emit('nodeDelete');
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
    activeFolder.value = node.id;
    emit('folderNodeSelect', [node.id], offspringIds);
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
      await moveReviewModule({
        dragNodeId: dragNode.id as string,
        dropNodeId: dropNode.id || '',
        dropPosition,
      });
      Message.success(t('caseManagement.caseReview.moduleMoveSuccess'));
      emit('nodeDrop');
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
      allFileCount.value = obj?.all || 0;
    }
  );

  defineExpose({
    setActiveFolder,
    initModules,
  });
</script>
