<template>

    <div class="dialog-footer">
      <el-button @click="cancel">{{$t('commons.cancel')}}</el-button>
      <el-button type="primary" @click="confirm" @keydown.enter.native.prevent>{{$t('commons.confirm')}}</el-button>
    </div>

</template>

<script>
    export default {
      name: "MsDialogFooter",
      methods: {
        cancel() {
          this.$emit("cancel");
        },
        confirm() {
          this.$emit("confirm");
        }
      }
    }
</script>

<style scoped>

</style>
