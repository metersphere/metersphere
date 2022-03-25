<template>
  <el-dialog
    destroy-on-close
    :title="$t('load_test.runtime_config')"
    width="550px"
    @close="close"
    :visible.sync="runModeVisible"
  >
    <div style="margin-bottom: 10px;">
      <span class="ms-mode-span">{{ $t("commons.environment") }}：</span>
      <env-popover :project-ids="projectIds"
                   :placement="'bottom-start'"
                   :project-list="projectList"
                   :project-env-map="projectEnvListMap"
                   :environment-type.sync="runConfig.environmentType"
                   :group-id="runConfig.environmentGroupId"
                   :has-option-group="true"
                   @setEnvGroup="setEnvGroup"
                   @setProjectEnvMap="setProjectEnvMap"
                   @showPopover="showPopover"
                   ref="envPopover" class="env-popover"/>
    </div>
    <div>
      <span class="ms-mode-span">{{ $t("run_mode.title") }}：</span>
      <el-radio-group v-model="runConfig.mode" @change="changeMode">
        <el-radio label="serial">{{ $t("run_mode.serial") }}</el-radio>
        <el-radio label="parallel">{{ $t("run_mode.parallel") }}</el-radio>
      </el-radio-group>
    </div>
    <div class="ms-mode-div">
      <el-row>
        <el-col :span="6">
          <span class="ms-mode-span">{{ $t("run_mode.other_config") }}：</span>
        </el-col>
        <el-col :span="18">
          <div>
            <el-radio-group v-model="runConfig.reportType">
              <el-radio label="iddReport">{{ $t("run_mode.idd_report") }}</el-radio>
              <el-radio label="setReport">{{ $t("run_mode.set_report") }}</el-radio>
            </el-radio-group>
          </div>
          <div style="padding-top: 10px">
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

    <!--- 失败停止 -->
    <div style="margin-top: 10px" v-if="runConfig.mode === 'serial'">
      <el-checkbox v-model="runConfig.onSampleError" style="margin-left: 127px">
        {{ $t("api_test.fail_to_stop") }}
      </el-checkbox>
    </div>

    <div class="ms-mode-div" v-if="runConfig.reportType === 'setReport'">
      <span class="ms-mode-span-label">{{ $t("run_mode.report_name") }}：</span>
      <el-input
        v-model="runConfig.reportName"
        :placeholder="$t('commons.input_content')"
        size="small"
        style="width: 300px"/>
    </div>
    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleRunBatch"/>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {ENV_TYPE} from "@/common/js/constants";
import {strMapToObj} from "@/common/js/utils";
import EnvPopover from "@/business/components/api/automation/scenario/EnvPopover";

export default {
  name: "RunMode",
  components: {MsDialogFooter, EnvPopover},
  data() {
    return {
      runModeVisible: false,
      testType: null,
      resourcePools: [],
      runConfig: {
        reportName: "",
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        runWithinResourcePool: false,
        resourcePoolId: null,
        envMap: new Map(),
        environmentGroupId: "",
        environmentType: ENV_TYPE.JSON
      },
      projectEnvListMap: {},
      projectList: [],
      projectIds: new Set(),
    };
  },
  props: ['request'],

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
    open() {
      this.runModeVisible = true;
      this.getResourcePools();
      this.getWsProjects();
      this.runConfig.environmentType = ENV_TYPE.JSON;
    },
    changeMode() {
      this.runConfig.runWithinResourcePool = false;
      this.runConfig.resourcePoolId = null;
      this.runConfig.reportType = "iddReport";
      this.runConfig.reportName = "";
    },
    close() {
      this.runConfig = {
        mode: "serial",
        reportType: "iddReport",
        reportName: "",
        environmentType: ENV_TYPE.JSON,
        runWithinResourcePool: false,
        resourcePoolId: null,
      };
      this.runModeVisible = false;
      this.$emit('close');
    },
    getWsProjects() {
      this.$get("/project/getOwnerProjects", res => {
        this.projectList = res.data;
      })
    },
    handleRunBatch() {
      if ((this.runConfig.mode === 'serial' || this.runConfig.mode === 'parallel') && this.runConfig.reportType === 'setReport' && this.runConfig.reportName.trim() === "") {
        this.$warning(this.$t('commons.input_name'));
        return;
      }
      this.$emit("handleRunBatch", this.runConfig);
      this.close();
    },
    getResourcePools() {
      this.result = this.$get('/testresourcepool/list/quota/valid', response => {
        this.resourcePools = response.data;
      });
    },
    setEnvGroup(id) {
      this.runConfig.environmentGroupId = id;
    },
    setProjectEnvMap(projectEnvMap) {
      this.runConfig.envMap = strMapToObj(projectEnvMap);
    },
    showPopover() {
      this.projectIds.clear();
      let url = "/api/automation/env";

      this.$post(url, this.request, res => {
        let data = res.data;
        if (data) {
          for (let d in data) {
            this.projectIds.add(data[d]);
          }
        }
        this.$refs.envPopover.openEnvSelect();
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

.ms-mode-span-label:before {
  content: '*';
  color: #F56C6C;
  margin-right: 4px;
  margin-left: 10px;
}

</style>
