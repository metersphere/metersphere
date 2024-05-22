<template>
  <PlanDetail :report-id="reportId" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';

  import PlanDetail from '@/views/test-plan/report/detail/component/planDetail copy.vue';

  const route = useRoute();
  const reportId = ref<string>(route.query.id as string);
</script>

<style scoped lang="less"></style>
