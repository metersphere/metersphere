<template>
  <el-dialog
    destroy-on-close
    :title="$t('load_test.runtime_config')"
    width="450px"
    :visible.sync="runModeVisible"
  >
    <div>
      <span class="ms-mode-span">{{ $t("run_mode.title") }}：</span>
      <el-radio-group v-model="runConfig.mode" @change="changeMode">
        <el-radio label="serial">{{ $t("run_mode.serial") }}</el-radio>
        <el-radio label="parallel">{{ $t("run_mode.parallel") }}</el-radio>
      </el-radio-group>
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
      testType: null,
      resourcePools: [],
      runConfig: {
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        runWithinResourcePool: false,
        resourcePoolId: null,
      },
    };
  },
  watch: {
    'runConfig.runWithinResourcePool'() {
      if (!this.runConfig.runWithinResourcePool) {
        this.runConfig = {
          mode: this.runConfig.mode,
          reportType: "iddReport",
          reportName: "",
          runWithinResourcePool: false,
          resourcePoolId: null,
        };
      }
    }
  },
  methods: {
    open(testType) {
      this.runModeVisible = true;
      this.testType = testType;
      this.getResourcePools();
    },
    changeMode() {
      this.runConfig.onSampleError = false;
      this.runConfig.runWithinResourcePool = false;
      this.runConfig.resourcePoolId = null;
    },
    close() {
      this.runConfig = {
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        runWithinResourcePool: false,
        resourcePoolId: null,
      };
      this.runModeVisible = false;
    },
    handleRunBatch() {
      this.$emit("handleRunBatch", this.runConfig);
      this.close();
    },
    getResourcePools() {
      this.result = this.$get('/testresourcepool/list/quota/valid', response => {
        this.resourcePools = response.data;
      });
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
