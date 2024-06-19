<!-- eslint-disable prefer-destructuring -->
<template>
  <div class="h-full">
    <!-- 用例表开始 -->
    <template v-if="showType === 'list'">
      <MsAdvanceFilter
        v-model:keyword="keyword"
        :filter-config-list="filterConfigList"
        :custom-fields-config-list="searchCustomFields"
        :search-placeholder="t('caseManagement.featureCase.searchPlaceholder')"
        :row-count="filterRowCount"
        :count="props.modulesCount[props.activeFolder] || 0"
        :name="moduleNamePath"
        @keyword-search="fetchData"
        @adv-search="handleAdvSearch"
        @refresh="fetchData()"
      >
        <template #right>
          <a-radio-group
            v-model:model-value="showType"
            type="button"
            size="small"
            class="list-show-type"
            @change="handleShowTypeChange"
          >
            <a-radio value="list" class="show-type-icon !m-[2px]">
              <MsIcon :size="14" type="icon-icon_view-list_outlined" />
            </a-radio>
            <a-radio value="minder" class="show-type-icon !m-[2px]">
              <MsIcon :size="14" type="icon-icon_mindnote_outlined" />
            </a-radio>
          </a-radio-group>
        </template>
      </MsAdvanceFilter>
      <ms-base-table
        v-bind="propsRes"
        ref="tableRef"
        filter-icon-align-left
        class="mt-[16px]"
        :action-config="tableBatchActions"
        @selected-change="handleTableSelect"
        v-on="propsEvent"
        @batch-action="handleTableBatch"
        @change="changeHandler"
        @module-change="initData"
        @cell-click="handleCellClick"
        @filter-change="filterChange"
      >
        <template #num="{ record }">
          <span type="text" class="one-line-text cursor-pointer px-0 text-[rgb(var(--primary-5))]">
            {{ record.num }}
          </span>
        </template>
        <template #name="{ record }">
          <div class="one-line-text">{{ characterLimit(record.name) }}</div>
        </template>
        <template #caseLevel="{ record }">
          <a-select
            v-model:model-value="record.caseLevel"
            :placeholder="t('common.pleaseSelect')"
            class="param-input w-full"
            @click.stop
            @change="() => handleStatusChange(record)"
          >
            <template #label>
              <span class="text-[var(--color-text-2)]"> <caseLevel :case-level="record.caseLevel" /></span>
            </template>
            <a-option v-for="item of caseLevelList" :key="item.value" :value="item.value">
              <caseLevel :case-level="item.text" />
            </a-option>
          </a-select>
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
        <template #reviewStatus="{ record }">
          <MsIcon
            :type="statusIconMap[record.reviewStatus]?.icon || ''"
            class="mr-1"
            :class="[statusIconMap[record.reviewStatus].color]"
          ></MsIcon>
          <span>{{ statusIconMap[record.reviewStatus]?.statusText || '' }} </span>
        </template>
        <template #lastExecuteResult="{ record }">
          <ExecuteStatusTag v-if="record.lastExecuteResult" :execute-result="record.lastExecuteResult" />
          <span v-else>-</span>
        </template>
        <template #moduleId="{ record }">
          <a-tree-select
            v-if="record.showModuleTree"
            v-model:modelValue="record.moduleId"
            dropdown-class-name="tree-dropdown"
            class="param-input w-full"
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
            @click.stop
            @change="(value) => handleChangeModule(record, value)"
          >
            <template #tree-slot-title="node">
              <a-tooltip :content="`${node.name}`" position="tl">
                <div class="one-line-text max-w-[200px] text-[var(--color-text-1)]">{{ node.name }}</div>
              </a-tooltip>
            </template>
          </a-tree-select>
          <a-tooltip v-else :content="getModules(record.moduleId)" position="top">
            <span class="one-line-text inline-block" @click.stop="record.showModuleTree = true">
              {{ getModules(record.moduleId) }}
            </span>
          </a-tooltip>
        </template>
        <!-- 渲染自定义字段开始TODO -->
        <template v-for="item in customFieldsColumns" :key="item.slotName" #[item.slotName]="{ record }">
          <a-tooltip
            :content="getTableFields(record.customFields, item as MsTableColumn, record.createUser)"
            position="top"
            :mouse-enter-delay="100"
            mini
          >
            <div class="one-line-text max-w-[300px]">{{
              getTableFields(record.customFields, item as MsTableColumn, record.createUser)
            }}</div>
          </a-tooltip>
        </template>
        <!-- 渲染自定义字段结束 -->
        <template #operation="{ record }">
          <MsButton v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" class="!mr-0" @click="operateCase(record, 'edit')">
            {{ t('common.edit') }}
          </MsButton>
          <a-divider
            v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
            class="!mx-2 h-[12px]"
            direction="vertical"
            :margin="8"
          ></a-divider>
          <MsButton v-permission="['FUNCTIONAL_CASE:READ+ADD']" class="!mr-0" @click="operateCase(record, 'copy')">
            {{ t('caseManagement.featureCase.copy') }}
          </MsButton>
          <a-divider
            v-permission="['FUNCTIONAL_CASE:READ+ADD']"
            class="!mx-2 h-[12px]"
            direction="vertical"
            :margin="8"
          ></a-divider>
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
    </template>
    <!-- 用例表结束 -->
    <div v-else class="h-full">
      <div class="flex flex-row items-center justify-between">
        <a-popover title="" position="bottom">
          <div class="show-table-top-title">
            <div class="one-line-text max-h-[32px] max-w-[300px] text-[var(--color-text-1)]">
              {{ moduleNamePath }}
            </div>
            <span class="text-[var(--color-text-4)]"> ({{ props.modulesCount[props.activeFolder] || 0 }})</span>
          </div>
          <template #content>
            <div class="max-w-[400px] text-[14px] font-medium text-[var(--color-text-1)]">
              {{ moduleNamePath }}
              <span class="text-[var(--color-text-4)]">({{ props.modulesCount[props.activeFolder] || 0 }})</span>
            </div>
          </template>
        </a-popover>
        <div class="flex items-center gap-[12px]">
          <a-radio-group
            v-model:model-value="showType"
            type="button"
            size="small"
            class="list-show-type"
            @change="handleShowTypeChange"
          >
            <a-radio value="list" class="show-type-icon !m-[2px]">
              <MsIcon :size="14" type="icon-icon_view-list_outlined" />
            </a-radio>
            <a-radio value="minder" class="show-type-icon !m-[2px]">
              <MsIcon :size="14" type="icon-icon_mindnote_outlined" />
            </a-radio>
          </a-radio-group>
        </div>
      </div>
      <div class="mt-[16px] h-[calc(100%-32px)] border-t border-[var(--color-text-n8)]">
        <!-- 脑图开始 -->
        <MsFeatureCaseMinder
          :module-id="props.activeFolder"
          :modules-count="props.modulesCount"
          :module-name="props.moduleName"
          @save="handleMinderSave"
        />
        <MsDrawer v-model:visible="visible" :width="480" :mask="false">
          {{ nodeData.text }}
        </MsDrawer>
        <!-- 脑图结束 -->
      </div>
    </div>
  </div>
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
        <!-- <div class="mr-2">
          <a-select class="w-[120px]" placeholder="请选择版本">
            <a-option v-for="item of versionOptions" :key="item.id" :value="item.id">{{ item.name }}</a-option>
          </a-select>
        </div> -->
      </div>
    </template>
    <FeatureCaseTree
      v-if="showBatchMoveDrawer"
      ref="caseTreeRef"
      v-model:selected-keys="selectedModuleKeys"
      v-model:group-keyword="groupKeyword"
      :active-folder="props.activeFolder"
      :is-expand-all="true"
      :is-modal="true"
      @case-node-select="caseNodeSelect"
    ></FeatureCaseTree>
  </a-modal>
  <ExportExcelDrawer v-model:visible="showExportExcelVisible" />
  <BatchEditModal
    v-model:visible="showEditModel"
    :batch-params="batchParams"
    :active-folder="props.activeFolder"
    :offspring-ids="props.offspringIds"
    :condition="conditionParams"
    @success="successHandler"
  />
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
    :platform-info="platformInfo"
    @save="saveThirdDemand"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message, TableChangeExtra, TableData, TreeNodeData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import { CustomTypeMaps, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult, FilterType } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteStatusTag from '@/components/business/ms-case-associate/executeResult.vue';
  import MsFeatureCaseMinder from '@/components/business/ms-minders/featureCaseMinder/index.vue';
  import BatchEditModal from './batchEditModal.vue';
  import CaseDetailDrawer from './caseDetailDrawer.vue';
  import FeatureCaseTree from './caseTree.vue';
  import ExportExcelDrawer from './exportExcelDrawer.vue';
  import AddDemandModal from './tabContent/tabDemand/addDemandModal.vue';
  import ThirdDemandDrawer from './tabContent/tabDemand/thirdDemandDrawer.vue';

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
  import { getCaseRelatedInfo } from '@/api/modules/project-management/menuManagement';
  import { getProjectOptions } from '@/api/modules/project-management/projectMember';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import useMinderStore from '@/store/modules/components/minder-editor';
  import { characterLimit, findNodeByKey, findNodePathByKey, mapTree } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type {
    CaseManagementTable,
    CaseModuleQueryParams,
    CreateOrUpdateDemand,
    CustomAttributes,
    DemandItem,
    DragCase,
  } from '@/models/caseManagement/featureCase';
  import { ModuleTreeNode } from '@/models/common';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  import { executionResultMap, getCaseLevels, getTableFields, statusIconMap } from './utils';
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
    moduleName: string;
    offspringIds: string[]; // 当前选中文件夹的所有子孙节点id
    modulesCount: Record<string, number>; // 模块数量
  }>();

  const emit = defineEmits<{
    (e: 'init', params: CaseModuleQueryParams, refreshModule?: boolean): void;
    (e: 'import', type: 'Excel' | 'Xmind'): void;
  }>();

  const minderStore = useMinderStore();

  const keyword = ref<string>('');
  const filterRowCount = ref(0);
  const groupKeyword = ref<string>('');

  const showType = ref<string>('list');

  function handleShowTypeChange(val: string | number | boolean) {
    if (minderStore.minderUnsaved && val === 'list') {
      showType.value = 'minder';
      openModal({
        type: 'warning',
        title: t('common.tip'),
        content: t('ms.minders.leaveUnsavedTip'),
        okText: t('common.confirm'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'normal',
        },
        onBeforeOk: async () => {
          showType.value = 'list';
        },
        hideCancel: false,
      });
    } else if (val === 'minder') {
      keyword.value = '';
      // 切换到脑图刷新模块统计
      emit('init', { moduleIds: [props.activeFolder], projectId: appStore.currentProjectId, pageSize: 10, current: 1 });
    }
  }

  const caseTreeData = computed(() => {
    return mapTree<ModuleTreeNode>(featureCaseStore.caseTree, (e) => {
      return {
        ...e,
        draggable: false,
      };
    });
  });
  const currentProjectId = computed(() => appStore.currentProjectId);

  const visible = ref<boolean>(false);
  const nodeData = ref<any>({});

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE', 'FUNCTIONAL_CASE:READ+DELETE'])
  );

  const executeResultOptions = computed(() => {
    return Object.keys(executionResultMap).map((key) => {
      return {
        value: key,
        label: executionResultMap[key].statusText,
      };
    });
  });
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
      'width': 130,
      'showInTable': true,
      'sortable': {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      'filter-icon-align-left': true,
      'showTooltip': true,
      'ellipsis': true,
      'showDrag': false,
      'columnSelectorDisabled': true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'name',
      dataIndex: 'name',
      showInTable: true,
      showTooltip: true,
      width: 180,
      editType: hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE']) ? ColumnEditTypeEnum.INPUT : undefined,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      ellipsis: true,
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
  const operationColumn: MsTableColumn = [
    {
      title: hasOperationPermission.value ? 'caseManagement.featureCase.tableColumnActions' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      showInTable: true,
      showDrag: false,
      width: hasOperationPermission.value ? 140 : 50,
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
      width: 150,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnExecutionResult',
      dataIndex: 'lastExecuteResult',
      slotName: 'lastExecuteResult',
      filterConfig: {
        options: executeResultOptions.value,
        filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_EXECUTE_RESULT,
      },
      showInTable: true,
      width: 150,
      showDrag: true,
    },
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
      width: 200,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnTag',
      slotName: 'tags',
      dataIndex: 'tags',
      showInTable: true,
      isTag: true,
      width: 300,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnUpdateUser',
      slotName: 'updateUserName',
      showTooltip: true,
      dataIndex: 'updateUserName',
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
        placeholderText: t('caseManagement.featureCase.PleaseSelect'),
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
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
        placeholderText: t('caseManagement.featureCase.PleaseSelect'),
      },
      showInTable: true,
      showTooltip: true,
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
  ];

  const platformInfo = ref<Record<string, any>>({});
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
        permission: ['FUNCTIONAL_CASE:READ+UPDATE'],
      },
      {
        label: 'caseManagement.featureCase.moveTo',
        eventTag: 'batchMoveTo',
        permission: ['FUNCTIONAL_CASE:READ+UPDATE'],
      },
      {
        label: 'caseManagement.featureCase.copyTo',
        eventTag: 'batchCopyTo',
        permission: ['FUNCTIONAL_CASE:READ+ADD'],
      },
    ],
    moreAction: [
      {
        label: 'caseManagement.featureCase.addDemand',
        eventTag: 'addDemand',
        permission: ['FUNCTIONAL_CASE:READ+UPDATE'],
      },
      {
        label: 'caseManagement.featureCase.associatedDemand',
        eventTag: 'associatedDemand',
        permission: ['FUNCTIONAL_CASE:READ+UPDATE'],
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
        permission: ['FUNCTIONAL_CASE:READ+DELETE'],
        danger: true,
      },
    ],
  };

  const filterConfigList = ref<FilterFormItem[]>([]);
  const searchCustomFields = ref<FilterFormItem[]>([]);
  const memberOptions = ref<{ label: string; value: string }[]>([]);

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

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector, setKeyword, setAdvanceFilter } = useTable(
    getCaseList,
    {
      tableKey: TableKeyEnum.CASE_MANAGEMENT_TABLE,
      selectable: true,
      showSetting: true,
      heightUsed: 236,
      enableDrag: true,
      showSubdirectory: true,
      paginationSize: 'mini',
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
        caseLevel: getCaseLevels(record.customFields),
      };
    },
    updateCaseName
  );

  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  const conditionParams = computed(() => {
    return {
      keyword: keyword.value,
      filter: propsRes.value.filter,
      combine: batchParams.value.condition,
    };
  });

  async function initTableParams() {
    let moduleIds: string[] = [];
    if (props.activeFolder && props.activeFolder !== 'all') {
      moduleIds = [props.activeFolder];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.CASE_MANAGEMENT_TABLE);
      if (getAllChildren) {
        moduleIds = [props.activeFolder, ...props.offspringIds];
      }
    }

    return {
      moduleIds,
      projectId: currentProjectId.value,
      excludeIds: batchParams.value.excludeIds,
      selectAll: batchParams.value.selectAll,
      selectIds: batchParams.value.selectedIds as string[],
      keyword: keyword.value,
      combine: batchParams.value.condition,
    };
  }
  // 获取父组件模块数量
  async function emitTableParams(refreshModule = false) {
    const tableParams = await initTableParams();
    emit(
      'init',
      {
        ...tableParams,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
        filter: propsRes.value.filter,
      },
      refreshModule
    );
  }

  function handleMinderSave() {
    emitTableParams(true);
  }

  const tableSelected = ref<(string | number)[]>([]);

  function handleTableSelect(selectArr: (string | number)[]) {
    tableSelected.value = selectArr;
  }

  function filterTreeNode(searchValue: string, nodeValue: TreeNodeData) {
    return (nodeValue as ModuleTreeNode).name.toLowerCase().indexOf(searchValue.toLowerCase()) > -1;
  }

  const caseLevelFields = ref<Record<string, any>>({});

  const caseLevelList = computed(() => {
    return caseLevelFields.value?.options || [];
  });

  async function getLoadListParams() {
    setLoadListParams(await initTableParams());
  }

  // 初始化列表
  async function initData() {
    await getLoadListParams();
    await loadList();
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

  const isMove = ref<boolean>(false);
  // 批量移动和复制
  async function handleCaseMoveOrCopy() {
    batchMoveCaseLoading.value = true;
    try {
      const params = {
        selectIds: batchParams.value.selectedIds || [],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        condition: {
          keyword: keyword.value,
          filter: propsRes.value.filter,
          combine: batchParams.value.condition,
        },
        projectId: currentProjectId.value,
        moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
        moduleId: selectedModuleKeys.value[0],
      };
      if (isMove.value) {
        await batchMoveToModules(params);
        groupKeyword.value = '';
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
    groupKeyword.value = '';
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
      : findNodeByKey<Record<string, any>>(caseTreeData.value, props.activeFolder, 'id')?.name;
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
          const { selectedIds, selectAll, excludeIds } = batchParams.value;
          await batchDeleteCase({
            projectId: currentProjectId.value,
            selectIds: selectAll ? [] : selectedIds,
            excludeIds: excludeIds || [],
            moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
            condition: {
              keyword: keyword.value,
              filter: propsRes.value.filter,
              combine: batchParams.value.condition,
            },
            selectAll,
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
    initData();
  };

  function successHandler() {
    resetSelector();
    initData();
  }
  const showDetailDrawer = ref(false);
  const activeDetailId = ref<string>('');
  const activeCaseIndex = ref<number>(0);

  // 抽屉详情
  function showCaseDetail(id: string, index: number) {
    activeDetailId.value = id;
    activeCaseIndex.value = index;
    showDetailDrawer.value = true;
  }

  function handleCellClick(record: TableData) {
    const index = propsRes.value.data.findIndex((item) => item.id === record.id);
    showCaseDetail(record.id, index);
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

  let fullColumns: MsTableColumn = [];
  // 处理自定义字段展示
  async function getDefaultFields() {
    customFieldsColumns = [];
    const result = await getCaseDefaultFields(currentProjectId.value);
    initDefaultFields.value = result.customFields;
    customFieldsColumns = initDefaultFields.value
      .filter((item: any) => !item.internal)
      .map((item: any) => {
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
      (item: any) => item.internal && (item.fieldName === 'Case Priority' || item.fieldName === '用例等级')
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
    await tableStore.initColumn(TableKeyEnum.CASE_MANAGEMENT_TABLE, fullColumns, 'drawer', true);
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
  // 更新用例等级
  async function handleStatusChange(record: any) {
    try {
      const detailResult = await getCaseDetail(record.id);
      const { customFields } = detailResult;
      const customFieldsList = customFields.map((item: any) => {
        if (item.internal && item.fieldName === '用例等级') {
          return {
            fieldId: item.fieldId,
            value: record.caseLevel,
          };
        }
        return {
          fieldId: item.fieldId,
          value: Array.isArray(item.defaultValue) ? JSON.stringify(item.defaultValue) : item.defaultValue,
        };
      });
      const params = {
        request: {
          ...detailResult,
          lastExecuteResult: record.lastExecuteResult,
          customFields: customFieldsList,
        },
        fileList: [],
      };
      await updateCaseRequest(params);
      initData();
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

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
      const { demandPlatform, demandList, functionalDemandBatchRequest } = params;
      const batchAddParams: CreateOrUpdateDemand = {
        selectIds: batchParams.value?.selectAll ? [] : batchParams.value.selectedIds,
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        projectId: currentProjectId.value,
        moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
        moduleId: selectedModuleKeys.value[0],
        demandPlatform,
        demandList,
        filter: propsRes.value.filter,
        condition: {
          keyword: keyword.value,
          filter: propsRes.value.filter,
          combine: batchParams.value.condition,
        },
        functionalDemandBatchRequest,
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

  const statusFilterVisible = ref(false);

  // 获取三方需求
  onBeforeMount(async () => {
    try {
      const result = await getCaseRelatedInfo(currentProjectId.value);
      if (result && result.platform_key) {
        platformInfo.value = { ...result };
      }
      if (Object.keys(result).length === 0) {
        tableBatchActions.moreAction = [
          ...tableBatchActions.moreAction.slice(0, 1),
          ...tableBatchActions.moreAction.slice(-2),
        ];
      }
    } catch (error) {
      console.log(error);
    }
  });

  function filterChange() {
    emitTableParams();
  }

  onMounted(async () => {
    if (route.query.id) {
      showCaseDetail(route.query.id as string, -1);
    }
    await initFilter();
    initData();
  });

  watch(
    () => showType.value,
    (val) => {
      if (val === 'list') {
        initData();
      }
    }
  );

  watch(
    () => props.activeFolder,
    (val) => {
      if (props.activeFolder !== 'recycle' && val) {
        initData();
        resetSelector();
      }
    }
  );

  onBeforeUnmount(() => {
    showDetailDrawer.value = false;
  });

  defineExpose({
    emitTableParams,
    initData,
  });
  await getDefaultFields();
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
  :deep(.param-input:not(.arco-input-focus, .arco-select-view-focus)) {
    &:not(:hover) {
      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-brand);
      }
    }
  }
  // :deep(.moduleNameClass) {
  //   box-shadow: 0 3px 14px 2px rgba(0/ 0/0 5%), 0 8px 10px 1px rgba(0/ 0/0 6%), 0 5px 5px -3px rgba(0/ 0/0 1%);
  // }
  .show-table-top-title {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
  }
  .tree-dropdown {
    .arco-tree-select-tree-wrapper {
      width: 200px !important;
    }
  }
  .list-show-type {
    padding: 0;
    :deep(.arco-radio-button-content) {
      padding: 4px 6px;
    }
  }
</style>
