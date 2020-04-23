<template>
  <el-button @click="exec"
             @click.stop="clickStop" :type="type"
             :icon="icon" size="mini" circle></el-button>
</template>

<script>
  import MsTableButton from "./MsTableButton";
  export default {
    name: "MsTableOperatorButton",
    components: {MsTableButton},
    props: {
      icon: {
        type: String,
        default: 'el-icon-question'
      },
      type: {
        type: String,
        default: 'primary'
      },
      exec: {
        type: Function
      }
    },
    methods: {
      clickStop() {
        this.$emit('clickStop');
      }
    }
  }
</script>

<style scoped>
</style>
