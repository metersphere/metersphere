<template>
  <a-spin class="min-h-[400px] w-full" :loading="loading">
    <MsTree
      v-model:focus-node-key="focusNodeKey"
      :selected-keys="props.selectedKeys"
      :data="testPlanTree"
      :keyword="moduleKeyword"
      :node-more-actions="caseMoreActions"
      :expand-all="props.isExpandAll"
      :empty-text="t('common.noMatchData')"
      :draggable="hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE'])"
      :virtual-list-props="virtualListProps"
      block-node
      :field-names="{
        title: 'name',
        key: 'id',
        children: 'children',
        count: 'count',
      }"
      title-tooltip-position="top"
      @select="planNodeSelect"
      @more-action-select="handlePlanMoreSelect"
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
          v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+ADD'])"
          :is-delete="false"
          :all-names="(nodeData.children || []).map((e: ModuleTreeNode) => e.name || '')"
          :title="t('testPlan.testPlanIndex.addSubModule')"
          :ok-text="t('common.confirm')"
          :field-config="{
            placeholder: t('testPlan.testPlanIndex.addGroupTip'),
            nameExistTipText: t('project.fileManagement.nameExist'),
          }"
          :loading="confirmLoading"
          @confirm="addSubModule"
          @cancel="resetFocusNodeKey"
        >
          <MsButton
            v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+ADD'])"
            type="icon"
            size="mini"
            class="ms-tree-node-extra__btn !mr-0"
            @click="setFocusKey(nodeData)"
          >
            <MsIcon type="icon-icon_add_outlined" size="14" class="text-[var(--color-text-4)]" />
          </MsButton>
        </MsPopConfirm>
        <MsPopConfirm
          v-if="hasAnyPermission(['PROJECT_TEST_PLAN:READ+UPDATE'])"
          :title="t('testPlan.testPlanIndex.rename')"
          :all-names="(nodeData.parent? nodeData.parent.children || [] : testPlanTree).filter((e: ModuleTreeNode) => e.id !== nodeData.id).map((e: ModuleTreeNode) => e.name || '')"
          :is-delete="false"
          :ok-text="t('common.confirm')"
          :field-config="{ field: renameCaseName, nameExistTipText: t('project.fileManagement.nameExist') }"
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
  import { useVModel } from '@vueuse/core';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsPopConfirm, { ConfirmValue } from '@/components/pure/ms-popconfirm/index.vue';
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
  import { characterLimit, mapTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

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
    groupKeyword: string;
  }>();

  const emits = defineEmits([
    'update:selectedKeys',
    'planTreeNodeSelect',
    'init',
    'dragUpdate',
    'getNodeName',
    'update:groupKeyword',
    'deleteNode',
  ]);

  const currentProjectId = computed(() => appStore.currentProjectId);

  const moduleKeyword = useVModel(props, 'groupKeyword', emits);

  const testPlanTree = ref<ModuleTreeNode[]>([]);

  const setFocusKey = (node: MsTreeNodeData) => {
    focusNodeKey.value = node.id || '';
  };

  const caseMoreActions: ActionsItem[] = [
    {
      label: 'caseManagement.featureCase.rename',
      eventTag: 'rename',
      permission: ['PROJECT_TEST_PLAN:READ+UPDATE'],
    },
    {
      label: 'caseManagement.featureCase.delete',
      eventTag: 'delete',
      danger: true,
      permission: ['PROJECT_TEST_PLAN:READ+DELETE'],
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
      testPlanTree.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: e.id === 'root',
          draggable: e.id !== 'root',
          count: props.modulesCount?.[e.id] || 0,
        };
      });
      if (isSetDefaultKey) {
        selectedNodeKeys.value = [testPlanTree.value[0].id];
      }
      emits('init', testPlanTree.value, isSetDefaultKey);
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
      title: t('common.deleteConfirmTitle', { name: characterLimit(node.name) }),
      content: t('caseManagement.featureCase.deleteCaseTipContent'),
      okText: t('caseManagement.featureCase.deleteConfirm'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deletePlanModuleTree(node.id);
          initModules();
          emits('deleteNode');
          Message.success(t('common.deleteSuccess'));
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
  const planNodeSelect = (selectedKeys: (string | number)[], node: MsTreeNodeData) => {
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });
    emits('planTreeNodeSelect', selectedKeys, offspringIds, node.name);
  };

  // 用例树节点更多事件
  const handlePlanMoreSelect = (item: ActionsItem, node: MsTreeNodeData) => {
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
      planNodeSelect(dropNode.id, treeNode.value);
      emits('dragUpdate');
    }
  }

  const moreActionsClose = () => {
    if (!renamePopVisible.value) {
      resetFocusNodeKey();
    }
  };

  const confirmLoading = ref(false);

  // 添加子模块
  async function addSubModule(formValue: ConfirmValue, cancel?: () => void) {
    try {
      confirmLoading.value = true;
      const params: CreateOrUpdateModule = {
        projectId: currentProjectId.value,
        name: formValue.field,
        parentId: focusNodeKey.value,
      };
      await createPlanModuleTree(params);
      Message.success(t('common.addSuccess'));
      if (cancel) {
        cancel();
      }
      initModules(true);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  // 更新子模块
  async function updateNameModule(formValue: ConfirmValue, cancel?: () => void) {
    try {
      confirmLoading.value = true;
      const params: UpdateModule = {
        id: focusNodeKey.value,
        name: formValue.field,
      };
      await updatePlanModuleTree(params);
      Message.success(t('common.updateSuccess'));
      if (cancel) {
        cancel();
      }
      initModules();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 200px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15,
    };
  });

  /**
   * 初始化模块文件数量
   */
  watch(
    () => props.modulesCount,
    (obj) => {
      testPlanTree.value = mapTree<ModuleTreeNode>(testPlanTree.value, (node) => {
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
