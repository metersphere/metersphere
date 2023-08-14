<template>
  <el-dialog
      destroy-on-close
      :title="$t('load_test.runtime_config')"
      width="550px"
      style="margin-top: -8.65vh; max-height: 87.3vh"
      @close="close"
      :visible.sync="runModeVisible"
  >

    <div class="env-container">
      <div>
        <div>{{ $t("commons.environment") }}：</div>
        <env-select-popover
            :project-ids="projectIds"
            :project-list="projectList"
            :project-env-map="projectEnvListMap"
            :environment-type.sync="runConfig.environmentType"
            :has-option-group="true"
            :is-env-saved="isEnvSaved"
            :group-id="runConfig.environmentGroupId"
            @setProjectEnvMap="setProjectEnvMap"
            @setDefaultEnv="setDefaultEnv"
            @setEnvGroup="setEnvGroup"
            ref="envSelectPopover"
            class="mode-row"
        ></env-select-popover>
      </div>
      <div v-if="haveUICase">
        <div>{{ $t("ui.browser") }}：</div>
        <div>
          <el-select
              size="mini"
              v-model="runConfig.browser"
              style="width: 100%"
              class="mode-row"
          >
            <el-option
                v-for="b in browsers"
                :key="b.value"
                :value="b.value"
                :label="b.label"
            ></el-option>
          </el-select>
        </div>
      </div>
      <div>
        <div class="mode-row">{{ $t("run_mode.title") }}：</div>
        <div>
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
      <div>
        <div class="mode-row">{{ $t("run_mode.other_config") }}：</div>
        <div>
          <!-- 资源池 -->
          <div
              class="mode-row"
              v-if="
              testType === 'API' &&
              (haveOtherExecCase)
            "
          >
            <span>{{ $t("run_mode.run_with_resource_pool") }}: </span>
            <el-select
                v-model="runConfig.resourcePoolId"
                size="mini"
                style="width: 100%; margin-top: 8px"
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

          <!-- 失败重试 -->
          <div class="mode-row">
            <el-checkbox
                v-model="runConfig.retryEnable"
                class="radio-change ms-failure-div-right"
            >
              {{ $t("run_mode.retry_on_failure") }}
            </el-checkbox>
            <span v-if="runConfig.retryEnable">
              <el-tooltip placement="top" style="margin: 0 4px 0 2px">
                <div slot="content">{{ $t("run_mode.retry_message") }}</div>
                <i
                    class="el-icon-question"
                    style="cursor: pointer"
                /> </el-tooltip
              ><br/>
              <span>
                {{ $t("run_mode.retry") }}
                <el-input-number
                    :value="runConfig.retryNum"
                    v-model="runConfig.retryNum"
                    :min="1"
                    :max="10000000"
                    size="mini"
                    style="width: 103px; margin-top: 8px"
                />
                &nbsp;
                {{ $t("run_mode.retry_frequency") }}
              </span>
            </span>
          </div>

          <div class="mode-row" v-if="runConfig.mode === 'serial'">
            <el-checkbox v-model="runConfig.onSampleError" class="radio-change"
            >{{ $t("api_test.fail_to_stop") }}
            </el-checkbox>
          </div>

          <div class="mode-row" v-if="haveUICase">
            <el-checkbox
                v-model="runConfig.headlessEnabled"
                class="radio-change"
            >
              {{ $t("ui.performance_mode") }}
            </el-checkbox>
          </div>
        </div>
      </div>
    </div>

    <template v-slot:footer>
      <div class="dialog-footer" v-if="showSave">
        <el-button @click="close">{{ $t("commons.cancel") }}</el-button>
        <el-dropdown @command="handleCommand" style="margin-left: 5px">
          <el-button type="primary">
            {{
              $t("api_test.run")
            }}<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="run"
            >{{ $t("api_test.run") }}
            </el-dropdown-item>
            <el-dropdown-item command="runAndSave"
            >{{ $t("load_test.save_and_run") }}
            </el-dropdown-item>
            <el-dropdown-item command="save"
            >{{ $t("commons.save") }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <ms-dialog-footer v-else @cancel="close" @confirm="handleRunBatch"/>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {strMapToObj} from "metersphere-frontend/src/utils";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import {getCurrentProjectID, getOwnerProjects,} from "@/business/utils/sdk-utils";
import {getQuotaValidResourcePools} from "@/api/remote/resource-pool";
import EnvGroupPopover from "@/business/plan/env/EnvGroupPopover";
import {getApiCaseEnv} from "@/api/remote/plan/test-plan-api-case";
import {
  getApiScenarioEnv,
  getPlanCaseEnv,
  getPlanCaseProjectIds,
  getProjectIdsByPlanIdAndCaseType,
} from "@/api/remote/plan/test-plan";
import EnvGroupWithOption from "../env/EnvGroupWithOption";
import EnvironmentGroup from "@/business/plan/env/EnvironmentGroupList";
import EnvSelectPopover from "@/business/plan/env/EnvSelectPopover";
import {getProjectConfig} from "@/api/project";

export default {
  name: "MsTestPlanRunModeWithEnv",
  components: {
    EnvGroupPopover,
    MsDialogFooter,
    MsTag,
    EnvGroupWithOption,
    EnvironmentGroup,
    EnvSelectPopover,
  },
  computed: {
    ENV_TYPE() {
      return ENV_TYPE;
    },
  },
  data() {
    return {
      btnStyle: {
        width: "260px",
      },
      result: {loading: false},
      runModeVisible: false,
      testType: null,
      resourcePools: [],
      projectEnvListMap: {},
      defaultEnvMap: {},
      runConfig: {
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        resourcePoolId: null,
        envMap: new Map(),
        environmentGroupId: "",
        environmentType: ENV_TYPE.JSON,
        retryEnable: false,
        retryNum: 1,
        browser: "CHROME",
        testPlanDefaultEnvMap: {},
      },
      projectList: [],
      projectIds: new Set(),
      //环境是否保存过。未保存过的话展示的是”用例环境“
      isEnvSaved: true,
      options: [
        {
          value: "confirmAndRun",
          label: this.$t("load_test.save_and_run"),
        },
        {
          value: "save",
          label: this.$t("commons.save"),
        },
      ],
      value: "confirmAndRun",
      browsers: [
        {
          label: this.$t("chrome"),
          value: "CHROME",
        },
        {
          label: this.$t("firefox"),
          value: "FIREFOX",
        },
      ],
    };
  },
  props: {
    planCaseIds: {
      type: Array,
    },
    type: String,
    planId: String,
    showSave: {
      type: Boolean,
      default: false,
    },
    //是否含有ui场景 有 ui 场景就要展示 浏览器选项，性能模式
    haveUICase: {
      type: Boolean,
      default: false,
    },
    //是否有其他用例（性能测试除外）
    haveOtherExecCase: {
      type: Boolean,
      default: true,
    },
  },
  methods: {
    open(testType, runModeConfig) {
      this.defaultEnvMap = {};
      if (this.type === 'plan') {
        if (runModeConfig) {
          this.runConfig = JSON.parse(runModeConfig);
          if (!this.runConfig.envMap || JSON.stringify(this.runConfig.envMap) === "{}") {
            this.isEnvSaved = false;
          } else {
            this.isEnvSaved = true;
          }
          this.runConfig.envMap = new Map();
          this.runConfig.testPlanDefaultEnvMap = {};
          this.runConfig.onSampleError =
              this.runConfig.onSampleError === "true" ||
              this.runConfig.onSampleError === true;
        } else {
          this.isEnvSaved = false;
        }
      }
      this.runConfig.environmentType = ENV_TYPE.JSON;
      this.runModeVisible = true;
      this.testType = testType;
      this.getResourcePools();
      this.getWsProjects();
      this.showPopover();
    },
    getProjectApplication() {
      getProjectConfig(getCurrentProjectID(), "").then((res) => {
        let hasPool = false;
        //判断之前配置的资源池是否在可用资源池中
        this.resourcePools.forEach((item) => {
          if (item.id === this.runConfig.resourcePoolId) {
            hasPool = true;
          }
        });
        if (!hasPool) {
          if (res.data && res.data.poolEnable && res.data.resourcePoolId) {
            //判断系统配置的默认资源池是否可用
            this.resourcePools.forEach((item) => {
              if (item.id === res.data.resourcePoolId) {
                hasPool = true;
              }
            });
            if (hasPool) {
              this.runConfig.resourcePoolId = res.data.resourcePoolId;
            }
          }
        }
        if (!hasPool) {
          this.runConfig.resourcePoolId = null;
        }
      });
    },
    changeMode() {
      this.runConfig.onSampleError = false;
    },
    close() {
      this.runConfig = {
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        resourcePoolId: null,
        envMap: new Map(),
        environmentGroupId: "",
        environmentType: ENV_TYPE.JSON,
        browser: "CHROME",
      };
      this.runModeVisible = false;
      this.$emit("close");
    },
    handleRunBatch() {
      if (this.runConfig.resourcePoolId == null && this.haveOtherExecCase) {
        this.$warning(
            this.$t("workspace.env_group.please_select_run_within_resource_pool"));
        return;
      }
      this.runConfig.testPlanDefaultEnvMap = this.defaultEnvMap;
      this.$emit("handleRunBatch", this.runConfig);
      this.close();
    },
    getResourcePools() {
      getQuotaValidResourcePools().then((response) => {
        this.resourcePools = response.data;
        this.getProjectApplication();
      });
    },
    setDefaultEnv(projectId, envId) {
      let ids = this.defaultEnvMap[projectId];
      if (!ids) {
        ids = [];
      }
      ids.push(envId);
      this.defaultEnvMap[projectId] = ids;
    },
    setProjectEnvMap(projectEnvMap) {
      this.runConfig.envMap = projectEnvMap;
    },
    setEnvGroup(id) {
      this.runConfig.environmentGroupId = id;
    },
    getWsProjects() {
      getOwnerProjects().then((res) => {
        this.projectList = res.data;
      });
    },
    showPopover() {
      this.projectIds.clear();
      let param = undefined;
      if (this.type === "apiCase") {
        param = this.planCaseIds;
        getApiCaseEnv(param).then((res) => {
          let data = res.data;
          if (data) {
            this.projectEnvListMap = data;
          }
          getProjectIdsByPlanIdAndCaseType(this.planId, 'apiCase').then((res) => {
            let data = res.data;
            if (data) {
              for (let i = 0; i < data.length; i++) {
                this.projectIds.add(data[i]);
              }
            }
          this.$refs.envSelectPopover.open();
          });
        });
      } else if (this.type === "apiScenario") {
        param = this.planCaseIds;
        getApiScenarioEnv(param).then((res) => {
          let data = res.data;
          if (data) {
            this.projectEnvListMap = data;
          }
          getProjectIdsByPlanIdAndCaseType(this.planId, 'apiScenario').then((res) => {
            let data = res.data;
            if (data) {
              for (let i = 0; i < data.length; i++) {
                this.projectIds.add(data[i]);
              }
            }
            this.$refs.envSelectPopover.open();
          });
        });
      } else if (this.type === "plan") {
        param = {id: this.planId};
        getPlanCaseEnv(param).then((res) => {
          let data = res.data;
          if (data) {
            this.projectEnvListMap = data;
          }
          param = {id: this.planId};
          getPlanCaseProjectIds(param).then((res) => {
            let data = res.data;
            if (data) {
              for (let i = 0; i < data.length; i++) {
                this.projectIds.add(data[i]);
              }
            }
            this.$refs.envSelectPopover.open();
          });
        });
      }
    },
    handleCommand(command) {
      if (
          this.runConfig.resourcePoolId == null &&
          this.haveOtherExecCase
      ) {
        this.$warning(
            this.$t("workspace.env_group.please_select_run_within_resource_pool")
        );
        return;
      }
      this.runConfig.envMap = strMapToObj(this.runConfig.envMap);
      if (command === "runAndSave") {
        this.runConfig.executionWay = "runAndSave";
      } else if (command === "save") {
        this.runConfig.executionWay = "save";
      } else {
        this.runConfig.executionWay = "run";
      }
      this.handleRunBatch();
    },
  },
};
</script>

<style scoped>
.env-container {
  max-height: 400px;
  overflow-y: auto;
  padding-bottom: 1px;
}

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
</style>
<style lang="scss" scoped>
.radio-change:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #606266 !important;
}

.radio-change:deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #606266 !important;
}
</style>
