<!-- eslint-disable prefer-destructuring -->
<template>
  <!-- 用例表开始 -->
  <MsAdvanceFilter
    :filter-config-list="filterConfigList"
    :custom-fields-config-list="searchCustomFields"
    :row-count="filterRowCount"
    @keyword-search="fetchData"
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
    class="mt-4"
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
    <!-- 渲染自定义字段开始 -->
    <template v-for="item in customFieldsColumns" :key="item.slotName" #[item.slotName]="{ record }">
      <div v-if="isCaseLevel(item.slotName as string).name === '用例等级'" class="flex items-center">
        <span v-if="!record.visible" class="flex items-center" @click="record.visible = true"
          ><caseLevel :case-level="getCaseLevel(record, item)"
        /></span>
        <TableFormChange
          v-model:visible="record.visible"
          :default-value="record[item.slotName]"
          :data-rules="record[`rule-${item.slotName}`] || []"
          @update-customs="updateHandler(record)"
        />
      </div>
      <div v-if="isCaseLevel(item.slotName as string).name === '用例状态'" class="flex items-center">
        <MsTag
          :type="getCaseState(record[item.slotName as string]).type"
          :theme="getCaseState(record[item.slotName as string]).theme"
          >{{ record[item.slotName as string] }}</MsTag
        >
      </div>
    </template>
    <!-- 渲染自定义字段结束 -->
    <template #operation="{ record }">
      <MsButton @click="operateCase(record, 'edit')">{{ t('common.edit') }}</MsButton>
      <a-divider direction="vertical" :margin="8"></a-divider>
      <MsButton @click="operateCase(record, 'copy')">{{ t('caseManagement.featureCase.copy') }}</MsButton>
      <a-divider direction="vertical" :margin="8"></a-divider>
      <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect($event, record)" />
    </template>

    <template v-if="(keyword || '').trim() === ''" #empty>
      <div class="flex items-center justify-center p-[8px] text-[var(--color-text-4)]">
        {{ t('caseManagement.caseReview.tableNoData') }}
        <MsButton class="ml-[8px]" @click="createCase">
          {{ t('caseManagement.featureCase.creatingCase') }}
        </MsButton>
        {{ t('caseManagement.featureCase.or') }}
        <MsButton class="ml-[8px]" @click="emit('import', 'Excel')">
          {{ t('caseManagement.featureCase.importExcel') }}
        </MsButton>
        <MsButton class="ml-[4px]" @click="emit('import', 'Xmind')">
          {{ t('caseManagement.featureCase.importXmind') }}
        </MsButton>
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
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MinderEditor from '@/components/pure/minder-editor/minderEditor.vue';
  import { CustomTypeMaps, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import { FieldTypeFormRules } from '@/components/pure/ms-form-create/form-create';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type {
    BatchActionParams,
    BatchActionQueryParams,
    MsTableColumn,
    MsTableColumnData,
  } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import type { TagType, Theme } from '@/components/pure/ms-tag/ms-tag.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import BatchEditModal from './batchEditModal.vue';
  import CaseDetailDrawer from './caseDetailDrawer.vue';
  import FeatureCaseTree from './caseTree.vue';
  import ExportExcelDrawer from './exportExcelDrawer.vue';
  import TableFormChange from './tableFormChange.vue';

  import {
    batchCopyToModules,
    batchDeleteCase,
    batchMoveToModules,
    deleteCaseRequest,
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

  import type {
    CaseManagementTable,
    CaseModuleQueryParams,
    CustomAttributes,
  } from '@/models/caseManagement/featureCase';
  import type { TableQueryParams } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

  import { getReviewStatusClass, getStatusText } from './utils';
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

  const keyword = ref<string>();
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

  const filterConfigList = ref<FilterFormItem[]>([]);
  const searchCustomFields = ref<FilterFormItem[]>([]);
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
        title: 'bugManagement.createTime',
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
      const formObject = item.type;
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
    const customFieldsMaps: Record<string, any> = {};
    customFields.forEach((item: any) => {
      customFieldsMaps[item.fieldId] = JSON.parse(item.defaultValue);
    });
    return customFieldsMaps;
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
  const initDefaultFields = ref<CustomAttributes[]>([]);
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setKeyword } = useTable(
    getCaseList,
    {
      tableKey: TableKeyEnum.CASE_MANAGEMENT_TABLE,
      scroll: { x: 3400 },
      selectable: true,
      showSetting: true,
      heightUsed: 374,
      enableDrag: true,
    },
    (record) => {
      const recordCustomFields = record.customFields;
      const recordMap: Record<string, any> = {};
      const recordMapRules: Record<string, any[]> = {};
      recordCustomFields.forEach((it: any) => {
        recordMap[it.fieldId] = JSON.parse(it.value);
      });
      initDefaultFields.value.forEach((item: any) => {
        const currentFormRules = FieldTypeFormRules[item.type];
        const options = item?.options;
        const currentOptions = options?.map((optionsItem: any) => {
          return {
            label: optionsItem.text,
            value: optionsItem.value,
          };
        });
        const currentForm = {
          ...currentFormRules,
          field: item.fieldId, // 字段
          title: item.fieldName, // label 表单标签
          value: recordMap[item.fieldId], // 目前的值
          effect: {
            required: false, // 是否必填
          },
          options: [],
          props: {
            ...currentFormRules.props,
            options: [],
            modelValue: recordMap[item.fieldId],
          },
          emit: ['on-change'],
        };
        if (currentOptions && currentOptions.length) {
          currentForm.options = currentOptions;
          currentForm.props.options = currentOptions;
        }
        recordMapRules[`rule-${item.fieldId}`] = [currentForm];
      });
      return {
        ...record,
        ...recordMap,
        tags: (JSON.parse(record.tags) || []).map((item: string, i: number) => {
          return {
            id: `${record.id}-${i}`,
            name: item,
          };
        }),
        visible: false,
        ...recordMapRules,
        showModuleTree: false,
      };
    },
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

  // const searchList = debounce(() => {
  //   getLoadListParams();
  //   loadList();
  // }, 100);

  const fetchData = (keywordStr = '') => {
    console.log(keywordStr);
    setKeyword(keywordStr);
    keyword.value = keywordStr;
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
  const customFieldsColumns = ref<Record<string, any>[]>([]);
  let fullColumns: MsTableColumn = []; // 全量列表
  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);

  // 处理自定义字段展示
  async function getDefaultFields() {
    const result = await getCaseDefaultFields(currentProjectId.value);
    initDefaultFields.value = result.customFields;
    customFieldsColumns.value = initDefaultFields.value.map((item: any) => {
      return {
        title: item.fieldName,
        slotName: item.fieldId as string,
        dataIndex: item.fieldId,
        showTooltip: true,
        showInTable: true,
        showDrag: true,
      };
    });

    fullColumns = [
      ...columns.slice(0, columns.length - 1),
      ...customFieldsColumns.value,
      ...columns.slice(columns.length - 1, columns.length),
    ];
    tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_TABLE, fullColumns, 'drawer');
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
    const customMaps: Record<string, any> = {};
    customFieldsList.forEach((key) => {
      record[key].forEach((it: any) => {
        customMaps[it.field] = it.value;
      });
    });

    return {
      request: {
        ...detailResult,
        customFields: customMaps,
        tags: JSON.parse(detailResult.tags),
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

  function getCaseLevel(record: CaseManagementTable, item: MsTableColumnData): CaseLevel {
    return ((record[item.slotName as string] || '').replaceAll('P', '') * 1) as CaseLevel;
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
          tags: JSON.parse(detailResult.tags),
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

  onBeforeMount(() => {
    if (route.query.id) {
      showCaseDetail(route.query.id as string, 0);
    }
    getDefaultFields();
    initFilter();
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
