<template>
  <div class="container">
    <div class="icon"></div>
    <div class="detail">
      <div class="filename">mybatis.txt</div>
      <div class="file-info-row">
        <div class="size">15.2M</div>
        <div class="split">|</div>
        <div class="username">youliyuan</div>
        <div class="upload-time">上传于2022-10-06 10:00:00</div>
      </div>
    </div>
    <div class="options"></div>
  </div>
</template>
<script>
export default {
  name: "CaseAttachmentItem",
  props: {
    fileItem: Object,
    readOnly: {
      type: Boolean,
      default: false,
    },
    isDelete: {
      type: Boolean,
      default: false,
    },
    isCopy: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
<style scoped lang="scss"></style>
