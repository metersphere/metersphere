<template>
  <a-scrollbar
    :style="{
      overflow: 'auto',
      width: props.isPreview ? '100%' : '100vw',
      height: props.isPreview ? '100%' : '100vh',
    }"
  >
    <div class="login-page" :style="props.isPreview ? '' : 'min-width: 1200px;'">
      <banner />
      <a-spin :loading="appStore.getLoginLoadingStatus" class="flex-1">
        <loginForm :is-preview="props.isPreview" />
      </a-spin>
    </div>
  </a-scrollbar>
</template>

<script lang="ts" setup>
  import banner from './components/banner.vue';
  import loginForm from './components/login-form.vue';

  import { useAppStore } from '@/store';

  const appStore = useAppStore();

  const props = defineProps<{
    isPreview?: boolean;
  }>();
</script>

<style lang="less" scoped>
  .login-page {
    @apply flex items-center;
  }
</style>
