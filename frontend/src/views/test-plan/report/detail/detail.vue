<template>
  <ViewReport
    v-model:card-list="cardItemList"
    :detail-info="detail"
    :is-group="isGroup"
    is-preview
    @update-success="getDetail()"
  />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { cloneDeep } from 'lodash-es';

  import ViewReport from '@/views/test-plan/report/detail/component/viewReport.vue';

  import { getReportDetail } from '@/api/modules/test-plan/report';
  import { defaultReportDetail } from '@/config/testPlan';

  import type { configItem, PlanReportDetail } from '@/models/testPlan/testPlanReport';

  const route = useRoute();
  const reportId = ref<string>(route.query.id as string);

  const detail = ref<PlanReportDetail>(cloneDeep(defaultReportDetail));

  const isGroup = computed(() => route.query.type === 'GROUP');

  const cardItemList = ref<configItem[]>([]);

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
