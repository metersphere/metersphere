<template>
  <TreeFolderAll
    v-model:selectedProtocols="selectedProtocols"
    :active-folder="activeFolder"
    :folder-name="props.folderName"
    :all-count="allCount"
    :show-expand-api="false"
    :not-show-operation="props.activeTab !== 'API'"
    @set-active-folder="setActiveFolder"
    @selected-protocols-change="selectedProtocolsChange"
  />
  <a-divider class="my-[8px] mt-0" />
  <div class="mb-[8px] flex items-center gap-[8px]">
    <a-input
      v-model:model-value="moduleKeyword"
      :placeholder="t('caseManagement.caseReview.folderSearchPlaceholder')"
      allow-clear
      :max-length="255"
    />
    <a-tooltip :content="isExpandAll ? t('common.collapseAllSubModule') : t('common.expandAllSubModule')">
      <a-button
        type="outline"
        class="expand-btn arco-btn-outline--secondary"
        @click="() => (isExpandAll = !isExpandAll)"
      >
        <MsIcon v-if="isExpandAll" type="icon-icon_comment_collapse_text_input" />
        <MsIcon v-else type="icon-icon_comment_expand_text_input" />
      </a-button>
    </a-tooltip>
  </div>
  <a-spin class="w-full" :loading="moduleLoading">
    <MsTree
      v-model:selected-keys="selectedKeys"
      :data="caseTree"
      :keyword="moduleKeyword"
      :empty-text="t('common.noData')"
      :virtual-list-props="virtualListProps"
      :field-names="{
        title: 'name',
        key: 'id',
        children: 'children',
        count: 'count',
      }"
      :expand-all="isExpandAll"
      block-node
      title-tooltip-position="top"
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
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import TreeFolderAll from '@/views/api-test/components/treeFolderAll.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { mapTree } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';
  import { CaseModulesApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  import { getModuleTreeFunc } from './utils/moduleTree';

  const { t } = useI18n();

  const props = defineProps<{
    modulesCount?: Record<string, number>; // 模块数量统计对象
    selectedKeys: string[]; // 选中的节点 key
    currentProject: string;
    getModulesApiType: CaseModulesApiTypeEnum[keyof CaseModulesApiTypeEnum];
    activeTab: keyof typeof CaseLinkEnum;
    extraModulesParams?: Record<string, any>; // 获取模块树请求额外参数
    showType?: string;
    folderName: string;
  }>();

  const emit = defineEmits<{
    (e: 'folderNodeSelect', ids: string[], _offspringIds: string[], nodeName?: string): void;
    (e: 'init', params: ModuleTreeNode[], selectedProtocols?: string[]): void;
    (e: 'changeProtocol', selectedProtocols: string[]): void;
    (e: 'update:selectedKeys', selectedKeys: string[]): void;
  }>();

  const selectedKeys = useVModel(props, 'selectedKeys', emit);
  const moduleKeyword = ref('');
  const activeFolder = ref<string>('all');
  const allCount = ref(0);
  const isExpandAll = ref(false);
  const caseTree = ref<ModuleTreeNode[]>([]);
  const moduleLoading = ref(false);

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 180px)',
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
    };
  });

  function setActiveFolder(id: string) {
    activeFolder.value = id;
    emit('folderNodeSelect', [id], [], props.folderName);
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
    emit('folderNodeSelect', _selectedKeys as string[], offspringIds, node.name);
  }

  const selectedProtocols = ref<string[]>([]);
  /**
   * 初始化模块树
   */
  async function initModules(setDefault = false) {
    try {
      moduleLoading.value = true;
      const getModuleParams = {
        projectId: props.currentProject,
        ...props.extraModulesParams,
        protocols:
          props.activeTab === CaseLinkEnum.API && props.showType === 'API' ? selectedProtocols.value : undefined,
      };

      const res = await getModuleTreeFunc(props.getModulesApiType, props.activeTab, getModuleParams);
      caseTree.value = mapTree<ModuleTreeNode>(res, (node) => {
        return {
          ...node,
          count: props.modulesCount?.[node.id] || 0,
        };
      });
      if (setDefault) {
        setActiveFolder('all');
      }
      emit('init', caseTree.value, selectedProtocols.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      moduleLoading.value = false;
    }
  }

  function selectedProtocolsChange() {
    emit('changeProtocol', selectedProtocols.value);
    initModules();
  }

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
      allCount.value = obj?.all || 0;
    }
  );

  watch(
    () => props.showType,
    (val) => {
      if (val) {
        selectedProtocolsChange();
      }
    }
  );

  watch(
    () => props.currentProject,
    (val) => {
      if (val) {
        initModules(true);
      }
    }
  );
</script>

<style scoped lang="less">
  .folder {
    @apply flex cursor-pointer items-center justify-between;

    padding: 8px 4px;
    border-radius: var(--border-radius-small);
    &:hover {
      background-color: rgb(var(--primary-1));
    }
    .folder-text {
      @apply flex cursor-pointer items-center;
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
  .footer {
    @apply flex items-center justify-between;

    margin: auto -16px -16px;
    padding: 12px 16px;
    box-shadow: 0 -1px 4px 0 rgb(31 35 41 / 10%);
  }
  .expand-btn {
    padding: 8px;
    .arco-icon {
      color: var(--color-text-4);
    }
    &:hover {
      border-color: rgb(var(--primary-5)) !important;
      background-color: rgb(var(--primary-1)) !important;
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
</style>
