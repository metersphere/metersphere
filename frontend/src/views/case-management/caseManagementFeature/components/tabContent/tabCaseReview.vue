<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div class="font-medium">{{ t('caseManagement.featureCase.caseReviewList') }}</div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
      ></a-input-search>
    </div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #reviewName="{ record }">
        <a-button type="text" class="px-0">{{ record.reviewName }}</a-button>
      </template>
      <template #reviewStatus="{ record }">
        <statusTag :status="record.reviewStatus || 'PREPARED'" />
      </template>
      <template #status="{ record }">
        <MsIcon
          :type="statusIconMap[record.status]?.icon || ''"
          class="mr-1"
          :class="[statusIconMap[record.status].color] || ''"
        ></MsIcon>
        <span>{{ statusIconMap[record.status]?.statusText || '' }} </span>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import statusTag from '@/views/case-management/caseReview/components/statusTag.vue';

  import { getDetailCaseReviewPage } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  import { TableKeyEnum } from '@/enums/tableEnum';

  import { statusIconMap } from '../utils';
  import debounce from 'lodash-es/debounce';

  const featureCaseStore = useFeatureCaseStore();
  const { t } = useI18n();

  const props = defineProps<{
    caseId: string; // 用例id
    activeTab: string;
  }>();

  const keyword = ref<string>('');

  const columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'reviewNum',
      sortIndex: 1,
      showTooltip: true,
      width: 300,
    },
    {
      title: 'caseManagement.caseReview.name',
      slotName: 'reviewName',
      dataIndex: 'reviewName',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showTooltip: true,
      width: 300,
    },
    {
      title: 'caseManagement.caseReview.status',
      dataIndex: 'reviewStatus',
      slotName: 'reviewStatus',
      width: 150,
    },
    {
      title: 'caseManagement.featureCase.reviewResult',
      slotName: 'status',
      dataIndex: 'status',
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.reviewTime',
      slotName: 'updateTime',
      dataIndex: 'updateTime',
      width: 200,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getDetailCaseReviewPage, {
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_REVIEW,
    scroll: { x: '100%' },
    heightUsed: 360,
    enableDrag: true,
  });

  async function initData() {
    setLoadListParams({ keyword: keyword.value, caseId: props.caseId });
    await loadList();
    featureCaseStore.getCaseCounts(props.caseId);
  }

  const searchList = debounce(() => {
    initData();
  }, 100);

  onMounted(() => {
    initData();
  });
</script>

<style scoped></style>
