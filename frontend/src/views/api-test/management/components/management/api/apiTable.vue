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
    >
      <template #right>
        <ShareButton
          v-if="hasAnyPermission(['PROJECT_API_DEFINITION:READ+SHARE'])"
          ref="shareButtonRef"
          @create="createShare"
          @show-share-list="showShareList"
        />
      </template>
    </MsAdvanceFilter>
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
      @filter-change="filterChange"
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
          <a-option v-for="item of fullAttrs" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-if="batchForm.attr === 'tags'"
        :class="`${selectedTagType === TagUpdateTypeEnum.CLEAR ? 'mb-0' : 'mb-[16px]'}`"
        field="type"
        :label="t('common.type')"
      >
        <a-radio-group v-model:model-value="selectedTagType" size="small">
          <a-radio :value="TagUpdateTypeEnum.UPDATE"> {{ t('common.update') }}</a-radio>
          <a-radio :value="TagUpdateTypeEnum.APPEND"> {{ t('caseManagement.featureCase.appendTag') }}</a-radio>
          <a-radio :value="TagUpdateTypeEnum.CLEAR">{{ t('common.clear') }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        v-if="batchForm.attr === 'tags' && selectedTagType !== TagUpdateTypeEnum.CLEAR"
        field="values"
        :label="t('common.batchUpdate')"
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
        v-if="batchForm.attr !== 'tags' && selectedTagType !== TagUpdateTypeEnum.CLEAR"
        field="value"
        :label="t('common.batchUpdate')"
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
      <div class="flex justify-end">
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
  <ApiExportModal
    v-model:visible="showExportModal"
    :batch-params="batchParams"
    :condition-params="getBatchConditionParams"
    :sorter="propsRes.sorter || {}"
  />
  <CreateShareModal
    v-model:visible="showShareModal"
    :record="editRecord"
    @close="cancelHandler"
    @load-list="loadShareList"
  />
  <ShareListDrawer
    ref="shareListRef"
    v-model:visible="showShareListDrawer"
    @edit-or-create="editHandler"
    @load-list="shareButtonRef?.initShareList()"
  />
</template>

<script setup lang="ts">
  import { useRoute } from 'vue-router';
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
  import CreateShareModal from './createShareModal.vue';
  import ShareButton from './shareButton.vue';
  import ShareListDrawer from './shareListDrawer.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import apiMethodSelect from '@/views/api-test/components/apiMethodSelect.vue';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import ApiExportModal from '@/views/api-test/management/components/management/api/apiExportModal.vue';
  import moduleTree from '@/views/api-test/management/components/moduleTree.vue';

  import {
    batchDeleteDefinition,
    batchMoveDefinition,
    batchUpdateDefinition,
    deleteDefinition,
    getDefinitionPage,
    sortDefinition,
    updateDefinition,
  } from '@/api/modules/api-test/management';
  import { NAV_NAVIGATION } from '@/config/workbench';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useCacheStore from '@/store/modules/cache/cache';
  import { characterLimit, operationWidth } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ProtocolItem } from '@/models/apiTest/common';
  import type { ShareDetail } from '@/models/apiTest/management';
  import { ApiDefinitionDetail, ApiDefinitionGetModuleParams } from '@/models/apiTest/management';
  import { DragSortParams, ModuleTreeNode } from '@/models/common';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { RequestDefinitionStatus, RequestMethods } from '@/enums/apiEnum';
  import { CacheTabTypeEnum } from '@/enums/cacheTabEnum';
  import { TagUpdateTypeEnum } from '@/enums/commonEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum, FilterSlotNameEnum } from '@/enums/tableFilterEnum';
  import { WorkNavValueEnum } from '@/enums/workbenchEnum';

  import { apiStatusOptions } from '@/views/api-test/components/config';

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
  const route = useRoute();
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
  const selectedTagType = ref<TagUpdateTypeEnum>(TagUpdateTypeEnum.UPDATE);

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

  initFilterColumn();
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
    if (route.query.home) {
      propsRes.value.filter = { ...NAV_NAVIGATION[route.query.home as WorkNavValueEnum] };
    }

    if (props.selectedProtocols.length > 0 || route.query.home) {
      loadApiList(!route.query.home);
    }
  }

  const isActivated = computed(() => cacheStore.cacheViews.includes(CacheTabTypeEnum.API_TEST_API_TABLE));

  onMounted(() => {
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
    selectedTagType.value = TagUpdateTypeEnum.UPDATE;
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
            append: selectedTagType.value === TagUpdateTypeEnum.APPEND,
            clear: selectedTagType.value === TagUpdateTypeEnum.CLEAR,
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

  const showShareModal = ref<boolean>(false);
  // 创建分享
  function createShare() {
    showShareModal.value = true;
  }
  const showShareListDrawer = ref<boolean>(false);
  // 分享列表
  function showShareList() {
    showShareListDrawer.value = true;
  }

  const editRecord = ref<ShareDetail>();
  // 编辑分享
  function editHandler(record?: ShareDetail) {
    editRecord.value = record;
    showShareModal.value = true;
  }
  const shareListRef = ref<InstanceType<typeof ShareListDrawer>>();
  const shareButtonRef = ref<InstanceType<typeof ShareButton>>();

  function cancelHandler() {
    showShareModal.value = false;
    editRecord.value = undefined;
  }
  // 创建分享后打开分享列表
  function loadShareList() {
    if (!showShareListDrawer.value) {
      showShareListDrawer.value = true;
    } else {
      shareListRef.value?.searchList();
    }
    shareButtonRef.value?.initShareList();
  }

  function filterChange() {
    if (typeof refreshModuleTreeCount === 'function' && !isAdvancedSearchMode.value) {
      refreshModuleTreeCount({
        keyword: keyword.value,
        filter: propsRes.value.filter,
        moduleIds: [],
        protocols: props.selectedProtocols,
        projectId: appStore.currentProjectId,
      });
    }
  }

  watch(
    () => requestMethodsOptions.value,
    () => {
      initFilterColumn();
    }
  );

  defineExpose({
    isAdvancedSearchMode,
  });

  await tableStore.initColumn(TableKeyEnum.API_TEST, columns, 'drawer', true);
</script>

<style lang="less" scoped>
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
