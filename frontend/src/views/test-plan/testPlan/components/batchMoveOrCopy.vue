<template>
  <a-modal
    v-model:visible="showModalVisible"
    title-align="start"
    class="ms-modal-no-padding ms-modal-small"
    :mask-closable="false"
    :ok-text="props.mode === 'move' ? t('common.move') : t('common.copy')"
    :ok-button-props="{ disabled: innerSelectedModuleKeys.length === 0 }"
    :cancel-button-props="{ disabled: props.okLoading }"
    :on-before-ok="handleCaseMoveOrCopy"
    @close="handleMoveCaseModalCancel"
  >
    <template #title>
      <div class="flex w-full items-center justify-between">
        <div>
          {{ props.mode === 'move' ? t('common.batchMove') : t('common.batchCopy') }}
          <span class="ml-[4px] text-[var(--color-text-4)]">
            {{ t('testPlan.testPlanIndex.selectedCount', { count: props.currentSelectCount }) }}
          </span>
        </div>
      </div>
    </template>
    <a-input
      v-model:model-value="moduleKeyword"
      :placeholder="t('caseManagement.caseReview.folderSearchPlaceholder')"
      allow-clear
      :max-length="255"
      class="mb-4"
    />
    <a-spin class="min-h-[400px] w-full" :loading="loading">
      <MsTree
        v-model:focus-node-key="focusNodeKey"
        v-model:selected-keys="innerSelectedModuleKeys"
        :data="treeData"
        :keyword="moduleKeyword"
        :default-expand-all="props.isExpandAll"
        :expand-all="isExpandAll"
        :empty-text="t(props.emptyText)"
        :draggable="false"
        :virtual-list-props="virtualListProps"
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
          count: 'count',
        }"
        block-node
        title-tooltip-position="top"
        @select="nodeSelect"
      >
        <template #title="nodeData">
          <div class="inline-flex w-full">
            <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
          </div>
        </template>
      </MsTree>
    </a-spin>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { mapTree } from '@/utils';

  import type { TableQueryParams } from '@/models/common';
  import { ModuleTreeNode } from '@/models/common';

  const appStore = useAppStore();
  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      mode: 'move' | 'copy';
      currentSelectCount: number;
      visible: boolean;
      isExpandAll?: boolean;
      getModuleTreeApi: (params: TableQueryParams) => Promise<ModuleTreeNode[]>; // 模块树接口
      selectedNodeKeys: (string | number)[];
      okLoading: boolean;
      emptyText?: string;
    }>(),
    {
      isExpandAll: false,
      emptyText: 'common.noData',
    }
  );

  const emit = defineEmits<{
    (e: 'update:visible', val: boolean): void;
    (e: 'update:selectedNodeKeys', val: string[]): void;
    (e: 'save'): void;
  }>();

  const showModalVisible = useVModel(props, 'visible', emit);
  const innerSelectedModuleKeys = useVModel(props, 'selectedNodeKeys', emit);

  const moduleKeyword = ref<string>('');

  const focusNodeKey = ref<string>('');

  // 批量移动和复制
  async function handleCaseMoveOrCopy() {
    emit('save');
  }

  function handleMoveCaseModalCancel() {
    showModalVisible.value = false;
    innerSelectedModuleKeys.value = [];
    moduleKeyword.value = '';
  }

  const loading = ref<boolean>(false);

  const treeData = ref<ModuleTreeNode[]>([]);

  /**
   * 初始化模块树
   * @param isSetDefaultKey 是否设置第一个节点为选中节点
   */
  async function initModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await props.getModuleTreeApi({ projectId: appStore.currentProjectId });
      treeData.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: true,
          draggable: false,
          disabled: false,
        };
      });
      if (isSetDefaultKey) {
        innerSelectedModuleKeys.value = [treeData.value[0].id];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const virtualListProps = computed(() => {
    return {
      height: 'calc(60vh - 190px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15,
    };
  });

  // 节点选中事件
  const nodeSelect = (selectedKeys: (string | number)[], node: MsTreeNodeData) => {
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });
    innerSelectedModuleKeys.value = selectedKeys;
  };

  watch(
    () => showModalVisible.value,
    (val) => {
      if (val) {
        initModules();
      }
    }
  );
</script>

<style scoped lang="less"></style>
