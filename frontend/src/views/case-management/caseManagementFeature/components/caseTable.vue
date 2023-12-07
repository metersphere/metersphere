<!-- eslint-disable prefer-destructuring -->
<template>
  <div class="page-header mb-4 h-[34px]">
    <div class="text-[var(--color-text-1)]"
      >{{ moduleNamePath }}
      <span class="text-[var(--color-text-4)]"> ({{ props.modulesCount[props.activeFolder] || 0 }})</span></div
    >
    <div class="flex w-[80%] items-center justify-end">
      <a-select class="w-[240px]" :placeholder="t('caseManagement.featureCase.versionPlaceholder')">
        <a-option v-for="version of versionOptions" :key="version.id" :value="version.id">{{ version.name }}</a-option>
      </a-select>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
      ></a-input-search>
      <MsTag
        :type="isExpandFilter ? 'primary' : 'default'"
        :theme="isExpandFilter ? 'lightOutLine' : 'outline'"
        size="large"
        class="-mt-[3px] min-w-[64px] cursor-pointer"
      >
        <span :class="!isExpandFilter ? 'text-[var(--color-text-4)]' : ''" @click="isExpandFilterHandler"
          ><icon-filter class="mr-[4px]" :style="{ 'font-size': '16px' }" />{{
            t('caseManagement.featureCase.filter')
          }}</span
        >
      </MsTag>
      <a-radio-group v-model:model-value="showType" type="button" class="file-show-type ml-[4px]">
        <a-radio value="list" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_view-list_outlined" /></a-radio>
        <a-radio value="xMind" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_mindnote_outlined" /></a-radio>
      </a-radio-group>
    </div>
  </div>
  <!-- 脑图开始 -->
  <MinderEditor
    v-if="showType === 'xMind'"
    :import-json="importJson"
    :tags="['模块', '用例', '前置条件', '备注', '步骤', '预期结果']"
    tag-enable
    sequence-enable
    @node-click="handleNodeClick"
  />
  <MsDrawer v-model:visible="visible" :width="480" :mask="false">
    {{ nodeData.text }}
  </MsDrawer>
  <!-- 脑图结束 -->
  <!-- 用例表开始 -->
  <ms-base-table
    v-bind="propsRes"
    :action-config="tableBatchActions"
    @selected-change="handleTableSelect"
    v-on="propsEvent"
    @batch-action="handleTableBatch"
  >
    <template #name="{ record, rowIndex }">
      <a-button type="text" class="px-0" @click="showCaseDetail(record.id, rowIndex)">{{ record.name }}</a-button>
    </template>
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
      <MsButton @click="operateCase(record, 'edit')">{{ t('common.edit') }}</MsButton>
      <a-divider direction="vertical" :margin="8"></a-divider>
      <MsButton @click="operateCase(record, 'copy')">{{ t('caseManagement.featureCase.copy') }}</MsButton>
      <a-divider direction="vertical" :margin="8"></a-divider>
      <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect($event, record)" />
    </template>
  </ms-base-table>
  <!-- 用例表结束 -->
  <a-modal
    v-model:visible="showBatchMoveDrawer"
    title-align="start"
    class="ms-modal-no-padding ms-modal-small"
    :mask-closable="false"
    :ok-text="
      t(
        isMove
          ? 'caseManagement.featureCase.batchMoveSelectedModules'
          : 'caseManagement.featureCase.batchCopySelectedModules',
        {
          number: batchParams?.currentSelectCount || batchParams?.selectedIds?.length,
        }
      )
    "
    :ok-button-props="{ disabled: selectedModuleKeys.length === 0 }"
    :cancel-button-props="{ disabled: batchMoveCaseLoading }"
    :on-before-ok="handleCaseMoveOrCopy"
    @close="handleMoveCaseModalCancel"
  >
    <template #title>
      <div class="flex w-full items-center justify-between">
        <div>
          {{ isMove ? t('caseManagement.featureCase.batchMoveTitle') : t('caseManagement.featureCase.batchCopyTitle') }}
          <span class="ml-[4px] text-[var(--color-text-4)]">
            {{ t('caseManagement.featureCase.batchMove', { number: batchParams.currentSelectCount }) }}
          </span>
        </div>
        <div class="mr-2">
          <a-select class="w-[120px]" placeholder="请选择版本">
            <a-option v-for="item of versionOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </div>
      </div>
    </template>
    <FeatureCaseTree
      v-if="showBatchMoveDrawer"
      ref="caseTreeRef"
      v-model:selected-keys="selectedModuleKeys"
      :active-folder="props.activeFolder"
      :is-expand-all="true"
      is-modal
      @case-node-select="caseNodeSelect"
    ></FeatureCaseTree>
  </a-modal>
  <ExportExcelDrawer v-model:visible="showExportExcelVisible" />
  <BatchEditModal v-model:visible="showEditModel" :batch-params="batchParams" @success="successHandler" />
  <CaseDetailDrawer
    v-model:visible="showDetailDrawer"
    :detail-id="activeDetailId"
    :detail-index="activeCaseIndex"
    :table-data="propsRes.data"
    :page-change="propsEvent.pageChange"
    :pagination="propsRes.msPagination!"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MinderEditor from '@/components/pure/minder-editor/minderEditor.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import BatchEditModal from './batchEditModal.vue';
  import CaseDetailDrawer from './caseDetailDrawer.vue';
  import FeatureCaseTree from './caseTree.vue';
  import ExportExcelDrawer from './exportExcelDrawer.vue';

  import {
    batchCopyToModules,
    batchDeleteCase,
    batchMoveToModules,
    deleteCaseRequest,
    getCaseDetail,
    getCaseList,
    updateCaseRequest,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { characterLimit, findNodeByKey, findNodePathByKey } from '@/utils';

  import type { CaseManagementTable, CaseModuleQueryParams } from '@/models/caseManagement/featureCase';
  import type { TableQueryParams } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

  import { getReviewStatusClass, getStatusText } from './utils';
  import debounce from 'lodash-es/debounce';

  const { openModal } = useModal();
  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();
  const appStore = useAppStore();
  const featureCaseStore = useFeatureCaseStore();
  const tableStore = useTableStore();

  const props = defineProps<{
    activeFolder: string;
    activeFolderType: 'folder' | 'module';
    offspringIds: string[]; // 当前选中文件夹的所有子孙节点id
    modulesCount: Record<string, number>; // 模块数量
  }>();

  const emit = defineEmits<{
    (e: 'init', params: CaseModuleQueryParams): void;
  }>();

  const keyword = ref<string>();

  const showType = ref<string>('list');

  const isExpandFilter = ref<boolean>(false);

  const versionOptions = ref([
    {
      id: '1001',
      name: 'v_1.0',
    },
  ]);

  const caseTreeData = computed(() => featureCaseStore.caseTree);
  const moduleId = computed(() => featureCaseStore.moduleId[0]);
  const currentProjectId = computed(() => appStore.currentProjectId);

  // 是否展开||折叠高级筛选
  const isExpandFilterHandler = () => {
    isExpandFilter.value = !isExpandFilter.value;
  };

  const visible = ref<boolean>(false);
  const nodeData = ref<any>({});

  const importJson = ref<any>({});

  function handleNodeClick(data: any) {
    if (data.resource && data.resource.includes('用例')) {
      visible.value = true;
      nodeData.value = data;
    }
  }

  onBeforeMount(() => {
    importJson.value = {
      root: {
        data: {
          text: '测试用例',
          id: 'xxxx',
        },
        children: [
          {
            data: {
              id: 'sdasdas',
              text: '模块 1',
              resource: ['模块'],
            },
          },
          {
            data: {
              id: 'dasdasda',
              text: '模块 2',
              expandState: 'collapse',
            },
            children: [
              {
                data: {
                  id: 'frihofiuho3f',
                  text: '用例 1',
                  resource: ['用例'],
                },
              },
              {
                data: {
                  id: 'df09348f034f',
                  text: ' 用例 2',
                  resource: ['用例'],
                },
              },
            ],
          },
        ],
      },
      template: 'default',
    };
  });

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
      editType: ColumnEditTypeEnum.INPUT,
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
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      showInTable: true,
      showDrag: false,
    },
  ];

  const tableBatchActions = {
    baseAction: [
      {
        label: 'caseManagement.featureCase.export',
        eventTag: 'export',
        children: [
          {
            label: 'caseManagement.featureCase.exportExcel',
            eventTag: 'exportExcel',
          },
          {
            label: 'caseManagement.featureCase.exportXMind',
            eventTag: 'exportXMind',
          },
        ],
      },
      {
        label: 'common.edit',
        eventTag: 'batchEdit',
      },
      {
        label: 'caseManagement.featureCase.moveTo',
        eventTag: 'batchMoveTo',
      },
      {
        label: 'caseManagement.featureCase.copyTo',
        eventTag: 'batchCopyTo',
      },
    ],
    moreAction: [
      {
        label: 'caseManagement.featureCase.addDemand',
        eventTag: 'addDemand',
      },
      {
        label: 'caseManagement.featureCase.associatedDemand',
        eventTag: 'associatedDemand',
      },
      {
        label: 'caseManagement.featureCase.generatingDependencies',
        eventTag: 'generatingDependencies',
      },
      {
        label: 'caseManagement.featureCase.addToPublic',
        eventTag: 'addToPublic',
      },
      {
        isDivider: true,
      },
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
      },
    ],
  };
  /**
   * 处理更新用例参数
   * @param detailResult 详情字段
   */
  function getUpdateParams(detailResult: Record<string, any>, name: string) {
    const { customFields } = detailResult;
    const customFieldsMaps: Record<string, any> = {};
    customFields.forEach((item: any) => {
      customFieldsMaps[item.fieldId] = JSON.parse(item.defaultValue);
    });

    return {
      request: {
        ...detailResult,
        name,
        customFields: customFieldsMaps,
        tags: JSON.parse(detailResult.tags),
      },
      fileList: [],
    };
  }

  /**
   * 更新用例名称
   */
  async function updateCaseName(record: CaseManagementTable) {
    try {
      const detailResult = await getCaseDetail(record.id);
      const params = await getUpdateParams(detailResult, record.name);
      await updateCaseRequest(params);
      Message.success(t('common.updateSuccess'));
      return Promise.resolve(true);
    } catch (error) {
      console.log(error);
      return Promise.resolve(false);
    }
  }

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setProps } = useTable(
    getCaseList,
    {
      tableKey: TableKeyEnum.CASE_MANAGEMENT_TABLE,
      scroll: { x: 3200 },
      selectable: true,
      showSetting: true,
      heightUsed: 374,
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
    }),
    updateCaseName
  );

  // 获取父组件模块数量
  function emitTableParams() {
    emit('init', {
      keyword: keyword.value,
      moduleIds: [],
      projectId: currentProjectId.value,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    });
  }

  const tableSelected = ref<(string | number)[]>([]);

  function handleTableSelect(selectArr: (string | number)[]) {
    tableSelected.value = selectArr;
  }

  const searchParams = ref<TableQueryParams>({
    projectId: currentProjectId.value,
    moduleIds: [],
  });

  function getLoadListParams() {
    if (props.activeFolder === 'all') {
      searchParams.value.moduleIds = [];
    } else {
      searchParams.value.moduleIds = [moduleId.value, ...props.offspringIds];
    }
    setLoadListParams({
      ...searchParams.value,
      keyword: keyword.value,
    });
  }

  // 初始化列表
  async function initData() {
    getLoadListParams();
    loadList();
    emitTableParams();
  }

  // 编辑&复制
  function operateCase(record: CaseManagementTable, mode: string) {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
      query: {
        id: record.id,
      },
      params: {
        mode,
      },
    });
  }

  // 删除
  function deleteCase(record: CaseManagementTable) {
    openModal({
      type: 'error',
      title: t('caseManagement.featureCase.deleteCaseTitle', { name: characterLimit(record.name) }),
      content: t('caseManagement.featureCase.beforeDeleteCase'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          const params = {
            id: record.id,
            deleteAll: false,
            projectId: currentProjectId.value,
          };
          await deleteCaseRequest(params);
          Message.success(t('common.deleteSuccess'));
          emitTableParams();
          loadList();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const moreActions: ActionsItem[] = [
    {
      label: 'common.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  function handleMoreActionSelect(item: ActionsItem, record: CaseManagementTable) {
    if (item.eventTag === 'delete') {
      deleteCase(record);
    }
  }

  const showExportExcelVisible = ref<boolean>(false);

  // 导出Excel
  function handleShowExportExcel() {
    showExportExcelVisible.value = true;
  }

  const selectData = ref<string[] | undefined>([]);

  const showEditModel = ref<boolean>(false);
  // 批量编辑
  function batchEdit() {
    showEditModel.value = true;
  }

  const showBatchMoveDrawer = ref<boolean>(false);

  /**
   * 处理文件夹树节点选中事件
   */
  const selectedModuleKeys = ref<string[]>([]); // 移动文件选中节点
  const batchMoveCaseLoading = ref(false);

  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  const isMove = ref<boolean>(false);
  // 批量移动和复制
  async function handleCaseMoveOrCopy() {
    batchMoveCaseLoading.value = true;
    try {
      const params = {
        selectIds: batchParams.value.selectedIds || [],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        condition: { keyword: keyword.value },
        projectId: currentProjectId.value,
        moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder],
        moduleId: selectedModuleKeys.value[0],
      };
      if (isMove.value) {
        await batchMoveToModules(params);
        Message.success(t('caseManagement.featureCase.batchMoveSuccess'));
      } else {
        await batchCopyToModules(params);
        Message.success(t('caseManagement.featureCase.batchCopySuccess'));
      }
      isMove.value = false;
      emitTableParams();
      loadList();
      resetSelector();
    } catch (error) {
      console.log(error);
    } finally {
      batchMoveCaseLoading.value = false;
    }
  }

  function handleMoveCaseModalCancel() {
    showBatchMoveDrawer.value = false;
    selectedModuleKeys.value = [];
  }

  function caseNodeSelect(keys: string[]) {
    selectedModuleKeys.value = keys;
  }

  // 批量移动
  function batchMoveOrCopy() {
    showBatchMoveDrawer.value = true;
  }

  const moduleNamePath = computed(() => {
    return props.activeFolder === 'all'
      ? t('caseManagement.featureCase.allCase')
      : findNodeByKey<Record<string, any>>(caseTreeData.value, featureCaseStore.moduleId[0], 'id')?.name;
  });
  // 获取对应模块name
  function getModules(moduleIds: string) {
    const modules = findNodePathByKey(caseTreeData.value, moduleIds, undefined, 'id');
    const moduleName = (modules || []).treePath.map((item: any) => item.name);
    if (moduleName.length === 1) {
      return moduleName[0];
    }
    return `/${moduleName.join('/')}`;
  }

  // 批量删除
  async function batchDelete() {
    openModal({
      type: 'error',
      title: t('caseManagement.featureCase.batchDelete', {
        number: batchParams.value.currentSelectCount,
      }),
      content: t('caseManagement.featureCase.beforeDeleteCase'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await batchDeleteCase({
            selectIds: batchParams.value.selectedIds as string[],
            projectId: currentProjectId.value,
          });
          resetSelector();
          Message.success(t('common.deleteSuccess'));
          emitTableParams();
          loadList();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  // 添加需求
  function addDemand() {}
  // 关联需求
  function handleAssociatedDemand() {}

  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchParams.value = params;
    switch (event.eventTag) {
      case 'exportExcel':
        handleShowExportExcel();
        break;
      case 'batchEdit':
        batchEdit();
        break;
      case 'delete':
        batchDelete();
        break;
      case 'batchMoveTo':
        batchMoveOrCopy();
        isMove.value = true;
        break;
      case 'batchCopyTo':
        batchMoveOrCopy();
        isMove.value = false;
        break;
      case 'addDemand':
        addDemand();
        break;
      case 'associatedDemand':
        handleAssociatedDemand();
        break;
      default:
        break;
    }
  }

  const searchList = debounce(() => {
    getLoadListParams();
    loadList();
  }, 100);

  function successHandler() {
    loadList();
    emitTableParams();
    resetSelector();
  }
  const showDetailDrawer = ref(false);
  const activeDetailId = ref<string>('');
  const activeCaseIndex = ref<number>(0);

  // 详情
  function showCaseDetail(id: string, index: number) {
    showDetailDrawer.value = true;
    activeDetailId.value = id;
    activeCaseIndex.value = index;
  }

  // 地址栏携带 id，自动打开资源池详情抽屉
  onMounted(() => {
    if (route.query.id) {
      showCaseDetail(route.query.id as string, 0);
    }
  });

  watch(
    () => showType.value,
    () => {
      initData();
    }
  );

  watch(
    () => props.activeFolder,
    () => {
      keyword.value = '';
      initData();
      resetSelector();
    },
    { immediate: true }
  );

  tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_TABLE, columns, 'drawer');
</script>

<style scoped lang="less">
  .page-header {
    @apply flex items-center justify-between;
  }
  .filter-panel {
    background: var(--color-text-n9);
    @apply mt-1 rounded-md p-3;
    .condition-text {
      color: var(--color-text-2);
    }
  }
</style>
