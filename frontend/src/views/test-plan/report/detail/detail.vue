<template>
  <a-spin :loading="loading" class="h-full w-full">
    <ViewReport
      v-model:card-list="cardItemList"
      :detail-info="detail"
      :is-group="isGroup"
      is-preview
      @update-success="getDetail()"
    />
  </a-spin>
</template>

<script setup lang="ts">
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
  const loading = ref<boolean>(false);

  async function getDetail() {
    try {
      loading.value = true;
      detail.value = await getReportDetail(reportId.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  onBeforeMount(() => {
    getDetail();
  });
</script>

<style scoped lang="less"></style>
