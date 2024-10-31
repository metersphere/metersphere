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
    <TreeFolderAll
      ref="treeFolderAllRef"
      v-model:isExpandAll="isExpandAll"
      :protocol-key="ProtocolKeyEnum.TEST_PLAN_API_CASE_PROTOCOL"
      :active-folder="activeFolder"
      :folder-name="t('testPlan.testPlanIndex.apiCase')"
      :all-count="allCount"
      :show-expand-api="false"
      @set-active-folder="setActiveFolder"
      @selected-protocols-change="selectedProtocolsChange"
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
  import { computed, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { useVModel } from '@vueuse/core';

  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import TreeFolderAll from '@/views/api-test/components/treeFolderAll.vue';

  import { getApiCaseModule } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { mapTree } from '@/utils';
  import { getNodeParentId } from '@/utils/tree';

  import { ModuleTreeNode } from '@/models/common';
  import { ProtocolKeyEnum } from '@/enums/apiEnum';

  const props = defineProps<{
    modulesCount?: Record<string, number>; // 模块数量统计对象
    selectedKeys: string[]; // 选中的节点 key
    treeType: 'MODULE' | 'COLLECTION';
  }>();
  const emit = defineEmits<{
    (e: 'folderNodeSelect', ids: string[], _offspringIds: string[], parentId?: string): void;
    (e: 'init', params: ModuleTreeNode[]): void;
    (e: 'changeProtocol', selectedProtocols: string[]): void;
  }>();

  const route = useRoute();
  const { t } = useI18n();

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
  const folderTree = ref<ModuleTreeNode[]>([]);
  const loading = ref(false);
  const selectedKeys = useVModel(props, 'selectedKeys', emit);
  const treeFolderAllRef = ref<InstanceType<typeof TreeFolderAll>>();
  const selectedProtocols = computed<string[]>(() => treeFolderAllRef.value?.selectedProtocols ?? []);
  const allProtocolList = computed<string[]>(() => treeFolderAllRef.value?.allProtocolList ?? []);

  //  初始化模块树
  async function initModules() {
    try {
      loading.value = true;
      const res = await getApiCaseModule({ testPlanId: route.query.id as string, treeType: props.treeType });
      folderTree.value = mapTree<ModuleTreeNode>(res, (node) => {
        return {
          ...node,
          count: props.modulesCount?.[node.id] || 0,
        };
      });
      emit('init', folderTree.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  // 处理文件夹树节点选中事件
  function folderNodeSelect(_selectedKeys: (string | number)[], node: MsTreeNodeData) {
    const offspringIds: string[] = [];
    mapTree(node.children || [], (e) => {
      offspringIds.push(e.id);
      return e;
    });
    activeFolder.value = node.id;
    emit('folderNodeSelect', _selectedKeys as string[], offspringIds, getNodeParentId(node));
  }

  // 初始化模块文件数量
  watch(
    () => props.modulesCount,
    (obj) => {
      folderTree.value = mapTree<ModuleTreeNode>(folderTree.value, (node) => {
        return {
          ...node,
          count: obj?.[node.id] || 0,
        };
      });
      allCount.value = obj?.all || 0;
    }
  );

  function selectedProtocolsChange() {
    emit('changeProtocol', selectedProtocols.value);
    initModules();
  }

  onBeforeMount(() => {
    setIsExpandAll();
  });

  defineExpose({
    allProtocolList,
    initModules,
    setActiveFolder,
  });
</script>
