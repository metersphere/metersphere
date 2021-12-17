<template>
  <el-dialog
    :close-on-click-modal="false"
    :title="$t('load_test.add_monitor')"
    :visible.sync="dialogVisible"
    width="70%"
    @closed="closeFunc"
    :destroy-on-close="true"
    :append-to-body="true"
    v-loading="result.loading"
  >
    <div v-html="$t('report.batch_add_monitor_tips')"></div>
    <div style="padding-top: 10px;">
      <ms-code-edit :enable-format="false" mode="text"
                    height="250px"
                    :data.sync="parameters"
                    theme="eclipse"
                    :modes="['text']"
                    ref="codeEdit"/>
    </div>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="dialogVisible = false"
        @confirm="save"/>
    </template>

  </el-dialog>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import MsCodeEdit from "@/business/components/api/definition/components/MsCodeEdit";

export default {
  name: "BatchAddMonitor",
  components: {MsCodeEdit, MsDialogFooter},
  data() {
    return {
      result: {},
      dialogVisible: false,
      parameters: "",
    };
  },
  methods: {
    open() {
      this.dialogVisible = true;
    },
    save() {
      this.dialogVisible = false;
      this.$emit("batchSave", this.parameters);
    },
    closeFunc() {
      this.dialogVisible = false;
    },
  }
};
</script>

<style scoped>

</style>
