<template>
  <PlanGroupDetail v-if="isGroup" :detail-info="detail" @update-success="getDetail()" />
  <PlanDetail v-else :detail-info="detail" @update-success="getDetail()" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { cloneDeep } from 'lodash-es';

  import PlanDetail from '@/views/test-plan/report/detail/component/planDetail.vue';
  import PlanGroupDetail from '@/views/test-plan/report/detail/component/planGroupDetail.vue';

  import { getReportDetail } from '@/api/modules/test-plan/report';
  import { defaultReportDetail } from '@/config/testPlan';

  import type { PlanReportDetail } from '@/models/testPlan/testPlanReport';

  const route = useRoute();
  const reportId = ref<string>(route.query.id as string);

  const detail = ref<PlanReportDetail>(cloneDeep(defaultReportDetail));

  const isGroup = computed(() => route.query.type === 'GROUP');

  async function getDetail() {
    try {
      detail.value = await getReportDetail(reportId.value);
    } catch (error) {
      console.log(error);
    }
  }

  onBeforeMount(() => {
    getDetail();
  });
</script>

<style scoped lang="less"></style>
