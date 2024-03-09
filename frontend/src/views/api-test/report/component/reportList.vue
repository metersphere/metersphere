<template>
  <div class="px-[16px]">
    <div class="mb-4 flex items-center justify-between">
      <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
        <a-radio value="All">{{ t('report.all') }}</a-radio>
        <a-radio value="Independent">{{ t('report.independent') }}</a-radio>
        <a-radio value="Collection">{{ t('report.collection') }}</a-radio>
      </a-radio-group>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('project.menu.nameSearch')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
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
      <!-- 报告类型筛选 -->
      <template #typeFilter="{ columnConfig }">
        <a-trigger v-model:popup-visible="typeFilterVisible" trigger="click" @popup-visible-change="handleFilterHidden">
          <a-button type="text" class="arco-btn-text--secondary p-[8px_4px]" @click="typeFilterVisible = true">
            <div class="font-medium">
              {{ t(columnConfig.title as string) }}
            </div>
            <icon-down :class="typeFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
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
        <ExecutionStatus :module-type="props.moduleType" :status="record.status" />
      </template>
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode]) }}</span>
      </template>
      <template #operationTime="{ record }">
        <span>{{ dayjs(record.operationTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #operation="{ record }">
        <MsButton class="!mr-0" @click="handleDelete(record)">{{ t('ms.comment.delete') }}</MsButton>
      </template>
    </ms-base-table>
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
  import ExecutionStatus from './reportStatus.vue';

  import { reportDelete, reportList, reportRename } from '@/api/modules/api-test/report';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import { BatchApiParams } from '@/models/common';
  import { ReportEnum, ReportStatus, ReportType, TriggerModeLabel } from '@/enums/reportEnum';
  import { ColumnEditTypeEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const props = defineProps<{
    moduleType: keyof typeof ReportEnum;
    name: string;
  }>();
  const keyword = ref<string>('');
  const statusFilterVisible = ref(false);
  const typeFilterVisible = ref(false);

  const statusListFilters = ref<string[]>(Object.keys(ReportStatus[props.moduleType]));

  type ReportShowType = 'All' | 'Independent' | 'Collection';
  const showType = ref<ReportShowType>('All');

  const columns: MsTableColumn = [
    {
      title: 'report.name',
      dataIndex: 'name',
      slotName: 'name',
      width: 200,
      showInTable: true,
      showTooltip: true,
      editType: ColumnEditTypeEnum.INPUT, // hasAnyPermission(['API_TEST_REPORT:READ+UPDATE']) ? ColumnEditTypeEnum.INPUT : undefined,
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
      slotName: 'reportType',
      dataIndex: 'reportType',
      width: 150,
      showDrag: true,
      titleSlotName: 'typeFilter',
    },
    {
      title: 'report.result',
      dataIndex: 'reportResult',
      slotName: 'reportResult',
      titleSlotName: 'statusFilter',
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'report.trigger.mode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      showInTable: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'report.operator',
      slotName: 'createUser',
      dataIndex: 'createUser',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'report.operating',
      dataIndex: 'createTime',
      slotName: 'createTime',
      width: 180,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      width: 100,
      fixed: 'right',
    },
  ];
  const rename = async (record: any) => {
    try {
      await reportRename(record.id, record.name);
      Message.success(t('common.updateSuccess'));
      return true;
    } catch (error) {
      return false;
    }
  };
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    reportList,
    {
      columns,
      scroll: {
        x: '100%',
      },
      showSetting: false,
      selectable: true,
      heightUsed: 330,
      showSelectAll: true,
    },
    undefined,
    rename
  );
  function initData() {
    setLoadListParams({
      keyword: keyword.value,
      moduleType: props.moduleType,
      filter: { status: statusListFilters.value },
    });
    loadList();
  }

  const tableBatchActions = {
    baseAction: [
      {
        label: 'report.batch.delete',
        eventTag: 'batchStop',
      },
    ],
  };
  const batchParams = ref<BatchApiParams>({
    selectIds: [],
    selectAll: false,
    excludeIds: [] as string[],
    condition: {},
  });

  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    batchParams.value = { ...params, selectIds: params?.selectedIds || [], condition: {} };
    if (event.eventTag === 'batchDelete') {
      // 批量删除
    }
  }

  function searchList() {
    resetSelector();
    initData();
  }

  function handleDelete(id: string) {
    Message.success(t('apiTestDebug.deleteSuccess'));
  }

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
    if (val === ReportType.COLLECTION) {
      console.log('Collection');
    } else if (val === ReportType.INDEPENDENT) {
      console.log('Independent');
    } else {
      console.log('All');
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
