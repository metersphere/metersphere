<!-- eslint-disable prefer-destructuring -->
<template>
  <!-- 用例表开始 -->
  <MsAdvanceFilter
    v-model:keyword="keyword"
    :filter-config-list="filterConfigList"
    :custom-fields-config-list="searchCustomFields"
    :search-placeholder="t('caseManagement.featureCase.searchPlaceholder')"
    :row-count="filterRowCount"
    @keyword-search="fetchData"
    @adv-search="handleAdvSearch"
    @refresh="fetchData()"
  >
    <template #left>
      <div class="text-[var(--color-text-1)]"
        >{{ moduleNamePath }}
        <span class="text-[var(--color-text-4)]"> ({{ props.modulesCount[props.activeFolder] || 0 }})</span></div
      >
    </template>
    <template #right>
      <a-radio-group v-model:model-value="showType" type="button" class="file-show-type">
        <a-radio value="list" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_view-list_outlined" /></a-radio>
        <a-radio value="xMind" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_mindnote_outlined" /></a-radio>
      </a-radio-group>
    </template>
  </MsAdvanceFilter>
  <ms-base-table
    v-if="showType === 'list'"
    v-bind="propsRes"
    ref="tableRef"
    filter-icon-align-left
    class="mt-4"
    :action-config="tableBatchActions"
    @selected-change="handleTableSelect"
    v-on="propsEvent"
    @batch-action="handleTableBatch"
    @change="changeHandler"
  >
    <template #num="{ record, rowIndex }">
      <a-button type="text" class="flex w-full" @click="showCaseDetail(record.id, rowIndex)">{{ record.num }}</a-button>
    </template>
    <template #name="{ record, rowIndex }">
      <span type="text" class="px-0" @click="showCaseDetail(record.id, rowIndex)">{{ record.name }}</span>
    </template>
    <template #caseLevel="{ record }">
      <caseLevel :case-level="getCaseLevels(record.customFields)" />
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
      <a-tree-select
        v-if="record.showModuleTree"
        v-model="record.moduleId"
        :data="caseTreeData"
        :allow-search="true"
        :field-names="{
          title: 'name',
          key: 'id',
          children: 'children',
        }"
        :tree-props="{
          virtualListProps: {
            height: 200,
          },
        }"
        @change="(value) => handleChangeModule(record, value)"
      ></a-tree-select>
      <a-tooltip v-else :content="getModules(record.moduleId)" position="top">
        <span class="one-line-text inline-block" @click="record.showModuleTree = true">{{
          getModules(record.moduleId)
        }}</span>
      </a-tooltip>
    </template>
    <!-- 渲染自定义字段开始TODO -->
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
    <!-- 渲染自定义字段结束 -->
    <template #operation="{ record }">
      <MsButton v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" @click="operateCase(record, 'edit')">{{
        t('common.edit')
      }}</MsButton>
      <a-divider v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" direction="vertical" :margin="8"></a-divider>
      <MsButton v-permission="['FUNCTIONAL_CASE:READ+ADD']" @click="operateCase(record, 'copy')">{{
        t('caseManagement.featureCase.copy')
      }}</MsButton>
      <a-divider v-permission="['FUNCTIONAL_CASE:READ+ADD']" direction="vertical" :margin="8"></a-divider>
      <span v-permission="['FUNCTIONAL_CASE:READ+DELETE']">
        <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect($event, record)" />
      </span>
    </template>

    <template v-if="(keyword || '').trim() === ''" #empty>
      <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
        {{ t('caseManagement.caseReview.tableNoData') }}
        <MsButton v-permission="['FUNCTIONAL_CASE:READ+ADD']" class="ml-[8px]" @click="createCase">
          {{ t('caseManagement.featureCase.creatingCase') }}
        </MsButton>
        {{ t('caseManagement.featureCase.or') }}
        <MsButton v-permission="['FUNCTIONAL_CASE:READ+IMPORT']" class="ml-[8px]" @click="emit('import', 'Excel')">
          {{ t('caseManagement.featureCase.importExcel') }}
        </MsButton>
        <!-- <MsButton class="ml-[4px]" @click="emit('import', 'Xmind')">
          {{ t('caseManagement.featureCase.importXmind') }}
        </MsButton> -->
      </div>
    </template>
  </ms-base-table>
  <!-- 用例表结束 -->
  <!-- 脑图开始 -->
  <MinderEditor
    v-else
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
    @success="initData()"
  />
  <AddDemandModal
    ref="demandRef"
    v-model:visible="showDemandModel"
    :loading="confirmLoading"
    :case-id="caseId"
    :form="modelForm"
    @save="actionDemand"
  />
  <ThirdDemandDrawer
    v-model:visible="showThirdDrawer"
    :case-id="caseId"
    :drawer-loading="drawerLoading"
    @save="saveThirdDemand"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message, TableChangeExtra, TableColumnData, TableData } from '@arco-design/web-vue';

  import { CustomTypeMaps, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MinderEditor from '@/components/pure/ms-minder-editor/minderEditor.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import type { TagType, Theme } from '@/components/pure/ms-tag/ms-tag.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import BatchEditModal from './batchEditModal.vue';
  import CaseDetailDrawer from './caseDetailDrawer.vue';
  import FeatureCaseTree from './caseTree.vue';
  import ExportExcelDrawer from './exportExcelDrawer.vue';
  import AddDemandModal from './tabContent/tabDemand/addDemandModal.vue';
  import ThirdDemandDrawer from './tabContent/tabDemand/thirdDemandDrawer.vue';
  import TableFormChange from './tableFormChange.vue';

  import {
    batchAssociationDemand,
    batchCopyToModules,
    batchDeleteCase,
    batchMoveToModules,
    deleteCaseRequest,
    dragSort,
    getCaseDefaultFields,
    getCaseDetail,
    getCaseList,
    getCustomFieldsTable,
    updateCaseRequest,
  } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import { characterLimit, findNodeByKey, findNodePathByKey } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type {
    CaseManagementTable,
    CaseModuleQueryParams,
    CreateOrUpdateDemand,
    CustomAttributes,
    DemandItem,
    DragCase,
  } from '@/models/caseManagement/featureCase';
  import type { TableQueryParams } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

  import { getCaseLevels, getReviewStatusClass, getStatusText, getTableFields } from './utils';
  import { LabelValue } from '@arco-design/web-vue/es/tree-select/interface';

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
    (e: 'import', type: 'Excel' | 'Xmind'): void;
  }>();

  const keyword = ref<string>('');
  const filterRowCount = ref(0);

  const showType = ref<string>('list');

  const versionOptions = ref([
    {
      id: '1001',
      name: 'v_1.0',
    },
  ]);

  const caseTreeData = computed(() => featureCaseStore.caseTree);
  const moduleId = computed(() => featureCaseStore.moduleId[0]);
  const currentProjectId = computed(() => appStore.currentProjectId);

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

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE', 'FUNCTIONAL_CASE:READ+DELETE'])
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
      editType: hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE']) ? ColumnEditTypeEnum.INPUT : undefined,
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
        sorter: true,
      },
      width: 200,
      showDrag: true,
    },
    {
      title: hasOperationPermission.value ? 'caseManagement.featureCase.tableColumnActions' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      showInTable: true,
      showDrag: false,
      width: hasOperationPermission.value ? 260 : 50,
    },
  ];

  const tableBatchActions = {
    baseAction: [
      // {
      //   label: 'caseManagement.featureCase.export',
      //   eventTag: 'export',
      //   children: [
      //     {
      //       label: 'caseManagement.featureCase.exportExcel',
      //       eventTag: 'exportExcel',
      //     },
      //     {
      //       label: 'caseManagement.featureCase.exportXMind',
      //       eventTag: 'exportXMind',
      //     },
      //   ],
      // },
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
      // {
      //   label: 'caseManagement.featureCase.generatingDependencies',
      //   eventTag: 'generatingDependencies',
      // },
      // {
      //   label: 'caseManagement.featureCase.addToPublic',
      //   eventTag: 'addToPublic',
      // },
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

  const filterConfigList = ref<FilterFormItem[]>([]);
  const searchCustomFields = ref<FilterFormItem[]>([]);
  const scrollWidth = ref<number>(3400);
  async function initFilter() {
    const result = await getCustomFieldsTable(currentProjectId.value);
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
        treeSelectData: caseTreeData.value,
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
        dataIndex: 'createUser',
        type: FilterType.SELECT,
        selectProps: {
          mode: 'static',
          options: [],
        },
      },
      {
        title: 'caseManagement.featureCase.tableColumnCreateTime',
        dataIndex: 'createTime',
        type: FilterType.DATE_PICKER,
      },
      {
        title: 'caseManagement.featureCase.tableColumnUpdateUser',
        dataIndex: 'updateUser',
        type: FilterType.SELECT,
        selectProps: {
          mode: 'static',
          options: [],
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

  function getCustomMaps(detailResult: CaseManagementTable) {
    const { customFields } = detailResult;
    return customFields.map((item: any) => {
      return {
        fieldId: item.fieldId,
        value: Array.isArray(item.defaultValue) ? JSON.stringify(item.defaultValue) : item.defaultValue,
      };
    });
  }
  /**
   * 处理更新用例参数
   * @param detailResult 详情字段
   */
  function getUpdateParams(detailResult: CaseManagementTable, name: string) {
    return {
      request: {
        ...detailResult,
        name,
        customFields: getCustomMaps(detailResult),
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
  const initDefaultFields = ref<CustomAttributes[]>([]);
  let fullColumns: MsTableColumn = []; // 全量列表

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setKeyword, setAdvanceFilter } = useTable(
    getCaseList,
    {
      tableKey: TableKeyEnum.CASE_MANAGEMENT_TABLE,
      scroll: { x: scrollWidth.value },
      selectable: true,
      showJumpMethod: true,
      showSetting: true,
      heightUsed: 374,
      enableDrag: true,
    },
    (record) => {
      return {
        ...record,
        tags: (record.tags || []).map((item: string, i: number) => {
          return {
            id: `${record.id}-${i}`,
            name: item,
          };
        }),
        visible: false,
        showModuleTree: false,
      };
    },
    updateCaseName
  );

  // 获取父组件模块数量
  function emitTableParams() {
    const moduleIds = props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds];
    emit('init', {
      keyword: keyword.value,
      moduleIds,
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
      searchParams.value.moduleIds = [...featureCaseStore.moduleId, ...props.offspringIds];
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
        moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
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
    if (modules) {
      const moduleName = (modules || [])?.treePath.map((item: any) => item.name);
      if (moduleName.length === 1) {
        return moduleName[0];
      }
      return `/${moduleName.join('/')}`;
    }
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

  const showDemandModel = ref<boolean>(false);
  const caseId = ref('');
  const modelForm = ref<DemandItem>({
    id: '',
    caseId: '', // 功能用例ID
    demandId: '', // 需求ID
    demandName: '', // 需求标题
    demandUrl: '', // 需求地址
    demandPlatform: '', // 需求所属平台
    createTime: '',
    updateTime: '',
    createUser: '',
    updateUser: '',
    children: [], // 平台下对应的需求
  });
  // 添加需求
  function addDemand() {
    showDemandModel.value = true;
  }

  const showThirdDrawer = ref<boolean>(false);

  // 关联需求
  function handleAssociatedDemand() {
    showThirdDrawer.value = true;
  }

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

  const fetchData = (keywordStr = '') => {
    setKeyword(keywordStr);
    getLoadListParams();
    loadList();
  };

  function successHandler() {
    loadList();
    emitTableParams();
    resetSelector();
  }
  const showDetailDrawer = ref(false);
  const activeDetailId = ref<string>('');
  const activeCaseIndex = ref<number>(0);

  // 抽屉详情
  function showCaseDetail(id: string, index: number) {
    showDetailDrawer.value = true;
    activeDetailId.value = id;
    activeCaseIndex.value = index;
  }

  // 创建详情
  function createCase() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
    });
  }

  // 处理自定义字段列
  let customFieldsColumns: Record<string, any>[] = [];
  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);

  // 处理自定义字段展示
  async function getDefaultFields() {
    const result = await getCaseDefaultFields(currentProjectId.value);
    initDefaultFields.value = result.customFields;
    customFieldsColumns = initDefaultFields.value
      .filter((item: any) => !item.internal)
      .map((item: any) => {
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
    await tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_TABLE, fullColumns, 'drawer');
    tableRef.value?.initColumn(fullColumns);
  }

  // 如果是用例等级
  function isCaseLevel(slotFieldId: string) {
    const currentItem = initDefaultFields.value.find((item: any) => item.fieldId === slotFieldId);
    return {
      name: currentItem?.fieldName,
      type: currentItem?.type,
      options: currentItem?.options,
    };
  }
  // 获取更新自定义字段参数
  function getCustomsParams(detailResult: CaseManagementTable, record: CaseManagementTable) {
    const customFieldsList = Object.keys(record).filter((item) => item.includes('rule-'));
    const customArr: { fieldId: string; value: any }[] = [];
    customFieldsList.forEach((key) => {
      record[key].forEach((it: any) => {
        customArr.push({
          fieldId: it.field,
          value: Array.isArray(it.value) ? JSON.stringify(it.value) : it.value,
        });
      });
    });

    return {
      request: {
        ...detailResult,
        customFields: customArr,
        tags: detailResult.tags,
      },
      fileList: [],
    };
  }

  // 更新自定义字段
  async function updateHandler(record: CaseManagementTable) {
    try {
      const detailResult = await getCaseDetail(record.id);
      const params = await getCustomsParams(detailResult, record);
      await updateCaseRequest(params);
      Message.success(t('common.updateSuccess'));
      initData();
    } catch (error) {
      console.log(error);
    }
  }

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

  // 模块树改变回调
  async function handleChangeModule(
    record: CaseManagementTable,
    value: string | number | LabelValue | Array<string | number> | LabelValue[] | undefined
  ) {
    try {
      const detailResult = await getCaseDetail(record.id);
      const params = {
        request: {
          ...detailResult,
          moduleId: value,
          customFields: getCustomMaps(detailResult),
        },
        fileList: [],
      };
      await updateCaseRequest(params);
      Message.success(t('common.updateSuccess'));
      record.showModuleTree = false;
      initData();
    } catch (error) {
      console.log(error);
    }
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
    initData();
  };

  // 拖拽排序
  async function changeHandler(data: TableData[], extra: TableChangeExtra, currentData: TableData[]) {
    if (!currentData || currentData.length === 1) {
      return;
    }

    if (extra && extra.dragTarget?.id) {
      const params: DragCase = {
        projectId: currentProjectId.value,
        targetId: '', // 放置目标id
        moveMode: 'BEFORE',
        moveId: extra.dragTarget.id as string, // 拖拽id
      };
      const index = currentData.findIndex((item: any) => item.key === extra.dragTarget?.id);

      if (index > -1 && currentData[index + 1]) {
        params.moveMode = 'BEFORE';
        params.targetId = currentData[index + 1].raw.id;
      } else if (index > -1 && !currentData[index + 1]) {
        if (index > -1 && currentData[index - 1]) {
          params.moveMode = 'AFTER';
          params.targetId = currentData[index - 1].raw.id;
        }
      }
      try {
        await dragSort(params);
        Message.success(t('caseManagement.featureCase.sortSuccess'));
        initData();
      } catch (error) {
        console.log(error);
      }
    }
  }

  // function showCaseDetailEvent(record: TableData, column: TableColumnData, ev: Event) {
  //   showDetailDrawer.value = false;
  //   if (column.title === 'name' || column.title === 'num') {
  //     const rowIndex = propsRes.value.data.map((item: any) => item.id).indexOf(record.id);
  //     showDetailDrawer.value = true;
  //     activeDetailId.value = record.id;
  //     activeCaseIndex.value = rowIndex;
  //   }
  // }

  // 批量添加需求
  const confirmLoading = ref<boolean>(false);
  const demandRef = ref();
  async function actionDemand(param: CreateOrUpdateDemand, isContinue: boolean) {
    try {
      confirmLoading.value = true;
      const { demandPlatform, demandList } = param;
      const batchAddParams: CreateOrUpdateDemand = {
        selectIds: batchParams.value?.selectAll ? [] : batchParams.value.selectedIds,
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        condition: { keyword: keyword.value },
        projectId: currentProjectId.value,
        moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
        moduleId: selectedModuleKeys.value[0],
        demandPlatform,
        demandList,
      };
      await batchAssociationDemand(batchAddParams);
      if (!isContinue) {
        showDemandModel.value = false;
      }
      demandRef.value.resetForm();
      Message.success(t('common.addSuccess'));
      resetSelector();
      initData();
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }

  // 批量关联需求
  const drawerLoading = ref<boolean>(false);
  async function saveThirdDemand(params: CreateOrUpdateDemand) {
    try {
      drawerLoading.value = true;
      const { demandPlatform, demandList } = params;
      const batchAddParams: CreateOrUpdateDemand = {
        selectIds: batchParams.value?.selectAll ? [] : batchParams.value.selectedIds,
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        condition: { keyword: keyword.value },
        projectId: currentProjectId.value,
        moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
        moduleId: selectedModuleKeys.value[0],
        demandPlatform,
        demandList,
      };
      await batchAssociationDemand(batchAddParams);
      Message.success(t('caseManagement.featureCase.associatedSuccess'));
      showThirdDrawer.value = false;
      resetSelector();
      initData();
    } catch (error) {
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  onMounted(() => {
    if (route.query.id) {
      showCaseDetail(route.query.id as string, 0);
    }
    getDefaultFields();
    initFilter();
    initData();
  });

  watch(
    () => showType.value,
    () => {
      initData();
    }
  );

  watch(
    () => props.activeFolder,
    (val) => {
      keyword.value = '';
      if (props.activeFolder !== 'recycle' && val) {
        initData();
        resetSelector();
      }
    }
  );
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
