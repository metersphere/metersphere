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
          class="one-line-text flex w-full text-[rgb(var(--primary-5))]"
          @click="showReportDetail(record.id, rowIndex)"
          >{{ characterLimit(record.name) }}</div
        >
      </template>
      <!-- 报告类型 -->
      <template #integrated="{ record }">
        <MsTag theme="light" :type="record.integrated ? 'primary' : undefined">
          {{ record.integrated ? t('report.collection') : t('report.independent') }}
        </MsTag>
      </template>
      <template #integratedFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="reportTypeVisible"
          v-model:status-filters="integratedFiltersMap[showType]"
          :title="(columnConfig.title as string)"
          :list="reportTypeList"
          @search="initData()"
        >
          <template #item="{ item }">
            <MsTag theme="light" :type="item.value === 'INTEGRATED' ? 'primary' : undefined">
              {{ item.value === 'INTEGRATED' ? t('report.collection') : t('report.independent') }}
            </MsTag>
          </template>
        </TableFilter>
      </template>
      <!-- 报告触发方式筛选 -->
      <template #triggerModeFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="triggerModeFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <a-button
            type="text"
            class="arco-btn-text--secondary p-[8px_4px]"
            @click.stop="triggerModeFilterVisible = true"
          >
            <div class="font-medium">
              {{ t(columnConfig.title as string) }}
            </div>
            <icon-down :class="triggerModeFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="ml-[6px] flex items-center justify-start px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="triggerModeListFilters" direction="vertical" size="small">
                  <a-checkbox v-for="(key, value) of TriggerModeLabel" :key="key" :value="value">
                    <div class="font-medium">{{ t(key) }}</div>
                  </a-checkbox>
                </a-checkbox-group>
              </div>
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetTriggerModeFilter">
                  {{ t('common.reset') }}
                </a-button>
                <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                  {{ t('system.orgTemplate.confirm') }}
                </a-button>
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
          <a-button type="text" class="arco-btn-text--secondary p-[8px_4px]" @click.stop="statusFilterVisible = true">
            <div class="font-medium">
              {{ t(columnConfig.title as string) }}
            </div>
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group
                  v-model:model-value="statusListFiltersMap[showType]"
                  direction="vertical"
                  size="small"
                >
                  <a-checkbox v-for="key of statusFilters" :key="key" :value="key">
                    <ExecutionStatus :module-type="props.moduleType" :status="key" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetStatusFilter">
                  {{ t('common.reset') }}
                </a-button>
                <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                  {{ t('system.orgTemplate.confirm') }}
                </a-button>
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
      :share-time="shareTime"
    />
    <CaseReportDrawer
      v-model:visible="showCaseDetailDrawer"
      :report-id="activeDetailId"
      :active-report-index="activeReportIndex"
      :table-data="propsRes.data"
      :page-change="propsEvent.pageChange"
      :pagination="propsRes.msPagination!"
      :share-time="shareTime"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
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
  import TableFilter from '@/views/case-management/caseManagementFeature/components/tableFilter.vue';

  import {
    getShareTime,
    reportBathDelete,
    reportDelete,
    reportList,
    reportRename,
  } from '@/api/modules/api-test/report';
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
  const route = useRoute();

  const { t } = useI18n();
  const props = defineProps<{
    moduleType: keyof typeof ReportEnum;
    name: string;
  }>();
  const keyword = ref<string>('');
  const statusFilterVisible = ref(false);
  const triggerModeFilterVisible = ref(false);

  const triggerModeListFilters = ref<string[]>([]);

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
      titleSlotName: 'integratedFilter',
      width: 150,
      showDrag: true,
    },
    {
      title: 'report.result',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'report.trigger.mode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 150,
      showDrag: true,
      titleSlotName: 'triggerModeFilter',
    },
    {
      title: 'report.operator',
      slotName: 'createUserName',
      dataIndex: 'createUserName',
      showInTable: true,
      width: 300,
      showDrag: true,
      showTooltip: true,
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
      selectable: hasAnyPermission(['PROJECT_API_REPORT:READ+DELETE']),
      heightUsed: 330,
      showSelectorAll: true,
    },
    (item) => ({
      ...item,
      startTime: dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss'),
    }),
    rename
  );
  // 全部过滤条件
  const allListFilters = ref<string[]>([]);
  const independentListFilters = ref<string[]>([]);
  const integratedListFilters = ref<string[]>([]);

  const statusListFiltersMap = ref<Record<string, string[]>>({
    All: allListFilters.value,
    INDEPENDENT: independentListFilters.value,
    INTEGRATED: integratedListFilters.value,
  });
  // 全部过滤条件
  const allIntegratedFilters = ref<string[]>([]);
  const independentIntegratedFilters = ref<string[]>([]);
  const integratedIntegratedFilters = ref<string[]>([]);

  const reportTypeVisible = ref<boolean>(false);

  const integratedFiltersMap = ref<Record<string, string[]>>({
    All: allIntegratedFilters.value,
    INDEPENDENT: independentIntegratedFilters.value,
    INTEGRATED: integratedIntegratedFilters.value,
  });

  const reportTypeList = ref([
    {
      value: 'INDEPENDENT',
      label: t('report.independent'),
    },
    {
      value: 'INTEGRATED',
      label: t('report.collection'),
    },
  ]);

  const integratedFilters = computed(() => {
    if (showType.value === 'All') {
      if (integratedFiltersMap.value[showType.value].length === 1) {
        return integratedFiltersMap.value[showType.value].includes('INDEPENDENT') ? [false] : [true];
      }
      return undefined;
    }
    if (showType.value === 'INTEGRATED') {
      return [true];
    }
    return [false];
  });

  function initData() {
    setLoadListParams({
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleType: props.moduleType,
      filter: {
        status: statusListFiltersMap.value[showType.value],
        integrated: integratedFilters.value,
        triggerMode: triggerModeListFilters.value,
      },
    });
    loadList();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'common.delete',
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
      condition: {
        filter: {
          status: statusListFiltersMap.value[showType.value],
          integrated: integratedFilters.value,
          triggerMode: triggerModeListFilters.value,
        },
        keyword: keyword.value,
      },
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
    return Object.keys(ReportStatus[props.moduleType]) || [];
  });

  function handleFilterHidden(val: boolean) {
    if (!val) {
      triggerModeFilterVisible.value = false;
      statusFilterVisible.value = false;
      initData();
    }
  }

  function resetTriggerModeFilter() {
    triggerModeFilterVisible.value = false;
    triggerModeListFilters.value = [];
    initData();
  }

  function resetStatusFilter() {
    statusFilterVisible.value = false;
    statusListFiltersMap.value[showType.value] = [];
    initData();
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

  function showReportDetail(id: string, rowIndex: number) {
    if (props.moduleType === ReportEnum.API_SCENARIO_REPORT) {
      showDetailDrawer.value = true;
    } else {
      showCaseDetailDrawer.value = true;
    }
    activeDetailId.value = id;
    activeReportIndex.value = rowIndex - 1;
  }

  const shareTime = ref<string>('');
  async function getTime() {
    try {
      const res = await getShareTime(appStore.currentProjectId);
      const match = res.match(/^(\d+)([MYHD])$/);
      if (match) {
        const value = parseInt(match[1], 10);
        const type = match[2];
        const translations = {
          M: t('msTimeSelector.month'),
          Y: t('msTimeSelector.year'),
          H: t('msTimeSelector.hour'),
          D: t('msTimeSelector.day'),
        };
        shareTime.value = value + (translations[type] || translations.D);
      }
    } catch (error) {
      console.log(error);
    }
  }

  function showDetail() {
    if (route.query.reportId && route.query.type) {
      activeDetailId.value = route.query.reportId as string;
      activeReportIndex.value = 0;
      if (route.query.type === 'API_SCENARIO') {
        showDetailDrawer.value = true;
      } else {
        showCaseDetailDrawer.value = true;
      }
    }
  }

  onMounted(() => {
    showDetail();
    getTime();
  });

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
