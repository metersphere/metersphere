<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <div class="font-medium">{{ t('caseManagement.featureCase.caseReviewList') }}</div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('caseManagement.featureCase.searchByIdAndName')"
        allow-clear
        class="mx-[8px] w-[240px]"
        @search="searchList"
        @press-enter="searchList"
        @clear="searchList"
      ></a-input-search>
    </div>
    <ms-base-table v-bind="propsRes" v-on="propsEvent">
      <template #reviewNum="{ record }">
        <a-button type="text" class="px-0" @click="review(record)">{{ record.reviewNum }}</a-button>
      </template>
      <template #reviewStatus="{ record }">
        <MsStatusTag :status="record.reviewStatus || 'PREPARED'" />
      </template>
      <!-- TODO 后台需要加 -->
      <!-- <template #[FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_STATUS]="{ filterContent }">
        <a-tag
          :color="reviewStatusMap[filterContent.value as ReviewStatus].color"
          :class="[reviewStatusMap[filterContent.value as ReviewStatus].class, 'px-[4px]']"
          size="small"
        >
          {{ t(reviewStatusMap[filterContent.value as ReviewStatus].label) }}
        </a-tag>
      </template> -->
      <template #status="{ record }">
        <MsIcon
          :type="statusIconMap[record.status]?.icon || ''"
          class="mr-1"
          :class="statusIconMap[record.status]?.color"
        ></MsIcon>
        <span>{{ statusIconMap[record.status]?.statusText || '' }} </span>
      </template>
    </ms-base-table>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { debounce } from 'lodash-es';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsStatusTag from '@/components/business/ms-status-tag/index.vue';

  import { getDetailCaseReviewPage } from '@/api/modules/case-management/featureCase';
  import { reviewStatusMap } from '@/config/caseManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  import { ReviewCaseItem, ReviewStatus } from '@/models/caseManagement/caseReview';
  import { CaseManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { statusIconMap } from '../utils';

  const featureCaseStore = useFeatureCaseStore();
  const router = useRouter();
  const route = useRoute();

  const { t } = useI18n();

  const props = defineProps<{
    caseId: string; // 用例id
  }>();

  const keyword = ref<string>('');
  // TODO 后台需要加
  const reviewStatusOptions = computed(() => {
    return Object.keys(reviewStatusMap).map((key) => {
      return {
        value: key,
        label: reviewStatusMap[key as ReviewStatus].label,
      };
    });
  });

  const columns: MsTableColumn = [
    {
      title: 'ID',
      slotName: 'reviewNum',
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
      dataIndex: 'status',
      slotName: 'reviewStatus',
      // TODO 后台需要加
      // filterConfig: {
      //   options: reviewStatusOptions.value,
      //   filterSlotName: FilterSlotNameEnum.CASE_MANAGEMENT_REVIEW_STATUS,
      // },
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
  });

  async function initData() {
    setLoadListParams({ keyword: keyword.value, caseId: props.caseId });
    await loadList();
    featureCaseStore.getCaseCounts(props.caseId);
  }

  const searchList = debounce(() => {
    initData();
  }, 100);

  // watch(
  //   () => activeTab.value,
  //   (val) => {
  //     if (val === 'caseReview') {
  //       initData();
  //     }
  //   }
  // );

  // 去用例评审页面
  function review(record: ReviewCaseItem) {
    router.push({
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL,
      query: {
        ...route.query,
        caseId: record.caseId,
        id: record.reviewId,
      },
      state: {
        params: JSON.stringify(setLoadListParams()),
      },
    });
  }

  watch(
    () => props.caseId,
    (val) => {
      if (val) {
        initData();
      }
    }
  );

  onMounted(() => {
    initData();
  });
</script>

<style scoped></style>
