<template>
  <el-dialog
    destroy-on-close
    :title="$t('load_test.runtime_config')"
    width="550px"
    @close="close"
    :visible.sync="runModeVisible">
    <div class="env-container">
      <div>
        <div>{{ $t('commons.environment') }}：</div>
        <env-select-popover
          :project-ids="projectIds"
          :project-list="projectList"
          :case-id-env-name-map="caseIdEnvNameMap"
          :environment-type.sync="runConfig.environmentType"
          :is-scenario="isScenario"
          :has-option-group="true"
          :project-env-map="projectEnvListMap"
          :group-id="runConfig.environmentGroupId"
          @setProjectEnvMap="setProjectEnvMap"
          @setEnvGroup="setEnvGroup"
          ref="envSelectPopover"
          class="mode-row"></env-select-popover>
      </div>

      <div>
        <div class="mode-row">{{ $t('run_mode.title') }}：</div>
        <div>
          <el-radio-group
            v-model="runConfig.mode"
            @change="changeMode"
            style="width: 100%"
            class="radio-change mode-row">
            <el-radio label="serial">{{ $t('run_mode.serial') }}</el-radio>
            <el-radio label="parallel">{{ $t('run_mode.parallel') }}</el-radio>
          </el-radio-group>
        </div>
      </div>
      <!--  资源池    -->
      <div>
        <div class="mode-row">{{ $t('run_mode.other_config') }}：</div>
        <div class="mode-row">
          <el-radio-group v-model="runConfig.reportType">
            <el-radio label="iddReport">{{ $t('run_mode.idd_report') }}</el-radio>
            <el-radio label="setReport">{{ $t('run_mode.set_report') }}</el-radio>
          </el-radio-group>
        </div>
        <div class="ms-mode-span-label" style="margin-top: 8px" v-if="runConfig.reportType === 'setReport'">
          {{ $t('run_mode.report_name') }}：
        </div>
        <div class="mode-row" v-if="runConfig.reportType === 'setReport'">
          <el-input
            v-model="runConfig.reportName"
            :placeholder="$t('commons.input_content')"
            size="small"
            style="width: 100%" />
        </div>
        <div class="mode-row">
          <span>{{ $t('run_mode.run_with_resource_pool') }} : </span>
          <el-select v-model="runConfig.resourcePoolId" size="mini" class="mode-row" style="width: 100%">
            <el-option
              v-for="item in resourcePools"
              :key="item.id"
              :label="item.name"
              :disabled="!item.api"
              :value="item.id">
              <template v-slot>
                <node-operation-label
                    :nodeName="item.name"
                    :node-operation-info="nodeInfo(item.id)"/>
              </template>
            </el-option>
          </el-select>
        </div>
        <!-- 失败停止 -->
        <div class="mode-row" v-if="runConfig.mode === 'serial'">
          <el-checkbox v-model="runConfig.onSampleError" class="radio-change"
            >{{ $t('api_test.fail_to_stop') }}
          </el-checkbox>
        </div>
      </div>
    </div>

    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="handleRunBatch" />
    </template>
  </el-dialog>
</template>

<script>
import {apiScenarioEnvMap} from '@/api/scenario';
import MsDialogFooter from 'metersphere-frontend/src/components/MsDialogFooter';
import {ENV_TYPE} from 'metersphere-frontend/src/utils/constants';
import {strMapToObj} from 'metersphere-frontend/src/utils';
import {getOwnerProjects, getProjectConfig} from '@/api/project';
import {getNodeOperationInfo, getTestResourcePools} from '@/api/test-resource-pool';
import {getCurrentProjectID} from 'metersphere-frontend/src/utils/token';
import EnvSelectPopover from '@/business/automation/scenario/EnvSelectPopover';
import {getApiCaseEnvironments} from '@/api/api-test-case';
import NodeOperationLabel from "metersphere-frontend/src/components/resource-pool/NodeOperationLabel";

export default {
  name: 'ApiRunMode',
  components: {MsDialogFooter, EnvSelectPopover, NodeOperationLabel},
  data() {
    return {
      loading: false,
      runModeVisible: false,
      testType: null,
      resourcePools: [],
      runConfig: {
        reportName: '',
        mode: 'serial',
        reportType: 'iddReport',
        onSampleError: false,
        resourcePoolId: null,
        envMap: new Map(),
        environmentGroupId: '',
        environmentType: ENV_TYPE.JSON,
      },
      projectEnvListMap: {},
      projectList: [],
      projectIds: new Set(),
      caseIdEnvNameMap: {},
      nodeOperationInfo: {},
    };
  },
  props: {
    runCaseIds: Array,
    request: Object,
    isScenario: {
      type: Boolean,
      default: true,
    },
  },
  methods: {
    nodeInfo(nodeId) {
      return this.nodeOperationInfo[nodeId];
    },
    selectNodeOperation() {
      let nodeOperationInfoRequest = {nodeIds: []};
      this.resourcePools.forEach(item => {
        nodeOperationInfoRequest.nodeIds.push(item.id);
      });

      getNodeOperationInfo(nodeOperationInfoRequest)
          .then(response => {
            this.parseNodeOperationStatus(response.data);
          });
    },
    parseNodeOperationStatus(nodeOperationData) {
      this.nodeOperationInfo = {};
      nodeOperationData.forEach(item => {
        this.nodeOperationInfo[item.id] = item;
      });
    },
    open() {
      this.runModeVisible = true;
      this.getResourcePools();
      this.getWsProjects();
      this.showPopover();
      this.runConfig.environmentType = ENV_TYPE.JSON;
    },
    changeMode() {
      this.runConfig.reportType = 'iddReport';
      this.runConfig.reportName = '';
    },
    close() {
      this.runConfig = {
        mode: 'serial',
        reportType: 'iddReport',
        reportName: '',
        environmentType: ENV_TYPE.JSON,
        resourcePoolId: null,
      };
      this.runModeVisible = false;
      this.$emit('close');
    },
    getWsProjects() {
      getOwnerProjects().then((res) => {
        this.projectList = res.data;
      });
    },
    handleRunBatch() {
      if (
        (this.runConfig.mode === 'serial' || this.runConfig.mode === 'parallel') &&
        this.runConfig.reportType === 'setReport' &&
        this.runConfig.reportName.trim() === ''
      ) {
        this.$warning(this.$t('commons.input_name'));
        return;
      }
      if (this.runConfig.resourcePoolId == null) {
        this.$warning(this.$t('workspace.env_group.please_select_run_within_resource_pool'));
        return;
      }
      this.$emit('handleRunBatch', this.runConfig);
      this.close();
    },
    getResourcePools() {
      this.result = getTestResourcePools().then((response) => {
        this.resourcePools = response.data;
        this.selectNodeOperation();
        this.getProjectApplication();
      });
    },
    getProjectApplication() {
      this.runConfig.resourcePoolId = null;
      getProjectConfig(getCurrentProjectID(), '').then((res) => {
        if (res.data && res.data.poolEnable && res.data.resourcePoolId) {
          this.runConfig.resourcePoolId = res.data.resourcePoolId;
        }
        let hasPool = false;
        this.resourcePools.forEach((item) => {
          if (item.id === this.runConfig.resourcePoolId) {
            hasPool = true;
            return;
          }
        });
        if (!hasPool) {
          this.runConfig.resourcePoolId = null;
        }
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
      this.projectIds.add(currentProjectID);
      getApiCaseEnvironments(this.runCaseIds).then((res) => {
        let data = res.data;
        if (data) {
          this.caseIdEnvNameMap = data;
        }
        this.$refs.envSelectPopover.open();
      });
    },
    showScenarioPopover() {
      let currentProjectID = getCurrentProjectID();
      this.projectIds.clear();
      apiScenarioEnvMap(this.request).then((res) => {
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
  color: #f56c6c;
}
</style>
<style lang="scss" scoped>
.radio-change:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #606266 !important;
}
</style>
