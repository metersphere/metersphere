<template>

    <div class="dialog-footer">
      <el-button @click="cancel">取 消</el-button>
      <el-button type="primary" @click="confirm" @keydown.enter.native.prevent>确 定</el-button>
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
