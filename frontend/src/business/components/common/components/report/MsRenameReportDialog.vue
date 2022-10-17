<template>
  <el-dialog
    :title="$t('report.rename_report')"
    :visible.sync="dialogVisible"
    width="40%"
    :modal-append-to-body="false"
    :close-on-click-modal="false"
    :before-close="close">
    <el-form>
      <el-form-item :label="$t('commons.name')">
        <el-input v-model="data.name" :maxlength="maxLength" show-word-limit></el-input>
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
        <el-button @click="close">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="submit">{{ $t('commons.confirm') }}</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: "MsRenameReportDialog",
  data() {
    return {
      dialogVisible: false,
      data: {}
    };
  },
  props: {
    maxLength: {
      type: Number,
      default: 60
    },
  },
  methods: {
    open(data) {
      this.dialogVisible = true;
      this.data = JSON.parse(JSON.stringify(data));
    },
    submit() {
      if (!this.data.name) {
        this.$error(this.$t("commons.name") + this.$t("commons.cannot_be_null"));
        return;
      }
      this.$emit('submit', this.data);
    },
    close() {
      this.dialogVisible = false;
    }
  }
}
</script>

<style scoped>

</style>
