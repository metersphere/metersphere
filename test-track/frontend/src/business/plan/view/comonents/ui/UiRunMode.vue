<template>
  <el-dialog
    destroy-on-close
    :title="$t('load_test.runtime_config')"
    width="550px"
    @close="close"
    :visible.sync="runModeVisible"
  >
    <div class="mode-container">

      <div>
        <div>{{ $t("commons.environment") }}：</div>
        <env-select-popover :project-ids="projectIds"
                            :project-list="projectList"
                            :project-env-map="projectEnvListMap"
                            :environment-type="'JSON'"
                            :has-option-group="false"
                            :show-env-group="false"
                            :group-id="runConfig.environmentGroupId"
                            @setProjectEnvMap="setProjectEnvMap"
                            ref="envSelectPopover"
                            class="mode-row"
        ></env-select-popover>
      </div>

      <!-- 浏览器 -->
      <div class="browser-row wrap">
        <div class="title">{{ $t("ui.browser") }}：</div>
        <div class="content">
          <el-select
            size="mini"
            v-model="runConfig.browser"
            style="margin-right: 30px; width: 163px"
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

      <!-- 运行模式 -->
      <div class="run-mode-row wrap" v-if="customRunMode">
        <div class="title">{{ $t("run_mode.title") }}：</div>
        <div class="content">
          <el-radio-group v-model="runConfig.mode" @change="changeMode">
            <el-radio label="serial">{{ $t("run_mode.serial") }}</el-radio>
            <el-radio label="parallel">{{ $t("run_mode.parallel") }}</el-radio>
          </el-radio-group>
        </div>
      </div>

      <!-- 性能模式 用于测试报告 -->
      <div class="other-config-row wrap" v-if="customSerialOnSampleError">
        <div class="other-title title">{{ $t("run_mode.other_config") }}：</div>
        <div class="other-content">
          <div class="sub-item-row">
            <el-checkbox v-model="runConfig.headlessEnabled">
              {{ $t("ui.performance_mode") }}
            </el-checkbox>
          </div>

          <div class="mode-row row-padding">
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

          <div
            class="sub-item-row row-padding"
            v-if="runConfig.mode == 'serial'"
          >
            <el-checkbox v-model="runConfig.onSampleError">
              {{ $t("api_test.fail_to_stop") }}
            </el-checkbox>
          </div>
        </div>
      </div>

      <!-- 其他配置 -->
      <div
        class="other-config-row wrap"
        v-if="customReportType && !customSerialOnSampleError"
      >
        <div class="other-title title">{{ $t("run_mode.other_config") }}：</div>
        <div class="other-content">
          <div class="sub-item-row">
            <el-radio-group v-model="runConfig.reportType">
              <el-radio label="iddReport">{{
                  $t("run_mode.idd_report")
                }}
              </el-radio>
              <el-radio label="setReport">{{
                  $t("run_mode.set_report")
                }}
              </el-radio>
            </el-radio-group>
          </div>
          <div class="sub-item-row row-padding">
            <el-checkbox v-model="runConfig.headlessEnabled">
              {{ $t("ui.performance_mode") }}
            </el-checkbox>
          </div>

          <div class="mode-row row-padding">
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

          <div
            class="sub-item-row row-padding"
            v-if="runConfig.mode == 'serial'"
          >
            <el-checkbox v-model="runConfig.onSampleError">
              {{ $t("api_test.fail_to_stop") }}
            </el-checkbox>
          </div>
        </div>
      </div>

      <!-- 报告名称 -->
      <div
        class="report-name-row wrap"
        v-if="runConfig.reportType === 'setReport'"
      >
        <div class="title">{{ $t("run_mode.report_name") }}：</div>
        <div class="content">
          <el-input
            v-model="runConfig.reportName"
            :placeholder="$t('commons.input_content')"
            size="small"
            style="width: 300px"
          />
        </div>
      </div>
    </div>

    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleRunBatch"/>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from 'metersphere-frontend/src/components/MsDialogFooter'
import {getCurrentProjectID, getOwnerProjects, strMapToObj} from "@/business/utils/sdk-utils";
import EnvSelectPopover from "@/business/plan/env/EnvSelectPopover";
import {testPlanUiScenarioCaseEnv} from "@/api/remote/ui/test-plan-ui-scenario-case";

export default {
  name: "UiRunMode",
  components: {MsDialogFooter, EnvSelectPopover},
  data() {
    return {
      runModeVisible: false,
      testType: null,
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
      runConfig: {
        reportName: "",
        mode: "serial",
        browser: "CHROME",
        reportType: "iddReport",
        onSampleError: false,
        runWithinResourcePool: false,
        resourcePoolId: null,
        headlessEnabled: true,
        retryEnable: false,
      },
      projectList: [],
      projectIds: new Set(),
      projectEnvListMap: {},
      caseIdEnvNameMap: {},
    };
  },
  props: {
    planCaseIds: {
      type: Array,
    },
    request: {
      type: Object,
    },
    /**
     * 是否允许设置报告类型
     */
    customReportType: {
      type: Boolean,
      default: false,
    },
    /**
     * 是否允许设置串行并行
     */
    customRunMode: {
      type: Boolean,
      default: true,
    },
    /**
     * 是否开启串行失败终止
     */
    customSerialOnSampleError: {
      type: Boolean,
      default: false,
    },
  },

  watch: {
    "runConfig.runWithinResourcePool"() {
      if (!this.runConfig.runWithinResourcePool) {
        this.runConfig = {
          mode: this.runConfig.mode,
          reportType: "iddReport",
          reportName: "",
          runWithinResourcePool: false,
          resourcePoolId: null,
        };
      }
    },
  },
  methods: {
    open() {
      this.runConfig = {
        mode: "serial",
        reportType: "iddReport",
        reportName: "",
        browser: "CHROME",
        runWithinResourcePool: false,
        resourcePoolId: null,
        headlessEnabled: true,
      };
      this.runModeVisible = true;
      this.getWsProjects();
      this.showPopover();
    },
    changeMode() {
      this.runConfig.runWithinResourcePool = false;
      this.runConfig.resourcePoolId = null;
      this.runConfig.reportType = "iddReport";
      this.runConfig.reportName = "";
    },
    close() {
      this.runModeVisible = false;
      this.$emit("close");
    },
    getWsProjects() {
      getOwnerProjects()
        .then((res) => {
        this.projectList = res.data;
      });
    },
    handleRunBatch() {
      if (
        this.runConfig.reportType === "setReport" &&
        !this.runConfig.reportName.trim()
      ) {
        this.$warning(this.$t("commons.input_name"));
        return;
      }

      //并行不支持失败终止
      if (this.runConfig.mode === "parallel") {
        this.runConfig.onSampleError = false;
      }

      this.$emit("handleRunBatch", this.runConfig);
      this.close();
    },
    setProjectEnvMap(projectEnvMap) {
      this.runConfig.envMap = strMapToObj(projectEnvMap);
    },
    showPopover() {
      this.showScenarioPopover();
    },
    showScenarioPopover() {
      let currentProjectID = getCurrentProjectID();
      this.projectIds.clear();
      testPlanUiScenarioCaseEnv(this.request.ids).then((res) => {
        let data = res.data;
        this.projectEnvListMap = data;
        if (data) {
          for (let d in data) {
            this.projectIds.add(d);
          }
        }
        if (this.projectIds.size === 0) {
          this.projectIds.add(currentProjectID);
        }
        this.$refs.envSelectPopover.open();
      });
    },
  },
};
</script>

<style scoped>
.mode-container .title {
  width: 100px;
  min-width: 100px;
  text-align: right;
  padding-right: 10px;
}

.mode-container .content {
  width: 163px;
}

.wrap {
  display: flex;
  align-items: center;
  padding: 10px 15px 10px 15px;
}

.other-title {
  height: 100%;
  align-items: flex-start;
}

.other-content {
  height: 100%;
}

.sub-item-row {
  width: 200px;
}

.row-padding {
  margin-top: 15px;
}

.other-config-row {
  height: 100px;
}
</style>
