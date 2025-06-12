<!-- eslint-disable prefer-destructuring -->
<template>
  <div class="h-full">
    <keep-alive :include="[CacheTabTypeEnum.CASE_MANAGEMENT_TABLE_FILTER]">
      <MsCacheWrapper
        v-if="showType === 'list'"
        :key="CacheTabTypeEnum.CASE_MANAGEMENT_TABLE_FILTER"
        :cache-name="CacheTabTypeEnum.CASE_MANAGEMENT_TABLE_FILTER"
      >
        <!-- 用例表开始 -->
        <MsAdvanceFilter
          ref="msAdvanceFilterRef"
          v-model:keyword="keyword"
          :view-type="ViewTypeEnum.FUNCTIONAL_CASE"
          :filter-config-list="filterConfigList"
          :custom-fields-config-list="searchCustomFields"
          :search-placeholder="t('caseManagement.featureCase.searchPlaceholder')"
          :count="modulesCount[props.activeFolder] || 0"
          :name="moduleNamePath"
          :view-name="viewName"
          @keyword-search="fetchData"
          @adv-search="handleAdvSearch"
          @refresh="fetchData()"
        >
          <template #left>
            <div class="flex">
              <a-button
                v-permission="['FUNCTIONAL_CASE:READ+ADD']"
                class="mr-[12px]"
                type="primary"
                @click="caseDetail"
              >
                {{ t('common.newCreate') }}
              </a-button>
              <ImportCase ref="importCaseRef" @init-modules="emit('initModules')" @confirm-import="confirmImport" />
              <MsAiButton
                v-if="aiStore.aiSourceNameList.length > 0"
                class="ml-[12px]"
                :text="t('settings.navbar.ai')"
                @click="openAI"
              />
            </div>
          </template>
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
          :not-show-table-filter="isAdvancedSearchMode"
          @selected-change="handleTableSelect"
          v-on="propsEvent"
          @batch-action="handleTableBatch"
          @change="changeHandler"
          @cell-click="handleCellClick"
          @filter-change="filterChange"
        >
          <template #num="{ record }">
            <div class="flex items-center gap-[8px]">
              <MsAiTag v-if="record.aiCreate" />
              <span type="text" class="one-line-text cursor-pointer px-0 text-[rgb(var(--primary-5))]">
                {{ record.num }}
              </span>
            </div>
          </template>
          <template #name="{ record }">
            <div class="one-line-text">{{ record.name }}</div>
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
                <span class="text-[var(--color-text-2)]">
                  <caseLevel :case-level="record.caseLevel" />
                </span>
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
              v-model:model-value="record.moduleId"
              dropdown-class-name="tree-dropdown"
              class="param-input w-full"
              :data="caseTreeData"
              :allow-search="true"
              :field-names="{
                title: 'name',
                key: 'id',
                children: 'children',
              }"
              :filter-tree-node="filterTreeNode"
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
                  <div class="one-line-text max-w-[200px]">{{ node.name }}</div>
                </a-tooltip>
              </template>
            </a-tree-select>
            <a-tooltip v-else :content="getModules(record.moduleId)" position="top">
              <span class="one-line-text inline-block" @click.stop="record.showModuleTree = true">
                {{ getModules(record.moduleId) }}
              </span>
            </a-tooltip>
          </template>
          <template #createUserName="{ record }">
            <a-tooltip :content="`${record.createUserName}`" position="tl">
              <div class="one-line-text">{{ record.createUserName }}</div>
            </a-tooltip>
          </template>
          <template #updateUserName="{ record }">
            <a-tooltip :content="`${record.updateUserName}`" position="tl">
              <div class="one-line-text">{{ record.updateUserName }}</div>
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
            <MsButton v-permission="['FUNCTIONAL_CASE:READ+UPDATE']" class="!mr-0" @click="operateCase(record, true)">
              {{ t('common.edit') }}
            </MsButton>
            <a-divider
              v-permission="['FUNCTIONAL_CASE:READ+UPDATE']"
              class="!mx-2 h-[12px]"
              direction="vertical"
              :margin="8"
            ></a-divider>
            <MsButton v-permission="['FUNCTIONAL_CASE:READ+ADD']" class="!mr-0" @click="operateCase(record, false)">
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
              <MsButton v-permission="['FUNCTIONAL_CASE:READ+ADD']" class="!mx-[8px]" @click="createCase">
                {{ t('caseManagement.featureCase.creatingCase') }}
              </MsButton>
              <span v-permission="['FUNCTIONAL_CASE:READ+IMPORT']"> {{ t('caseManagement.featureCase.or') }} </span>
              <MsButton v-permission="['FUNCTIONAL_CASE:READ+IMPORT']" class="!mx-[8px]" @click="importCase()">
                {{ t('common.import') }}
              </MsButton>
            </div>
          </template>
        </ms-base-table>
      </MsCacheWrapper>
      <!-- 用例表结束 -->
      <div v-else class="h-full">
        <div class="flex flex-row items-center justify-between">
          <a-popover title="" position="bottom">
            <div class="show-table-top-title">
              <div class="one-line-text max-h-[32px] max-w-[300px] text-[var(--color-text-1)]">
                {{ moduleNamePath }}
              </div>
              <span class="text-[var(--color-text-4)]"> ({{ modulesCount[props.activeFolder] || 0 }})</span>
            </div>
            <template #content>
              <div class="max-w-[400px] text-[14px] font-medium text-[var(--color-text-1)]">
                {{ moduleNamePath }}
                <span class="text-[var(--color-text-4)]">({{ modulesCount[props.activeFolder] || 0 }})</span>
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
            v-if="props.moduleCountIsInit"
            :module-id="props.activeFolder"
            :modules-count="modulesCount"
            :module-name="props.moduleName"
            @save="handleMinderSave"
          />
          <MsDrawer v-model:visible="visible" :width="480" :mask="false">
            {{ nodeData.text }}
          </MsDrawer>
          <!-- 脑图结束 -->
        </div>
      </div>
    </keep-alive>
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
  <MsExportDrawer
    v-model:visible="showExportVisible"
    :export-loading="exportLoading"
    :all-data="
      exportType === 'exportExcel'
        ? exportOptionData
        : { systemColumns: exportOptionData.systemColumns, customColumns: exportOptionData.customColumns }
    "
    :disabled-cancel-keys="['name']"
    :drawer-title-props="{
      title:
        exportType === 'exportExcel'
          ? t('caseManagement.featureCase.exportExcel')
          : t('caseManagement.featureCase.exportXMindNoUnit'),
      count: batchParams.currentSelectCount,
    }"
    @confirm="exportConfirm"
  >
    <template v-if="exportType === 'exportExcel'" #footerLeft>
      <div class="flex items-center gap-[8px]">
        <div>{{ t('caseManagement.featureCase.exportExcel.exportFormat') }}</div>
        <a-radio-group v-model:model-value="isMerge">
          <a-radio :value="false">
            {{ t('common.default') }}
            <a-tooltip :content="t('caseManagement.featureCase.exportExcel.defaultTip')">
              <MsIcon
                class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-4))]"
                type="icon-icon-maybe_outlined"
              />
            </a-tooltip>
          </a-radio>
          <a-radio :value="true">
            {{ t('caseManagement.featureCase.exportExcel.cellSplitting') }}
            <a-tooltip :content="t('caseManagement.featureCase.exportExcel.cellSplittingTip')">
              <MsIcon
                class="ml-[4px] text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-4))]"
                type="icon-icon-maybe_outlined"
              />
            </a-tooltip>
          </a-radio>
        </a-radio-group>
      </div>
    </template>
  </MsExportDrawer>
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
    :is-edit="isEdit"
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
  <!-- AI 生成 -->
  <MsAIDrawer
    v-if="isInitAiDrawer"
    v-model:visible="aiDrawerVisible"
    type="case"
    :module-id="props.activeFolder"
    :template-id="templateId"
    @sync-feature-case="handleSyncFeatureCase"
    @sync-success="initData()"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Message, TableChangeExtra, TableData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import { getFilterCustomFields, MsAdvanceFilter } from '@/components/pure/ms-advance-filter';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsAiButton from '@/components/pure/ms-ai-button/index.vue';
  import MsAiTag from '@/components/pure/ms-ai-tag/index.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCacheWrapper from '@/components/pure/ms-cache-wrapper/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import { MsExportDrawerMap, MsExportDrawerOption } from '@/components/pure/ms-export-drawer/types';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type {
    BatchActionConfig,
    BatchActionParams,
    BatchActionQueryParams,
    MsTableColumn,
  } from '@/components/pure/ms-table/type';
  import { MsTableProps } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import ExecuteStatusTag from '@/components/business/ms-case-associate/executeResult.vue';
  import MsFeatureCaseMinder from '@/components/business/ms-minders/featureCaseMinder/index.vue';
  import BatchEditModal from './batchEditModal.vue';
  import CaseDetailDrawer from './caseDetailDrawer.vue';
  import FeatureCaseTree from './caseTree.vue';
  import ImportCase from './import/index.vue';
  import AddDemandModal from './tabContent/tabDemand/addDemandModal.vue';
  import ThirdDemandDrawer from './tabContent/tabDemand/thirdDemandDrawer.vue';

  import {
    batchAssociationDemand,
    batchCopyToModules,
    batchDeleteCase,
    batchMoveToModules,
    checkCaseExportTask,
    deleteCaseRequest,
    dragSort,
    exportExcelCase,
    exportXMindCase,
    getCaseDefaultFields,
    getCaseDetail,
    getCaseDownloadFile,
    getCaseExportConfig,
    getCaseList,
    getCustomFieldsTable,
    stopCaseExport,
    updateCaseRequest,
  } from '@/api/modules/case-management/featureCase';
  import { getCaseRelatedInfo } from '@/api/modules/project-management/menuManagement';
  import { NAV_NAVIGATION } from '@/config/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useWebsocket from '@/hooks/useWebsocket';
  import { useAppStore, useTableStore } from '@/store';
  import useCacheStore from '@/store/modules/cache/cache';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';
  import useMinderStore from '@/store/modules/components/minder-editor';
  import { ShowType } from '@/store/modules/components/minder-editor/types';
  import useAIStore from '@/store/modules/setting/ai';
  import {
    characterLimit,
    downloadByteFile,
    filterTreeNode,
    findNodeByKey,
    findNodePathByKey,
    getGenerateId,
    mapTree,
  } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { AiCaseTransformResult } from '@/models/ai';
  import type {
    CaseManagementTable,
    CaseModuleQueryParams,
    CreateOrUpdateDemand,
    CustomAttributes,
    DemandItem,
    DragCase,
  } from '@/models/caseManagement/featureCase';
  import { ModuleTreeNode } from '@/models/common';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { CacheTabTypeEnum } from '@/enums/cacheTabEnum';
  import { MinderKeyEnum } from '@/enums/minderEnum';
  import { CaseManagementRouteEnum, RouteEnum } from '@/enums/routeEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { WorkNavValueEnum } from '@/enums/workbenchEnum';

  import { executionResultMap, getCaseLevels, getTableFields, statusIconMap } from './utils';
  import { LabelValue } from '@arco-design/web-vue/es/tree-select/interface';

  const MsAIDrawer = defineAsyncComponent(() => import('@/components/business/ms-ai-drawer/index.vue'));

  const cacheStore = useCacheStore();

  const MsExportDrawer = defineAsyncComponent(() => import('@/components/pure/ms-export-drawer/index.vue'));

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
    moduleCountIsInit: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'init', params: CaseModuleQueryParams, refreshModule?: boolean): void;
    (e: 'initModules'): void;
    (e: 'setActiveFolder'): void;
  }>();

  const minderStore = useMinderStore();
  const aiStore = useAIStore();

  const keyword = ref<string>('');
  const groupKeyword = ref<string>('');

  const showType = ref<ShowType>('list');

  const modulesCount = computed(() => {
    return featureCaseStore.modulesCount;
  });

  const isInitAiDrawer = ref<boolean>(false);
  const aiDrawerVisible = ref<boolean>(false);
  function openAI() {
    isInitAiDrawer.value = true;
    aiDrawerVisible.value = true;
  }

  function handleSyncFeatureCase(detail: AiCaseTransformResult) {
    aiDrawerVisible.value = false;
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
      query: {
        aiCreate: 'Y',
      },
      state: {
        detail: JSON.stringify(detail),
      },
    });
  }

  function handleShowTypeChange(val: string | number | boolean) {
    minderStore.setShowType(MinderKeyEnum.FEATURE_CASE_MINDER, val as ShowType);
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
      'title': 'ID',
      'dataIndex': 'num',
      'slotName': 'num',
      'sortIndex': 1,
      'sortable': {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      'width': 150,
      'showTooltip': true,
      'columnSelectorDisabled': true,
      'filter-icon-align-left': true,
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
      dataIndex: 'updateUser',
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

  const batchMoreActions = computed<BatchActionParams[]>(() => {
    const addDemandAction = [
      {
        label: 'caseManagement.featureCase.addDemand',
        eventTag: 'addDemand',
        permission: ['FUNCTIONAL_CASE:READ+UPDATE'],
      },
    ];
    const linkDemandAction = [
      {
        label: 'caseManagement.featureCase.associatedDemand',
        eventTag: 'associatedDemand',
        permission: ['FUNCTIONAL_CASE:READ+UPDATE'],
      },
    ];
    const deleteMoreAction = [
      {
        isDivider: true,
      },
      {
        label: 'common.delete',
        eventTag: 'delete',
        permission: ['FUNCTIONAL_CASE:READ+DELETE'],
        danger: true,
      },
    ];
    if (!Object.keys(platformInfo.value).length) {
      return [...addDemandAction, ...deleteMoreAction];
    }
    return [...addDemandAction, ...linkDemandAction, ...deleteMoreAction];
  });

  const tableBatchActions: BatchActionConfig = {
    baseAction: [
      {
        label: 'caseManagement.featureCase.export',
        eventTag: 'export',
        permission: ['FUNCTIONAL_CASE:READ+EXPORT'],
        children: [
          {
            label: 'caseManagement.featureCase.exportExcelXlsx',
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
    moreAction: [],
  };

  const filterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      dataIndex: 'name',
      type: FilterType.INPUT,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleId',
      type: FilterType.TREE_SELECT,
      treeSelectData: caseTreeData.value,
      treeSelectProps: {
        fieldNames: {
          title: 'name',
          key: 'id',
          children: 'children',
        },
        multiple: true,
        treeCheckable: true,
        treeCheckStrictly: true,
      },
    },
    {
      title: 'caseManagement.featureCase.tableColumnReviewResult',
      dataIndex: 'reviewStatus',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: reviewResultOptions.value,
      },
    },
    {
      title: 'caseManagement.featureCase.tableColumnExecutionResult',
      dataIndex: 'lastExecuteResult',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        options: executeResultOptions.value,
      },
    },
    {
      title: 'caseManagement.featureCase.associatedDemand',
      dataIndex: 'demand',
      type: FilterType.INPUT,
    },
    {
      title: 'caseManagement.featureCase.relatedAttachments',
      dataIndex: 'attachment',
      type: FilterType.INPUT,
    },
    {
      title: 'common.creator',
      dataIndex: 'createUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.createTime',
      dataIndex: 'createTime',
      type: FilterType.DATE_PICKER,
    },
    {
      title: 'common.updateUserName',
      dataIndex: 'updateUser',
      type: FilterType.MEMBER,
    },
    {
      title: 'common.updateTime',
      dataIndex: 'updateTime',
      type: FilterType.DATE_PICKER,
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      type: FilterType.TAGS_INPUT,
      numberProps: {
        min: 0,
        precision: 0,
      },
    },
  ]);
  const searchCustomFields = ref<FilterFormItem[]>([]);

  async function initFilter() {
    try {
      const result = await getCustomFieldsTable(currentProjectId.value);
      searchCustomFields.value = getFilterCustomFields(result); // 处理自定义字段
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
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

  const tableProps = ref<Partial<MsTableProps<CaseManagementTable>>>({
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TABLE,
    selectable: true,
    showSetting: true,
    heightUsed: 236,
    draggable: { type: 'handle' },
    paginationSize: 'mini',
    draggableCondition: true,
  });

  const {
    propsRes,
    propsEvent,
    viewId,
    advanceFilter,
    loadList,
    setLoadListParams,
    resetSelector,
    setKeyword,
    setAdvanceFilter,
  } = useTable(
    getCaseList,
    tableProps.value,
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

  const hasUpdatePermission = computed(() => hasAnyPermission(['FUNCTIONAL_CASE:READ+UPDATE']));

  watch(
    () => hasUpdatePermission.value,
    (val) => {
      tableProps.value.draggableCondition = val;
    },
    {
      immediate: true,
    }
  );

  const initBatchParams: BatchActionQueryParams = {
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  };
  const batchParams = ref<BatchActionQueryParams>(cloneDeep(initBatchParams));

  const conditionParams = computed(() => {
    return {
      keyword: keyword.value,
      filter: propsRes.value.filter,
      viewId: viewId.value,
      combineSearch: advanceFilter,
    };
  });

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);
  async function initTableParams() {
    let moduleIds: string[] = [];
    if (props.activeFolder !== 'all' && !isAdvancedSearchMode.value) {
      moduleIds = [props.activeFolder];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.CASE_MANAGEMENT_TABLE);
      if (getAllChildren) {
        moduleIds = [props.activeFolder, ...props.offspringIds];
      }
    }

    return {
      moduleIds,
      projectId: currentProjectId.value,
      excludeIds: batchParams.value.excludeIds || [],
      selectAll: batchParams.value.selectAll,
      selectIds: batchParams.value.selectedIds || [],
      keyword: keyword.value,
      filter: propsRes.value.filter,
    };
  }
  // 获取父组件模块数量
  async function emitTableParams(refreshModule = false) {
    if (isAdvancedSearchMode.value) return;
    const tableParams: CaseModuleQueryParams = await initTableParams();
    emit(
      'init',
      {
        moduleIds: tableParams.moduleIds,
        current: propsRes.value.msPagination?.current,
        pageSize: propsRes.value.msPagination?.pageSize,
        filter: propsRes.value.filter,
        projectId: currentProjectId.value,
        keyword: showType.value === 'list' ? keyword.value : '',
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

  // 创建用例
  function caseDetail() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
    });
  }
  // 导入用例
  const importCaseRef = ref<InstanceType<typeof ImportCase>>();
  function importCase() {
    importCaseRef.value?.importCase();
  }
  function confirmImport() {
    emit('initModules');
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
  const isEdit = ref<boolean>(false);
  // 编辑&复制
  function operateCase(record: CaseManagementTable, operateType: boolean) {
    // TODO 这个版本暂时调整为打开详情抽屉编辑
    isEdit.value = operateType;
    if (operateType) {
      const index = propsRes.value.data.findIndex((item) => item.id === record.id);
      showCaseDetail(record.id, index);
    } else {
      router.push({
        name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
        query: {
          id: record.id,
        },
        params: {
          mode: operateType ? 'edit' : 'copy',
        },
      });
    }
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

  const exportType = ref<'exportExcel' | 'exportXMind'>('exportExcel');
  const showExportVisible = ref<boolean>(false);
  const exportLoading = ref(false);
  const exportOptionData = ref<MsExportDrawerMap>({});
  const isMerge = ref<boolean>(false);
  async function getCaseExportData() {
    try {
      const res = await getCaseExportConfig(currentProjectId.value);
      exportOptionData.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const websocket = ref<WebSocket>();
  const reportId = ref('');
  const taskId = ref('');

  // 下载文件
  async function downloadFile(id: string) {
    try {
      const response = await getCaseDownloadFile(currentProjectId.value, id);
      const fileName = response?.headers.get('content-disposition').split('filename=')[1];
      downloadByteFile(response.data, decodeURIComponent(fileName));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 提示：导出成功
  function showExportSuccessfulMessage(id: string, count: number) {
    Message.success({
      content: () =>
        h('div', { class: 'flex flex-col gap-[8px] items-start' }, [
          h('div', { class: 'font-medium' }, t('common.exportSuccessful')),
          h('div', { class: 'flex items-center gap-[12px]' }, [
            h('div', t('caseManagement.featureCase.exportCaseCount', { number: count })),
            h(
              MsButton,
              {
                type: 'text',
                onClick() {
                  downloadFile(id);
                },
              },
              { default: () => t('common.downloadFile') }
            ),
          ]),
        ]),
      duration: 10000, // 10s 自动关闭
      closable: true,
    });
  }

  const isShowExportingMessage = ref(false); // 正在导出提示显示中
  const exportingMessage = ref();
  // 取消导出
  async function cancelExport() {
    try {
      await stopCaseExport(taskId.value);
      exportingMessage.value.close();
      websocket.value?.close();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 提示：正在导出
  function showExportingMessage() {
    if (isShowExportingMessage.value) return;
    isShowExportingMessage.value = true;
    exportingMessage.value = Message.loading({
      content: () =>
        h('div', { class: 'flex items-center gap-[12px]' }, [
          h('div', t('common.exporting')),
          h(
            MsButton,
            {
              type: 'text',
              onClick() {
                cancelExport();
              },
            },
            { default: () => t('common.cancel') }
          ),
        ]),
      duration: 999999999, // 一直展示，除非手动关闭
      closable: true,
      onClose() {
        isShowExportingMessage.value = false;
      },
    });
  }
  // 开启websocket监听，接收结果
  async function startWebsocketGetExportResult() {
    const { createSocket, websocket: _websocket } = useWebsocket({
      reportId: reportId.value,
      socketUrl: '/ws/export',
      onMessage: (event) => {
        const data = JSON.parse(event.data);
        if (data.msgType === 'EXEC_RESULT') {
          exportingMessage.value.close();
          reportId.value = data.fileId;
          taskId.value = data.taskId;
          if (data.isSuccessful) {
            showExportSuccessfulMessage(reportId.value, data.count);
          } else {
            Message.error({
              content: t('common.exportFailed'),
              duration: 999999999, // 一直展示，除非手动关闭
              closable: true,
            });
          }
          websocket.value?.close();
        }
      },
    });
    await createSocket();
    websocket.value = _websocket.value;
  }

  function getConfirmFields(option: MsExportDrawerOption[], columnType: string) {
    return option
      .filter((optionItem) => optionItem.columnType === columnType)
      .map((item) => ({ id: item.key, name: item.text }));
  }
  const exportConfirm = async (option: MsExportDrawerOption[]) => {
    try {
      exportLoading.value = true;
      const { selectedIds, selectAll, excludeIds } = batchParams.value;
      reportId.value = getGenerateId();
      await startWebsocketGetExportResult();
      const params = {
        projectId: currentProjectId.value,
        selectIds: selectAll ? [] : selectedIds,
        excludeIds: excludeIds || [],
        moduleIds: props.activeFolder === 'all' ? [] : [props.activeFolder, ...props.offspringIds],
        condition: conditionParams.value,
        selectAll,
        systemFields: getConfirmFields(option, 'system'),
        customFields: getConfirmFields(option, 'custom'),
        fileId: reportId.value,
      };
      let res;
      if (exportType.value === 'exportExcel') {
        res = await exportExcelCase({
          ...params,
          otherFields: getConfirmFields(option, 'other'),
          isMerge: isMerge.value,
        });
      } else {
        res = await exportXMindCase(params);
      }
      taskId.value = res;
      showExportingMessage();
      exportLoading.value = false;
      showExportVisible.value = false;
      resetSelector();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      exportLoading.value = false;
    }
  };

  async function batchExport() {
    try {
      const res = await checkCaseExportTask();
      if (!res.fileId || !res.fileId.length) {
        showExportVisible.value = true;
      } else {
        reportId.value = res.fileId;
        taskId.value = res.taskId;
        Message.error(t('caseManagement.featureCase.alreadyExportTasks'));
        if (!websocket.value) {
          await startWebsocketGetExportResult();
        }
        showExportingMessage();
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
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
        condition: conditionParams.value,
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
        batchParams.value = cloneDeep(initBatchParams);
        Message.success(t('caseManagement.featureCase.batchCopySuccess'));
      }
      isMove.value = false;
      resetSelector();
      initData();
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
            condition: conditionParams.value,
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

  async function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchParams.value = params;
    switch (event.eventTag) {
      case 'exportExcel':
      case 'exportXMind':
        exportType.value = event.eventTag;
        batchExport();
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
    batchParams.value.selectIds = [];
    batchParams.value.excludeIds = [];
    resetSelector();
    initData();
  }

  function handleCellClick(record: TableData) {
    const index = propsRes.value.data.findIndex((item) => item.id === record.id);
    isEdit.value = false;
    showCaseDetail(record.id, index);
  }

  // 创建详情
  function createCase() {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
    });
  }

  const templateId = ref<string>(''); // 模板ID
  // 处理自定义字段列
  let customFieldsColumns: Record<string, any>[] = [];
  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);

  let fullColumns: MsTableColumn = [];
  // 处理自定义字段展示
  async function getDefaultFields() {
    customFieldsColumns = [];
    const result = await getCaseDefaultFields(currentProjectId.value);
    templateId.value = result.id;
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
  // 高级检索
  const handleAdvSearch = async (filter: FilterResult, id: string) => {
    resetSelector();
    emit('setActiveFolder');
    keyword.value = '';
    await getLoadListParams(); // 基础筛选都清空
    setAdvanceFilter(filter, id);
    loadList();
  };
  // 更新用例等级
  async function handleStatusChange(record: any) {
    try {
      const detailResult = await getCaseDetail(record.id);
      const { customFields } = detailResult;
      const customFieldsList = customFields.map((item: any) => {
        if (item.internal && item.internalFieldKey === 'functional_priority') {
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
        condition: conditionParams.value,
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
        condition: conditionParams.value,
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

  // 获取三方需求
  onBeforeMount(async () => {
    if (route.query.openAi === 'Y') {
      openAI();
    }
    try {
      const result = await getCaseRelatedInfo(currentProjectId.value);
      if (result && result.platform_key) {
        platformInfo.value = { ...result };
      }
      tableBatchActions.moreAction = batchMoreActions.value;
    } catch (error) {
      console.log(error);
    }
  });

  function filterChange() {
    emitTableParams();
  }

  async function mountedLoad() {
    if (route.query.home) {
      propsRes.value.filter = { ...NAV_NAVIGATION[route.query.home as WorkNavValueEnum] };
    }
    await initFilter();
    await initData();
    getCaseExportData();
    if (route.query.id) {
      showCaseDetail(route.query.id as string, -1);
    }
  }

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
      if (props.activeFolder !== 'recycle' && val && !isAdvancedSearchMode.value) {
        initData();
        resetSelector();
      }
    }
  );

  const isActivated = computed(() => cacheStore.cacheViews.includes(RouteEnum.CASE_MANAGEMENT_CASE));
  const viewName = ref<string>('');

  onBeforeUnmount(() => {
    showDetailDrawer.value = false;
  });

  onMounted(() => {
    if (!isActivated.value) {
      // 切换菜单默认还是列表；已经在脑图的时候，刷新浏览器，保持脑图状态
      showType.value = minderStore.getShowType(MinderKeyEnum.FEATURE_CASE_MINDER);
      if (route.query.showType) {
        showType.value = route.query.showType as ShowType;
      }
      if (route.query.view) {
        setAdvanceFilter({ conditions: [], searchMode: 'AND' }, route.query.view as string);
        viewName.value = route.query.view as string;
      }
      mountedLoad();
    }
  });

  onActivated(() => {
    if (isActivated.value) {
      mountedLoad();
    }
  });

  defineExpose({
    isAdvancedSearchMode,
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
  :deep(.arco-radio-group) {
    display: flex;
  }
</style>
