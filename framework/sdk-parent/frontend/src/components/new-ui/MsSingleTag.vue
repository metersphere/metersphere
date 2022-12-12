<template>
  <el-tag size="mini" class="status-label">{{ content }}</el-tag>
</template>

<script>
export default {
  name: "MsTag",
  props: {
    content: {
      type: String
    }
  }
}
</script>

<style scoped>
.status-label {
  height: 24px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  padding: 0 6px;
  border-radius: 2px;
  border-color: transparent;
  align-items: center;
  background-color: rgba(120, 56, 135, 0.2);;
  color: #783887;
}
</style>
