<template>
  <el-main class="ms-main-container" :style="{
              'height': calHeight,
             }">
    <slot></slot>
  </el-main>
</template>

<script>
export default {
  name: "MsMainContainer",
  props: {
    enableAutoHeight: Boolean
  },
  computed: {
    calHeight() {
      return this.enableAutoHeight ? null : 'calc(100vh - 80px)';
    },
  },
}
</script>

<style scoped>

.ms-main-container {
  padding: 5px 10px;
}

</style>
