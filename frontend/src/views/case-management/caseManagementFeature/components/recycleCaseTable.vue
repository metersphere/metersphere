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
                <a-tooltip :content="isExpandAll ? t('common.collapseAllSubModule') : t('common.expandAllSubModule')">
                  <MsButton type="icon" status="secondary" class="!mr-0 p-[4px]" @click="expandHandler">
                    <MsIcon :type="isExpandAll ? 'icon-icon_folder_collapse1' : 'icon-icon_folder_expansion1'" />
                  </MsButton>
                </a-tooltip>
              </div>
            </div>
            <a-spin class="h-[calc(100vh-274px)] w-full" :loading="loading">
              <MsTree
                v-model:focus-node-key="focusNodeKey"
                :selected-keys="selectedKeys"
                :data="caseTree"
                :keyword="groupKeyword"
                :expand-all="isExpandAll"
                :empty-text="t('common.noData')"
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
                  <div class="inline-flex w-full gap-[8px]" @click="setFocusKey(nodeData)">
                    <div class="one-line-text w-full text-[var(--color-text-1)]">{{ nodeData.name }}</div>
                    <div class="ms-tree-node-count ml-[4px] text-[var(--color-text-brand)]">
                      {{ nodeData.count || 0 }}
                    </div>
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
            v-model:keyword="keyword"
            :filter-config-list="filterConfigList"
            :custom-fields-config-list="searchCustomFields"
            :count="recycleModulesCount[activeFolder] || 0"
            :name="moduleNamePath"
            :search-placeholder="t('caseManagement.featureCase.searchPlaceholder')"
            @keyword-search="fetchData"
            @refresh="fetchData()"
          />
          <ms-base-table
            class="my-4"
            v-bind="propsRes"
            :action-config="tableBatchActions"
            @selected-change="handleTableSelect"
            v-on="propsEvent"
            @batch-action="handleTableBatch"
            @module-change="initRecycleList()"
          >
            <template #num="{ record }">
              <span class="flex w-full">{{ record.num }}</span>
            </template>
            <template #caseLevel="{ record }">
              <caseLevel :case-level="(getCaseLevels(record.customFields) as CaseLevel)" />
            </template>
            <template #deleteUserName="{ record }">
              <a-tooltip :content="`${record.deleteUserName}`" position="tl">
                <div class="one-line-text">{{ record.deleteUserName }}</div>
              </a-tooltip>
            </template>
            <!-- 用例等级 -->
            <template #[FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL]="{ filterContent }">
              <caseLevel :case-level="filterContent.text" />
            </template>
            <!-- 执行结果 -->
            <template #[FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT]="{ filterContent }">
              <ExecuteStatusTag :execute-result="filterContent.value" />
            </template>
            <!-- 评审结果 -->
            <template #[FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_RESULT]="{ filterContent }">
              <MsIcon
                :type="statusIconMap[filterContent.value]?.icon"
                class="mr-1"
                :class="[statusIconMap[filterContent.value].color]"
              ></MsIcon>
              <span>{{ statusIconMap[filterContent.value]?.statusText }} </span>
            </template>
            <template #reviewStatus="{ record }">
              <MsIcon
                :type="statusIconMap[record.reviewStatus]?.icon || ''"
                class="mr-1"
                :class="[statusIconMap[record.reviewStatus].color]"
              ></MsIcon>
              <span>{{ statusIconMap[record.reviewStatus]?.statusText || '' }} </span>
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
            <template #updateUserName="{ record }">
              <span type="text" class="px-0">{{ record.updateUserName || '-' }}</span>
            </template>
            <template #createUserName="{ record }">
              <span type="text" class="px-0">{{ record.createUserName || '-' }}</span>
            </template>
            <template #deleteTime="{ record }">
              {{ dayjs(record.deleteTime).format('YYYY-MM-DD HH:mm:ss') || '-' }}
            </template>
            <!-- 回收站自定义字段 -->
            <template v-for="item in customFieldsColumns" :key="item.slotName" #[item.slotName]="{ record }">
              <a-tooltip
                :content="getTableFields(record.customFields, item as MsTableColumn, record.createUser)"
                position="top"
                :mouse-enter-delay="100"
                mini
              >
                <div>{{ getTableFields(record.customFields, item as MsTableColumn, record.createUser) }}</div>
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
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import { CustomTypeMaps, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSplitBox from '@/components/pure/ms-split-box/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteStatusTag from '@/components/business/ms-case-associate/executeResult.vue';
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
  import { getProjectOptions } from '@/api/modules/project-management/projectMember';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { characterLimit, findNodeByKey, findNodePathByKey, mapTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { CaseManagementTable, CustomAttributes } from '@/models/caseManagement/featureCase';
  import type { ModuleTreeNode, TableQueryParams } from '@/models/common';
  import { FilterType } from '@/enums/advancedFilterEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { executionResultMap, getCaseLevels, getTableFields, statusIconMap } from './utils';

  const tableStore = useTableStore();
  const featureCaseStore = useFeatureCaseStore();

  const { t } = useI18n();
  const { openModal } = useModal();

  const activeCaseType = ref<'folder' | 'module'>('folder'); // 激活用例类型

  const appStore = useAppStore();

  const currentProjectId = computed(() => appStore.currentProjectId);
  const scrollWidth = ref<number>(3400);

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setKeyword } = useTable(
    getRecycleListRequest,
    {
      tableKey: TableKeyEnum.CASE_MANAGEMENT_RECYCLE_TABLE,
      scroll: { x: scrollWidth.value },
      selectable: true,
      showSetting: true,
      heightUsed: 380,
      showSubdirectory: true,
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

  const hasOperationPermission = computed(() => hasAnyPermission(['FUNCTIONAL_CASE:READ+DELETE']));
  const reviewResultOptions = computed(() => {
    return Object.keys(statusIconMap).map((key) => {
      return {
        value: key,
        label: statusIconMap[key].statusText,
      };
    });
  });
  const firstStaticColumn: MsTableColumn = [
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
      'showDrag': false,
      'columnSelectorDisabled': true,
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
      showDrag: false,
      columnSelectorDisabled: true,
    },
  ];
  const caseLevelColumn: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnLevel',
      slotName: 'caseLevel',
      dataIndex: 'caseLevel',
      filterConfig: {
        options: [],
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_CASE_LEVEL,
      },
      showInTable: true,
      width: 150,
      showDrag: true,
    },
  ];
  const lastStaticColumn: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnReviewResult',
      dataIndex: 'reviewStatus',
      slotName: 'reviewStatus',
      filterConfig: {
        options: reviewResultOptions.value,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_RESULT,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    // {
    //   title: 'caseManagement.featureCase.tableColumnExecutionResult',
    //   dataIndex: 'lastExecuteResult',
    //   slotName: 'lastExecuteResult',
    //   titleSlotName: 'executeResultFilter',
    //   showInTable: true,
    //   width: 200,
    //   showDrag: true,
    // },
    // {
    //   title: 'caseManagement.featureCase.tableColumnVersion',
    //   slotName: 'versionName',
    //   dataIndex: 'versionName',
    //   width: 300,
    //   showTooltip: true,
    //   showInTable: true,
    //   showDrag: true,
    // },
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
      dataIndex: 'updateUser',
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
      },
      showInTable: true,
      showTooltip: true,
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
      dataIndex: 'createUser',
      showTooltip: true,
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnDeleteUser',
      slotName: 'deleteUserName',
      dataIndex: 'deleteUser',
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnDeleteTime',
      slotName: 'deleteTime',
      dataIndex: 'deleteTime',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      showDrag: true,
    },
  ];
  const operationColumn: MsTableColumn = [
    {
      title: hasOperationPermission.value ? 'caseManagement.featureCase.tableColumnActions' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      showInTable: true,
      showDrag: false,
      width: hasOperationPermission.value ? 150 : 50,
    },
  ];

  const tableBatchActions = {
    baseAction: [
      {
        label: 'caseManagement.featureCase.batchRecover',
        eventTag: 'batchRecover',
        permission: ['FUNCTIONAL_CASE:READ+DELETE'],
      },
      {
        label: 'caseManagement.featureCase.batchCleanOut',
        eventTag: 'batchCleanOut',
        danger: true,
        permission: ['FUNCTIONAL_CASE:READ+DELETE'],
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
      : findNodeByKey<Record<string, any>>(caseTree.value, activeFolder.value, 'id')?.name;
  });
  const memberOptions = ref<{ label: string; value: string }[]>([]);

  const searchParams = ref<TableQueryParams>({
    projectId: currentProjectId.value,
    moduleIds: [],
  });

  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  const caseLevelFields = ref<Record<string, any>>({});

  // 获取批量操作参数
  function getBatchParams(): TableQueryParams {
    return {
      excludeIds: batchParams.value.excludeIds,
      selectAll: batchParams.value.selectAll,
      selectIds: batchParams.value.selectedIds,
      moduleIds: searchParams.value.moduleIds,
      projectId: currentProjectId.value,
      condition: {
        keyword: keyword.value,
        filter: {
          ...propsRes.value.filter,
        },
        combine: batchParams.value.condition,
      },
    };
  }

  async function initTableParams() {
    let moduleIds: string[] = [];
    if (activeFolder.value !== 'all') {
      moduleIds = [activeFolder.value];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.CASE_MANAGEMENT_RECYCLE_TABLE);
      if (getAllChildren) {
        moduleIds = [activeFolder.value, ...offspringIds.value];
      }
    }
    return {
      keyword: keyword.value,
      moduleIds,
      projectId: currentProjectId.value,
    };
  }

  // 获取回收站模块数量
  async function initRecycleModulesCount() {
    const tableParams = await initTableParams();
    featureCaseStore.getRecycleModulesCount({
      ...tableParams,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
  }

  // 获取用例参数
  async function getLoadListParams() {
    if (activeFolder.value === 'all') {
      searchParams.value.moduleIds = [];
    } else {
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.CASE_MANAGEMENT_RECYCLE_TABLE);
      if (getAllChildren) {
        searchParams.value.moduleIds = [activeFolder.value, ...offspringIds.value];
      } else {
        searchParams.value.moduleIds = [activeFolder.value];
      }
    }
    setLoadListParams(await initTableParams());
  }

  // 初始化回收站列表
  async function initRecycleList() {
    await getLoadListParams();
    await loadList();
    initRecycleModulesCount();
  }

  const fetchData = (keywordStr = '') => {
    setKeyword(keywordStr);
    initRecycleList();
  };

  function brashModule() {
    resetSelector();
    getRecycleModules();
    if (activeFolder.value !== 'all') {
      activeFolder.value = 'all';
    } else {
      initRecycleList();
    }
  }

  // 批量恢复
  async function handleBatchRecover() {
    try {
      await restoreCaseList(getBatchParams());
      Message.success(t('caseManagement.featureCase.recoveredSuccessfully'));
      brashModule();
    } catch (error) {
      console.log(error);
    }
  }
  // 批量删除
  async function handleBatchDelete() {
    openModal({
      type: 'error',
      title: t('caseManagement.featureCase.batchDeleteCompleted', { number: batchParams.value.currentSelectCount }),
      content: t('caseManagement.featureCase.cleanOutDeleteOnRecycleTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await batchDeleteRecycleCase(getBatchParams());
          Message.success(t('common.deleteSuccess'));
          brashModule();
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

  // 恢复用例
  async function recoverCase(id: string) {
    try {
      await recoverRecycleCase(id);
      Message.success(t('caseManagement.featureCase.recoveredSuccessfully'));
      brashModule();
    } catch (error) {
      console.log(error);
    }
  }

  // 彻底删除
  function handleBatchCleanOut(record: CaseManagementTable) {
    openModal({
      type: 'error',
      title: t('caseManagement.featureCase.completedDeleteCaseTitle', { name: characterLimit(record.name) }),
      content: t('caseManagement.featureCase.cleanOutDeleteOnRecycleTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await deleteRecycleCaseList(record.id);
          Message.success(t('common.deleteSuccess'));
          brashModule();
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

  // 处理自定义字段列
  let customFieldsColumns: Record<string, any>[] = [];

  const initDefaultFields = ref<CustomAttributes[]>([]);

  let fullColumns: MsTableColumn = []; // 全量列表

  // 处理自定义字段展示
  async function getDefaultFields() {
    customFieldsColumns = [];
    const result = await getCaseDefaultFields(currentProjectId.value);
    initDefaultFields.value = result.customFields.filter((item: any) => !item.internal);
    customFieldsColumns = initDefaultFields.value.map((item: any) => {
      return {
        title: item.fieldName,
        slotName: item.fieldId as string,
        dataIndex: item.fieldId,
        showInTable: false,
        showDrag: true,
        width: 300,
      };
    });
    caseLevelFields.value = result.customFields.find(
      (item: any) => item.internal && item.internalFieldKey === 'functional_priority'
    );
    if (caseLevelColumn[0].filterConfig?.options) {
      caseLevelColumn[0].filterConfig.options = cloneDeep(unref(caseLevelFields.value?.options)) || [];
    }
    fullColumns = [
      ...firstStaticColumn,
      ...caseLevelColumn,
      ...lastStaticColumn,
      ...customFieldsColumns,
      ...operationColumn,
    ];
    await tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_RECYCLE_TABLE, fullColumns, 'drawer');
  }

  async function initFilter() {
    const result = await getCustomFieldsTable(currentProjectId.value);
    memberOptions.value = await getProjectOptions(appStore.currentProjectId, keyword.value);
    memberOptions.value = memberOptions.value.map((e: any) => ({ label: e.name, value: e.id }));
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
          options: memberOptions.value,
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
          options: memberOptions.value,
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

  onMounted(async () => {
    await getRecycleModules();
    await initFilter();
    await initRecycleList();
  });
  await getDefaultFields();
</script>

<style scoped lang="less">
  .pageWrap {
    min-width: 1000px;
    height: calc(100vh - 126px);
    border-radius: var(--border-radius-large);
    background-color: var(--color-text-fff);
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
  .show-table-top-title {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
  }
</style>
