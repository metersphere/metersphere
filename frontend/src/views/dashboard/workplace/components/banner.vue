<template>
  <a-col class="banner">
    <a-col :span="8">
      <a-typography-title :heading="5" style="margin-top: 0">
        {{ $t('workplace.welcome') }} {{ userInfo.name }}
      </a-typography-title>
    </a-col>
    <a-divider class="panel-border" />
  </a-col>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { useUserStore } from '@/store';

  const userStore = useUserStore();
  const userInfo = computed(() => {
    return {
      name: userStore.name,
    };
  });
</script>

<style scoped lang="less">
  .banner {
    padding: 20px 20px 0;
    width: 100%;
    border-radius: 4px 4px 0 0;
    background-color: var(--color-bg-1);
  }
  :deep(.arco-icon-home) {
    margin-right: 6px;
  }
</style>
