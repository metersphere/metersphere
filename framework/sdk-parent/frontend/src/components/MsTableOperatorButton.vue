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
  <el-button @click="exec" v-else-if="isTextButton"
             @keydown.enter.native.prevent
             type="text" class="text-btn"
             :disabled="isReadOnly"
             size="mini"> {{ tip }}
  </el-button>
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
    isTextButton: {
      type: Boolean,
      default: false,
    },
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
.text-btn {
  width: 28px;
  height: 22px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  text-align: center;
  color: #783887;
  flex: none;
  order: 1;
  flex-grow: 0;
  padding: 0;
  margin: 0 16px 0 0;
}
</style>
