<template>
  <div>
    <MsNotRemind tip="apiScenario.historyListTip" class="mb-[16px]" type="warning" visited-key="scenarioHistoryTip" />
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent"></ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsNotRemind from '@/components/business/ms-not-remind/index.vue';

  import { getScenarioHistory } from '@/api/modules/api-test/scenario';
  import { operationTypeOptions } from '@/config/common';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const appStore = useAppStore();
  const { t } = useI18n();
  const props = defineProps<{
    sourceId?: string | number;
  }>();
  const columns: MsTableColumn = [
    {
      title: 'apiScenario.changeOrder',
      dataIndex: 'id',
      width: 150,
    },
    {
      title: 'apiScenario.type',
      dataIndex: 'type',
      slotName: 'type',
      titleSlotName: 'typeFilter',
      width: 150,
    },
    {
      title: 'mockManagement.operationUser',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'apiTestManagement.updateTime',
      dataIndex: 'createTime',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 180,
    },
    // {
    //   title: 'common.operation',
    //   slotName: 'action',
    //   dataIndex: 'operation',
    //   width: 50,
    // },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getScenarioHistory,
    {
      columns,
      scroll: { x: '100%' },
      selectable: false,
      heightUsed: 454,
    },
    (item) => ({
      ...item,
      type: t(operationTypeOptions.find((e) => e.value === item.type)?.label || ''),
      createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  function loadHistory() {
    setLoadListParams({
      projectId: appStore.currentProjectId,
      sourceId: props.sourceId,
    });
    loadList();
  }

  onMounted(() => {
    loadHistory();
  });
</script>

<style lang="less" scoped></style>
