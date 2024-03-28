<template>
  <div class="px-[16px]">
    <div class="mb-4 flex items-center justify-between">
      <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
        <a-radio value="All">{{ t('report.all') }}</a-radio>
        <a-radio value="INDEPENDENT">{{ t('report.independent') }}</a-radio>
        <a-radio value="INTEGRATED">{{ t('report.collection') }}</a-radio>
      </a-radio-group>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('project.menu.nameSearch')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
        @clear="searchList"
      />
    </div>
    <!-- 报告列表 -->
    <ms-base-table
      v-bind="propsRes"
      ref="tableRef"
      :action-config="tableBatchActions"
      v-on="propsEvent"
      @batch-action="handleTableBatch"
    >
      <template #name="{ record, rowIndex }">
        <div
          type="text"
          class="one-text-line flex w-full text-[rgb(var(--primary-5))]"
          @click="showReportDetail(record.id, rowIndex, record.integrated)"
          >{{ characterLimit(record.name) }}</div
        >
      </template>
      <!-- 报告类型 -->
      <template #integrated="{ record }">
        <MsTag theme="light" :type="record.integrated ? 'primary' : undefined">
          {{ record.integrated ? t('report.collection') : t('report.independent') }}
        </MsTag>
      </template>
      <!-- 报告触发方式筛选 -->
      <template #triggerModeFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="triggerModeFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <a-button type="text" class="arco-btn-text--secondary p-[8px_4px]" @click="triggerModeFilterVisible = true">
            <div class="font-medium">
              {{ t(columnConfig.title as string) }}
            </div>
            <icon-down :class="triggerModeFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="triggerModeListFilters" direction="vertical" size="small">
                  <a-checkbox v-for="(key, value) of TriggerModeLabel" :key="key" :value="value">
                    <div class="font-medium">{{ t(key) }}</div>
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <!-- 报告结果筛选 -->
      <template #statusFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="statusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <a-button type="text" class="arco-btn-text--secondary p-[8px_4px]" @click="statusFilterVisible = true">
            <div class="font-medium">
              {{ t(columnConfig.title as string) }}
            </div>
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusListFilters" direction="vertical" size="small">
                  <a-checkbox v-for="key of statusFilters" :key="key" :value="key">
                    <ExecutionStatus :module-type="props.moduleType" :status="key" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>

      <template #status="{ record }">
        <ExecutionStatus
          :module-type="props.moduleType"
          :status="record.status"
          :script-identifier="record.scriptIdentifier"
        />
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode]) }}</span>
      </template>
      <template #operationTime="{ record }">
        <span>{{ dayjs(record.operationTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #operation="{ record }">
        <MsButton
          v-permission="['PROJECT_API_REPORT:READ+DELETE']"
          class="!mr-0"
          @click="handleDelete(record.id, record.name)"
          >{{ t('ms.comment.delete') }}</MsButton
        >
      </template>
    </ms-base-table>
    <ReportDetailDrawer
      v-model:visible="showDetailDrawer"
      :report-id="activeDetailId"
      :active-report-index="activeReportIndex"
      :table-data="propsRes.data"
      :page-change="propsEvent.pageChange"
      :pagination="propsRes.msPagination!"
      :show-type="showType"
    />
    <CaseReportDrawer
      v-model:visible="showCaseDetailDrawer"
      :report-id="activeDetailId"
      :active-report-index="activeReportIndex"
      :table-data="propsRes.data"
      :page-change="propsEvent.pageChange"
      :pagination="propsRes.msPagination!"
      :show-type="activeCaseReportType"
    />
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
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import CaseReportDrawer from './caseReportDrawer.vue';
  import ReportDetailDrawer from './reportDetailDrawer.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';

  import { reportBathDelete, reportDelete, reportList, reportRename } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useTableStore } from '@/store';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import { BatchApiParams } from '@/models/common';
  import { ReportEnum, ReportStatus, TriggerModeLabel } from '@/enums/reportEnum';
  import { ColumnEditTypeEnum, TableKeyEnum } from '@/enums/tableEnum';

  const { openModal } = useModal();

  const appStore = useAppStore();
  const tableStore = useTableStore();

  const { t } = useI18n();
  const props = defineProps<{
    moduleType: keyof typeof ReportEnum;
    name: string;
  }>();
  const keyword = ref<string>('');
  const statusFilterVisible = ref(false);
  const triggerModeFilterVisible = ref(false);

  const statusListFilters = ref<string[]>(Object.keys(ReportStatus[props.moduleType]));
  const triggerModeListFilters = ref<string[]>(Object.keys(TriggerModeLabel));

  type ReportShowType = 'All' | 'INDEPENDENT' | 'INTEGRATED';
  const showType = ref<ReportShowType>('All');

  const columns: MsTableColumn = [
    {
      title: 'report.name',
      dataIndex: 'name',
      slotName: 'name',
      width: 300,
      showInTable: true,
      showTooltip: true,
      editType: hasAnyPermission(['PROJECT_API_REPORT:READ+UPDATE']) ? ColumnEditTypeEnum.INPUT : undefined,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      ellipsis: true,
      showDrag: false,
      columnSelectorDisabled: true,
    },
    {
      title: 'report.type',
      slotName: 'integrated',
      dataIndex: 'integrated',
      width: 150,
      showDrag: true,
    },
    {
      title: 'report.result',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'report.trigger.mode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      showInTable: true,
      width: 150,
      showDrag: true,
      titleSlotName: 'triggerModeFilter',
    },
    {
      title: 'report.operator',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'report.operating',
      dataIndex: 'startTime',
      slotName: 'startTime',
      width: 180,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
    },
    {
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      title: hasAnyPermission(['PROJECT_API_REPORT:READ+DELETE']) ? 'common.operation' : '',
      width: hasAnyPermission(['PROJECT_API_REPORT:READ+DELETE']) ? 130 : 50,
    },
  ];

  await tableStore.initColumn(TableKeyEnum.API_TEST_REPORT, columns, 'drawer');

  const rename = async (record: any) => {
    try {
      await reportRename(props.moduleType, record.id, record.name);
      Message.success(t('common.updateSuccess'));
      return true;
    } catch (error) {
      return false;
    }
  };
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    reportList,
    {
      tableKey: TableKeyEnum.API_TEST_REPORT,
      scroll: {
        x: '100%',
      },
      showSetting: true,
      selectable: true,
      heightUsed: 330,
      showSelectorAll: true,
    },
    (item) => ({
      ...item,
      startTime: dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss'),
    }),
    rename
  );

  function initData() {
    setLoadListParams({
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleType: props.moduleType,
      filter: {
        status: statusListFilters.value,
        integrated: showType.value === 'All' ? undefined : Array.of((showType.value === 'INTEGRATED').toString()),
        triggerMode: triggerModeListFilters.value,
      },
    });
    loadList();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'report.batch.delete',
        eventTag: 'batchStop',
        permission: ['PROJECT_API_REPORT:READ+DELETE'],
      },
    ],
  };

  const batchParams = ref<BatchApiParams>({
    selectIds: [],
    selectAll: false,
    excludeIds: [] as string[],
    condition: {},
  });

  // 批量删除
  const handleTableBatch = async (event: BatchActionParams, params: BatchActionQueryParams) => {
    batchParams.value = {
      ...params,
      selectIds: params?.selectedIds || [],
      condition: {},
      projectId: appStore.currentProjectId,
    };

    openModal({
      type: 'error',
      title: t('report.delete.tip', {
        count: params?.currentSelectCount || params?.selectedIds?.length,
      }),
      content: '',
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await reportBathDelete(props.moduleType, batchParams.value);
          Message.success(t('apiTestDebug.deleteSuccess'));
          resetSelector();
          initData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  function searchList() {
    resetSelector();
    initData();
  }
  const handleDelete = async (id: string, currentName: string) => {
    openModal({
      type: 'error',
      title: t('apiTestManagement.deleteApiTipTitle', { name: characterLimit(currentName) }),
      content: '',
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          await reportDelete(props.moduleType, id);
          Message.success(t('apiTestDebug.deleteSuccess'));
          initData();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  onBeforeMount(() => {
    initData();
  });

  const statusFilters = computed(() => {
    return Object.keys(ReportStatus[props.moduleType]);
  });

  function handleFilterHidden(val: boolean) {
    if (!val) {
      initData();
    }
  }

  function changeShowType(val: string | number | boolean) {
    showType.value = val as ReportShowType;
    initData();
  }

  /**
   * 报告详情 showReportDetail
   */
  const activeDetailId = ref<string>('');
  const activeReportIndex = ref<number>(0);
  const showDetailDrawer = ref<boolean>(false);
  const showCaseDetailDrawer = ref<boolean>(false);
  const activeCaseReportType = ref('');

  function showReportDetail(id: string, rowIndex: number, integrated: boolean) {
    activeDetailId.value = id;
    activeReportIndex.value = rowIndex - 1;
    if (props.moduleType === ReportEnum.API_SCENARIO_REPORT) {
      showDetailDrawer.value = true;
    } else {
      showCaseDetailDrawer.value = true;
      activeCaseReportType.value = integrated ? 'INTEGRATED' : 'INDEPENDENT';
    }
  }

  watch(
    () => props.moduleType,
    (val) => {
      if (val) {
        resetSelector();
        initData();
      }
    }
  );
</script>

<style scoped></style>
