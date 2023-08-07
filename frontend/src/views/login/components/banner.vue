<template>
  <div class="banner-wrap">
    <img class="img" :style="props.isPreview ? 'height: 100%;' : 'height: 100vh'" :src="innerBanner" />
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import useAppStore from '@/store/modules/app';

  const props = defineProps<{
    isPreview?: boolean;
    banner?: string;
  }>();

  const appStore = useAppStore();

  const defaultBanner = `${import.meta.env.BASE_URL}images/login-banner.jpg`;
  const innerBanner = computed(() => {
    return props.banner || appStore.pageConfig.loginImage[0]?.url || defaultBanner;
  });
</script>

<style lang="less" scoped>
  .banner-wrap {
    width: 55%;
    .img {
      width: 100%;
      object-fit: cover;
    }
  }
</style>
