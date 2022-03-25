<template>

  <div class="dialog-header">
    <el-button @click="cancel" :size="btnSize">{{ $t('commons.cancel') }}</el-button>
    <el-button type="primary" @click="confirm" @keydown.enter.native.prevent :size="btnSize">
      {{ $t('commons.confirm') }}
    </el-button>
    <el-button type="primary" v-if="isShow" @click="saveAsEdit" @keydown.enter.native.prevent :size="btnSize">{{
        title
      }}
    </el-button>
  </div>

</template>

<script>
export default {
  name: "MsDialogHeader",
  props: {
    isShow: {
      type: Boolean,
      default: false,
    },
    title: String,
    btnSize: {
      type: String,
      default() {
        return 'mini';
      }
    }
  },
  methods: {
    cancel() {
      this.$emit("cancel");
    },
    confirm() {
      this.$emit("confirm");
    },
    saveAsEdit() {
      this.$emit("saveAsEdit");
    }
  }
}
</script>

<style scoped>

.dialog-header {
  top: 20px;
  right: 20px;
  padding: 0;
  background: 0 0;
  border: none;
  outline: 0;
  cursor: pointer;
  display: inline-block
}

</style>
