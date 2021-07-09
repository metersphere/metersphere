<template>
  <el-tooltip :content="tip" v-if=isDivButton
              placement="bottom"
              :enterable="false"
              effect="dark">
    <el-button @click="exec"
               @keydown.enter.native.prevent
               type="primary"
               :disabled="isReadOnly"
               circle
               style="color:white;padding: 0px 0.1px;width: 28px;height: 28px;"
               size="mini">
      <div style="transform: scale(0.8)">
        <span style="margin-left: -4px;line-height: 27px;">{{ tip }}</span>
      </div>
    </el-button>
  </el-tooltip>
  <ms-tip-button v-else
                 :disabled="disabled || isReadOnly"
                 @click="exec"
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
    data() {
      return {
        isReadOnly: false
      }
    },
    props: {
      isDivButton: {
        type: Boolean,
        default: false,
      },
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
      },
      disabled: {
        type: Boolean,
        default: false
      },
      isTesterPermission: {
        type: Boolean,
        default: false
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
