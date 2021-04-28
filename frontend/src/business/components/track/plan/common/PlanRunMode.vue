<template>
  <el-dialog
    destroy-on-close
    :title="$t('load_test.runtime_config')"
    width="350px"
    :visible.sync="runModeVisible"
  >
    <div>
      <span class="ms-mode-span">{{ $t("run_mode.title") }}：</span>
      <el-radio-group v-model="runConfig.mode">
        <el-radio label="serial">{{ $t("run_mode.serial") }}</el-radio>
        <el-radio label="parallel">{{ $t("run_mode.parallel") }}</el-radio>
      </el-radio-group>
    </div>
    <div class="ms-mode-div" v-if="runConfig.mode === 'serial'">
      <span class="ms-mode-span">{{ $t("run_mode.other_config") }}：</span>
      <el-checkbox v-model="runConfig.onSampleError">失败停止</el-checkbox>
    </div>
    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleRunBatch"/>
    </template>
  </el-dialog>
</template>

<script>
  import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";

  export default {
    name: "MsPlanRunMode",
    components: {MsDialogFooter},
    data() {
      return {
        runModeVisible: false,
        runConfig: {mode: "serial", reportType: "iddReport", onSampleError: false},
      };
    },
    methods: {
      open() {
        this.runModeVisible = true;
      },
      close() {
        this.runConfig = {mode: "serial", reportType: "iddReport", onSampleError: false};
        this.runModeVisible = false;
      },
      handleRunBatch() {
        this.$emit("handleRunBatch", this.runConfig);
        this.close();
      },
    },
  };
</script>

<style scoped>
  .ms-mode-span {
    margin-right: 10px;
  }

  .ms-mode-div {
    margin-top: 20px;
  }
</style>
