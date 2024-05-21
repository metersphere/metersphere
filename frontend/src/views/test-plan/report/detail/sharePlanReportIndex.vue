<template>
  <PlanDetail :report-id="reportId" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';

  import PlanDetail from '@/views/test-plan/report/detail/component/planDetail.vue';

  import { planGetShareHref } from '@/api/modules/test-plan/report';

  const route = useRoute();
  const shareId = ref<string>(route.query.shareId as string);
  const reportId = ref<string>(route.query.id as string);

  async function getShareDetail() {
    try {
      const hrefShareDetail = await planGetShareHref(shareId.value);
      reportId.value = hrefShareDetail.reportId;
    } catch (error) {
      console.log(error);
    }
  }

  onMounted(() => {
    getShareDetail();
  });
</script>

<style scoped></style>
