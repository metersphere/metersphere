<template>
  <div class="count-div">
    <span>{{countContent}}</span>
  </div>
</template>

<script>
export default {
  name: "MsTableCountBar.vue",
  props: {
    countContent: String
  }
}
</script>

<style scoped>
.count-div {
  display: inline-block;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 500;
  font-size: 18px;
  line-height: 26px;
  color: #1F2329;
}
</style>
