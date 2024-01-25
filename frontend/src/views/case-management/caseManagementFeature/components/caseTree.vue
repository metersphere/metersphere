<template>
  <a-input-search
    v-model:model-value="groupKeyword"
    :placeholder="t('caseManagement.featureCase.searchTip')"
    allow-clear
    class="mb-[16px]"
  ></a-input-search>
  <a-spin class="w-full" :style="{ height: `calc(100vh - 346px)` }" :loading="loading">
    <MsTree
      v-model:focus-node-key="focusNodeKey"
      :selected-keys="props.selectedKeys"
      :data="caseTree"
      :keyword="groupKeyword"
      :node-more-actions="caseMoreActions"
      :expand-all="props.isExpandAll"
      :empty-text="t('caseManagement.featureCase.caseEmptyContent')"
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
        <div class="inline-flex w-full">
          <div class="one-line-text w-[calc(100%-32px)] text-[var(--color-text-1)]">{{ nodeData.name }}</div>
          <div v-if="!props.isModal" class="ml-[4px] text-[var(--color-text-4)]">({{ nodeData.count || 0 }})</div>
        </div>
      </template>
      <template v-if="!props.isModal" #extra="nodeData">
        <MsPopConfirm
          :visible="addSubVisible"
          :is-delete="false"
          :all-names="[]"
          :title="t('caseManagement.featureCase.addSubModule')"
          :ok-text="t('common.confirm')"
          :field-config="{
            placeholder: t('caseManagement.featureCase.addGroupTip'),
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
          :title="t('caseManagement.featureCase.rename')"
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
  import { computed, ref, watch } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsPopConfirm from '@/components/pure/ms-popconfirm/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import {
    createCaseModuleTree,
    deleteCaseModuleTree,
    getCaseModuleTree,
    moveCaseModuleTree,
    updateCaseModuleTree,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useAppStore from '@/store/modules/app';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { mapTree } from '@/utils';

  import type { CreateOrUpdateModule, UpdateModule } from '@/models/caseManagement/featureCase';
  import { ModuleTreeNode } from '@/models/projectManagement/file';

  const featureCaseStore = useFeatureCaseStore();

  const { t } = useI18n();
  const { openModal } = useModal();
  const appStore = useAppStore();
  const focusNodeKey = ref<string>('');
  const loading = ref(false);

  const props = defineProps<{
    isModal?: boolean; // 是否是弹窗模式
    activeFolder?: string; // 当前选中的文件夹，弹窗模式下需要使用
    selectedKeys?: Array<string | number>; // 选中的节点 key
    isExpandAll: boolean; // 是否展开用例节点
    allNames?: string[]; // 所有的模块name列表
    modulesCount?: Record<string, number>; // 模块数量统计对象
  }>();

  const emits = defineEmits(['update:selectedKeys', 'caseNodeSelect', 'init']);

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
      featureCaseStore.setModuleId(val as string[]);
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
      const res = await getCaseModuleTree({ projectId: currentProjectId.value });
      caseTree.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: e.id === 'root',
          draggable: e.id !== 'root' && !props.isModal,
          disabled: e.id === props.activeFolder && props.isModal,
          count: props.modulesCount?.[e.id] || 0,
        };
      });
      featureCaseStore.setModulesTree(caseTree.value);
      featureCaseStore.setModuleId(['all']);
      if (isSetDefaultKey) {
        selectedNodeKeys.value = [caseTree.value[0].id];
      }
      emits(
        'init',
        caseTree.value.map((e) => e.name)
      );
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
          await deleteCaseModuleTree(node.id);
          Message.success(t('caseManagement.featureCase.deleteSuccess'));
          initModules(selectedNodeKeys.value[0] === node.id);
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
    emits('caseNodeSelect', selectedKeys, offspringIds);
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
      await moveCaseModuleTree({
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
      initModules();
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
      await createCaseModuleTree(params);
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
      await updateCaseModuleTree(params);
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
      height: 'calc(100vh - 366px)',
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
