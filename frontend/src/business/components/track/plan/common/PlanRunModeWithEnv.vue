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
                   ref="envPopover" class="env-popover"/>
    </div>
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
            <el-checkbox v-model="runConfig.onSampleError">{{ $t("api_test.fail_to_stop") }}</el-checkbox>
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
      <div class="dialog-footer">
        <el-button @click="close" >{{$t('commons.cancel')}}</el-button>
        <el-dropdown @command="handleCommand"  style="margin-left: 5px">
          <el-button type="primary" >
            {{$t('load_test.save_and_run')}}<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="run">{{$t('load_test.save_and_run')}}</el-dropdown-item>
            <el-dropdown-item command="save">{{$t('commons.save')}}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import EnvPopover from "@/business/components/api/automation/scenario/EnvPopover";
import {strMapToObj} from "@/common/js/utils";
import {ENV_TYPE} from "@/common/js/constants";

export default {
  name: "MsPlanRunModeWithEnv",
  components: {EnvPopover, MsDialogFooter},
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
        envMap: new Map(),
        environmentGroupId: "",
        environmentType: ENV_TYPE.JSON
      },
      projectEnvListMap: {},
      projectList: [],
      projectIds: new Set(),
      options: [{
        value: 'confirmAndRun',
        label: this.$t('load_test.save_and_run')
      }, {
        value: 'save',
        label: this.$t('commons.save')
      }],
      value: 'confirmAndRun'
    };
  },
  props: ['planCaseIds', 'type', 'planId'],
  methods: {
    open(testType) {
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
        environmentType: ENV_TYPE.JSON
      };
      this.runModeVisible = false;
      this.$emit('close');
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
    setProjectEnvMap(projectEnvMap) {
      this.runConfig.envMap = strMapToObj(projectEnvMap);
    },
    setEnvGroup(id) {
      this.runConfig.environmentGroupId = id;
    },
    getWsProjects() {
      this.$get("/project/getOwnerProjects", res => {
        this.projectList = res.data;
      })
    },
    showPopover() {
      this.projectIds.clear();
      let param = undefined;
      let url = "";
      if (this.type === 'apiCase') {
        url = '/test/plan/api/case/env';
        param = this.planCaseIds;
      } else if (this.type === 'apiScenario') {
        url = '/test/plan/api/scenario/env';
        param = this.planCaseIds;
      } else if (this.type === 'plan') {
        url = '/test/plan/case/env';
        param = {id: this.planId};
      }
      this.$post(url, param, res => {
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
    handleCommand(command){
      if (this.runConfig.runWithinResourcePool && this.runConfig.resourcePoolId == null) {
        this.$warning(this.$t('workspace.env_group.please_select_run_within_resource_pool'));
        return;
      }
      if (command === 'run') {
        this.runConfig.isRun = true
        this.handleRunBatch();
      } else {
        this.runConfig.isRun = false
        this.handleRunBatch();
      }
    }
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
