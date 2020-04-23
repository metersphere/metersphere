<template>
  <el-button @click="exec()" plain :type="type" :icon="icon" :size="size">
    {{content}}
  </el-button>
</template>

<script>
    export default {
      name: "MsTableButton",
      props: {
        content: String,
        icon: {
          type: String,
          default: 'el-icon-question'
        },
        type: {
          type: String,
          default: null
        },
        effect: {
          type: String,
          default: 'dark'
        },
        size: {
          type: String,
          default: 'mini'
        },
        disabled: {
          type: Boolean,
          default: false
        }
      },
      methods: {
        exec() {
          this.$emit('click');
        }
      }
    }
</script>

<style scoped>

  .el-button {
    padding: 8px 10px;
  }
</style>
