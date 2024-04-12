<template>
  <div class="banner-wrap">
    <img class="img" :style="props.isPreview ? 'height: 100%;' : 'height: 100vh'" :src="innerBanner" />
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';

  import { GetLoginImageUrl } from '@/api/requrls/setting/config';

  const props = defineProps<{
    isPreview?: boolean;
    banner?: string;
  }>();

  const innerBanner = computed(() => {
    return props.banner || GetLoginImageUrl;
  });
</script>

<style lang="less" scoped>
  .banner-wrap {
    .img {
      width: 100%;
      object-fit: cover;
    }
  }
</style>
