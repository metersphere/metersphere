<template>
  <el-button @click="click"
             @click.stop="clickStop" :type="type"
             :icon="icon" size="mini" circle/>
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
      }
    },
    methods: {
      click() {
        this.$emit('click');
      },
      clickStop() {
        this.$emit('clickStop');
      }
    }
  }
</script>

<style scoped>
</style>
