<template>
  <el-dialog
    destroy-on-close
    :title="$t('load_test.runtime_config')"
    width="550px"
    @close="close"
    :visible.sync="runModeVisible">
    <div class="env-container">
      <div>
        <div>{{ $t("commons.environment") }}：</div>
        <env-select-popover :project-ids="projectIds"
                            :project-list="projectList"
                            :case-id-env-name-map="caseIdEnvNameMap"
                            :is-scenario="isScenario"
                            :project-env-map="projectEnvListMap"
                            :group-id="runConfig.environmentGroupId"
                            @setProjectEnvMap="setProjectEnvMap"
                            @setEnvGroup="setEnvGroup"
                            ref="envSelectPopover"
                            class="mode-row"
        ></env-select-popover>
      </div>

      <div>
        <div class="mode-row">{{ $t("run_mode.title") }}：</div>
        <div >
          <el-radio-group
            v-model="runConfig.mode"
            @change="changeMode"
            style="width: 100%"
            class="radio-change mode-row"
          >
            <el-radio label="serial">{{ $t("run_mode.serial") }}</el-radio>
            <el-radio label="parallel">{{ $t("run_mode.parallel") }}</el-radio>
          </el-radio-group>
        </div>
      </div>
      <!--  资源池    -->
      <div>
        <div class="mode-row">{{ $t("run_mode.other_config") }}：</div>
        <div class="mode-row">
          <el-radio-group v-model="runConfig.reportType">
            <el-radio label="iddReport">{{ $t("run_mode.idd_report") }}</el-radio>
            <el-radio label="setReport">{{ $t("run_mode.set_report") }}</el-radio>
          </el-radio-group>
        </div>
        <div class="ms-mode-span-label" style="margin-top: 8px" v-if="runConfig.reportType === 'setReport'">{{ $t("run_mode.report_name") }}：</div>
        <div class="mode-row" v-if="runConfig.reportType === 'setReport'">
          <el-input
            v-model="runConfig.reportName"
            :placeholder="$t('commons.input_content')"
            size="small"
            style="width: 100%"/>
        </div>
        <div >
          <!-- 串行 -->
          <div
            class="mode-row"
            v-if="runConfig.mode === 'serial' && testType === 'API'"
          >
            <el-checkbox
              v-model="runConfig.runWithinResourcePool"
              style="padding-right: 10px"
              class="radio-change"
              :disabled="runMode === 'POOL'"
            >
              {{ $t("run_mode.run_with_resource_pool") }}
            </el-checkbox><br/>
            <el-select
              :disabled="!runConfig.runWithinResourcePool"
              v-model="runConfig.resourcePoolId"
              size="mini"
              style="width:100%; margin-top: 8px"
            >
              <el-option
                v-for="item in resourcePools"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
              </el-option>
            </el-select>
          </div>
          <!-- 并行 -->
          <div
            class="mode-row"
            v-if="runConfig.mode === 'parallel' && testType === 'API'"
          >
            <el-checkbox
              v-model="runConfig.runWithinResourcePool"
              style="padding-right: 10px"
              class="radio-change"
              :disabled="runMode === 'POOL'"
            >
              {{ $t("run_mode.run_with_resource_pool") }}
            </el-checkbox><br/>
            <el-select
              :disabled="!runConfig.runWithinResourcePool"
              v-model="runConfig.resourcePoolId"
              size="mini"
              style="width:100%; margin-top: 8px"
            >
              <el-option
                v-for="item in resourcePools"
                :key="item.id"
                :label="item.name"
                :disabled="!item.api"
                :value="item.id"
              >
              </el-option>
            </el-select>
          </div>

          <!-- 失败停止 -->
          <div class="mode-row" v-if="runConfig.mode === 'serial'">
            <el-checkbox v-model="runConfig.onSampleError" class="radio-change">{{
                $t("api_test.fail_to_stop")
              }}
            </el-checkbox>
          </div>

        </div>
      </div>

    </div>
    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleRunBatch"/>
    </template>
  </el-dialog>
</template>


<script>
import {apiScenarioEnvMap} from "@/api/scenario";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import {strMapToObj} from "metersphere-frontend/src/utils";
import {getOwnerProjects, getProjectConfig} from "@/api/project";
import {getTestResourcePools} from "@/api/test-resource-pool";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getSystemBaseSetting} from "metersphere-frontend/src/api/system";
import EnvSelectPopover from "@/business/automation/scenario/EnvSelectPopover";
import {getApiCaseEnvironments} from "@/api/api-test-case";

export default {
  name: "ApiRunMode",
  components: {MsDialogFooter, EnvSelectPopover},
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
      caseIdEnvNameMap:{},
    };
  },
  props: {
    runCaseIds:Array,
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
      this.showPopover();
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
      let currentProjectID = getCurrentProjectID();
      this.projectIds.clear();
      getApiCaseEnvironments(this.runCaseIds).then((res) => {
        let data = res.data;
        if (data) {
          this.caseIdEnvNameMap = data;
          this.projectIds.add(currentProjectID);
        }
        this.$refs.envSelectPopover.open();
      });
    },
    showScenarioPopover() {
      this.projectIds.clear();
      apiScenarioEnvMap(this.request).then(res => {
        let data = res.data;
        this.projectEnvListMap = data;
        if (data) {
          for (let d in data) {
            this.projectIds.add(d);
          }
        }
       this.$refs.envSelectPopover.open();
      });
    },
  },
};
</script>

<style scoped>
.env-container .title {
  width: 100px;
  min-width: 100px;
  text-align: right;
}

.env-container .content {
  width: 163px;
}

:deep(.content .el-popover__reference) {
  width: 100%;
}

.mode-row {
  margin-top: 8px;
}

.ms-mode-span-label:before {
  content: '*';
  color: #F56C6C;
}

</style>
<style  lang="scss" scoped>
.radio-change:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #606266 !important;
}

</style>
