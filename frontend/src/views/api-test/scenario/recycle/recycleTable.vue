<template>
  <div :class="['p-[16px_16px]', props.class]">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="flex items-center gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('api_scenario.table.searchPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @search="loadScenarioList(true)"
          @clear="loadScenarioList(true)"
          @press-enter="loadScenarioList"
        />
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]" @click="loadScenarioList(true)">
          <template #icon>
            <icon-refresh class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
      </div>
    </div>
    <a-spin class="w-full" :loading="recoverLoading">
      <ms-base-table
        v-bind="propsRes"
        :action-config="batchActions"
        :first-column-width="44"
        no-disable
        filter-icon-align-left
        v-on="propsEvent"
        @selected-change="handleTableSelect"
        @batch-action="handleTableBatch"
        @module-change="loadScenarioList(false)"
      >
        <template #statusFilter="{ columnConfig }">
          <a-trigger
            v-model:popup-visible="statusFilterVisible"
            trigger="click"
            @popup-visible-change="handleFilterHidden"
          >
            <MsButton type="text" class="arco-btn-text--secondary ml-[10px]" @click="statusFilterVisible = true">
              {{ t(columnConfig.title as string) }}
              <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
            </MsButton>
            <template #content>
              <div class="arco-table-filters-content">
                <div class="flex items-center justify-center px-[6px] py-[2px]">
                  <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                    <a-checkbox v-for="val of Object.values(ApiScenarioStatus)" :key="val" :value="val">
                      <apiStatus :status="val" />
                    </a-checkbox>
                  </a-checkbox-group>
                </div>
              </div>
            </template>
          </a-trigger>
        </template>
        <template #num="{ record }">
          <MsButton type="text">{{ record.num }}</MsButton>
        </template>
        <template #status="{ record }">
          <apiStatus :status="record.status" />
        </template>
        <template #priority="{ record }">
          <caseLevel :case-level="record.priority as CaseLevel" />
        </template>
        <!-- 报告结果筛选 -->
        <template #lastReportStatusFilter="{ columnConfig }">
          <a-trigger
            v-model:popup-visible="lastReportStatusFilterVisible"
            trigger="click"
            @popup-visible-change="handleFilterHidden"
          >
            <a-button
              type="text"
              class="arco-btn-text--secondary p-[8px_4px]"
              @click="lastReportStatusFilterVisible = true"
            >
              <div class="font-medium">
                {{ t(columnConfig.title as string) }}
              </div>
              <icon-down :class="lastReportStatusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
            </a-button>
            <template #content>
              <div class="arco-table-filters-content">
                <div class="flex items-center justify-center px-[6px] py-[2px]">
                  <a-checkbox-group v-model:model-value="lastReportStatusListFilters" direction="vertical" size="small">
                    <a-checkbox v-for="key of lastReportStatusFilters" :key="key" :value="key">
                      <ExecutionStatus :module-type="ReportEnum.API_SCENARIO_REPORT" :status="key" />
                    </a-checkbox>
                  </a-checkbox-group>
                </div>
              </div>
            </template>
          </a-trigger>
        </template>
        <template #lastReportStatus="{ record }">
          <ExecutionStatus
            v-if="record.lastReportStatus"
            :module-type="ReportEnum.API_SCENARIO_REPORT"
            :status="record.lastReportStatus"
          />
        </template>
        <template #operation="{ record }">
          <MsButton
            v-permission="['PROJECT_API_SCENARIO:READ+DELETED']"
            type="text"
            class="!mr-0"
            @click="recover(record)"
          >
            {{ t('api_scenario.recycle.recover') }}
          </MsButton>
          <a-divider v-permission="['PROJECT_API_SCENARIO:READ+DELETED']" direction="vertical" :margin="8"></a-divider>
          <MsButton
            v-permission="['PROJECT_API_SCENARIO:READ+DELETED']"
            type="text"
            class="!mr-0"
            @click="deleteOperation(record)"
          >
            {{ t('common.delete') }}
          </MsButton>
        </template>
      </ms-base-table>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import {
    batchDeleteScenario,
    batchRecoverScenario,
    deleteScenario,
    getTrashScenarioPage,
    recoverScenario,
  } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';

  import { ApiScenarioTableItem } from '@/models/apiTest/scenario';
  import { ApiScenarioStatus } from '@/enums/apiEnum';
  import { ReportEnum, ReportStatus } from '@/enums/reportEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const props = defineProps<{
    class?: string;
    activeModule: string;
    offspringIds: string[];
    readOnly?: boolean; // 是否是只读模式
  }>();

  const lastReportStatusFilterVisible = ref(false);
  const lastReportStatusListFilters = ref<string[]>([]);
  const lastReportStatusFilters = computed(() => {
    return Object.keys(ReportStatus[ReportEnum.API_SCENARIO_REPORT]);
  });
  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();
  const emit = defineEmits(['refreshModuleTree']);
  const keyword = ref('');
  const recoverLoading = ref(false);

  const columns: MsTableColumn = [
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
      width: 126,
      showTooltip: true,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiScenario.table.columns.name',
      dataIndex: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 134,
      showTooltip: true,
      showInTable: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiScenario.table.columns.level',
      dataIndex: 'priority',
      slotName: 'priority',
      width: 100,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.status',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      width: 140,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.runResult',
      titleSlotName: 'lastReportStatusFilter',
      dataIndex: 'lastReportStatus',
      slotName: 'lastReportStatus',
      showTooltip: true,
      width: 100,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.tags',
      dataIndex: 'tags',
      isTag: true,
      isStringTag: true,
      width: 456,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.scenarioEnv',
      dataIndex: 'environmentName',
      width: 159,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.steps',
      dataIndex: 'stepTotal',
      width: 100,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.passRate',
      dataIndex: 'requestPassRate',
      width: 100,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.module',
      dataIndex: 'modulePath',
      width: 176,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.createTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 189,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.updateTime',
      dataIndex: 'updateTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 189,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.createUser',
      dataIndex: 'createUserName',
      titleSlotName: 'createUser',
      width: 109,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.updateUser',
      dataIndex: 'updateUserName',
      titleSlotName: 'updateUser',
      width: 109,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 180,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getTrashScenarioPage,
    {
      columns: props.readOnly ? columns : [],
      scroll: { x: '100%' },
      tableKey: props.readOnly ? undefined : TableKeyEnum.API_SCENARIO,
      showSetting: !props.readOnly,
      selectable: true,
      showSelectAll: !props.readOnly,
      draggable: props.readOnly ? undefined : { type: 'handle', width: 32 },
      heightUsed: 374,
      showSubdirectory: true,
    },
    (item) => ({
      ...item,
      createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  const batchActions = {
    baseAction: [
      {
        label: 'api_scenario.recycle.recover',
        eventTag: 'recover',
        permission: ['PROJECT_API_SCENARIO:READ+DELETE'],
      },
      {
        label: 'api_scenario.recycle.batchCleanOut',
        eventTag: 'delete',
        permission: ['PROJECT_API_SCENARIO:READ+DELETE'],
      },
    ],
  };

  const statusFilterVisible = ref(false);
  const statusFilters = ref<string[]>([]);
  const tableStore = useTableStore();

  async function loadScenarioList(refreshTreeCount?: boolean) {
    let moduleIds: string[] = [];
    if (props.activeModule && props.activeModule !== 'all') {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.API_SCENARIO);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }

    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds,
      filter: {
        lastReportStatus: lastReportStatusListFilters.value,
        status: statusFilters.value,
      },
    };
    setLoadListParams(params);
    await loadList();
    if (refreshTreeCount) {
      emit('refreshModuleTree', params);
    }
  }

  function handleFilterHidden(val: boolean) {
    if (!val) {
      loadScenarioList(false);
    }
  }

  const tableSelected = ref<(string | number)[]>([]);

  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  // 恢复
  async function recover(record?: ApiScenarioTableItem, isBatch?: boolean) {
    try {
      if (isBatch) {
        recoverLoading.value = true;
        await batchRecoverScenario({
          selectIds: batchParams.value?.selectedIds || [],
          selectAll: !!batchParams.value?.selectAll,
          excludeIds: batchParams.value?.excludeIds || [],
          condition: { keyword: keyword.value },
          projectId: appStore.currentProjectId,
          moduleIds: props.activeModule === 'all' ? [] : [props.activeModule],
        });
      } else {
        await recoverScenario(record?.id as string);
      }

      Message.success(t('common.deleteSuccess'));
      tableSelected.value = [];
      resetSelector();
      loadScenarioList(true);
    } catch (e) {
      console.log(e);
    } finally {
      recoverLoading.value = false;
    }
  }
  /**
   * 删除接口
   */
  function deleteOperation(record?: ApiScenarioTableItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('api_scenario.table.deleteScenarioTipTitle', { name: record?.name });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('api_scenario.table.batchDeleteScenarioTip', {
        count: params?.currentSelectCount || tableSelected.value.length,
      });
      selectIds = tableSelected.value as string[];
    }
    openModal({
      type: 'error',
      title,
      content: t('api_scenario.table.deleteScenarioTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (isBatch) {
            await batchDeleteScenario({
              selectIds,
              selectAll: !!params?.selectAll,
              excludeIds: params?.excludeIds || [],
              condition: { keyword: keyword.value },
              projectId: appStore.currentProjectId,
              moduleIds: props.activeModule === 'all' ? [] : [props.activeModule],
            });
          } else {
            await deleteScenario(record?.id as string);
          }
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadScenarioList(true);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }
  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  const batchForm = ref({
    attr: '',
    value: '',
    values: [],
  });

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'delete':
        deleteOperation(undefined, true, batchParams.value);
        break;
      case 'recover':
        recover(undefined, true);
        break;
      default:
        break;
    }
  }

  defineExpose({
    loadScenarioList,
  });

  onBeforeMount(() => {
    loadScenarioList();
  });
  watch(
    () => props.activeModule,
    () => {
      resetSelector();
      loadScenarioList();
    }
  );
  watch(
    () => batchForm.value.attr,
    () => {
      batchForm.value.value = '';
    }
  );
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
