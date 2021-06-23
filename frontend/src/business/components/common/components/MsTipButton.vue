<template>

  <el-tooltip :content="tip"
              placement="bottom"
              :enterable="false"
              :effect="effect">
    <span class="tooltip-btn">
      <el-button @click="exec()"
                 @click.stop="clickStop"
                 @keydown.enter.native.prevent
                 circle
                 :plain="plain"
                 :disabled="disabled"
                 :type="type"
                 :icon="icon"
                 :size="size"/>
    </span>
  </el-tooltip>

</template>

<script>
    export default {
      name: "MsTipButton",
      props: {
        tip: String,
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
        },
        plain: Boolean
      },
      methods: {
        exec() {
          this.$emit('click');
        },
        clickStop() {
          this.$emit('clickStop');
        }
      }
    }
</script>

<style scoped>
.tooltip-btn+.tooltip-btn {
  margin-left: 10px;
}
</style>
