<template>
  <div class="pageWrap">
    <MsSplitBox>
      <template #left>
        <div class="p-[24px]">
          <a-input-search
            v-model:model-value="groupKeyword"
            :placeholder="t('caseManagement.featureCase.searchTip')"
            allow-clear
            class="mb-[16px]"
          ></a-input-search>
          <div class="feature-case">
            <div class="case h-[38px]">
              <div class="flex items-center" :class="getActiveClass('all')" @click="setActiveFolder('all')">
                <MsIcon type="icon-icon_folder_filled1" class="folder-icon" />
                <div class="folder-name mx-[4px]">{{ t('caseManagement.featureCase.allCase') }}</div>
                <div class="folder-count">({{ recycleModulesCount.all || 0 }})</div></div
              >
              <div class="ml-auto flex items-center">
                <a-tooltip
                  :content="
                    isExpandAll ? t('project.fileManagement.collapseAll') : t('project.fileManagement.expandAll')
                  "
                >
                  <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" @click="expandHandler">
                    <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
                  </MsButton>
                </a-tooltip>
              </div>
            </div>
            <a-divider class="my-[8px]" />
            <a-spin class="w-full" :style="{ height: `calc(100vh - 274px)` }" :loading="loading">
              <MsTree
                v-model:focus-node-key="focusNodeKey"
                :selected-keys="selectedKeys"
                :data="caseTree"
                :keyword="groupKeyword"
                :expand-all="isExpandAll"
                :empty-text="t('caseManagement.featureCase.caseEmptyRecycle')"
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
              >
                <template #title="nodeData">
                  <div class="inline-flex w-full" @click="setFocusKey(nodeData)">
                    <div class="one-line-text w-[calc(100%-32px)] text-[var(--color-text-1)]">{{ nodeData.name }}</div>
                    <div class="ml-[4px] text-[var(--color-text-4)]">({{ nodeData.count || 0 }})</div>
                  </div>
                </template>
              </MsTree>
            </a-spin>
          </div>
        </div>
      </template>
      <template #right>
        <div class="p-[24px]">
          <div class="page-header mb-4 h-[34px]">
            <div class="text-[var(--color-text-1)]"
              >{{ currentModuleName }}
              <span class="text-[var(--color-text-4)]"> ({{ recycleModulesCount[activeFolder] || 0 }})</span></div
            >
            <div class="flex w-[80%] items-center justify-end">
              <a-select class="w-[240px]" :placeholder="t('caseManagement.featureCase.versionPlaceholder')">
                <a-option v-for="version of versionOptions" :key="version.id" :value="version.id">{{
                  version.name
                }}</a-option>
              </a-select>
              <a-input-search
                v-model="keyword"
                :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
                allow-clear
                class="mx-[8px] w-[240px]"
                @search="searchList"
                @press-enter="searchList"
              ></a-input-search>
            </div>
          </div>
          <ms-base-table
            v-bind="propsRes"
            :action-config="tableBatchActions"
            @selected-change="handleTableSelect"
            v-on="propsEvent"
            @batch-action="handleTableBatch"
          >
            <template #reviewStatus="{ record }">
              <MsIcon
                :type="getStatusText(record.reviewStatus)?.iconType || ''"
                class="mr-1"
                :class="[getReviewStatusClass(record.reviewStatus)]"
              ></MsIcon>
              <span>{{ getStatusText(record.reviewStatus)?.statusType || '' }} </span>
            </template>
            <template #lastExecuteResult="{ record }">
              <MsIcon
                :type="getStatusText(record.lastExecuteResult)?.iconType || ''"
                class="mr-1"
                :class="[getReviewStatusClass(record.lastExecuteResult)]"
              ></MsIcon>
              <span>{{ getStatusText(record.lastExecuteResult)?.statusType || '' }}</span>
            </template>
            <template #moduleId="{ record }">
              <a-tooltip :content="getModules(record.moduleId)" position="top">
                <span class="one-line-text inline-block">{{ getModules(record.moduleId) }}</span>
              </a-tooltip>
            </template>
            <template #operation="{ record }">
              <MsButton @click="recoverCase(record.id)">{{ t('caseManagement.featureCase.batchRecover') }}</MsButton>
              <MsButton class="!mr-0" @click="handleBatchCleanOut(record)">{{
                t('caseManagement.featureCase.batchCleanOut')
              }}</MsButton>
            </template>
          </ms-base-table>
        </div>
      </template>
    </MsSplitBox>
  </div>
</template>

<script setup lang="ts">
  /*
   * @description 功能用例-回收站
   */
  import { computed, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import {
    batchDeleteRecycleCase,
    deleteRecycleCaseList,
    getRecycleListRequest,
    getTrashCaseModuleTree,
    recoverRecycleCase,
    restoreCaseList,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { characterLimit, findNodeByKey, findNodePathByKey, mapTree } from '@/utils';

  import type {
    BatchMoveOrCopyType,
    CaseManagementTable,
    CaseModuleQueryParams,
  } from '@/models/caseManagement/featureCase';
  import type { TableQueryParams } from '@/models/common';
  import { ModuleTreeNode } from '@/models/projectManagement/file';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { getReviewStatusClass, getStatusText } from './utils';
  import debounce from 'lodash-es/debounce';

  const tableStore = useTableStore();
  const featureCaseStore = useFeatureCaseStore();

  const { t } = useI18n();
  const { openModal } = useModal();

  const activeCaseType = ref<'folder' | 'module'>('folder'); // 激活用例类型

  const appStore = useAppStore();

  const currentProjectId = computed(() => appStore.currentProjectId);

  const versionOptions = ref([
    {
      id: '1001',
      name: 'v_1.0',
    },
  ]);

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getRecycleListRequest,
    {
      tableKey: TableKeyEnum.FILE_MANAGEMENT_CASE_RECYCLE,
      scroll: { x: 3200 },
      selectable: true,
      showSetting: true,
      heightUsed: 340,
      enableDrag: true,
    },
    (record) => ({
      ...record,
      tags: (JSON.parse(record.tags) || []).map((item: string, i: number) => {
        return {
          id: `${record.id}-${i}`,
          name: item,
        };
      }),
    })
  );

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'id',
      width: 200,
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      showTooltip: true,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 300,
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnLevel',
      dataIndex: 'level',
      showInTable: true,
      width: 200,
      showTooltip: true,
      ellipsis: true,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCaseState',
      dataIndex: 'caseState',
      showInTable: true,
      width: 200,
      showTooltip: true,
      ellipsis: true,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnReviewResult',
      dataIndex: 'reviewStatus',
      slotName: 'reviewStatus',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnExecutionResult',
      dataIndex: 'lastExecuteResult',
      slotName: 'lastExecuteResult',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnVersion',
      slotName: 'versionId',
      dataIndex: 'versionId',
      width: 300,
      showTooltip: true,
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnModule',
      slotName: 'moduleId',
      showInTable: true,
      width: 300,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnTag',
      slotName: 'tags',
      dataIndex: 'tags',
      showInTable: true,
      isTag: true,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'createUser',
      dataIndex: 'createUser',
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnUpdateUser',
      slotName: 'updateUser',
      dataIndex: 'updateUser',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnUpdateTime',
      slotName: 'updateTime',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 140,
      showInTable: true,
      showDrag: false,
    },
  ];

  const tableBatchActions = {
    baseAction: [
      {
        label: 'caseManagement.featureCase.batchRecover',
        eventTag: 'batchRecover',
      },
      {
        label: 'caseManagement.featureCase.batchCleanOut',
        eventTag: 'batchCleanOut',
        danger: true,
      },
    ],
  };

  const tableSelected = ref<(string | number)[]>([]);
  function handleTableSelect(selectArr: (string | number)[]) {
    tableSelected.value = selectArr;
  }

  const isExpandAll = ref(false);

  // 全部展开或折叠
  function expandHandler() {
    isExpandAll.value = !isExpandAll.value;
  }

  const activeFolder = ref<string>('all');

  // 获取激活用例类型样式
  function getActiveClass(type: string) {
    return activeFolder.value === type ? 'folder-text case-active' : 'folder-text';
  }

  // 设置激活文件类型
  function setActiveFolder(type: string) {
    activeFolder.value = type;
    if (type === 'all') {
      activeCaseType.value = 'folder';
    }
  }

  // 选中节点
  const selectedKeys = computed({
    get: () => [activeFolder.value],
    set: (val) => val,
  });

  const offspringIds = ref<string[]>([]);

  const selectedKeysNode = ref<(string | number)[]>([]);

  const focusNodeKey = ref<string>('');
  // 用例树节点选中事件
  const caseNodeSelect = (selectedNodeKeys: (string | number)[] | string[], node: MsTreeNodeData) => {
    [activeFolder.value] = selectedNodeKeys as string[];
    offspringIds.value = [];
    mapTree(node.children || [], (e) => {
      offspringIds.value.push(e.id);
      return e;
    });
    focusNodeKey.value = '';
  };

  const setFocusKey = (node: MsTreeNodeData) => {
    focusNodeKey.value = node.id || '';
  };

  const virtualListProps = computed(() => {
    return {
      height: 'calc(100vh - 270px)',
    };
  });

  const loading = ref(false);
  const caseTree = ref<ModuleTreeNode[]>([]);
  const recycleModulesCount = ref<Record<string, any>>({});
  const groupKeyword = ref<string>('');

  /**
   * @param 获取回收站模块
   */
  async function getRecycleModules(isSetDefaultKey = false) {
    try {
      loading.value = true;
      const res = await getTrashCaseModuleTree(currentProjectId.value);
      caseTree.value = mapTree<ModuleTreeNode>(res, (e) => {
        return {
          ...e,
          hideMoreAction: e.id === 'root',
          draggable: false,
          disabled: false,
          count: recycleModulesCount.value?.[e.id] || 0,
        };
      });
      if (isSetDefaultKey) {
        selectedKeysNode.value = [caseTree.value[0].id];
      }
    } catch (error) {
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  const currentModuleName = computed(() => {
    return activeFolder.value === 'all'
      ? t('caseManagement.featureCase.allCase')
      : findNodeByKey<Record<string, any>>(caseTree.value, activeFolder.value, 'id')?.name;
  });

  const keyword = ref<string>('');

  const searchParams = ref<TableQueryParams>({
    projectId: currentProjectId.value,
    moduleIds: [],
  });

  // 回收站模块树count参数
  const emitTableParams: CaseModuleQueryParams = {
    keyword: keyword.value,
    moduleIds: [],
    projectId: currentProjectId.value,
    current: propsRes.value.msPagination?.current,
    pageSize: propsRes.value.msPagination?.pageSize,
  };

  // 获取回收站模块数量
  function initRecycleModulesCount() {
    featureCaseStore.getRecycleMModulesCountCount(emitTableParams);
  }

  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  // 获取批量操作参数
  function getBatchParams(): BatchMoveOrCopyType {
    return {
      excludeIds: batchParams.value.excludeIds,
      selectAll: batchParams.value.selectAll,
      selectIds: batchParams.value.selectedIds,
      condition: {
        keyword: keyword.value,
      },
      moduleIds: searchParams.value.moduleIds,
      projectId: currentProjectId.value,
    };
  }

  // 批量恢复
  async function handleBatchRecover() {
    try {
      await restoreCaseList(getBatchParams());
      Message.success(t('caseManagement.featureCase.recoveredSuccessfully'));
      loadList();
      resetSelector();
      initRecycleModulesCount();
    } catch (error) {
      console.log(error);
    }
  }
  // 批量删除
  async function handleBatchDelete() {
    openModal({
      type: 'error',
      title: t('caseManagement.featureCase.batchDelete', { number: batchParams.value.currentSelectCount }),
      content: t('caseManagement.featureCase.cleanOutDeleteTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await batchDeleteRecycleCase(getBatchParams());
          Message.success(t('common.deleteSuccess'));
          loadList();
          resetSelector();
          initRecycleModulesCount();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  // 批量操作
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchParams.value = { ...params };
    if (event.eventTag === 'batchRecover') {
      handleBatchRecover();
    } else {
      handleBatchDelete();
    }
  }

  // 获取对应模块路径name
  function getModules(moduleIds: string) {
    const modules = findNodePathByKey(caseTree.value, moduleIds, undefined, 'id');
    if (modules) {
      const moduleName = (modules || []).treePath.map((item: any) => item.name);

      if (moduleName.length === 1) {
        return moduleName[0];
      }
      return `/${moduleName.join('/')}`;
    }
  }

  // 获取用例参数
  function getLoadListParams() {
    if (activeFolder.value === 'all') {
      searchParams.value.moduleIds = [];
    } else {
      searchParams.value.moduleIds = [activeFolder.value, ...offspringIds.value];
    }
    setLoadListParams({
      ...searchParams.value,
      keyword: keyword.value,
    });
  }

  // 初始化回收站列表
  function initRecycleList() {
    getLoadListParams();
    loadList();
  }

  const searchList = debounce(() => {
    getLoadListParams();
    loadList();
  }, 100);

  // 恢复用例
  async function recoverCase(id: string) {
    try {
      await recoverRecycleCase(id);
      Message.success(t('caseManagement.featureCase.recoveredSuccessfully'));
      loadList();
      resetSelector();
      initRecycleModulesCount();
    } catch (error) {
      console.log(error);
    }
  }

  // 彻底删除
  function handleBatchCleanOut(record: CaseManagementTable) {
    openModal({
      type: 'error',
      title: t('caseManagement.featureCase.deleteCaseTitle', { name: characterLimit(record.name) }),
      content: t('caseManagement.featureCase.cleanOutDeleteTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await deleteRecycleCaseList(record.id);
          Message.success(t('common.deleteSuccess'));
          loadList();
          initRecycleModulesCount();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  watch(
    () => activeFolder.value,
    () => {
      initRecycleList();
    }
  );

  watch(
    () => featureCaseStore.recycleModulesCount,
    (val) => {
      recycleModulesCount.value = { ...val };
      caseTree.value = mapTree<ModuleTreeNode>(caseTree.value, (node) => {
        return {
          ...node,
          count: val?.[node.id] || 0,
        };
      });
    }
  );

  onMounted(() => {
    initRecycleList();
    getRecycleModules();
    initRecycleModulesCount();
  });
  tableStore.initColumn(TableKeyEnum.FILE_MANAGEMENT_CASE_RECYCLE, columns, 'drawer');
</script>

<style scoped lang="less">
  .pageWrap {
    min-width: 1000px;
    height: calc(100vh - 126px);
    border-radius: var(--border-radius-large);
    @apply bg-white;
    .back {
      margin-right: 8px;
      width: 20px;
      height: 20px;
      border: 1px solid #ffffff;
      background: linear-gradient(90deg, rgb(var(--primary-9)) 3.36%, #ffffff 100%);
      box-shadow: 0 0 7px rgb(15 0 78 / 9%);
      .arco-icon {
        color: rgb(var(--primary-5));
      }
      @apply flex cursor-pointer items-center rounded-full;
    }
    .case {
      padding: 8px 4px;
      border-radius: var(--border-radius-small);
      @apply flex cursor-pointer  items-center justify-between;
      &:hover {
        background-color: rgb(var(--primary-1));
      }
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
      .case-active {
        .folder-icon,
        .folder-name,
        .folder-count {
          color: rgb(var(--primary-5));
        }
      }
    }
  }
  .page-header {
    @apply flex items-center justify-between;
  }
</style>
