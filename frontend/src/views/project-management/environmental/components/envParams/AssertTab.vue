<template>
  <div>
    <ms-assertion />
  </div>
</template>

<script lang="ts" setup>
  import MsAssertion from '@/components/business/ms-assertion/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();
</script>

<style lang="less" scoped></style>
