<template>
  <div class="banner-wrap">
    <img class="img w-567px m-auto block" :src="bannerImage" />
  </div>
</template>

<script setup>
  import bannerImage from '@/assets/images/login-banner.png';
</script>

<style lang="less" scoped>
  .banner-wrap {
    padding-top: 160px;
    height: 760px;
    .img {
      height: 365px;
    }
  }
</style>
