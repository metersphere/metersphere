<template>
  <div class="msDialogHeader">
    <span style="float: left; font-size: 18px; color: #303133">{{
      title
    }}</span>

    <div style="top: 20px; right: 50px; position: absolute" v-if="enableFullScreen">
      <el-tooltip
        effect="dark"
        :content="$t('commons.full_screen_editing')"
        placement="top-start"
      >
        <font-awesome-icon
          class="alt-ico"
          :icon="icon"
          size="lg"
          @click="fullScreen"
        />
      </el-tooltip>
    </div>

    <div v-if="!hideButton" style="float: right; width: fit-content">
      <div style="float: left; margin-right: 8px">
        <slot name="other"></slot>
      </div>
      <div class="ms_btn">
        <el-button v-if="enableCancel" @click="cancel" :size="btnSize">{{
          $t("commons.cancel")
        }}</el-button>
        <el-button
          type="primary"
          :loading="isButtonSaving"
          @click="confirm"
          @keydown.enter.native.prevent
          v-prevent-re-click
          :size="btnSize"
        >
          {{ $t("commons.confirm") }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "MsDialogHeader",
  data() {
    return {
      icon:'',
    };
  },
  props: {
    title: String,
    btnSize: {
      type: String,
      default() {
        return "small";
      },
    },
    hideButton: Boolean,
    enableCancel: Boolean,
    isButtonSaving: Boolean,
    isFullScreen: Boolean,
    enableFullScreen: {
      type: Boolean,
      default() {
        return true;
      },
    },
  },
  created(){
    if(this.isFullScreen) {
      this.icon= ['fa', 'compress-alt'];
    }else {
      this.icon= ['fa', 'expand-alt'];
    }
  },
  methods: {
    cancel() {
      this.$emit("cancel");
    },
    confirm() {
      this.$emit("confirm");
    },
    fullScreen() {
      let bool = !this.isFullScreen;
      if(bool) {
        this.icon= ['fa', 'compress-alt'];
      }else {
        this.icon= ['fa', 'expand-alt'];
      }
      this.$emit("update:isFullScreen",bool);
      this.$emit("fullScreen");
    },
  },
};
</script>

<style scoped>
.ms_btn {
  float: right;
  margin-right: 70px;
}

.msDialogHeader {
  margin-bottom: 5px;
}

.alt-ico {
  font-size: 13px;
  color: #8c939d;
}
</style>
