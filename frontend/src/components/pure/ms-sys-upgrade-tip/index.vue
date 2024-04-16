<template>
  <div class="flex items-center gap-[12px]">
    <div class="text-[var(--color-text-1)]">{{ t('ms.sys.upgrade.tip.content') }} </div>
    <MsButton type="text" @click="refresh">{{ t('common.refresh') }}</MsButton>
  </div>
</template>

<script setup lang="ts">
  import MsButton from '@/components/pure/ms-button/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  function refresh() {
    window.location.reload();
  }
</script>

<style lang="less" scoped></style>
