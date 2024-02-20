<template>
  <div class="pageWrap">
    <MsSplitBox>
      <template #first>
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
            <a-spin class="h-[calc(100vh-274px)] w-full" :loading="loading">
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
      <template #second>
        <div class="p-[24px]">
          <MsAdvanceFilter
            :filter-config-list="filterConfigList"
            :custom-fields-config-list="searchCustomFields"
            :row-count="filterRowCount"
            @keyword-search="initRecycleList"
            @adv-search="handleAdvSearch"
          >
            <template #left>
              <div class="text-[var(--color-text-1)]"
                >{{ moduleNamePath }}
                <span class="text-[var(--color-text-4)]"> ({{ recycleModulesCount[activeFolder] || 0 }})</span></div
              >
            </template>
          </MsAdvanceFilter>
          <ms-base-table
            class="my-4"
            v-bind="propsRes"
            :action-config="tableBatchActions"
            @selected-change="handleTableSelect"
            v-on="propsEvent"
            @batch-action="handleTableBatch"
          >
            <template #num="{ record }">
              <span class="flex w-full">{{ record.num }}</span>
            </template>
            <template #caseLevel="{ record }">
              <caseLevel :case-level="(getCaseLevels(record.customFields) as CaseLevel)" />
            </template>
            <template #reviewStatus="{ record }">
              <MsIcon
                :type="statusIconMap[record.reviewStatus]?.icon || ''"
                class="mr-1"
                :class="[statusIconMap[record.reviewStatus].color]"
              ></MsIcon>
              <span>{{ statusIconMap[record.reviewStatus]?.statusText || '' }} </span>
            </template>
            <template #reviewStatusFilter="{ columnConfig }">
              <a-trigger
                v-model:popup-visible="statusFilterVisible"
                trigger="click"
                @popup-visible-change="handleFilterHidden"
              >
                <a-button type="text" class="arco-btn-text--secondary p-[8px_4px]" @click="statusFilterVisible = true">
                  <div class="font-medium">
                    {{ t(columnConfig.title as string) }}
                  </div>
                  <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
                </a-button>
                <template #content>
                  <div class="arco-table-filters-content">
                    <div class="flex items-center justify-center px-[6px] py-[2px]">
                      <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                        <a-checkbox v-for="key of Object.keys(statusIconMap)" :key="key" :value="key">
                          <MsIcon
                            :type="statusIconMap[key]?.icon || ''"
                            class="mr-1"
                            :class="[statusIconMap[key].color]"
                          ></MsIcon>
                          <span>{{ statusIconMap[key]?.statusText || '' }} </span>
                        </a-checkbox>
                      </a-checkbox-group>
                    </div>
                  </div>
                </template>
              </a-trigger>
            </template>
            <template #lastExecuteResult="{ record }">
              <MsIcon
                :type="executionResultMap[record.lastExecuteResult]?.icon || ''"
                class="mr-1"
                :class="[executionResultMap[record.lastExecuteResult].color]"
              ></MsIcon>
              <span>{{ executionResultMap[record.lastExecuteResult]?.statusText || '' }}</span>
            </template>
            <template #moduleId="{ record }">
              <a-tooltip :content="getModules(record.moduleId)" position="top">
                <span class="one-line-text inline-block">{{ getModules(record.moduleId) }}</span>
              </a-tooltip>
            </template>
            <!-- 回收站自定义字段 -->
            <template v-for="item in customFieldsColumns" :key="item.slotName" #[item.slotName]="{ record }">
              <a-tooltip
                :content="getTableFields(record.customFields, item as MsTableColumn)"
                position="top"
                :mouse-enter-delay="100"
                mini
              >
                <div>{{ getTableFields(record.customFields, item as MsTableColumn) }}</div>
              </a-tooltip>
            </template>
            <template #operation="{ record }">
              <MsButton v-permission="['FUNCTIONAL_CASE:READ+DELETE']" @click="recoverCase(record.id)">{{
                t('caseManagement.featureCase.batchRecover')
              }}</MsButton>
              <MsButton
                v-permission="['FUNCTIONAL_CASE:READ+DELETE']"
                class="!mr-0"
                @click="handleBatchCleanOut(record)"
                >{{ t('caseManagement.featureCase.batchCleanOut') }}</MsButton
              >
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

  import { CustomTypeMaps, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import type { TagType, Theme } from '@/components/pure/ms-tag/ms-tag.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import MsTree from '@/components/business/ms-tree/index.vue';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';

  import {
    batchDeleteRecycleCase,
    deleteRecycleCaseList,
    getCaseDefaultFields,
    getCustomFieldsTable,
    getRecycleListRequest,
    getTrashCaseModuleTree,
    recoverRecycleCase,
    restoreCaseList,
  } from '@/api/modules/case-management/featureCase';
  import { getProjectMemberOptions } from '@/api/modules/project-management/projectMember';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { characterLimit, findNodeByKey, findNodePathByKey, mapTree } from '@/utils';

  import type {
    BatchMoveOrCopyType,
    CaseManagementTable,
    CaseModuleQueryParams,
    CustomAttributes,
  } from '@/models/caseManagement/featureCase';
  import type { ModuleTreeNode, TableQueryParams } from '@/models/common';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { executionResultMap, getCaseLevels, getTableFields, statusIconMap } from './utils';

  const tableStore = useTableStore();
  const featureCaseStore = useFeatureCaseStore();

  const { t } = useI18n();
  const { openModal } = useModal();

  const activeCaseType = ref<'folder' | 'module'>('folder'); // 激活用例类型

  const appStore = useAppStore();

  const currentProjectId = computed(() => appStore.currentProjectId);
  const scrollWidth = ref<number>(3400);

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setAdvanceFilter } = useTable(
    getRecycleListRequest,
    {
      tableKey: TableKeyEnum.CASE_MANAGEMENT_RECYCLE_TABLE,
      scroll: { x: scrollWidth.value },
      selectable: true,
      showSetting: true,
      showJumpMethod: true,
      heightUsed: 340,
      enableDrag: true,
    },
    (record) => ({
      ...record,
      tags: (record.tags || []).map((item: string, i: number) => {
        return {
          id: `${record.id}-${i}`,
          name: item,
        };
      }),
    })
  );
  const columns: MsTableColumn = [
    {
      'title': 'caseManagement.featureCase.tableColumnID',
      'slotName': 'num',
      'dataIndex': 'num',
      'width': 200,
      'showInTable': true,
      'sortable': {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      'filter-icon-align-left': true,
      'showTooltip': true,
      'ellipsis': true,
      'showDrag': false,
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
        sorter: true,
      },
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnLevel',
      slotName: 'caseLevel',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnReviewResult',
      dataIndex: 'reviewStatus',
      slotName: 'reviewStatus',
      titleSlotName: 'reviewStatusFilter',
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
      slotName: 'versionName',
      dataIndex: 'versionName',
      width: 300,
      showTooltip: true,
      showInTable: true,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnModule',
      slotName: 'moduleId',
      dataIndex: 'moduleId',
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
      title: 'caseManagement.featureCase.tableColumnUpdateUser',
      slotName: 'updateUserName',
      dataIndex: 'updateUserName',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
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
        sorter: true,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      width: 200,
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
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 260,
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
      threshold: 200,
      fixedSize: true,
      buffer: 15, // 缓冲区默认 10 的时候，虚拟滚动的底部 padding 计算有问题
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

  const keyword = ref<string>('');

  const moduleNamePath = computed(() => {
    return activeFolder.value === 'all'
      ? t('caseManagement.featureCase.allCase')
      : findNodeByKey<Record<string, any>>(caseTree.value, featureCaseStore.moduleId[0], 'id')?.name;
  });

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
    featureCaseStore.getRecycleModulesCount(emitTableParams);
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
  const statusFilters = ref<string[]>(Object.keys(statusIconMap));
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
      filter: { reviewStatus: statusFilters.value },
    });
  }

  // 初始化回收站列表
  function initRecycleList() {
    getLoadListParams();
    loadList();
  }

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

  const filterConfigList = ref<FilterFormItem[]>([]);
  const searchCustomFields = ref<FilterFormItem[]>([]);

  const filterRowCount = ref(0);

  // 处理自定义字段列
  let customFieldsColumns: Record<string, any>[] = [];
  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);

  const initDefaultFields = ref<CustomAttributes[]>([]);

  let fullColumns: MsTableColumn = []; // 全量列表

  // 处理自定义字段展示
  async function getDefaultFields() {
    const result = await getCaseDefaultFields(currentProjectId.value);
    initDefaultFields.value = result.customFields.filter((item: any) => !item.internal);
    customFieldsColumns = initDefaultFields.value.map((item: any) => {
      return {
        title: item.fieldName,
        slotName: item.fieldId as string,
        dataIndex: item.fieldId,
        showInTable: true,
        showDrag: true,
        width: 300,
      };
    });

    fullColumns = [
      ...columns.slice(0, columns.length - 1),
      ...customFieldsColumns,
      ...columns.slice(columns.length - 1, columns.length),
    ];
    tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_RECYCLE_TABLE, fullColumns, 'drawer');
  }

  async function initFilter() {
    const result = await getCustomFieldsTable(currentProjectId.value);
    let memberOptions = await getProjectMemberOptions(appStore.currentProjectId, keyword.value);
    memberOptions = memberOptions.map((e) => ({ label: e.name, value: e.id }));
    filterConfigList.value = [
      {
        title: 'caseManagement.featureCase.tableColumnID',
        dataIndex: 'id',
        type: FilterType.INPUT,
      },
      {
        title: 'caseManagement.featureCase.tableColumnName',
        dataIndex: 'name',
        type: FilterType.INPUT,
      },
      {
        title: 'caseManagement.featureCase.tableColumnModule',
        dataIndex: 'moduleId',
        type: FilterType.TREE_SELECT,
        treeSelectData: caseTree.value,
        treeSelectProps: {
          fieldNames: {
            title: 'name',
            key: 'id',
            children: 'children',
          },
        },
      },
      {
        title: 'caseManagement.featureCase.tableColumnVersion',
        dataIndex: 'versionId',
        type: FilterType.INPUT,
      },
      {
        title: 'caseManagement.featureCase.tableColumnCreateUser',
        dataIndex: 'createUserName',
        type: FilterType.SELECT,
        selectProps: {
          mode: 'static',
          options: memberOptions,
        },
      },
      {
        title: 'caseManagement.featureCase.tableColumnCreateTime',
        dataIndex: 'createTime',
        type: FilterType.DATE_PICKER,
      },
      {
        title: 'caseManagement.featureCase.tableColumnUpdateUser',
        dataIndex: 'updateUserName',
        type: FilterType.SELECT,
        selectProps: {
          mode: 'static',
          options: memberOptions,
        },
      },
      {
        title: 'caseManagement.featureCase.tableColumnUpdateTime',
        dataIndex: 'updateTime',
        type: FilterType.DATE_PICKER,
      },
      {
        title: 'caseManagement.featureCase.tableColumnTag',
        dataIndex: 'tags',
        type: FilterType.TAGS_INPUT,
      },
    ];
    // 处理系统自定义字段
    searchCustomFields.value = result.map((item: any) => {
      const FilterTypeKey: keyof typeof FilterType = CustomTypeMaps[item.type].type;
      const formType = FilterType[FilterTypeKey];
      const formObject = CustomTypeMaps[item.type];
      const { props: formProps } = formObject;
      const currentItem: any = {
        title: item.name,
        dataIndex: item.id,
        type: formType,
      };

      if (formObject.propsKey && formProps.options) {
        formProps.options = item.options;
        currentItem[formObject.propsKey] = {
          ...formProps,
        };
      }
      return currentItem;
    });
  }

  const filterResult = ref<FilterResult>({ accordBelow: 'AND', combine: {} });
  // 当前选择的条数
  const currentSelectParams = ref<BatchActionQueryParams>({ selectAll: false, currentSelectCount: 0 });
  // 高级检索
  const handleAdvSearch = (filter: FilterResult) => {
    filterResult.value = filter;
    const { accordBelow, combine } = filter;
    setAdvanceFilter(filter);
    currentSelectParams.value = {
      ...currentSelectParams.value,
      condition: {
        keyword: keyword.value,
        searchMode: accordBelow,
        filter: propsRes.value.filter,
        combine,
      },
    };
    initRecycleList();
  };

  // 如果是用例状态
  function getCaseState(caseState: string | undefined): { type: TagType; theme: Theme } {
    switch (caseState) {
      case '已完成':
        return {
          type: 'success',
          theme: 'default',
        };
      case '进行中':
        return {
          type: 'link',
          theme: 'default',
        };

      default:
        return {
          type: 'default',
          theme: 'default',
        };
    }
  }

  const statusFilterVisible = ref(false);

  function handleFilterHidden(val: boolean) {
    if (!val) {
      initRecycleList();
    }
  }

  onMounted(() => {
    getDefaultFields();
    initFilter();
    initRecycleList();
    getRecycleModules();
    initRecycleModulesCount();
  });
  tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_RECYCLE_TABLE, columns, 'drawer');
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
