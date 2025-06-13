<template>
  <div :class="['p-[16px_22px]', props.class]">
    <div :class="['mb-[16px]', 'flex', 'items-center', props.isApi ? 'justify-between' : 'justify-end']">
      <a-button
        v-show="props.isApi"
        v-permission="['PROJECT_API_DEFINITION_MOCK:READ+ADD']"
        type="primary"
        @click="createMock"
      >
        {{ t('mockManagement.createMock') }}
      </a-button>
      <MsAdvanceFilter
        ref="msAdvanceFilterRef"
        v-model:keyword="keyword"
        :view-type="ViewTypeEnum.API_MOCK"
        :filter-config-list="filterConfigList"
        :search-placeholder="t('apiTestManagement.searchPlaceholder')"
        @keyword-search="loadMockList"
        @adv-search="handleAdvSearch"
        @refresh="loadMockList"
      />
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="batchActions"
      :first-column-width="44"
      no-disable
      filter-icon-align-left
      :not-show-table-filter="isAdvancedSearchMode"
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
    >
      <template #expectNum="{ record }">
        <MsButton type="text" @click="handleOpenDetail(record)">
          {{ record.expectNum }}
        </MsButton>
      </template>
      <template #protocol="{ record }">
        <apiMethodName :method="record.protocol" />
      </template>
      <template #enable="{ record }">
        <a-switch
          v-model="record.enable"
          type="line"
          :before-change="() => handleBeforeEnableChange(record)"
          :disabled="!hasAnyPermission(['PROJECT_API_DEFINITION_MOCK:READ+UPDATE'])"
        ></a-switch>
      </template>
      <template #apiMethod="{ record }">
        <apiMethodName :method="record.apiMethod" is-tag />
      </template>
      <template #action="{ record }">
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_MOCK:READ+UPDATE']"
          type="text"
          class="!mr-0"
          @click="editMock(record)"
        >
          {{ t('common.edit') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION:READ+EXECUTE']"
          type="text"
          class="!mr-0"
          @click="debugMock(record)"
        >
          {{ t('apiTestManagement.debug') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsButton
          v-permission="['PROJECT_API_DEFINITION_MOCK:READ+ADD']"
          type="text"
          class="!mr-0"
          @click="handleCopyMock(record)"
        >
          {{ t('common.copy') }}
        </MsButton>
        <a-divider direction="vertical" :margin="8"></a-divider>
        <MsTableMoreAction :list="tableMoreActionList" @select="handleTableMoreActionSelect($event, record)" />
      </template>
      <template v-if="hasAnyPermission(['PROJECT_API_DEFINITION_MOCK:READ+ADD']) && props.isApi" #empty>
        <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{ t('apiTestManagement.tableNoDataAndPlease') }}
          <MsButton class="ml-[8px]" @click="createMock">
            {{ t('mockManagement.createMock') }}
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
        <a-select v-model="batchForm.attr" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of fullAttrs" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-if="batchForm.attr === 'Tags'"
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
        v-if="batchForm.attr === 'Tags' && selectedTagType !== TagUpdateTypeEnum.CLEAR"
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
        v-if="batchForm.attr !== 'Tags'"
        field="value"
        :label="t('common.batchUpdate')"
        :rules="[{ required: true, message: t('apiTestManagement.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <a-radio-group v-model:model-value="batchForm.value">
          <a-radio :value="true">
            {{ t('common.enable') }}
          </a-radio>
          <a-radio :value="false">
            {{ t('common.disable') }}
          </a-radio>
        </a-radio-group>
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
  <mockDetailDrawer
    v-model:visible="mockDetailDrawerVisible"
    :definition-detail="mockBelongDefinitionDetail"
    :detail-id="activeMockRecord?.id"
    :is-copy="isCopy"
    :is-edit-mode="isEdit"
    @add-done="loadMockList"
    @delete="() => removeMock(activeMockRecord)"
  />
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsAdvanceFilter from '@/components/pure/ms-advance-filter/index.vue';
  import { FilterFormItem, FilterResult } from '@/components/pure/ms-advance-filter/type';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import mockDetailDrawer from './mockDetailDrawer.vue';
  import apiMethodName from '@/views/api-test/components/apiMethodName.vue';
  import { RequestParam } from '@/views/api-test/components/requestComposition/index.vue';

  import {
    batchDeleteMock,
    batchEditMock,
    deleteMock,
    getDefinitionDetail,
    getDefinitionMockPage,
    getMockDetail,
    getMockUrl,
    updateMockStatusPage,
  } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import useCacheStore from '@/store/modules/cache/cache';
  import { characterLimit, operationWidth } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { ProtocolItem } from '@/models/apiTest/common';
  import { ApiDefinitionMockDetail } from '@/models/apiTest/management';
  import { MockDetail } from '@/models/apiTest/mock';
  import { FilterType, ViewTypeEnum } from '@/enums/advancedFilterEnum';
  import { RequestComposition, RequestMethods } from '@/enums/apiEnum';
  import { CacheTabTypeEnum } from '@/enums/cacheTabEnum';
  import { TagUpdateTypeEnum } from '@/enums/commonEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  defineOptions({
    name: CacheTabTypeEnum.API_TEST_MOCK_TABLE,
  });
  const props = defineProps<{
    isApi?: boolean; // 接口定义详情的case tab下
    class?: string;
    activeModule: string;
    offspringIds: string[];
    definitionDetail: RequestParam;
    readOnly?: boolean; // 是否是只读模式
    selectedProtocols: string[]; // 查看的协议类型
    heightUsed?: number;
    isNeedInit?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'change'): void;
    (e: 'debug', mock: MockDetail): void;
    (e: 'handleAdvSearch', isStartAdvance: boolean): void;
  }>();

  const appStore = useAppStore();
  const cacheStore = useCacheStore();
  const tableStore = useTableStore();
  const { t } = useI18n();
  const { openModal } = useModal();

  const keyword = ref('');
  const protocolList = inject<Ref<ProtocolItem[]>>('protocols', ref([]));

  let columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'expectNum',
      slotName: 'expectNum',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 120,
    },
    {
      title: 'mockManagement.name',
      dataIndex: 'name',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
    },
    {
      title: 'apiTestManagement.apiType',
      dataIndex: 'apiMethod',
      slotName: 'apiMethod',
      width: 200,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.protocol',
      dataIndex: 'protocol',
      slotName: 'protocol',
      width: 150,
      showDrag: true,
    },
    {
      title: 'mockManagement.apiPath',
      dataIndex: 'apiPath',
      slotName: 'apiPath',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'common.tag',
      dataIndex: 'tags',
      isTag: true,
      isStringTag: true,
      showDrag: true,
    },
    {
      title: 'common.status',
      dataIndex: 'enable',
      slotName: 'enable',
      width: 100,
      showDrag: true,
    },
    {
      title: 'mockManagement.operationUser',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'mockManagement.updateTime',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
      width: 180,
    },
    {
      title: 'common.operation',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: operationWidth(230, 200),
    },
  ];
  const { propsRes, propsEvent, viewId, advanceFilter, setAdvanceFilter, loadList, setLoadListParams, resetSelector } =
    useTable(getDefinitionMockPage, {
      columns: props.readOnly ? columns : [],
      scroll: { x: '100%' },
      tableKey: props.readOnly ? undefined : TableKeyEnum.API_TEST_MANAGEMENT_MOCK,
      showSetting: !props.readOnly,
      selectable: true,
      heightUsed: (props.heightUsed || 0) + 282,
      showSelectAll: !props.readOnly,
      draggable: props.readOnly ? undefined : { type: 'handle', width: 32 },
      paginationSize: 'mini',
    });
  const batchActions = {
    baseAction: [
      {
        label: 'common.edit',
        eventTag: 'edit',
        permission: ['PROJECT_API_DEFINITION_MOCK:READ+UPDATE'],
      },
      {
        label: 'common.delete',
        eventTag: 'delete',
        danger: true,
        permission: ['PROJECT_API_DEFINITION_MOCK:READ+DELETE'],
      },
    ],
  };
  const tableMoreActionList = [
    {
      eventTag: 'copyMock',
      label: t('mockManagement.copyMock'),
      danger: false,
      permission: ['PROJECT_API_DEFINITION_MOCK:READ'],
    },
    {
      eventTag: 'delete',
      label: t('common.delete'),
      danger: true,
      permission: ['PROJECT_API_DEFINITION_MOCK:READ+DELETE'],
    },
  ];

  const msAdvanceFilterRef = ref<InstanceType<typeof MsAdvanceFilter>>();
  const isAdvancedSearchMode = computed(() => msAdvanceFilterRef.value?.isAdvancedSearchMode);
  async function getModuleIds() {
    let moduleIds: string[] = [];
    if (props.activeModule !== 'all' && !isAdvancedSearchMode.value) {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.API_TEST_MANAGEMENT_MOCK);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    return moduleIds;
  }

  async function loadMockList() {
    const selectModules = await getModuleIds();
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      protocols: isAdvancedSearchMode.value ? protocolList.value.map((item) => item.protocol) : props.selectedProtocols,
      apiDefinitionId: props.definitionDetail.id !== 'all' ? props.definitionDetail.id : undefined,
      filter: {},
      moduleIds: selectModules,
      viewId: viewId.value,
      combineSearch: advanceFilter,
    };
    setLoadListParams(params);
    loadList();
  }

  watch([() => props.activeModule, () => props.selectedProtocols], () => {
    if (isAdvancedSearchMode.value) return;
    loadMockList();
  });

  const isActivated = computed(() => cacheStore.cacheViews.includes(CacheTabTypeEnum.API_TEST_MOCK_TABLE));

  onBeforeMount(() => {
    cacheStore.clearCache();
    if (isActivated.value) return;
    if (props.isNeedInit) {
      loadMockList();
    }
    cacheStore.setCache(CacheTabTypeEnum.API_TEST_MOCK_TABLE);
  });
  onActivated(() => {
    if (isActivated.value) {
      loadMockList();
    }
  });

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

  const filterConfigList = computed<FilterFormItem[]>(() => [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'expectNum',
      type: FilterType.INPUT,
    },
    {
      title: 'mockManagement.name',
      dataIndex: 'name',
      type: FilterType.INPUT,
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
      title: 'mockManagement.apiPath',
      dataIndex: 'apiPath',
      type: FilterType.INPUT,
    },
    {
      title: 'apiTestManagement.apiType',
      dataIndex: 'apiMethod',
      type: FilterType.SELECT,
      selectProps: {
        multiple: true,
        labelKey: 'key',
        options: requestMethodsOptions.value,
      },
    },
    {
      title: 'common.status',
      dataIndex: 'enable',
      type: FilterType.SELECT_EQUAL,
      selectProps: {
        options: [
          { label: t('system.config.email.close'), value: false },
          { label: t('system.config.email.open'), value: true },
        ],
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
      title: 'mockManagement.operationUser',
      dataIndex: 'createUser',
      type: FilterType.MEMBER,
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
    await loadMockList(); // 基础筛选都清空
  };

  async function handleBeforeEnableChange(record: ApiDefinitionMockDetail) {
    try {
      await updateMockStatusPage(record.id);
      Message.success(record.enable ? t('common.disableSuccess') : t('common.enableSuccess'));
      return true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      return false;
    }
  }

  const tableSelected = ref<(string | number)[]>([]);
  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  /**
   * 删除接口
   */
  function removeMock(record?: ApiDefinitionMockDetail, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('apiTestManagement.confirmDelete', { name: characterLimit(record?.name) });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('mockManagement.batchDeleteMockTip', {
        count: params?.currentSelectCount || tableSelected.value.length,
      });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'error',
      title,
      content: t('apiTestManagement.deleteMockTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (isBatch) {
            const selectModules = await getModuleIds();
            await batchDeleteMock({
              selectIds,
              selectAll: !!params?.selectAll,
              excludeIds: params?.excludeIds || [],
              condition: {
                keyword: keyword.value,
                viewId: viewId.value,
                combineSearch: advanceFilter,
              },
              projectId: appStore.currentProjectId,
              moduleIds: selectModules,
              protocols: isAdvancedSearchMode.value
                ? protocolList.value.map((item) => item.protocol)
                : props.selectedProtocols,
            });
          } else {
            await deleteMock({
              id: record?.id as string,
              projectId: appStore.currentProjectId,
            });
          }
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadMockList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const { copy, isSupported } = useClipboard({ legacy: true });

  async function copyMockUrl(record: ApiDefinitionMockDetail) {
    try {
      appStore.showLoading();
      const url = await getMockUrl(record.id);
      if (isSupported) {
        copy(url);
        Message.success(t('common.copySuccess'));
      } else {
        Message.warning(t('common.copyNotSupport'));
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
    }
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleTableMoreActionSelect(item: ActionsItem, record: ApiDefinitionMockDetail) {
    switch (item.eventTag) {
      case 'delete':
        removeMock(record);
        break;
      case 'copyMock':
        copyMockUrl(record);
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
    attr: 'Status' as 'Status' | 'Tags',
    value: true,
    values: [],
    append: false,
  });
  const fullAttrs = [
    {
      name: 'common.status',
      value: 'Status',
    },
    {
      name: 'common.tag',
      value: 'Tags',
    },
  ];

  const selectedTagType = ref<TagUpdateTypeEnum>(TagUpdateTypeEnum.UPDATE);

  function cancelBatch() {
    showBatchModal.value = false;
    batchFormRef.value?.resetFields();
    batchForm.value = {
      attr: 'Status',
      value: true,
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
          await batchEditMock({
            selectIds: batchParams.value?.selectedIds || [],
            selectAll: !!batchParams.value?.selectAll,
            excludeIds: batchParams.value?.excludeIds || [],
            condition: {
              keyword: keyword.value,
              viewId: viewId.value,
              combineSearch: advanceFilter,
            },
            projectId: appStore.currentProjectId,
            moduleIds: await getModuleIds(),
            type: batchForm.value.attr,
            append: selectedTagType.value === TagUpdateTypeEnum.APPEND,
            tags: batchForm.value.attr === 'Tags' ? batchForm.value.values : [],
            enable: batchForm.value.attr === 'Status' ? batchForm.value.value : false,
            protocols: isAdvancedSearchMode.value
              ? protocolList.value.map((item) => item.protocol)
              : props.selectedProtocols,
            clear: selectedTagType.value === TagUpdateTypeEnum.CLEAR,
          });
          Message.success(t('common.updateSuccess'));
          cancelBatch();
          resetSelector();
          loadMockList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          batchUpdateLoading.value = false;
        }
      }
    });
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'delete':
        removeMock(undefined, true, params);
        break;
      case 'edit':
        showBatchModal.value = true;
        break;
      default:
        break;
    }
  }

  const mockDetailDrawerVisible = ref(false);
  const activeMockRecord = ref<ApiDefinitionMockDetail>();
  const isCopy = ref(false);
  const isEdit = ref(false);

  function createMock() {
    activeMockRecord.value = undefined;
    isCopy.value = false;
    isEdit.value = false;
    mockDetailDrawerVisible.value = true;
  }

  const mockBelongDefinitionDetail = ref<RequestParam>(props.definitionDetail);

  watch(
    () => props.definitionDetail.id,
    () => {
      mockBelongDefinitionDetail.value = props.definitionDetail;
    }
  );

  async function openMockDetailDrawer(record: ApiDefinitionMockDetail) {
    try {
      activeMockRecord.value = record;
      if (props.definitionDetail.id === 'all') {
        // 从全部 mock 列表页查看 mock 详情，需要先加载其接口定义详情
        appStore.showLoading();
        const res = await getDefinitionDetail(record.apiDefinitionId);
        mockBelongDefinitionDetail.value = {
          ...(res.request as RequestParam),
          id: res.id,
          type: 'mock',
          isNew: false,
          isCopy: false,
          protocol: res.protocol,
          activeTab: RequestComposition.BODY,
          executeLoading: false,
          responseDefinition: res.response,
        };
      } else {
        mockBelongDefinitionDetail.value = props.definitionDetail;
      }
      mockDetailDrawerVisible.value = true;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
    }
  }

  function handleOpenDetail(record: ApiDefinitionMockDetail) {
    isEdit.value = false;
    isCopy.value = false;
    openMockDetailDrawer(record);
  }

  function handleCopyMock(record: ApiDefinitionMockDetail) {
    isCopy.value = true;
    isEdit.value = false;
    openMockDetailDrawer(record);
  }

  function editMock(record: ApiDefinitionMockDetail) {
    isEdit.value = true;
    isCopy.value = false;
    openMockDetailDrawer(record);
  }

  async function debugMock(record: ApiDefinitionMockDetail) {
    try {
      appStore.showLoading();
      const res = await getMockDetail({
        id: record.id,
        projectId: appStore.currentProjectId,
      });
      emit('debug', res);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      appStore.hideLoading();
    }
  }

  defineExpose({
    loadMockList,
  });

  if (!props.readOnly) {
    await tableStore.initColumn(TableKeyEnum.API_TEST_MANAGEMENT_MOCK, columns, 'drawer');
  } else {
    columns = columns.filter(
      (item) => !['version', 'createTime', 'updateTime', 'operation'].includes(item.dataIndex as string)
    );
  }
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
