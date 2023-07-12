<template>
  <el-dialog
    destroy-on-close
    :title="$t('load_test.runtime_config')"
    width="550px"
    @close="close"
    :visible.sync="runModeVisible"
  >
    <div style="margin-bottom: 10px">
      <div>{{ $t("commons.environment") }}：</div>
      <env-select-popover
        :project-ids="projectIds"
        :project-list="projectList"
        :case-id-env-name-map="caseIdEnvNameMap"
        :environment-type.sync="runConfig.environmentType"
        :is-scenario="false"
        :has-option-group="true"
        :project-env-map="projectEnvListMap"
        :group-id="runConfig.environmentGroupId"
        @setProjectEnvMap="setProjectEnvMap"
        @setEnvGroup="setEnvGroup"
        ref="envSelectPopover"
        class="env-select-popover"
      ></env-select-popover>
    </div>
    <div>
      <div class="mode-row">{{ $t("run_mode.title") }}：</div>
      <div>
        <el-radio-group v-model="runConfig.mode" @change="changeMode">
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
      <div class="mode-row">
        <span>{{ $t("run_mode.run_with_resource_pool") }}</span>
        <el-select
          :disabled="!runConfig.runWithinResourcePool"
          v-model="runConfig.resourcePoolId"
          size="mini"
          class="mode-row"
          style="width: 100%"
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
    </div>


    <div
      class="ms-mode-span-label"
      style="margin-top: 8px"
      v-if="runConfig.reportType === 'setReport'"
    >
      {{ $t("run_mode.report_name") }}：
    </div>
    <div class="mode-row" v-if="runConfig.reportType === 'setReport'">
      <el-input
        v-model="runConfig.reportName"
        :placeholder="$t('commons.input_content')"
        size="small"
        style="width: 100%"
      />
    </div>

     <!--- 失败停止 -->
    <div class="mode-row" v-if="runConfig.mode === 'serial'">
      <el-checkbox v-model="runConfig.onSampleError">
        {{ $t("api_test.fail_to_stop") }}
      </el-checkbox>
    </div>
    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleRunBatch" />
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import EnvSelectPopover from "../environment/EnvSelectPopover";
import { strMapToObj } from "metersphere-frontend/src/utils";
import { ENV_TYPE } from "metersphere-frontend/src/utils/constants";
import { parseEnvironment } from "metersphere-frontend/src/model/EnvironmentModel";
import { getOwnerProjects, getProjectConfig } from "@/api/project";
import { getTestResourcePools } from "@/api/test-resource-pool";
import { getEnvironmentByProjectId } from "metersphere-frontend/src/api/environment";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";
import { getApiCaseEnvironments } from "@/api/api";

export default {
  name: "MsApiCaseRunModeWithEnv",
  components: { EnvSelectPopover, MsDialogFooter },
  data() {
    return {
      runModeVisible: false,
      resourcePools: [],
      runConfig: {
        reportName: "",
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        runWithinResourcePool: true,
        resourcePoolId: null,
        envMap: new Map(),
        environmentGroupId: "",
        environmentType: ENV_TYPE.JSON,
      },
      projectEnvListMap: {},
      projectList: [],
      projectIds: new Set(),
      caseIdEnvNameMap: {},
    };
  },
  props: {
    runCaseIds: Array,
    projectId: String,
  },
  methods: {
    open() {
      this.runModeVisible = true;
      this.getResourcePools();
      this.getWsProjects();
      this.getDefaultResourcePool();
      this.showPopover();
    },
    getDefaultResourcePool() {
      getProjectConfig(getCurrentProjectID()).then((res) => {
        if (res.data && res.data.poolEnable && res.data.resourcePoolId) {
          this.runConfig.resourcePoolId = res.data.resourcePoolId;
        }
      });
    },
    changeMode() {
      this.runConfig.onSampleError = false;
      this.runConfig.runWithinResourcePool = true;
      // this.runConfig.resourcePoolId = null;
      this.runConfig.reportName = "";
    },
    close() {
      this.runConfig = {
        mode: "serial",
        reportType: "iddReport",
        onSampleError: false,
        reportName: "",
        runWithinResourcePool: true,
        resourcePoolId: null,
        envMap: new Map(),
        environmentGroupId: "",
        environmentType: ENV_TYPE.JSON,
      };
      this.runModeVisible = false;
      this.$emit("close");
    },
    handleRunBatch() {
      if (
        (this.runConfig.mode === "serial" ||
          this.runConfig.mode === "parallel") &&
        this.runConfig.reportType === "setReport" &&
        this.runConfig.reportName.trim() === ""
      ) {
        this.$warning(this.$t("commons.input_name"));
        return;
      }
      if (
        this.runConfig.runWithinResourcePool &&
        this.runConfig.resourcePoolId == null
      ) {
        this.$warning(
          this.$t("workspace.env_group.please_select_run_within_resource_pool")
        );
        return;
      }
      this.$emit("handleRunBatch", this.runConfig);
      this.close();
    },
    getResourcePools() {
      this.result = getTestResourcePools().then((response) => {
        this.resourcePools = response.data;
      });
    },
    setProjectEnvMap(projectEnvMap) {
      this.runConfig.envMap = strMapToObj(projectEnvMap);
    },
    setEnvGroup(id) {
      this.runConfig.environmentGroupId = id;
    },
    getWsProjects() {
      getOwnerProjects().then((res) => {
        this.projectList = res.data;
      });
    },
    getEnvironments() {
      return new Promise((resolve) => {
        if (this.projectId) {
          getEnvironmentByProjectId(this.projectId).then((response) => {
            this.environments = response.data;
            this.environments.forEach((environment) => {
              parseEnvironment(environment);
            });
            resolve();
          });
        }
      });
    },
    showPopover() {
      let currentProjectID = getCurrentProjectID();
      this.projectIds.clear();
      this.projectIds.add(currentProjectID);
      getApiCaseEnvironments(this.runCaseIds).then((res) => {
        let data = res.data;
        if (data) {
          this.caseIdEnvNameMap = data;
        }
        this.$refs.envSelectPopover.open();
      });
    },
  },
};
</script>

<style scoped>
.mode-row-span {
  margin-right: 10px;
}

.mode-row-div {
  margin-top: 20px;
}

.mode-row-span {
  margin-right: 10px;
  margin-left: 10px;
}

.mode-row-span-label:before {
  content: "*";
  color: #f56c6c;
  margin-right: 4px;
  margin-left: 10px;
}
.mode-row {
  margin-top: 8px;
}
.ms-mode-span-label:before {
  content: '*';
  color: #f56c6c;
}
</style>
