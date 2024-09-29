<template>
  <div class="history-container">
    <MsNotRemind
      tip="apiTestManagement.historyListTip"
      class="mb-[16px]"
      type="warning"
      visited-key="apiTestCaseChangeHistoryTip"
    />
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent" @filter-change="filterChange"> </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsNotRemind from '@/components/business/ms-not-remind/index.vue';

  import { getApiCaseChangeHistory } from '@/api/modules/api-test/management';
  import { operationTypeOptions } from '@/config/common';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterSlotNameEnum } from '@/enums/tableFilterEnum';

  const props = defineProps<{
    sourceId: string | number;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  const typeOptions = [
    {
      label: t('system.log.operateType.add'),
      value: 'ADD',
    },
    {
      label: t('system.log.operateType.update'),
      value: 'UPDATE',
    },
    {
      label: t('system.log.operateType.import'),
      value: 'IMPORT',
    },
  ];

  const columns: MsTableColumn = [
    {
      title: 'apiTestManagement.changeOrder',
      dataIndex: 'id',
      width: 100,
    },
    {
      title: 'apiTestManagement.type',
      dataIndex: 'type',
      slotName: 'type',
      filterConfig: {
        options: typeOptions,
        filterSlotName: FilterSlotNameEnum.GLOBAL_CHANGE_HISTORY_TYPE,
      },
      width: 100,
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
    getApiCaseChangeHistory,
    {
      columns,
      tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_CHANGE_HISTORY,
      scroll: { x: '100%' },
      selectable: false,
      heightUsed: 374,
    },
    (item) => ({
      ...item,
      type: t(operationTypeOptions.find((e) => e.value === item.type)?.label || ''),
      createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );

  function loadHistory(types?: string[]) {
    setLoadListParams({
      projectId: appStore.currentProjectId,
      sourceId: props.sourceId,
      modules: 'API_TEST_MANAGEMENT_CASE',
      types,
    });
    loadList();
  }

  function filterChange(dataIndex: string, value: string[] | (string | number | boolean)[] | undefined) {
    loadHistory(value as string[]);
  }

  watch(
    () => props.sourceId,
    () => {
      loadHistory();
    }
  );

  onBeforeMount(() => {
    if (hasAnyPermission(['PROJECT_API_DEFINITION_CASE:READ', 'PROJECT_API_DEFINITION_CASE:READ+UPDATE'])) {
      loadHistory();
    }
  });

  // async function recover(record: any) {
  //   try {
  //     await recovergetApiCaseChangeHistory({
  //       id: record.id,
  //       sourceId: props.sourceId,
  //     });
  //   } catch (error) {
  //     // eslint-disable-next-line no-console
  //     console.log(error);
  //   }
  // }
</script>

<style lang="less" scoped>
  .history-container {
    @apply h-full overflow-y-auto;

    .ms-scroll-bar();
  }
</style>
