<template>
  <div class="ms-header">
    <div class="ms-div" v-loading="loading" @click="showAll">
      <!-- 调试部分 -->
      <el-row class="ms-header-margin">
        <el-col :span="8">
          <el-tooltip placement="top" :content="currentScenario.name">
            <span class="ms-scenario-name">{{ currentScenario.name }}</span>
          </el-tooltip>
        </el-col>
        <el-col :span="8">
          {{ $t('api_test.automation.step_total') }}：{{ scenarioDefinition.length }}
        </el-col>
        <el-col :span="8">
          <el-link class="head" @click="showScenarioParameters">{{ $t('api_test.automation.scenario_total') }}</el-link>
          ：{{ varSize }}
        </el-col>
      </el-row>
    </div>
    <div class="ms-header-right">
      <el-checkbox v-model="cookieShare" @change="setCookieShare">共享cookie</el-checkbox>
      <el-checkbox v-model="sampleError" @change="setOnSampleError" style="margin-right: 10px">{{$t('commons.failure_continues')}}</el-checkbox>
      <env-popover :disabled="scenarioDefinition.length < 1" :isReadOnly="scenarioDefinition.length < 1"
                   :env-map="envMap" :project-ids="projectIds" @setProjectEnvMap="setProjectEnvMap"
                   @showPopover="showPopover" :project-list="projectList" ref="envPopover" class="ms-right"
                   :result="envResult"/>

      <el-dropdown split-button type="primary" @click="runDebug" style="margin-right: 10px" size="mini" @command="handleCommand" v-if="!debug">
        {{ $t('api_test.request.debug') }}
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item>{{ $t('api_test.automation.generate_report') }}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-button icon="el-icon-loading" size="mini" type="primary" :disabled="debug" v-else>执行中</el-button>
      <font-awesome-icon class="ms-alt-ico" :icon="['fa', 'compress-alt']" size="lg" @click="unFullScreen"/>
    </div>
  </div>
</template>

<script>
import {exportPdf, getCurrentProjectID} from "@/common/js/utils";
import html2canvas from 'html2canvas';
import EnvPopover from "../../scenario/EnvPopover";

export default {
  name: "ScenarioHeader",
  components: {EnvPopover},
  props: {
    currentScenario: {}, scenarioDefinition: Array, enableCookieShare: Boolean, onSampleError: Boolean,
    projectEnvMap: Map,
    projectIds: Set,
    projectList: Array,
    isFullUrl: Boolean,
    execDebug: String,
  },
  data() {
    return {
      envMap: new Map,
      loading: false,
      varSize: 0,
      cookieShare: false,
      sampleError: true,
      envResult: {
        loading: false
      },
      debugLoading: false,
      reqTotal: 0,
      reqSuccess: 0,
      reqError: 0,
      reqTotalTime: 0,
      debug: false,
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  watch:{
    execDebug(){
      this.debug = false;
    }
  },
  mounted() {
    this.envMap = this.projectEnvMap;
    this.getVariableSize();
    this.cookieShare = this.enableCookieShare;
    this.sampleError = this.onSampleError;
  },
  methods: {
    handleExport() {
      let name = this.$t('commons.report_statistics.test_case_analysis');
      this.$nextTick(function () {
        setTimeout(() => {
          html2canvas(document.getElementById('reportAnalysis'), {
            scale: 2
          }).then(function (canvas) {
            exportPdf(name, [canvas]);
          });
        }, 1000);
      });
    },
    showAll() {
      this.$emit('showAllBtn');
    },
    close() {
      this.$emit('closePage', this.currentScenario.name);
    },
    unFullScreen() {
      this.$emit('unFullScreen');
    },
    runDebug() {
      this.debug = true;
      this.debugLoading = true;
      this.$emit('runDebug');
    },
    handleCommand() {
      this.debug = false;
      this.debugLoading = false;
      this.$emit('handleCommand');
    },
    setCookieShare() {
      this.$emit('setCookieShare', this.cookieShare);
    },
    setOnSampleError() {
      this.$emit('setSampleError', this.sampleError);
    },
    showScenarioParameters() {
      this.$emit('showScenarioParameters');
    },
    getVariableSize() {
      let size = 0;
      if (this.currentScenario.variables) {
        size += this.currentScenario.variables.length;
      }
      if (this.currentScenario.headers && this.currentScenario.headers.length > 1) {
        size += this.currentScenario.headers.length - 1;
      }
      this.varSize = size;
      this.reload();
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    setProjectEnvMap(projectEnvMap) {
      this.$emit('setProjectEnvMap', projectEnvMap);
      this.envMap = projectEnvMap;
    },
    showPopover() {
      let definition = JSON.parse(JSON.stringify(this.currentScenario));
      definition.hashTree = this.scenarioDefinition;
      this.envResult.loading = true;
      this.getEnv(JSON.stringify(definition)).then(() => {
        this.$refs.envPopover.openEnvSelect();
        this.envResult.loading = false;
      })
    },
    getEnv(definition) {
      return new Promise((resolve) => {
        this.$post("/api/automation/getApiScenarioEnv", {definition: definition}, res => {
          if (res.data) {
            res.data.projectIds.push(this.projectId);
            this.$emit("update:projectIds", new Set(res.data.projectIds));
            this.$emit("update:isFullUrl", res.data.fullUrl);
          }
          resolve();
        })
      });
    },
  },
}
</script>

<style scoped>
.ms-header {
  border-bottom: 1px solid #E6E6E6;
  height: 50px;
  background-color: #FFF;
}

.ms-div {
  float: left;
  width: 60%;
  margin-left: 20px;
  margin-top: 12px;
}

.ms-header-right {
  float: right;
  width: 520px;
  margin-top: 4px;
  z-index: 1;
}

.alt-ico-close {
  font-size: 18px;
  margin: 10px 10px 10px;
}

.alt-ico-close:hover {
  color: black;
  cursor: pointer;
}

.ms-alt-ico {
  color: #8c939d;
  font-size: 16px;
  margin: 10px 30px 0px;
}

.ms-alt-ico:hover {
  color: black;
  cursor: pointer;
}

.ms-header-margin {
  margin-top: 3px;
}

.ms-right {
  margin-right: 40px;
}

.head {
  border-bottom: 1px solid #303133;
  color: #303133;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  font-size: 13px;
}

.ms-scenario-name {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 200px;
}

</style>
