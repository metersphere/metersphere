<template>
  <div>
    <div class="mb-[8px] flex items-center justify-end">
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('apiScenario.executeHistory.searchPlaceholder')"
        allow-clear
        class="mr-[8px] w-[240px]"
        @search="loadExecuteHistoryList"
        @press-enter="loadExecuteHistoryList"
      />
    </div>
    <ms-base-table
      v-bind="propsRes"
      :first-column-width="44"
      :secnario-id="props.scenarioId"
      no-disable
      filter-icon-align-left
      v-on="propsEvent"
    >
      <template #num="{ record }">
        <span type="text" class="px-0">{{ record.num }}</span>
      </template>
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
              <div class="ml-[6px] flex items-center justify-start px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                  <a-checkbox v-for="val of Object.values(ExecuteStatusFilters)" :key="val" :value="val">
                    <executeStatus :status="val" />
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
      <template #triggerMode="{ record }">
        <span>{{ t(TriggerModeLabel[record.triggerMode]) }}</span>
      </template>
      <template #status="{ record }">
        <executeStatus :status="record.status" />
      </template>
      <template #operation="{ record }">
        <MsButton class="!mr-0" @click="showResult(record)"
          >{{ t('apiScenario.executeHistory.execution.operation') }}
        </MsButton>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import ExecuteStatus from '@/views/api-test/scenario/components/executeStatus.vue';

  import { getExecuteHistory } from '@/api/modules/api-test/scenario';
  import { useI18n } from '@/hooks/useI18n';

  import { ExecuteHistoryItem } from '@/models/apiTest/scenario';
  import { ExecuteStatusFilters } from '@/enums/apiEnum';
  import { TriggerModeLabel } from '@/enums/reportEnum';

  const triggerModeListFilters = ref<string[]>([]);
  const triggerModeFilterVisible = ref(false);
  const statusFilterVisible = ref(false);
  const statusFilters = ref<string[]>([]);
  const tableQueryParams = ref<any>();

  const keyword = ref('');
  const { t } = useI18n();
  const props = defineProps<{
    scenarioId?: string | number; // 详情 id
    readOnly?: boolean;
  }>();
  const columns: MsTableColumn = [
    {
      title: 'apiScenario.executeHistory.num',
      dataIndex: 'id',
      slotName: 'num',
      fixed: 'left',
      width: 150,
    },
    {
      title: 'apiScenario.executeHistory.execution.triggerMode',
      dataIndex: 'triggerMode',
      slotName: 'triggerMode',
      showTooltip: true,
      titleSlotName: 'triggerModeFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 100,
    },
    {
      title: 'apiScenario.executeHistory.execution.status',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 100,
    },
    {
      title: 'apiScenario.executeHistory.execution.operator',
      dataIndex: 'operationUser',
      showTooltip: true,
      width: 100,
    },
    {
      title: 'apiScenario.executeHistory.execution.operatorTime',
      dataIndex: 'startTime',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      showInTable: true,
      showDrag: false,
      width: 100,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getExecuteHistory,
    {
      columns,
      scroll: { x: '100%' },
      showSetting: false,
      selectable: false,
      heightUsed: 398,
    },
    (item) => ({
      ...item,
      startTime: dayjs(item.startTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  // 加载评审历史列表
  function loadExecuteHistoryList() {
    const params = {
      keyword: keyword.value,
      id: props.scenarioId,
      filter: {
        triggerMode: triggerModeListFilters.value,
        status: statusFilters.value,
      },
    };
    setLoadListParams(params);
    loadList();
    tableQueryParams.value = {
      ...params,
      current: propsRes.value.msPagination?.current,
      pageSize: propsRes.value.msPagination?.pageSize,
    };
  }

  function handleFilterHidden(val: boolean) {
    if (!val) {
      triggerModeFilterVisible.value = false;
      statusFilterVisible.value = false;
      loadExecuteHistoryList();
    }
  }

  function resetTriggerModeFilter() {
    triggerModeFilterVisible.value = false;
    triggerModeListFilters.value = [];
    loadExecuteHistoryList();
  }

  function resetStatusFilter() {
    statusFilterVisible.value = false;
    statusFilters.value = [];
    loadExecuteHistoryList();
  }

  function showResult(record: ExecuteHistoryItem) {}

  onBeforeMount(() => {
    loadExecuteHistoryList();
  });
</script>

<style lang="less" scoped></style>
