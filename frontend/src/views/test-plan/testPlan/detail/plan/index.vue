<template>
  <div class="flex h-full flex-col">
    <MsNotRemind tip="testPlan.planTip" class="p-[16px]" type="info" visited-key="testPlanTip" />
    <div class="flex-1 overflow-hidden p-[16px]">
      <MsTestPlanMinder :plan-id="props.planId" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import MsTestPlanMinder from '@/components/business/ms-minders/testPlanMinder/index.vue';
  import MsNotRemind from '@/components/business/ms-not-remind/index.vue';

  const props = defineProps<{
    planId: string;
  }>();
</script>

<style lang="less" scoped></style>
