<template>
  <div class="my-container h-[100vh] w-[100vw] min-w-[1440px]">
    <a-row class="flex h-[730px] flex-row">
      <a-col :span="11"> <loginForm /></a-col>
      <a-divider direction="vertical" />
      <a-col :span="11"> <banner /></a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
  import loginForm from './components/login-form.vue';
  import banner from './components/banner.vue';
</script>

<style lang="less" scoped>
  .my-container {
    .login-box {
      margin-top: calc(50vh - 400px);
    }
    .arco-divider-vertical {
      height: 52em;
    }
  }
</style>
