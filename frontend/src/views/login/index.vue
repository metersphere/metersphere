<template>
  <div class="my-container">
    <a-row class="login-box flex flex-row">
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
    width: 1440px;
    height: 100vh;
    background-color: #ffffff;
    .login-box {
      margin-top: calc(50vh - 400px);
      height: 730px;
    }
    .arco-divider-vertical {
      height: 52em;
    }
  }
</style>
