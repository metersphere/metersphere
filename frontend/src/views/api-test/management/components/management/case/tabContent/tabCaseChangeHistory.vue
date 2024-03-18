<template>
  <div class="history-container">
    <a-alert v-if="!getIsVisited()" :show-icon="false" class="mb-[16px]" type="warning" closable @close="addVisited">
      {{ t('apiTestManagement.historyListTip') }}
      <template #close-element>
        <span class="text-[14px]">{{ t('common.notRemind') }}</span>
      </template>
    </a-alert>
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
      <template #typeFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="statusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <MsButton type="text" class="arco-btn-text--secondary" @click="statusFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="typeFilter" direction="vertical" size="small">
                  <a-checkbox v-for="val of typeOptions" :key="val.value" :value="val.value">
                    <span>{{ t(val.label) }}</span>
                  </a-checkbox>
                </a-checkbox-group>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getApiCaseChangeHistory } from '@/api/modules/api-test/management';
  import { operationTypeOptions } from '@/config/common';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import useAppStore from '@/store/modules/app';
  import { hasAnyPermission } from '@/utils/permission';

  import { ChangeHistoryStatusFilters } from '@/enums/apiEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const typeFilter = ref(Object.keys(ChangeHistoryStatusFilters));

  const statusFilterVisible = ref(false);

  const props = defineProps<{
    sourceId: string | number;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();
  const visitedKey = 'messageManagementRobotListTip';
  const { addVisited, getIsVisited } = useVisit(visitedKey);

  const typeOptions = [
    {
      label: 'system.log.operateType.add',
      value: 'ADD',
    },
    {
      label: 'system.log.operateType.update',
      value: 'UPDATE',
    },
    {
      label: 'system.log.operateType.import',
      value: 'IMPORT',
    },
    {
      label: 'system.log.operateType.delete',
      value: 'DELETE',
    },
  ];

  const columns: MsTableColumn = [
    {
      title: 'apiTestManagement.changeOrder',
      dataIndex: 'id',
      width: 200,
    },
    {
      title: 'apiTestManagement.type',
      dataIndex: 'type',
      slotName: 'type',
      titleSlotName: 'typeFilter',
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

  function loadHistory() {
    setLoadListParams({
      projectId: appStore.currentProjectId,
      sourceId: props.sourceId,
      modules: ['API_TEST_MANAGEMENT_CASE'],
      types: typeFilter.value.length === Object.keys(ChangeHistoryStatusFilters).length ? undefined : typeFilter.value,
    });
    loadList();
  }

  function handleFilterHidden(val: boolean) {
    if (!val) {
      loadHistory();
    }
  }

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
