<template>
  <a-spin class="min-h-[400px] w-full" :loading="loading">
    <MsTree
      v-model:focus-node-key="focusNodeKey"
      :selected-keys="props.selectedKeys"
      :data="caseTree"
      :keyword="groupKeyword"
      :node-more-actions="caseMoreActions"
      :expand-all="props.isExpandAll"
      :empty-text="t('testPlan.testPlanIndex.planEmptyContent')"
      draggable
      :virtual-list-props="virtualListProps"
      block-node
      :field-names="{
        title: 'name',
        key: 'id',
        children: 'children',
        count: 'count',
      }"
      title-tooltip-position="left"
      @select="caseNodeSelect"
      @more-action-select="handleCaseMoreSelect"
      @more-actions-close="moreActionsClose"
      @drop="handleDrag"
    >
      <template #title="nodeData">
        <div class="inline-flex w-full gap-[8px]">
          <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
          <div class="ms-tree-node-count ml-[4px] text-[var(--color-text-brand)]">
            {{ nodeData.count || 0 }}
          </div>
        </div>
      </template>
      <template #extra="nodeData">
        <MsPopConfirm
          :visible="addSubVisible"
          :is-delete="false"
          :all-names="[]"
          :title="t('testPlan.testPlanIndex.addSubModule')"
          :ok-text="t('common.confirm')"
          :field-config="{
            placeholder: t('testPlan.testPlanIndex.addGroupTip'),
          }"
          :loading="confirmLoading"
          @confirm="addSubModule"
          @cancel="resetFocusNodeKey"
        >
          <MsButton type="icon" size="mini" class="ms-tree-node-extra__btn !mr-0" @click="setFocusKey(nodeData)">
            <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
          </MsButton>
        </MsPopConfirm>
        <MsPopConfirm
          :title="t('testPlan.testPlanIndex.rename')"
          :all-names="[]"
          :is-delete="false"
          :ok-text="t('common.confirm')"
          :field-config="{ field: renameCaseName }"
          :loading="confirmLoading"
          @confirm="updateNameModule"
          @cancel="resetFocusNodeKey"
        >
          <span :id="`renameSpan${nodeData.id}`" class="relative"></span>
        </MsPopConfirm>
      </template>
    </MsTree>
  </a-spin>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsPopConfirm from '@/components/pure/ms-popconfirm/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import {
    createPlanModuleTree,
    deletePlanModuleTree,
    getTestPlanModule,
    moveTestPlanModuleTree,
    updatePlanModuleTree,
  } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import { mapTree } from '@/utils';

  import type { CreateOrUpdateModule, UpdateModule } from '@/models/caseManagement/featureCase';
  import { ModuleTreeNode } from '@/models/common';

  const { t } = useI18n();
  const { openModal } = useModal();
  const appStore = useAppStore();
  const focusNodeKey = ref<string>('');
  const loading = ref(false);

  const props = defineProps<{
    activeFolder?: string; // 当前选中的文件夹，弹窗模式下需要使用
    selectedKeys?: Array<string | number>; // 选中的节点 key
    isExpandAll: boolean; // 是否展开用例节点
    allNames?: string[]; // 所有的模块name列表
    modulesCount?: Record<string, number>; // 模块数量统计对象
  }>();

  const emits = defineEmits(['update:selectedKeys', 'planTreeNodeSelect', 'init', 'dragUpdate', 'getNodeName']);

  const currentProjectId = computed(() => appStore.currentProjectId);

  const groupKeyword = ref<string>('');

  const caseTree = ref<ModuleTreeNode[]>([]);

  const setFocusKey = (node: MsTreeNodeData) => {
    focusNodeKey.value = node.id || '';
  };

  const caseMoreActions: ActionsItem[] = [
    {
      label: 'caseManagement.featureCase.rename',
      eventTag: 'rename',
    },
    {
      label: 'caseManagement.featureCase.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];

  const selectedNodeKeys = ref(props.selectedKeys || []);

  watch(
    () => props.selectedKeys,
    (val) => {
      selectedNodeKeys.value = val || [];
    }
  );

  watch(
    () => selectedNodeKeys.value,
    (val) => {
      emits('update:selectedKeys', val);
    }
  );
  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await getTestPlanModule({ projectId: currentProjectId.value });
      caseTree.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: e.id === 'root',
          draggable: e.id !== 'root',
          disabled: e.id === props.activeFolder,
          count: props.modulesCount?.[e.id] || 0,
        };
      });
      if (isSetDefaultKey) {
        selectedNodeKeys.value = [caseTree.value[0].id];
      }
      emits('init', caseTree.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  // 删除节点
  const deleteHandler = (node: MsTreeNodeData) => {
    openModal({
      type: 'error',
      title: t('caseManagement.featureCase.deleteTipTitle', { name: node.name }),
      content: t('caseManagement.featureCase.deleteCaseTipContent'),
      okText: t('caseManagement.featureCase.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deletePlanModuleTree(node.id);
          Message.success(t('common.deleteSuccess'));
          initModules(true);
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  const renamePopVisible = ref(false);
  const renameCaseName = ref('');

  function resetFocusNodeKey() {
    focusNodeKey.value = '';
    renamePopVisible.value = false;
    renameCaseName.value = '';
  }

  // 用例树节点选中事件
  const caseNodeSelect = (selectedKeys: (string | number)[], node: MsTreeNodeData) => {
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });
    emits('planTreeNodeSelect', selectedKeys, offspringIds, node.name);
  };

  // 用例树节点更多事件
  const handleCaseMoreSelect = (item: ActionsItem, node: MsTreeNodeData) => {
    switch (item.eventTag) {
      case 'delete':
        deleteHandler(node);
        resetFocusNodeKey();
        break;
      case 'rename':
        renameCaseName.value = node.name || '';
        renamePopVisible.value = true;
        document.querySelector(`#renameSpan${node.id}`)?.dispatchEvent(new Event('click'));
        break;
      default:
        break;
    }
  };

  /**
   * 处理文件夹树节点拖拽事件
   * @param tree 树数据
   * @param dragNode 拖拽节点
   * @param dropNode 释放节点
   * @param dropPosition 释放位置
   */
  async function handleDrag(
    tree: MsTreeNodeData[],
    dragNode: MsTreeNodeData,
    dropNode: MsTreeNodeData,
    dropPosition: number
  ) {
    try {
      loading.value = true;
      await moveTestPlanModuleTree({
        dragNodeId: dragNode.id as string,
        dropNodeId: dropNode.id || '',
        dropPosition,
      });
      Message.success(t('caseManagement.featureCase.moduleMoveSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
      await initModules();
      const treeNode = ref<MsTreeNodeData | null>(null);
      treeNode.value = dropNode;
      treeNode.value.children = [];
      if (dropPosition === 0) {
        treeNode.value.children.push(dragNode);
      }
      caseNodeSelect(dropNode.id, treeNode.value);
      emits('dragUpdate');
    }
  }

  const moreActionsClose = () => {
    if (!renamePopVisible.value) {
      resetFocusNodeKey();
    }
  };

  const addSubVisible = ref(false);
  const confirmLoading = ref(false);

  // 添加子模块
  async function addSubModule(formValue?: { field: string }, cancel?: () => void) {
    try {
      confirmLoading.value = true;
      const params: CreateOrUpdateModule = {
        projectId: currentProjectId.value,
        name: formValue?.field as string,
        parentId: focusNodeKey.value,
      };
      await createPlanModuleTree(params);
      Message.success(t('common.addSuccess'));
      if (cancel) {
        cancel();
      }
      initModules();
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  // 更新子模块
  async function updateNameModule(formValue?: { field: string }, cancel?: () => void) {
    try {
      confirmLoading.value = true;
      const params: UpdateModule = {
        id: focusNodeKey.value,
        name: formValue?.field as string,
      };
      await updatePlanModuleTree(params);
      Message.success(t('common.updateSuccess'));
      if (cancel) {
        cancel();
      }
      initModules();
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 240px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15,
    };
  });

  watch(
    () => props.activeFolder,
    (val) => {
      if (val === 'all') {
        initModules();
      }
    }
  );

  /**
   * 初始化模块文件数量
   */
  watch(
    () => props.modulesCount,
    (obj) => {
      caseTree.value = mapTree<ModuleTreeNode>(caseTree.value, (node) => {
        return {
          ...node,
          count: obj?.[node.id] || 0,
        };
      });
    }
  );

  onBeforeMount(() => {
    initModules();
  });

  defineExpose({
    initModules,
  });
</script>

<style scoped lang="less"></style>
