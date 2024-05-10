<template>
  <div class="p-[16px]">
    <div class="flex items-center justify-between">
      <div
        >{{ t('testPlan.bugManagement.bug') }}
        <span class="!text-[var(--color-text-n4)]">({{ addCommasToNumber(count) }})</span>
      </div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByName')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="getFetch"
        @press-enter="getFetch"
        @clear="getFetch"
      />
    </div>
    <MsBaseTable ref="tableRef" v-bind="propsRes" v-on="propsEvent">
      <template #num="{ record }">
        <a-tooltip :content="`${record.num}`">
          <a-button type="text" class="px-0 !text-[14px] !leading-[22px]" size="mini">
            <div class="one-line-text max-w-[168px]">{{ record.num }}</div>
          </a-button>
        </a-tooltip>
      </template>
      <template #name="{ record }">
        <span class="one-line-text max-w-[300px]"> {{ record.name }}</span>
        <a-popover title="" position="right" style="width: 480px">
          <span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
          <template #content>
            <div v-dompurify-html="record.content" class="markdown-body" style="margin-left: 48px"> </div>
          </template>
        </a-popover>
      </template>
      <template #linkCase="{ record }">
        <CaseCountPopover :record="record" />
      </template>
    </MsBaseTable>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import CaseCountPopover from './caseCountPopover.vue';

  import { planDetailBugPage } from '@/api/modules/test-plan/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { addCommasToNumber } from '@/utils';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const appStore = useAppStore();

  const props = defineProps<{
    planId: string | undefined;
  }>();

  const keyword = ref<string>('');

  function getFetch() {}

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 100,
    },
    {
      title: 'testPlan.bugManagement.bugName',
      slotName: 'title',
      dataIndex: 'title',
      showInTable: true,
      showTooltip: false,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'testPlan.bugManagement.defectState',
      slotName: 'statusName',
      dataIndex: 'statusName',
      showInTable: true,
      showTooltip: true,
      width: 200,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.linkCase',
      slotName: 'linkCase',
      dataIndex: 'linkCase',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'handleUser',
      dataIndex: 'handleUser',
      titleSlotName: 'handleUserFilter',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
    },
    {
      title: 'common.createTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      showDrag: true,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(planDetailBugPage, {
    columns,
    tableKey: TableKeyEnum.TEST_PLAN_DETAIL_BUG_TABLE,
    scroll: { x: '100%' },
    showSelectorAll: false,
    heightUsed: 340,
    enableDrag: false,
  });

  const count = computed(() => {
    return propsRes.value.msPagination?.total || 0;
  });

  function initData() {
    setLoadListParams({
      planId: props.planId,
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
    });
    loadList();
  }

  onBeforeMount(() => {
    initData();
  });
</script>

<style scoped></style>
