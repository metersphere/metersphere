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
                   @setEnvGroup="setEnvGroup"
                   @setProjectEnvMap="setProjectEnvMap"
                   @showPopover="showPopover"
                   :show-env-group="false"
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
            <span style="padding-right: 10px;">{{ $t('run_mode.run_with_resource_pool') }}</span>
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
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import EnvPopover from "@/business/module/environment/EnvPopover";
import {strMapToObj} from "metersphere-frontend/src/utils";
import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import {parseEnvironment} from "metersphere-frontend/src/model/EnvironmentModel";
import { getOwnerProjects, getProjectConfig} from "@/api/project";
import {getTestResourcePools} from "@/api/test-resource-pool";
import {getEnvironmentByProjectId} from "metersphere-frontend/src/api/environment";
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';

export default {
  name: "MsApiCaseRunModeWithEnv",
  components: {EnvPopover, MsDialogFooter},
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
        environmentType: ENV_TYPE.JSON
      },
      projectEnvListMap: {},
      projectList: [],
      projectIds: new Set(),
    };
  },
  props: ['projectId'],
  methods: {
    open() {
      this.runModeVisible = true;
      this.getResourcePools();
      this.getWsProjects();
      this.getDefaultResourcePool();
    },
    getDefaultResourcePool() {
      getProjectConfig(getCurrentProjectID())
        .then((res) => {
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
        environmentType: ENV_TYPE.JSON
      };
      this.runModeVisible = false;
      this.$emit('close');
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
    setProjectEnvMap(projectEnvMap) {
      this.runConfig.envMap = strMapToObj(projectEnvMap);
    },
    setEnvGroup(id) {
      this.runConfig.environmentGroupId = id;
    },
    getWsProjects() {
      getOwnerProjects().then(res => {
        this.projectList = res.data;
      })
    },
    getEnvironments() {
      return new Promise((resolve) => {
        if (this.projectId) {
          getEnvironmentByProjectId(this.projectId).then(response => {
            this.environments = response.data;
            this.environments.forEach(environment => {
              parseEnvironment(environment);
            });
            resolve();
          });
        }
      })
    },
    showPopover() {
      this.projectIds.clear();
      this.projectIds.add(this.projectId);
      this.$refs.envPopover.openEnvSelect();
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

.ms-mode-span {
  margin-right: 10px;
  margin-left: 10px;
}

.ms-mode-span-label:before {
  content: '*';
  color: #F56C6C;
  margin-right: 4px;
  margin-left: 10px;
}

</style>
