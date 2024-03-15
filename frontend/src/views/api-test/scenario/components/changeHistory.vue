<template>
  <div>
    <a-alert v-if="isShowTip" :show-icon="false" class="mb-[16px]" type="warning" closable @close="addVisited">
      {{ t('apiScenario.historyListTip') }}
      <template #close-element>
        <span class="text-[14px]">{{ t('common.notRemind') }}</span>
      </template>
    </a-alert>
    <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent"></ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import dayjs from 'dayjs';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getScenarioHistory } from '@/api/modules/api-test/scenario';
  import { operationTypeOptions } from '@/config/common';
  import { useI18n } from '@/hooks/useI18n';
  import useVisit from '@/hooks/useVisit';
  import useAppStore from '@/store/modules/app';

  const appStore = useAppStore();
  const { t } = useI18n();
  const isShowTip = ref<boolean>(true);
  const visitedKey = 'scenarioHistoryTip';
  const { addVisited, getIsVisited } = useVisit(visitedKey);
  const props = defineProps<{
    sourceId: string | number;
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
      title: 'apiScenario.operationUser',
      dataIndex: 'createUserName',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'apiScenario.updateTime',
      dataIndex: 'updateTime',
      showTooltip: true,
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
      heightUsed: 374,
    },
    (item) => ({
      ...item,
      type: t(operationTypeOptions.find((e) => e.value === item.type)?.label || ''),
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
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
