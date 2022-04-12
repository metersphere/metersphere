<template>
  <div>
    <el-alert
      v-if="isShowWarning"
      :title="$t('license.count_warning')"
      type="warning"
      center
      :closable="false"
      show-icon>
    </el-alert>
    <el-container class="ms-container">
      <slot></slot>
    </el-container>
  </div>

</template>

<script>
    export default {
      name: "MsContainer",
      props: ['isShowWarning']
    }
</script>

<style scoped>

  .ms-container >>> span.title {
    font-size: 16px;
    font-weight: 500;
    margin-top: 0;
    text-overflow: ellipsis;
    overflow: hidden;
    word-wrap: break-word;
    white-space: nowrap;
  }

</style>
