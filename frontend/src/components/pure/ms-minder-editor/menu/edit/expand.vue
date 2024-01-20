<template>
  <div class="expand-group">
    <a-button class="tab-icons expand" type="text" @click="expand" />
    {{ t('minder.menu.expand.expand') }}
  </div>
</template>

<script lang="ts" name="expand" setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  function expand() {
    window.minder?.execCommand('Expand', 9999);
  }
</script>

<style lang="less" scoped>
  .dropdown-link {
    @apply cursor-pointer;
  }
</style>
