<template>
  <div class="banner-wrap ml-2.5 flex items-center">
    <img class="h-365px w-567px m-auto block" :src="bannerImage" />
  </div>
</template>

<script setup>
  import bannerImage from '@/assets/images/login-banner.png';
</script>

<style lang="less" scoped>
  .banner-wrap {
    height: 760px;
  }
</style>
