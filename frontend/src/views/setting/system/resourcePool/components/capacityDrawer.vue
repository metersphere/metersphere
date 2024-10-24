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
          <span class="capacity-count">{{ capacityDetail.concurrentNumber ?? 0 }}</span>
        </div>
        <div class="count-resources">
          {{ t('system.resourcePool.remainingConcurrency') }}
          <span class="capacity-count">{{
            capacityDetail.concurrentNumber - capacityDetail.occupiedConcurrentNumber
          }}</span>
        </div>
      </div>
    </template>
    <div class="flex items-center justify-between p-[16px]">
      <div class="flex items-center gap-[8px]">
        <a-select
          v-model="selectedNode"
          class="mr-[16px] w-[200px]"
          :placeholder="t('common.pleaseSelect')"
          allow-clear
          @change="changeNode"
        >
          <template #prefix>
            <div class="text-[var(--color-text-brand)]">{{ t('system.resourcePool.capacityNode') }}</div>
          </template>
          <a-tooltip v-for="item of nodeList" :key="item.ip" :mouse-enter-delay="500" :content="item.ip">
            <a-option :value="item.ip">
              {{ item.ip }}
            </a-option>
          </a-tooltip>
        </a-select>
        <CapacityProgress
          v-show="selectedNode"
          :name="t('system.resourcePool.memory')"
          :percent="capacityDetail.memoryUsage ?? 0"
          color="rgb(var(--link-6))"
        />
        <CapacityProgress
          v-show="selectedNode"
          name="CPU"
          :percent="capacityDetail.cpuusage ?? 0"
          color="rgb(var(--success-6))"
        />
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
        <template #result="{ record }">
          <ExecutionStatus v-if="record.result !== '-'" :status="record.result" :module-type="ReportEnum.API_REPORT" />
        </template>

        <template #[FilterSlotNameEnum.API_TEST_CASE_API_REPORT_STATUS]="{ filterContent }">
          <ExecutionStatus :module-type="ReportEnum.API_REPORT" :status="filterContent.value" />
        </template>
        <template #status="{ record }">
          <ExecStatus :status="record.status" />
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

  import { getCapacityDetail, getCapacityTaskList } from '@/api/modules/setting/resourcePool';
  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';

  import type { CapacityDetailType, NodesListItem, ResourcePoolItem } from '@/models/setting/resourcePool';
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
      dataIndex: 'num',
      showTooltip: true,
      width: 120,
      columnSelectorDisabled: true,
    },
    {
      title: 'system.resourcePool.taskName',
      slotName: 'taskName',
      dataIndex: 'taskName',
      showDrag: true,
      width: 150,
      showTooltip: true,
    },
    {
      title: 'system.resourcePool.useCaseName',
      slotName: 'resourceName',
      dataIndex: 'resourceName',
      showTooltip: true,
      width: 150,
      showDrag: true,
    },
    {
      title: 'system.resourcePool.tableColumnExecuteStatus',
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
      slotName: 'result',
      dataIndex: 'result',
      width: 200,
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

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getCapacityTaskList, {
    tableKey: TableKeyEnum.SYSTEM_RESOURCE_POOL_CAPACITY,
    scroll: { x: '100%' },
    selectable: false,
    showSetting: true,
    heightUsed: 310,
    showSelectAll: false,
  });

  const selectedNode = ref<string>('');
  const nodeList = computed<NodesListItem[]>(() => props.activeRecord?.testResourceDTO.nodesList ?? []);
  const nodePort = computed(() => nodeList.value.find((e) => e.ip === selectedNode.value)?.port ?? '');

  async function searchList() {
    setLoadListParams({
      poolId: props.activeRecord?.id,
      ip: selectedNode.value,
      port: nodePort.value,
    });
    loadList();
  }

  const defaultCapacityDetail: CapacityDetailType = {
    concurrentNumber: 0,
    occupiedConcurrentNumber: 0,
    memoryUsage: 0,
    cpuusage: 0,
  };

  const capacityDetail = ref<CapacityDetailType>({
    ...defaultCapacityDetail,
  });

  function resetCapacityDetail() {
    capacityDetail.value = {
      ...defaultCapacityDetail,
    };
  }

  async function initCapacityDetail() {
    try {
      capacityDetail.value = await getCapacityDetail({
        poolId: props.activeRecord?.id,
        ip: selectedNode.value,
        port: nodePort.value,
        current: 1,
        pageSize: 10,
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  function changeNode() {
    searchList();
    initCapacityDetail();
  }

  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        changeNode();
      } else {
        selectedNode.value = '';
        resetCapacityDetail();
      }
    }
  );

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
