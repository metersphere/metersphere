<template>
  <ViewReport v-model:card-list="cardItemList" :detail-info="detail" :is-group="isGroup" is-preview />
</template>

<script setup lang="ts">
  // TODO 待联调 分享页面也要调整
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { cloneDeep } from 'lodash-es';

  import ViewReport from '@/views/test-plan/report/detail/component/viewReport.vue';

  import { getReportDetail, planGetShareHref } from '@/api/modules/test-plan/report';
  import { defaultReportDetail } from '@/config/testPlan';
  import { NOT_FOUND_RESOURCE } from '@/router/constants';

  import type { configItem, PlanReportDetail } from '@/models/testPlan/testPlanReport';

  import { defaultGroupConfig, defaultSingleConfig } from '@/views/test-plan/report/detail/component/reportConfig';

  const route = useRoute();
  const router = useRouter();
  const reportId = ref<string>(route.query.id as string);
  const isGroup = computed(() => route.query.type === 'GROUP');
  const detail = ref<PlanReportDetail>(cloneDeep(defaultReportDetail));

  const cardItemList = ref<configItem[]>([]);
  async function getShareDetail() {
    cardItemList.value = isGroup ? cloneDeep(defaultGroupConfig) : cloneDeep(defaultSingleConfig);
    try {
      const hrefShareDetail = await planGetShareHref(route.query.shareId as string);
      reportId.value = hrefShareDetail.reportId;
      if (hrefShareDetail.deleted) {
        router.push({
          name: NOT_FOUND_RESOURCE,
          query: {
            type: 'DELETE',
          },
        });
        return;
      }

      if (hrefShareDetail.expired) {
        router.push({
          name: NOT_FOUND_RESOURCE,
          query: {
            type: 'EXPIRED',
          },
        });
        return;
      }
      detail.value = await getReportDetail(reportId.value, route.query.shareId as string);
    } catch (error) {
      console.log(error);
    }
  }

  watchEffect(() => {
    if (route.query.shareId) {
      getShareDetail();
    }
  });
</script>

<style scoped></style>
