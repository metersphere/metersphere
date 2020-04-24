<template>
  <ms-tip-button @click="exec"
                 @clickStop="clickStop"
                 :type="type"
                 :tip="tip"
                 :icon="icon" size="mini" circle/>
</template>

<script>
  import MsTableButton from "./MsTableButton";
  import MsTipButton from "./MsTipButton";
  export default {
    name: "MsTableOperatorButton",
    components: {MsTipButton, MsTableButton},
    props: {
      icon: {
        type: String,
        default: 'el-icon-question'
      },
      type: {
        type: String,
        default: 'primary'
      },
      tip: {
        type: String
      }
    },
    methods: {
      exec() {
        this.$emit('exec');
      },
      clickStop() {
        this.$emit('clickStop');
      }
    }
  }
</script>

<style scoped>
</style>
