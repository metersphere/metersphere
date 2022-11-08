<template>
  <el-dialog
    destroy-on-close
    :title="$t('load_test.runtime_config')"
    width="550px"
    @close="close"
    :visible.sync="runModeVisible">
    <div v-loading="loading">
      <div style="margin-bottom: 10px;">
        <span class="ms-mode-span"> {{ $t("commons.environment") }}： </span>
        <env-popover
          :project-ids="projectIds"
          :placement="'bottom-start'"
          :project-list="projectList"
          :project-env-map="projectEnvListMap"
          :environment-type.sync="runConfig.environmentType"
          :group-id="runConfig.environmentGroupId"
          :has-option-group="true"
          :show-env-group="isScenario"
          @setEnvGroup="setEnvGroup"
          @setProjectEnvMap="setProjectEnvMap"
          @showPopover="showPopover"
          ref="envPopover" class="env-popover"/>
      </div>

      <div>
        <span>{{ $t("run_mode.title") }}：</span>
        <el-radio-group v-model="runConfig.mode" @change="changeMode">
          <el-radio label="serial">{{ $t("run_mode.serial") }}</el-radio>
          <el-radio label="parallel">{{ $t("run_mode.parallel") }}</el-radio>
        </el-radio-group>
      </div>
      <!--  资源池    -->
      <div class="ms-mode-div">
        <span class="ms-mode-span">{{ $t("run_mode.other_config") }}：</span>
        <span>
          <el-radio-group v-model="runConfig.reportType">
            <el-radio label="iddReport">{{ $t("run_mode.idd_report") }}</el-radio>
            <el-radio label="setReport">{{ $t("run_mode.set_report") }}</el-radio>
          </el-radio-group>
        </span>
        <div style="padding:10px 90px">
          <el-checkbox v-model="runConfig.runWithinResourcePool"
                       style="padding-right: 10px;" :disabled="runMode === 'POOL'">
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
      </div>

      <!--- 失败停止 -->
      <div style="padding:0px 90px" v-if="runConfig.mode === 'serial'">
        <el-checkbox v-model="runConfig.onSampleError">
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
    </div>
    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleRunBatch"/>
    </template>
  </el-dialog>
</template>


<script>
import {apiScenarioEnv} from "@/api/scenario";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import {strMapToObj} from "metersphere-frontend/src/utils";
import EnvPopover from "@/business/automation/scenario/EnvPopover";
import {getOwnerProjects, getProjectConfig} from "@/api/project";
import {getTestResourcePools} from "@/api/test-resource-pool";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getSystemBaseSetting} from "metersphere-frontend/src/api/system";

export default {
  name: "RunMode",
  components: {MsDialogFooter, EnvPopover},
  data() {
    return {
      runMode: "",
      loading: false,
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
  props: {
    request: Object,
    isScenario: {
      type: Boolean,
      default: true
    }
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
    open() {
      this.runModeVisible = true;
      this.getResourcePools();
      this.getWsProjects();
      this.query();
      this.runConfig.environmentType = ENV_TYPE.JSON;
    },
    changeMode() {
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
      getOwnerProjects().then(res => {
        this.projectList = res.data;
      })
    },
    handleRunBatch() {
      if ((this.runConfig.mode === 'serial' || this.runConfig.mode === 'parallel') && this.runConfig.reportType === 'setReport' && this.runConfig.reportName.trim() === "") {
        this.$warning(this.$t('commons.input_name'));
        return;
      }
      if (this.runConfig.runWithinResourcePool && this.runConfig.resourcePoolId == null) {
        this.$warning(this.$t('workspace.env_group.please_select_run_within_resource_pool'));
        return;
      }
      this.$emit("handleRunBatch", this.runConfig);
      this.close();
    },
    getResourcePools() {
      this.result = getTestResourcePools().then(response => {
        this.resourcePools = response.data;
      });
    },
    query() {
      this.loading = true;
      this.result = getSystemBaseSetting().then(response => {
        if (!response.data.runMode) {
          response.data.runMode = 'LOCAL'
        }
        this.runMode = response.data.runMode;
        if (this.runMode === 'POOL') {
          this.runConfig.runWithinResourcePool = true;
          this.getProjectApplication();
        } else {
          this.loading = false;
        }
      })
    },
    getProjectApplication() {
      getProjectConfig(getCurrentProjectID(), "").then(res => {
          if (res.data && res.data.poolEnable && res.data.resourcePoolId) {
            this.runConfig.resourcePoolId = res.data.resourcePoolId;
          }
          this.loading = false;
        });
    },
    setEnvGroup(id) {
      this.runConfig.environmentGroupId = id;
    },
    setProjectEnvMap(projectEnvMap) {
      this.runConfig.envMap = strMapToObj(projectEnvMap);
    },
    showPopover() {
      if (this.isScenario) {
        this.showScenarioPopover();
      } else {
        this.showApiPopover();
      }
    },
    showApiPopover() {
      this.projectIds.clear();
      this.projectIds.add(getCurrentProjectID());
      this.$refs.envPopover.openEnvSelect();
    },
    showScenarioPopover() {
      this.projectIds.clear();
      apiScenarioEnv(this.request).then(res => {
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
  display: -moz-inline-box;
  display: inline-block;
  width: 90px;
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
