<template>
  <div :class="['p-[0_16px_8px_16px]', props.class]">
    <MsAdvanceFilter
      ref="msAdvanceFilterRef"
      v-model:keyword="keyword"
      :view-type="ViewTypeEnum.API_DEFINITION"
      :filter-config-list="filterConfigList"
      :search-placeholder="t('apiTestManagement.searchPlaceholder')"
      @keyword-search="loadApiList(false)"
      @adv-search="handleAdvSearch"
      @refresh="loadApiList(false)"
    />
    <ms-base-table
      ref="apiTableRef"
      v-bind="propsRes"
      :action-config="batchActions"
      :first-column-width="44"
      no-disable
      class="mt-[16px]"
      :not-show-table-filter="isAdvancedSearchMode"
      filter-icon-align-left
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
      @drag-change="handleTableDragSort"
    >
      <template #[FilterSlotNameEnum.API_TEST_API_REQUEST_METHODS]="{ filterContent }">
        <apiMethodName :method="filterContent.value" />
      </template>
      <template #[FilterSlotNameEnum.API_TEST_API_REQUEST_API_STATUS]="{ filterContent }">
        <apiStatus :status="filterContent.value" />
      </template>
      <template #num="{ record }">
        <MsButton type="text" @click="openApiTab(record)">{{ record.num }}</MsButton>
      </template>
      <template #protocol="{ record }">
        <apiMethodName :method="record.protocol" />
      </template>
      <template #method="{ record }">
        <apiMethodName :method="record.method" is-tag />
      </template>
      <template #caseTotal="{ record }">
        {{ record.caseTotal }}
      </template>
      <template #createUserName="{ record }">
        <a-tooltip :content="`${record.createUserName}`" position="tl">
          <div class="one-line-text">{{ characterLimit(record.createUserName) }}</div>
        </a-tooltip>
      </template>
      <template #status="{ record }">
        <a-select
          v-if="hasAnyPermission(['PROJECT_API_DEFINITION:READ+UPDATE'])"
          v-model:model-value="record.status"
          class="param-input w-full"
          size="mini"
          @change="() => handleStatusChange(record)"
        >
          <template #label>
            <apiStatus :status="record.status" />
          </template>
          <a-option v-for="item of Object.values(RequestDefinitionStatus)" :key="item" :value="item">
            <apiStatus :status="item" />
          </a-option>
        </a-select>
        <apiStatus v-else :status="record.status" />
      </template>
      <template #action="{ record }">
        <MsButton
          v-permission="['PROJECT_API_DEFINITION:READ+UPDATE']"
          type="text"
          class="!mr-0"
          @click="editDefinition(record)"
        >
          {{ t('common.edit') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_DEFINITION:READ+UPDATE']" direction="vertical" :margin="8"></a-divider>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION:READ+EXECUTE']"
          type="text"
          class="!mr-0"
          @click="executeDefinition(record)"
        >
          {{ t('apiTestManagement.execute') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_DEFINITION:READ+EXECUTE']" direction="vertical" :margin="8"></a-divider>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION:READ+ADD']"
          type="text"
          class="!mr-0"
          @click="copyDefinition(record)"
        >
          {{ t('common.copy') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_DEFINITION:READ+ADD']" direction="vertical" :margin="8"></a-divider>
        <MsTableMoreAction :list="tableMoreActionList" @select="handleTableMoreActionSelect($event, record)" />
      </template>

      <template v-if="hasAnyPermission(['PROJECT_API_DEFINITION:READ+ADD', 'FUNCTIONAL_CASE:READ+IMPORT'])" #empty>
        <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{ t('apiTestManagement.tableNoDataAndPlease') }}
          <MsButton class="ml-[8px]" @click="emit('addApiTab')">
            {{ t('apiTestManagement.newApi') }}
          </MsButton>
          {{ t('apiTestManagement.or') }}
          <MsButton class="ml-[8px]" @click="emit('import')">
            {{ t('common.import') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
  </div>
  <a-modal v-model:visible="showBatchModal" title-align="start" class="ms-modal-upload ms-modal-medium" :width="480">
    <template #title>
      {{ t('common.edit') }}
      <div class="text-[var(--color-text-4)]">
        {{
          t('apiTestManagement.batchModalSubTitle', {
            count: batchParams.currentSelectCount || tableSelected.length,
          })
        }}
      </div>
    </template>
    <a-form ref="batchFormRef" class="rounded-[4px]" :model="batchForm" layout="vertical">
      <a-form-item
        field="attr"
        :label="t('apiTestManagement.chooseAttr')"
        :rules="[{ required: true, message: t('apiTestManagement.attrRequired') }]"
        asterisk-position="end"
      >
        <template v-if="batchForm.attr === 'method'" #extra>{{ t('apiTestManagement.requestTypeTip') }}</template>
        <a-select v-model="batchForm.attr" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of attrOptions" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-if="batchForm.attr === 'tags'"
        field="values"
        :label="t('apiTestManagement.batchUpdate')"
        :validate-trigger="['blur', 'input']"
        :rules="[{ required: true, message: t('common.inputPleaseEnterTags') }]"
        asterisk-position="end"
        class="mb-0"
        required
      >
        <MsTagsInput
          v-model:model-value="batchForm.values"
          placeholder="common.tagsInputPlaceholder"
          allow-clear
          unique-value
          empty-priority-highest
          retain-input-value
        />
        <div class="text-[12px] leading-[20px] text-[var(--color-text-4)]">{{ t('ms.tagsInput.tagLimitTip') }}</div>
      </a-form-item>
      <a-form-item
        v-else
        field="value"
        :label="t('apiTestManagement.batchUpdate')"
        :rules="[{ required: true, message: t('apiTestManagement.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <apiMethodSelect v-if="batchForm.attr === 'method'" v-model:model-value="batchForm.value" />
        <a-select
          v-else
          v-model="batchForm.value"
          :placeholder="t('common.pleaseSelect')"
          :disabled="batchForm.attr === ''"
        >
          <a-option v-for="item of valueOptions" :key="item.value" :value="item.value">
            {{ item.name }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="flex" :class="[batchForm.attr === 'tags' ? 'justify-between' : 'justify-end']">
        <div
          v-if="batchForm.attr === 'tags'"
          class="flex flex-row items-center justify-center"
          style="padding-top: 10px"
        >
          <a-switch v-model="batchForm.append" class="mr-1" size="small" type="line" />
          <span class="flex items-center">
            <span class="mr-1">{{ t('caseManagement.featureCase.appendTag') }}</span>
            <span class="mt-[2px]">
              <a-tooltip>
                <IconQuestionCircle class="h-[16px] w-[16px] text-[rgb(var(--primary-5))]" />
                <template #content>
                  <div>{{ t('caseManagement.featureCase.enableTags') }}</div>
                  <div>{{ t('caseManagement.featureCase.closeTags') }}</div>
                </template>
              </a-tooltip>
            </span>
          </span>
        </div>
        <div class="flex justify-end">
          <a-button type="secondary" :disabled="batchUpdateLoading" @click="cancelBatch">
            {{ t('common.cancel') }}
          </a-button>
          <a-button class="ml-3" type="primary" :loading="batchUpdateLoading" @click="batchUpdate">
            {{ t('common.update') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
  <a-modal
    v-model:visible="moveModalVisible"
    title-align="start"
    class="ms-modal-no-padding ms-modal-small"
    :mask-closable="false"
    :ok-text="t('apiTestManagement.batchMoveConfirm')"
    :ok-button-props="{ disabled: selectedModuleKeys.length === 0 }"
    :cancel-button-props="{ disabled: batchMoveApiLoading }"
    :on-before-ok="handleApiMove"
    @close="handleMoveApiModalCancel"
  >
    <template #title>
      <div class="flex items-center">
        {{ isBatchMove ? t('common.batchMove') : t('common.move') }}
        <div
          class="one-line-text ml-[4px] max-w-[70%] text-[var(--color-text-4)]"
          :title="
            isBatchMove
              ? t('apiTestManagement.batchModalSubTitle', { count: batchParams.currentSelectCount })
              : `(${activeApi?.name})`
          "
        >
          {{
            isBatchMove
              ? t('apiTestManagement.batchModalSubTitle', { count: batchParams.currentSelectCount })
              : `(${activeApi?.name})`
          }}
        </div>
      </div>
    </template>
    <moduleTree
      v-if="moveModalVisible"
      ref="moveModuleTreeRef"
      :is-expand-all="true"
      :is-modal="true"
      :active-module="props.activeModule"
      @folder-node-select="folderNodeSelect"
    />
  </a-modal>
  <a-modal
    v-model:visible="showExportModal"
    :title="t('common.export')"
    title-align="start"
    class="ms-modal-upload ms-modal-medium"
    :width="400"
  >
    <div class="mb-[16px] flex gap-[8px]">
      <div
        v-for="item of platformList"
        :key="item.value"
        :class="`import-item ${exportPlatform === item.value ? 'import-item--active' : ''}`"
        @click="exportPlatform = item.value"
      >
        <div class="text-[var(--color-text-1)]">{{ item.name }}</div>
      </div>
    </div>
    <template v-if="exportPlatform === RequestExportFormat.MeterSphere">
      <div class="mb-[16px] flex items-center gap-[4px]">
        <a-switch v-model:model-value="exportApiCase" size="small" />
        {{ t('apiTestManagement.exportCase') }}
      </div>
      <div class="flex items-center gap-[4px]">
        <a-switch v-model:model-value="exportApiMock" size="small" />
        {{ t('apiTestManagement.exportMock') }}
      </div>
    </template>
    <div v-else-if="exportPlatform === RequestExportFormat.SWAGGER" class="text-[var(--color-text-4)]">
      {{ t('apiTestManagement.exportSwaggerTip') }}
    </div>
    <template #footer>
      <div class="flex justify-end">
        <a-button type="secondary" :disabled="exportLoading" @click="cancelExport">
          {{ t('common.cancel') }}
        </a-button>
        <a-button class="ml-3" type="primary" :loading="exportLoading" @click="exportApi">
          {{ t('common.export') }}
        </a-button>
      </div>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { FormInstance, Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsAdvanceFilter from '@/components/pure/ms-advance-filter/index.vue';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import moduleTree from '@/views/api-test/management/components/moduleTree.vue';

  import {
    batchDeleteDefinition,
    batchMoveDefinition,
    batchUpdateDefinition,
    deleteDefinition,
    exportApiDefinition,
    getApiDownloadFile,
    getDefinitionPage,
    sortDefinition,
    stopApiExport,
    updateDefinition,
  } from '@/api/modules/api-test/management';
  import { getSocket } from '@/api/modules/project-management/commonScript';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useCacheStore from '@/store/modules/cache/cache';
  import { characterLimit, downloadByteFile, getGenerateId, operationWidth } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { ApiDefinitionDetail, ApiDefinitionGetModuleParams } from '@/models/apiTest/management';
  import { DragSortParams, ModuleTreeNode } from '@/models/common';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { RequestDefinitionStatus, RequestExportFormat, RequestMethods } from '@/enums/apiEnum';
  import { CacheTabTypeEnum } from '@/enums/cacheTabEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const cacheStore = useCacheStore();
  defineOptions({
    name: CacheTabTypeEnum.API_TEST_API_TABLE,
  });

  const props = defineProps<{
    class?: string;
    activeModule: string;
    offspringIds: string[];
    selectedProtocols: string[]; // 查看的协议类型
    readOnly?: boolean; // 是否是只读模式
    refreshTimeStamp?: number;
    moduleTreeData?: ModuleTreeNode[];
    memberOptions: { label: string; value: string }[];
  }>();
  const emit = defineEmits<{
    (e: 'openApiTab', record: ApiDefinitionDetail, isExecute?: boolean): void;
    (e: 'openCopyApiTab', record: ApiDefinitionDetail): void;
    (e: 'addApiTab'): void;
    (e: 'import'): void;
    (e: 'handleAdvSearch', isStartAdvance: boolean): void;
    (
      e: 'openEditApiTab',
      options: { apiInfo: ApiDefinitionDetail; isCopy: boolean; isExecute: boolean; isEdit: boolean }
    ): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();
  const tableStore = useTableStore();

  const folderTreePathMap = inject<MsTreeNodeData[]>('folderTreePathMap');
  const refreshModuleTree: (() => Promise<any>) | undefined = inject('refreshModuleTree');
  const refreshModuleTreeCount: ((data: ApiDefinitionGetModuleParams) => Promise<any>) | undefined =
    inject('refreshModuleTreeCount');
  const keyword = ref('');

  const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'PROJECT_API_DEFINITION:READ+DELETE',
      'PROJECT_API_DEFINITION:READ+ADD',
      'PROJECT_API_DEFINITION:READ+EXECUTE',
      'PROJECT_API_DEFINITION:READ+UPDATE',
    ])
  );

  const protocolList = inject<Ref<ProtocolItem[]>>('protocols', ref([]));
  const requestMethodsOptions = computed(() => {
    const otherMethods = protocolList.value
      .filter((e) => e.protocol !== 'HTTP')
      .map((item) => {
        return {
          value: item.protocol,
          key: item.protocol,
        };
      });
    const httpMethods = Object.values(RequestMethods).map((e) => {
      return {
        value: e,
        key: e,
      };
    });
    return [...httpMethods, ...otherMethods];
  });
  const requestApiStatus = computed(() => {
    return Object.values(RequestDefinitionStatus).map((e) => {
      return {
        value: e,
        key: e,
      };
    });
  });
  const apiStatusOptions = [
    {
      name: t('apiTestManagement.processing'),
      value: RequestDefinitionStatus.PROCESSING,
    },
    {
      name: t('apiTestManagement.done'),
      value: RequestDefinitionStatus.DONE,
    },
    {
      name: t('apiTestManagement.deprecate'),
      value: RequestDefinitionStatus.DEPRECATED,
    },
    {
      name: t('apiTestManagement.debugging'),
      value: RequestDefinitionStatus.DEBUGGING,
    },
  ];
  const apiTableRef = ref();
  let columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      fixed: 'left',
      width: 100,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiTestManagement.apiName',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiTestManagement.protocol',
      dataIndex: 'protocol',
      slotName: 'protocol',
      showTooltip: true,
      width: 80,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.apiType',
      dataIndex: 'method',
      slotName: 'method',
      width: 100,
      showDrag: true,
      filterConfig: {
        options: [],
        filterSlotName: FilterSlotNameEnum.API_TEST_API_REQUEST_METHODS,
      },
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      slotName: 'status',
      filterConfig: {
        options: requestApiStatus.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_API_REQUEST_API_STATUS,
      },
      width: 130,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.belongModule',
      dataIndex: 'moduleName',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.caseTotal',
      dataIndex: 'caseTotal',
      showTooltip: true,
      width: 100,
      showDrag: true,
      slotName: 'caseTotal',
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      isTag: true,
      isStringTag: true,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.updateTime',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
      showDrag: true,
    },
    {
      title: 'common.creator',
      slotName: 'createUserName',
      dataIndex: 'createUser',
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
      title: hasOperationPermission.value ? 'common.operation' : '',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: operationWidth(215, hasOperationPermission.value ? 200 : 50),
    },
  ];

  function initFilterColumn() {
    columns = columns.map((item) => {
      if (item.dataIndex === 'method') {
        return {
          ...item,
          filterConfig: {
            ...item.filterConfig,
            options: requestMethodsOptions.value,
          },
        };
      }
      return item;
    });
  }

  await initFilterColumn();
  await tableStore.initColumn(TableKeyEnum.API_TEST, columns, 'drawer', true);
  if (props.readOnly) {
    columns = columns.filter(
      (item) => !['version', 'createTime', 'updateTime', 'operation'].includes(item.dataIndex as string)
    );
  }

  const { propsRes, propsEvent, viewId, advanceFilter, setAdvanceFilter, loadList, setLoadListParams, resetSelector } =
    useTable(
      getDefinitionPage,
      {
        columns: props.readOnly ? columns : [],
        scroll: { x: '100%' },
        tableKey: props.readOnly ? undefined : TableKeyEnum.API_TEST,
        showSetting: !props.readOnly,
        selectable: hasAnyPermission([
          'PROJECT_API_DEFINITION:READ+DELETE',
          'PROJECT_API_DEFINITION:READ+EXECUTE',
          'PROJECT_API_DEFINITION:READ+UPDATE',
        ]),
        showSelectAll: !props.readOnly,
        draggable: hasAnyPermission(['PROJECT_API_DEFINITION:READ+UPDATE']) ? { type: 'handle', width: 32 } : undefined,
        heightUsed: 272,
        paginationSize: 'mini',
      },
      (item) => ({
        ...item,
        fullPath: folderTreePathMap?.[item.moduleId],
        createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
        updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
      })
    );
  const batchActions = {
    baseAction: [
      {
        label: 'common.export',
        eventTag: 'export',
        permission: ['PROJECT_API_DEFINITION:READ+EXPORT'],
      },
      {
        label: 'common.edit',
        eventTag: 'edit',
        permission: ['PROJECT_API_DEFINITION:READ+UPDATE'],
      },
      {
        label: 'common.move',
        eventTag: 'move',
        permission: ['PROJECT_API_DEFINITION:READ+UPDATE'],
      },
    ],
    moreAction: [
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['PROJECT_API_DEFINITION:READ+DELETE'],
      },
    ],
  };
  const tableMoreActionList = [
    {
      eventTag: 'delete',
      label: t('common.delete'),
      danger: true,
      permission: ['PROJECT_API_DEFINITION:READ+DELETE'],
    },
  ];

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);
  async function getModuleIds() {
    let moduleIds: string[] = [];
    if (props.activeModule !== 'all' && !isAdvancedSearchMode.value) {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.API_TEST);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    return moduleIds;
  }

  async function loadApiList(hasRefreshTree: boolean) {
    const moduleIds = await getModuleIds();
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds,
      protocols: isAdvancedSearchMode.value ? protocolList.value.map((item) => item.protocol) : props.selectedProtocols,
      filter: propsRes.value.filter,
      viewId: viewId.value,
      combineSearch: advanceFilter,
    };

    if (!hasRefreshTree && typeof refreshModuleTreeCount === 'function' && !isAdvancedSearchMode.value) {
      refreshModuleTreeCount({
        keyword: keyword.value,
        filter: propsRes.value.filter,
        moduleIds: [],
        protocols: props.selectedProtocols,
        projectId: appStore.currentProjectId,
      });
    }

    setLoadListParams(params);
    loadList();
  }

  watch(
    () => props.refreshTimeStamp,
    (val) => {
      if (val) {
        loadApiList(true);
      }
    }
  );

  watch(
    () => [props.activeModule, props.selectedProtocols],
    () => {
      if (isAdvancedSearchMode.value) return;
      resetSelector();
      loadApiList(true);
    }
  );

  const filterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      type: FilterType.INPUT,
    },
    {
      title: 'apiTestManagement.apiName',
      dataIndex: 'name',
      type: FilterType.INPUT,
    },
    {
      title: 'common.belongModule',
      dataIndex: 'moduleId',
      type: FilterType.TREE_SELECT,
      treeSelectData: props.moduleTreeData,
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
      title: 'apiTestManagement.protocol',
      dataIndex: 'protocol',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        labelKey: 'protocol',
        valueKey: 'protocol',
        options: protocolList.value,
      },
    },
    {
      title: 'apiTestManagement.apiType',
      dataIndex: 'method',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        labelKey: 'key',
        options: requestMethodsOptions.value,
      },
    },
    {
      title: 'apiTestManagement.path',
      dataIndex: 'path',
      type: FilterType.INPUT,
    },
    {
      title: 'apiTestManagement.apiStatus',
      dataIndex: 'status',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        labelKey: 'name',
        options: apiStatusOptions,
      },
    },
    {
      title: 'apiTestManagement.caseTotal',
      dataIndex: 'caseTotal',
      type: FilterType.NUMBER,
      numberProps: {
        min: 0,
        precision: 0,
      },
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
      title: 'common.updateTime',
      dataIndex: 'updateTime',
      type: FilterType.DATE_PICKER,
    },
  ]);
  // 高级检索
  const handleAdvSearch = async (filter: FilterResult, id: string, isStartAdvance: boolean) => {
    resetSelector();
    emit('handleAdvSearch', isStartAdvance);
    keyword.value = '';
    setAdvanceFilter(filter, id);
    await loadApiList(false); // 基础筛选都清空
  };

  async function handleStatusChange(record: ApiDefinitionDetail) {
    try {
      await updateDefinition({
        id: record.id,
        status: record.status,
      });
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function onMountedLoad() {
    if (props.selectedProtocols.length > 0) {
      loadApiList(true);
    }
  }

  const isActivated = computed(() => cacheStore.cacheViews.includes(CacheTabTypeEnum.API_TEST_API_TABLE));

  onBeforeMount(() => {
    cacheStore.clearCache();
    if (!isActivated.value) {
      onMountedLoad();
      cacheStore.setCache(CacheTabTypeEnum.API_TEST_API_TABLE);
    }
  });

  onActivated(() => {
    if (isActivated.value) {
      onMountedLoad();
    }
  });

  const tableSelected = ref<(string | number)[]>([]);
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });
  async function getBatchConditionParams() {
    const selectModules = await getModuleIds();
    return {
      condition: {
        keyword: keyword.value,
        filter: propsRes.value.filter,
        viewId: viewId.value,
        combineSearch: advanceFilter,
      },
      projectId: appStore.currentProjectId,
      protocols: isAdvancedSearchMode.value ? protocolList.value.map((item) => item.protocol) : props.selectedProtocols,
      moduleIds: selectModules,
    };
  }

  /**
   * 删除接口
   */
  function deleteApi(record?: ApiDefinitionDetail, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('apiTestManagement.deleteApiTipTitle', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('apiTestManagement.batchDeleteApiTip', {
        count: params?.currentSelectCount || tableSelected.value.length,
      });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'error',
      title,
      content: t('apiTestManagement.deleteApiTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (isBatch) {
            const batchConditionParams = await getBatchConditionParams();
            await batchDeleteDefinition({
              selectIds,
              selectAll: !!params?.selectAll,
              excludeIds: params?.excludeIds || [],
              ...batchConditionParams,
              deleteAll: true,
            });
          } else {
            await deleteDefinition(record?.id as string);
          }
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadApiList(true);
          if (typeof refreshModuleTree === 'function') {
            refreshModuleTree();
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleTableMoreActionSelect(item: ActionsItem, record: ApiDefinitionDetail) {
    switch (item.eventTag) {
      case 'delete':
        deleteApi(record);
        break;
      default:
        break;
    }
  }

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  const showBatchModal = ref(false);
  const batchUpdateLoading = ref(false);
  const batchFormRef = ref<FormInstance>();
  const batchForm = ref({
    attr: '',
    value: '',
    values: [],
    append: false,
  });
  const fullAttrs = [
    {
      name: 'apiTestManagement.apiStatus',
      value: 'status',
    },
    {
      name: 'apiTestManagement.apiType',
      value: 'method',
    },
    {
      name: 'common.tag',
      value: 'tags',
    },
  ];
  const attrOptions = computed(() => {
    // TODO 协议 选择的数据不包含HTTP协议，则选择属性中不展示“请求类型“
    // if (props.protocol === 'HTTP') {
    return fullAttrs;
    // }
    // return fullAttrs.filter((e) => e.value !== 'method');
  });
  const valueOptions = computed(() => {
    switch (batchForm.value.attr) {
      case 'status':
        return apiStatusOptions;
      default:
        return [];
    }
  });

  function cancelBatch() {
    showBatchModal.value = false;
    batchFormRef.value?.resetFields();
    batchForm.value = {
      attr: '',
      value: '',
      values: [],
      append: false,
    };
  }

  function batchUpdate() {
    batchFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          batchUpdateLoading.value = true;
          const batchConditionParams = await getBatchConditionParams();
          await batchUpdateDefinition({
            selectIds: batchParams.value?.selectedIds || [],
            selectAll: !!batchParams.value?.selectAll,
            excludeIds: batchParams.value?.excludeIds || [],
            ...batchConditionParams,
            type: batchForm.value.attr,
            append: batchForm.value.append,
            [batchForm.value.attr]: batchForm.value.attr === 'tags' ? batchForm.value.values : batchForm.value.value,
          });
          Message.success(t('common.updateSuccess'));
          cancelBatch();
          resetSelector();
          loadApiList(false);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          batchUpdateLoading.value = false;
        }
      }
    });
  }

  const moveModuleTreeRef = ref<InstanceType<typeof moduleTree>>();
  const moveModalVisible = ref(false);
  const selectedModuleKeys = ref<(string | number)[]>([]); // 移动文件选中节点
  const isBatchMove = ref(false); // 是否批量移动文件
  const activeApi = ref<ApiDefinitionDetail | null>(null); // 当前查看的接口项
  const batchMoveApiLoading = ref(false); // 批量移动文件loading

  /**
   * 单个/批量移动接口
   */
  async function handleApiMove() {
    try {
      batchMoveApiLoading.value = true;
      const batchConditionParams = await getBatchConditionParams();
      await batchMoveDefinition({
        selectIds: isBatchMove.value ? batchParams.value?.selectedIds || [] : [activeApi.value?.id || ''],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        ...batchConditionParams,
        moduleId: selectedModuleKeys.value[0],
      });
      Message.success(t('common.batchMoveSuccess'));
      if (isBatchMove.value) {
        tableSelected.value = [];
        isBatchMove.value = false;
      } else {
        activeApi.value = null;
      }
      resetSelector();
      loadList();

      if (typeof refreshModuleTree === 'function') {
        refreshModuleTree();
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      batchMoveApiLoading.value = false;
    }
  }

  function handleMoveApiModalCancel() {
    moveModalVisible.value = false;
    selectedModuleKeys.value = [];
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(keys: (string | number)[]) {
    selectedModuleKeys.value = keys;
  }

  const showExportModal = ref(false);
  const platformList = [
    {
      name: 'Swagger',
      value: RequestExportFormat.SWAGGER,
    },
    {
      name: 'MeterSphere',
      value: RequestExportFormat.MeterSphere,
    },
  ];
  const exportPlatform = ref(RequestExportFormat.SWAGGER);
  const exportApiCase = ref(false);
  const exportApiMock = ref(false);
  const exportLoading = ref(false);

  function cancelExport() {
    showExportModal.value = false;
    exportPlatform.value = RequestExportFormat.SWAGGER;
  }

  // 下载文件
  async function downloadFile(id: string) {
    try {
      const response = await getApiDownloadFile(appStore.currentProjectId, id);
      downloadByteFile(response, 'metersphere-export.json');
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
      duration: 999999999, // 一直展示，除非手动关闭
      closable: true,
    });
  }

  const websocket = ref<WebSocket>();
  const reportId = ref('');
  const isShowExportingMessage = ref(false); // 正在导出提示显示中
  const exportingMessage = ref();

  // 开启websocket监听，接收结果
  function startWebsocketGetExportResult() {
    websocket.value = getSocket(reportId.value, '/ws/export');
    websocket.value.addEventListener('message', (event) => {
      const data = JSON.parse(event.data);
      if (data.msgType === 'EXEC_RESULT') {
        exportingMessage.value.close();
        reportId.value = data.fileId;
        // taskId.value = data.taskId;
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
    });
  }

  // 取消导出
  async function stopExport(taskId: string) {
    try {
      await stopApiExport(taskId);
      exportingMessage.value.close();
      websocket.value?.close();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  // 提示：正在导出
  function showExportingMessage(taskId: string) {
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
                stopExport(taskId);
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

  /**
   * 导出接口
   */
  async function exportApi() {
    try {
      exportLoading.value = true;
      reportId.value = getGenerateId();
      startWebsocketGetExportResult();
      const batchConditionParams = await getBatchConditionParams();
      const res = await exportApiDefinition(
        {
          selectIds: tableSelected.value as string[],
          selectAll: !!batchParams.value?.selectAll,
          excludeIds: batchParams.value?.excludeIds || [],
          ...batchConditionParams,
          exportApiCase: exportApiCase.value,
          exportApiMock: exportApiMock.value,
          sort: propsRes.value.sorter || {},
          fileId: reportId.value,
        },
        exportPlatform.value
      );

      showExportingMessage(res);
      showExportModal.value = false;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      exportLoading.value = false;
    }
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'export':
        showExportModal.value = true;
        break;
      case 'delete':
        deleteApi(undefined, true, params);
        break;
      case 'edit':
        showBatchModal.value = true;
        break;
      case 'move':
        isBatchMove.value = true;
        moveModalVisible.value = true;
        nextTick(() => {
          moveModuleTreeRef.value?.refresh();
        });
        break;
      default:
        break;
    }
  }

  function openApiTab(record: ApiDefinitionDetail) {
    emit('openApiTab', record);
  }

  function copyDefinition(record: ApiDefinitionDetail) {
    emit('openCopyApiTab', record);
  }

  function executeDefinition(record: ApiDefinitionDetail) {
    emit('openApiTab', record, true);
  }

  function editDefinition(record: ApiDefinitionDetail) {
    emit('openEditApiTab', { apiInfo: record, isCopy: false, isExecute: false, isEdit: true });
  }

  // 拖拽排序
  async function handleTableDragSort(params: DragSortParams) {
    try {
      await sortDefinition({
        ...params,
      });
      Message.success(t('caseManagement.featureCase.sortSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => requestMethodsOptions.value,
    () => {
      initFilterColumn();
    }
  );
</script>

<style lang="less" scoped>
  .import-item {
    @apply flex cursor-pointer items-center bg-white;

    padding: 8px;
    width: 150px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    gap: 6px;
  }
  .import-item--active {
    border: 1px solid rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
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
</style>
