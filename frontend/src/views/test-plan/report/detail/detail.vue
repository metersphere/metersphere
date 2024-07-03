<template>
  <ViewReport v-model:card-list="cardItemList" :detail-info="detail" :is-group="isGroup" is-preview />
</template>

<script setup lang="ts">
  // TODO 待联调 需要接口更新后联调下
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { cloneDeep } from 'lodash-es';

  import ViewReport from '@/views/test-plan/report/detail/component/viewReport.vue';

  import { getReportDetail } from '@/api/modules/test-plan/report';
  import { defaultReportDetail } from '@/config/testPlan';

  import type { configItem, PlanReportDetail } from '@/models/testPlan/testPlanReport';

  import { defaultGroupConfig, defaultSingleConfig } from '@/views/test-plan/report/detail/component/reportConfig';

  const route = useRoute();
  const reportId = ref<string>(route.query.id as string);

  const detail = ref<PlanReportDetail>(cloneDeep(defaultReportDetail));

  const isGroup = computed(() => route.query.type === 'GROUP');

  const cardItemList = ref<configItem[]>([]);

  async function getDetail() {
    cardItemList.value = isGroup ? cloneDeep(defaultGroupConfig) : cloneDeep(defaultSingleConfig);
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
