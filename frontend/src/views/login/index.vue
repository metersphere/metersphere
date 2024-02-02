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
      <loginForm :is-preview="props.isPreview" />
    </div>
  </a-scrollbar>
</template>

<script lang="ts" setup>
  import banner from './components/banner.vue';
  import loginForm from './components/login-form.vue';

  import { useUserStore } from '@/store';

  const props = defineProps<{
    isPreview?: boolean;
  }>();

  const userStore = useUserStore();

  onMounted(() => {
    userStore.getAuthentication();
  });
</script>

<style lang="less" scoped>
  .login-page {
    @apply flex items-center;
  }
</style>
