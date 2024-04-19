<template>
  <ExpireAlert />
  <logCards mode="SYSTEM"></logCards>
</template>

<script lang="tsx" setup>
  /**
   * @description 系统设置-日志
   */
  import logCards from './components/logCards.vue';
  import ExpireAlert from '@/views/setting/system/authorizedManagement/components/expireAlert.vue';
</script>

<style lang="less" scoped></style>
