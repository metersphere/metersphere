<template>
  <el-dialog
    destroy-on-close
    :title="$t('load_test.runtime_config')"
    width="550px"
    @close="close"
    :visible.sync="runModeVisible"
  >
    <div class="env-container">
      <div class="run-env-row wrap">
        <div class="title">{{ $t("commons.environment") }}：</div>
        <div class="content">
          <env-popover
            :project-ids="projectIds"
            :placement="'bottom-start'"
            :project-list="projectList"
            :project-env-map="projectEnvListMap"
            :environment-type.sync="runConfig.environmentType"
            :group-id="runConfig.environmentGroupId"
            @setEnvGroup="setEnvGroup"
            @setProjectEnvMap="setProjectEnvMap"
            @showPopover="showPopover"
            width="300"
            ref="envPopover"
            :btnStyle="btnStyle"
            class="env-popover"
          />
        </div>
      </div>
      <div class="browser-row wrap" v-if="haveUICase">
        <div class="title">{{ $t("ui.browser") }}：</div>
        <div class="content">
          <el-select
            size="mini"
            v-model="runConfig.browser"
            style="margin-right: 30px; width: 100%"
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
      <div class="mode-row wrap">
        <div class="title">{{ $t("run_mode.title") }}：</div>
        <div class="content">
          <el-radio-group
            v-model="runConfig.mode"
            @change="changeMode"
            style="width: 100%"
          >
            <el-radio label="serial">{{ $t("run_mode.serial") }}</el-radio>
            <el-radio label="parallel">{{ $t("run_mode.parallel") }}</el-radio>
          </el-radio-group>
        </div>
      </div>
      <div class="other-row wrap">
        <div class="title other-title">{{ $t("run_mode.other_config") }}：</div>
        <div class="other-content">
          <!-- 串行 -->
          <div
            class="mode-row"
            v-if="runConfig.mode === 'serial' && testType === 'API'"
          >
            <el-checkbox
              v-model="runConfig.runWithinResourcePool"
              style="padding-right: 10px"
            >
              {{ $t("run_mode.run_with_resource_pool") }}
            </el-checkbox>
            <el-select
              :disabled="!runConfig.runWithinResourcePool"
              v-model="runConfig.resourcePoolId"
              size="mini"
              style="width: 135px; margin-left: 10px"
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
            >
              {{ $t("run_mode.run_with_resource_pool") }}
            </el-checkbox>
            <el-select
              :disabled="!runConfig.runWithinResourcePool"
              v-model="runConfig.resourcePoolId"
              size="mini"
              style="width: 135px; margin-left: 10px"
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

          <!-- 失败重试 -->
          <div class="mode-row" v-if="isHasLicense">
            <el-checkbox
              v-model="runConfig.retryEnable"
              class="ms-failure-div-right"
            >
              {{ $t("run_mode.retry_on_failure") }}
            </el-checkbox>
            <span v-if="runConfig.retryEnable">
              <el-tooltip placement="top" style="margin: 0 4px 0 2px">
                <div slot="content">{{ $t("run_mode.retry_message") }}</div>
                <i class="el-icon-question" style="cursor: pointer"/>
              </el-tooltip>
              <span style="margin-left: 10px">
                {{ $t("run_mode.retry") }}
                <el-input-number
                  :value="runConfig.retryNum"
                  v-model="runConfig.retryNum"
                  :min="1"
                  :max="10000000"
                  size="mini"
                  style="width: 103px"
                />
                &nbsp;
                {{ $t("run_mode.retry_frequency") }}
              </span>
            </span>
          </div>

          <div class="mode-row" v-if="runConfig.mode === 'serial'">
            <el-checkbox v-model="runConfig.onSampleError">{{
                $t("api_test.fail_to_stop")
              }}
            </el-checkbox>
          </div>

          <div class="mode-row" v-if="haveUICase">
            <el-checkbox v-model="runConfig.headlessEnabled">
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
              $t("load_test.save_and_run")
            }}<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="run">{{
                $t("load_test.save_and_run")
              }}
            </el-dropdown-item>
            <el-dropdown-item command="save">{{
                $t("commons.save")
              }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <ms-dialog-footer v-else @cancel="close" @confirm="handleRunBatch"/>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import EnvPopover from "@/business/components/api/automation/scenario/EnvPopover";
import {hasLicense, strMapToObj} from "@/common/js/utils";
import {ENV_TYPE} from "@/common/js/constants";

export default {
  name: "MsPlanRunModeWithEnv",
  components: {EnvPopover, MsDialogFooter},
  data() {
    return {
      btnStyle: {
        width: "260px",
      },
      runModeVisible: false,
      testType: null,
      resourcePools: [],
      runConfig: {
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        runWithinResourcePool: false,
        resourcePoolId: null,
        envMap: new Map(),
        environmentGroupId: "",
        environmentType: ENV_TYPE.JSON,
        retryEnable: false,
        retryNum: 1,
        browser: "CHROME",
      },
      isHasLicense: hasLicense(),
      projectEnvListMap: {},
      projectList: [],
      projectIds: new Set(),
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
  },
  methods: {
    open(testType, runModeConfig) {
      if (runModeConfig) {
        this.runConfig = JSON.parse(runModeConfig);
        this.runConfig.onSampleError = this.runConfig.onSampleError === 'true' || this.runConfig.onSampleError === true;
        this.runConfig.runWithinResourcePool = this.runConfig.runWithinResourcePool === 'true' || this.runConfig.runWithinResourcePool === true;
      }
      this.runModeVisible = true;
      this.testType = testType;
      this.getResourcePools();
      this.getWsProjects();
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
        envMap: new Map(),
        environmentGroupId: "",
        environmentType: ENV_TYPE.JSON,
        browser: "CHROME",
      };
      this.runModeVisible = false;
      this.$emit("close");
    },
    handleRunBatch() {
      this.$emit("handleRunBatch", this.runConfig);
      this.close();
    },
    getResourcePools() {
      this.result = this.$get(
        "/testresourcepool/list/quota/valid",
        (response) => {
          this.resourcePools = response.data;
        }
      );
    },
    setProjectEnvMap(projectEnvMap) {
      this.runConfig.envMap = strMapToObj(projectEnvMap);
    },
    setEnvGroup(id) {
      this.runConfig.environmentGroupId = id;
    },
    getWsProjects() {
      this.$get("/project/getOwnerProjects", (res) => {
        this.projectList = res.data;
      });
    },
    showPopover() {
      this.projectIds.clear();
      let param = undefined;
      let url = "";
      if (this.type === "apiCase") {
        url = "/test/plan/api/case/env";
        param = this.planCaseIds;
      } else if (this.type === "apiScenario") {
        url = "/test/plan/api/scenario/env";
        param = this.planCaseIds;
      } else if (this.type === "plan") {
        url = "/test/plan/case/env";
        param = {id: this.planId};
      }
      this.$post(url, param, (res) => {
        let data = res.data;
        if (data) {
          this.projectEnvListMap = data;
          for (let d in data) {
            this.projectIds.add(d);
          }
        }
        this.$refs.envPopover.openEnvSelect();
      });
    },
    handleCommand(command) {
      if (
        this.runConfig.runWithinResourcePool &&
        this.runConfig.resourcePoolId == null
      ) {
        this.$warning(
          this.$t("workspace.env_group.please_select_run_within_resource_pool")
        );
        return;
      }
      if (command === "run") {
        this.runConfig.isRun = true;
        this.handleRunBatch();
      } else {
        this.runConfig.isRun = false;
        this.handleRunBatch();
      }
    },
  },
};
</script>

<style scoped>
.env-container .title {
  width: 100px;
  min-width: 100px;
  text-align: right;
  padding-right: 10px;
}

.env-container .content {
  width: 163px;
}

.wrap {
  display: flex;
  align-items: center;
  padding: 5px 10px 5px 10px;
}

/deep/ .content .el-popover__reference {
  width: 100%;
}

.mode-row {
  margin-top: 10px;
}

.other-title {
  height: 100%;
  margin-top: 25px;
  align-items: flex-start;
}

.other-content {
  height: 100%;
}

.other-row {
  height: 125px;
}
</style>
