<template>
  <PlanGroupDetail :detail-info="detail" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { cloneDeep } from 'lodash-es';

  import PlanGroupDetail from '@/views/test-plan/report/detail/component/planGroupDetail.vue';

  import { getReportDetail } from '@/api/modules/test-plan/report';
  import { defaultReportDetail } from '@/config/testPlan';

  import type { PlanReportDetail } from '@/models/testPlan/testPlanReport';

  const route = useRoute();
  const reportId = ref<string>(route.query.id as string);

  const detail = ref<PlanReportDetail>(cloneDeep(defaultReportDetail));

  async function getDetail() {
    try {
      detail.value = await getReportDetail(reportId.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    getDetail();
  });
</script>

<style scoped></style>
