<template>
  <div>
    <a-input
      v-model:model-value="moduleKeyword"
      :placeholder="
        props.treeType === 'MODULE'
          ? t('caseManagement.caseReview.folderSearchPlaceholder')
          : t('testPlan.testPlanGroup.newPlanPlaceHolder')
      "
      allow-clear
      class="mb-[8px]"
      :max-length="255"
    />
    <MsFolderAll
      v-model:isExpandAll="isExpandAll"
      :active-folder="activeFolder"
      :folder-name="t('testPlan.testPlanIndex.functionalUseCase')"
      :all-count="allCount"
      @set-active-folder="setActiveFolder"
    />
    <a-spin class="min-h-[200px] w-full" :loading="loading">
      <MsTree
        :selected-keys="selectedKeys"
        :data="folderTree"
        :keyword="moduleKeyword"
        :default-expand-all="isExpandAll"
        :expand-all="isExpandAll"
        :empty-text="t('common.noMatchData')"
        :draggable="false"
        :virtual-list-props="virtualListProps"
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
          count: 'count',
        }"
        block-node
        @select="folderNodeSelect"
      >
        <template #title="nodeData">
          <div class="inline-flex w-full gap-[8px]">
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
  import { useRoute } from 'vue-router';
  import { useVModel } from '@vueuse/core';

  import MsFolderAll from '@/components/business/ms-folder-all/index.vue';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import { useI18n } from '@/hooks/useI18n';
  import useTestPlanFeatureCaseStore from '@/store/modules/testPlan/testPlanFeatureCase';
  import { mapTree } from '@/utils';
  import { getNodeParentId } from '@/utils/tree';

  import { ModuleTreeNode } from '@/models/common';

  const props = defineProps<{
    modulesCount?: Record<string, number>; // 模块数量统计对象
    selectedKeys: string[]; // 选中的节点 key
    treeType: 'MODULE' | 'COLLECTION';
  }>();
  const emit = defineEmits<{
    (e: 'folderNodeSelect', ids: string[], _offspringIds: string[], parentId?: string): void;
    (e: 'init', params: ModuleTreeNode[]): void;
  }>();

  const route = useRoute();
  const { t } = useI18n();
  const testPlanFeatureCaseStore = useTestPlanFeatureCaseStore();

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 260px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });

  const activeFolder = ref<string>('all');
  const allCount = ref(0);
  const isExpandAll = ref<boolean | undefined>(undefined);
  function setIsExpandAll() {
    if (props.treeType === 'COLLECTION') {
      isExpandAll.value = undefined;
    } else {
      isExpandAll.value = false;
    }
  }
  watch(
    () => props.treeType,
    () => {
      setIsExpandAll();
    }
  );

  function setActiveFolder(id: string) {
    activeFolder.value = id;
    emit('folderNodeSelect', [id], []);
  }

  const moduleKeyword = ref('');
  const folderTree = computed(() => testPlanFeatureCaseStore.moduleTree);
  const loading = computed(() => testPlanFeatureCaseStore.loading);

  const selectedKeys = useVModel(props, 'selectedKeys', emit);

  /**
   * 初始化模块树
   */
  async function initModules() {
    await testPlanFeatureCaseStore.initModules(route.query.id as string, props.treeType);
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
    emit('folderNodeSelect', _selectedKeys as string[], offspringIds, getNodeParentId(node));
  }

  /**
   * 选中父节点
   * @param tree 原来的模块树
   */
  function selectParentNode(tree: ModuleTreeNode[]) {
    mapTree(tree || [], (e) => {
      if (e.id === selectedKeys.value[0]) {
        if (e.parentId) {
          selectedKeys.value = [e.parentId];
          folderNodeSelect([e.parentId], e.parent);
        } else {
          setActiveFolder('all');
        }
        return e;
      }
      return e;
    });
  }

  onBeforeMount(() => {
    setIsExpandAll();
    initModules();
  });

  /**
   * 初始化模块文件数量
   */
  watch(
    () => props.modulesCount,
    (obj) => {
      const tree = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: obj?.[node.id] || 0,
        };
      });
      testPlanFeatureCaseStore.setModulesTree(tree);
      allCount.value = obj?.all || 0;
    }
  );

  defineExpose({
    setActiveFolder,
    initModules,
    selectParentNode,
  });
</script>
