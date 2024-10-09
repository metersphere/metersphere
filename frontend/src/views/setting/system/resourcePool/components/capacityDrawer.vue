<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="props.activeRecord?.name"
    :width="800"
    :footer="false"
    no-content-padding
    unmount-on-close
  >
    <template #tbutton>
      <div class="flex items-center gap-[16px]">
        <div class="count-resources">
          {{ t('system.resourcePool.concurrentNumber') }}
          <span class="capacity-count">100</span>
        </div>
        <div class="count-resources">
          {{ t('system.resourcePool.remainingConcurrency') }}
          <span class="capacity-count">100</span>
        </div>
      </div>
    </template>
    <!-- TODO 等待联调 -->
    <div class="flex items-center justify-between p-[16px]">
      <div class="flex items-center gap-[8px]">
        <a-select
          v-model="selectedNode"
          class="mr-[16px] w-[200px]"
          :placeholder="t('common.pleaseSelect')"
          allow-clear
        >
          <template #prefix>
            <div class="text-[var(--color-text-brand)]">{{ t('system.resourcePool.capacityNode') }}</div>
          </template>
          <template #tree-slot-title="node">
            <a-tooltip :content="`${node.name}`" position="tl">
              <div class="one-line-text w-[180px]">{{ node.name }}</div>
            </a-tooltip>
          </template>
        </a-select>
        <CapacityProgress :name="t('system.resourcePool.memory')" :percent="0.3" color="rgb(var(--link-6))" />
        <CapacityProgress name="CPU" :percent="0.3" color="rgb(var(--success-6))" />
      </div>
      <MsTag no-margin size="large" :tooltip-disabled="true" class="cursor-pointer" theme="outline" @click="searchList">
        <MsIcon class="text-[16px] text-[var(color-text-4)]" :size="32" type="icon-icon_reset_outlined" />
      </MsTag>
    </div>
    <div class="p-[16px]">
      <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
        <template #[FilterSlotNameEnum.TEST_PLAN_REPORT_EXEC_STATUS]="{ filterContent }">
          <ExecStatus :status="filterContent.value" />
        </template>
        <template #execStatus="{ record }">
          <ExecStatus :status="record.execStatus" />
        </template>

        <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS]="{ filterContent }">
          <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
        </template>
        <template #status="{ record }">
          <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="record.status" />
        </template>
      </ms-base-table>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import CapacityProgress from './capacityProgress.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import ExecStatus from '@/views/test-plan/report/component/execStatus.vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';

  import type { ResourcePoolItem } from '@/models/setting/resourcePool';
  import { ReportExecStatus } from '@/enums/apiEnum';
  import { ReportEnum, ReportStatus } from '@/enums/reportEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const { t } = useI18n();
  const tableStore = useTableStore();

  const props = defineProps<{
    activeRecord?: ResourcePoolItem;
  }>();

  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const ExecStatusList = computed(() => {
    return Object.values(ReportExecStatus).map((e) => {
      return {
        value: e,
        key: e,
      };
    });
  });

  const statusList = computed(() => {
    return Object.keys(ReportStatus).map((key) => {
      return {
        value: key,
        label: t(ReportStatus[key].label),
      };
    });
  });

  const columns: MsTableColumn = [
    {
      title: 'system.resourcePool.taskID',
      slotName: 'ID',
      dataIndex: 'id',
      showTooltip: true,
      width: 200,
      columnSelectorDisabled: true,
    },
    {
      title: 'system.resourcePool.taskName',
      slotName: 'taskName',
      dataIndex: 'enable',
      showDrag: true,
    },
    {
      title: 'system.resourcePool.useCaseName',
      slotName: 'useCaseName',
      dataIndex: 'useCaseName',
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'system.resourcePool.tableColumnStatus',
      dataIndex: 'status',
      slotName: 'status',
      filterConfig: {
        options: ExecStatusList.value,
        filterSlotName: FilterSlotNameEnum.TEST_PLAN_REPORT_EXEC_STATUS,
      },
      showInTable: true,
      width: 200,
      showDrag: true,
    },
    {
      title: 'common.executionResult',
      slotName: 'execStatus',
      dataIndex: 'execStatus',

      filterConfig: {
        options: statusList.value,
        filterSlotName: FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS,
      },
      showDrag: true,
    },
    {
      title: '',
      slotName: 'action',
      dataIndex: 'operation',
      fixed: 'right',
      width: 50,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setKeyword } = useTable(undefined, {
    tableKey: TableKeyEnum.SYSTEM_RESOURCE_POOL_CAPACITY,
    scroll: { y: 'auto' },
    selectable: false,
    showSetting: true,
    heightUsed: 310,
    showSelectAll: false,
  });

  const selectedNode = ref<string>('');

  async function searchList() {
    loadList();
  }

  await tableStore.initColumn(TableKeyEnum.SYSTEM_RESOURCE_POOL_CAPACITY, columns, 'drawer');
</script>

<style scoped lang="less">
  .count-resources {
    font-size: 16px;
    color: var(--color-text-4);
    @apply font-normal;
    .capacity-count {
      color: var(--color-text-1);
      @apply font-medium;
    }
  }
  .progress-capacity {
    padding: 4px 8px;
    min-width: 116px;
    height: 32px;
    border-radius: 4px;
    background-color: var(--color-text-n9) !important;
    @apply flex flex-col;
    .capacity-attr {
      font-size: 12px;
      color: var(--color-text-4);
      @apply flex items-center justify-between;
      .capacity-value {
        color: var(--color-text-1);
      }
    }
  }
</style>
