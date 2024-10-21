<template>
  <TreeFolderAll
    ref="treeFolderAllRef"
    :protocol-key="ProtocolKeyEnum.ASSOCIATE_CASE_PROTOCOL"
    :active-folder="activeFolder"
    :folder-name="props.folderName"
    :all-count="allCount"
    :show-expand-api="false"
    :not-show-operation="props.activeTab !== 'API'"
    @set-active-folder="setActiveFolder"
    @selected-protocols-change="selectedProtocolsChange"
  />
  <div class="my-[8px] flex items-center gap-[8px]">
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
  <slot></slot>
  <a-spin class="w-full pl-[8px]" :loading="moduleLoading">
    <MsTree
      v-model:selected-keys="selectedKeys"
      v-model:checked-keys="checkedKeys"
      v-model:halfCheckedKeys="halfCheckedKeys"
      v-model:data="caseTree"
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
      checkable
      check-strictly
      @select="folderNodeSelect"
      @check="checkNode"
    >
      <template #title="nodeData">
        <div class="inline-flex w-full gap-[8px]">
          <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
          <div class="ms-tree-node-count ml-[4px] flex items-center text-[var(--color-text-brand)]">
            {{ nodeData.count || 0 }}/{{ nodeData.totalCount }}
          </div>
        </div>
      </template>
      <template #extra="nodeData">
        <MsButton
          v-if="nodeData.children && nodeData.children.length"
          @click="selectCurrent(nodeData, !!checkedKeys.includes(nodeData.id))"
        >
          {{
            checkedKeys.includes(nodeData.id)
              ? t('ms.case.associate.cancelCurrent')
              : t('ms.case.associate.selectCurrent')
          }}
        </MsButton>
      </template>
    </MsTree>
  </a-spin>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useVModel } from '@vueuse/core';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import useCalculateTreeCount from '@/components/business/ms-associate-case/useCalculateTreeCount';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import TreeFolderAll from '@/views/api-test/components/treeFolderAll.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { mapTree } from '@/utils';

  import { ModuleTreeNode } from '@/models/common';
  import { ProtocolKeyEnum } from '@/enums/apiEnum';
  import { CaseModulesApiTypeEnum } from '@/enums/associateCaseEnum';
  import { CaseLinkEnum } from '@/enums/caseEnum';

  import { getModuleTreeFunc } from './utils/moduleTree';

  const { t } = useI18n();

  const props = defineProps<{
    modulesCount: Record<string, number>; // 模块数量统计对象
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
    (e: 'update:halfCheckedKeys', halfCheckedKeys: string[]): void;
    (e: 'check', _checkedKeys: Array<string | number>, checkedNodes: MsTreeNodeData): void;
    (e: 'selectParent', node: MsTreeNodeData, isSelected: boolean): void;
    (e: 'checkAllModule', isCheckedAll: boolean): void;
  }>();

  const selectedKeys = useVModel(props, 'selectedKeys', emit);
  const checkedKeys = defineModel<(string | number)[]>('checkedKeys', {
    required: true,
  });
  const halfCheckedKeys = defineModel<(string | number)[]>('halfCheckedKeys', {
    required: true,
  });
  const moduleKeyword = ref('');
  const activeFolder = ref<string>('all');
  const allCount = ref(0);
  const isExpandAll = ref(false);
  const caseTree = ref<ModuleTreeNode[]>([]);
  const moduleLoading = ref(false);

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 250px)',
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

  const treeFolderAllRef = ref<InstanceType<typeof TreeFolderAll>>();
  const selectedProtocols = computed<string[]>(() => treeFolderAllRef.value?.selectedProtocols ?? []);
  const allProtocolList = computed<string[]>(() => treeFolderAllRef.value?.allProtocolList ?? []);
  const { calculateTreeCount } = useCalculateTreeCount();
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
      caseTree.value = calculateTreeCount(res, props.modulesCount);
      allCount.value = props.modulesCount.all || 0;
      emit('init', caseTree.value, selectedProtocols.value);
      if (setDefault) {
        setActiveFolder('all');
      }
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

  function checkNode(_checkedKeys: Array<string | number>, checkedNodes: MsTreeNodeData) {
    emit('check', _checkedKeys, checkedNodes);
  }

  function selectCurrent(node: MsTreeNodeData, isSelected: boolean) {
    emit('selectParent', node, isSelected);
  }

  /**
   * 初始化模块文件数量
   */
  watch(
    () => props.modulesCount,
    (val) => {
      if (val) {
        caseTree.value = calculateTreeCount(caseTree.value, props.modulesCount);
        allCount.value = val.all || 0;
        emit('init', caseTree.value, selectedProtocols.value);
      }
    },
    {
      deep: true,
      immediate: true,
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

  defineExpose({
    setActiveFolder,
    allProtocolList,
  });
</script>

<style scoped lang="less">
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
