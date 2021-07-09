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
    <div class="ms-mode-div" v-if="runConfig.mode === 'serial'">
      <el-row>
        <el-col :span="6">
          <span class="ms-mode-span">{{ $t("run_mode.other_config") }}：</span>
        </el-col>
        <el-col :span="18">
          <div>
            <el-checkbox v-model="runConfig.onSampleError">失败停止</el-checkbox>
          </div>
          <div v-if="testType === 'API'" style="padding-top: 10px">
            <el-checkbox v-model="runConfig.runWithinResourcePool" style="padding-right: 10px;">
              {{ $t('run_mode.run_with_resource_pool') }}
            </el-checkbox>
            <el-select :disabled="!runConfig.runWithinResourcePool" v-model="runConfig.resourcePoolId" size="mini">
              <el-option
                v-for="item in resourcePools"
                :key="item.id"
                :label="item.name"
                :value="item.id">
              </el-option>
            </el-select>
          </div>
        </el-col>
      </el-row>
    </div>
    <div class="ms-mode-div" v-if="runConfig.mode === 'parallel'">
      <el-row>
        <el-col :span="6">
          <span class="ms-mode-span">{{ $t("run_mode.other_config") }}：</span>
        </el-col>
        <el-col :span="18">
          <div v-if="testType === 'API'">
            <el-checkbox v-model="runConfig.runWithinResourcePool" style="padding-right: 10px;">
              {{ $t('run_mode.run_with_resource_pool') }}
            </el-checkbox>
            <el-select :disabled="!runConfig.runWithinResourcePool" v-model="runConfig.resourcePoolId" size="mini">
              <el-option
                v-for="item in resourcePools"
                :key="item.id"
                :label="item.name"
                :disabled="!item.api"
                :value="item.id">
              </el-option>
            </el-select>
          </div>
        </el-col>
      </el-row>
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
