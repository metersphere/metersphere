<template>
  <div class="flex h-full flex-col p-[16px]">
    <MsNotRemind tip="testPlan.planTip" class="mb-[16px]" type="info" visited-key="testPlanTip" />
    <div class="flex-1 overflow-hidden">
      <MsTestPlanMinder :plan-id="props.planId" :status="props.status" @save="emit('refresh')" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import MsTestPlanMinder from '@/components/business/ms-minders/testPlanMinder/index.vue';
  import MsNotRemind from '@/components/business/ms-not-remind/index.vue';

  const props = defineProps<{
    planId: string;
    status: string;
  }>();
  const emit = defineEmits<{
    (e: 'refresh'): void;
  }>();
</script>

<style lang="less" scoped></style>
