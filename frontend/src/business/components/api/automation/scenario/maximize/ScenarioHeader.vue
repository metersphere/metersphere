<template>
  <div class="ms-header" @click="showAll">
    <el-row>
      <div class="ms-div" v-loading="loading">
        <!-- 调试部分 -->
        <el-row style="margin: 5px">
          <el-col :span="6" class="ms-col-one ms-font">
            {{$t('api_test.automation.step_total')}}：{{scenarioDefinition.length}}
          </el-col>
          <el-col :span="6" class="ms-col-one ms-font">
            <el-link class="head" @click="showScenarioParameters">{{$t('api_test.automation.scenario_total')}}</el-link>
            ：{{varSize }}
          </el-col>
          <el-col :span="5" class="ms-col-one ms-font">
            <el-checkbox v-model="enableCookieShare">共享cookie</el-checkbox>
          </el-col>
          <el-col :span="7">
            <env-popover :env-map="envMap" :project-ids="projectIds" @setProjectEnvMap="setProjectEnvMap"
                         :project-list="projectList" ref="envPopover" style="margin-top: 0px"/>
          </el-col>
        </el-row>
      </div>
      <div class="ms-header-right">
        <el-button :disabled="scenarioDefinition.length < 1" size="small" type="primary" v-prevent-re-click @click="runDebug">{{$t('api_test.request.debug')}}</el-button>
        ｜
        <i class="el-icon-close alt-ico" @click="close"/>
      </div>
    </el-row>
  </div>
</template>

<script>
  import {checkoutTestManagerOrTestUser, exportPdf} from "@/common/js/utils";
  import html2canvas from 'html2canvas';
  import EnvPopover from "../../scenario/EnvPopover";

  export default {
    name: "ScenarioHeader",
    components: {EnvPopover},
    props: {currentScenario: {}, scenarioDefinition: Array, enableCookieShare: Boolean, projectEnvMap: Map, projectIds: Set, projectList: Array},
    data() {
      return {
        envMap: new Map,
        loading: false,
        varSize: 0,
      }
    },
    mounted() {
      this.envMap = this.projectEnvMap;
      this.getVariableSize();
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
        this.$emit('closePage');
      },
      runDebug() {
        this.$emit('runDebug');
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
        this.envMap = projectEnvMap;
      }
    },
  }
</script>

<style scoped>
  .ms-header {
    border-bottom: 1px solid #E6E6E6;
    height: 65px;
    background-color: #FFF;
  }

  .ms-div {
    float: left;
    width: 80%;
    margin-left: 20px;
    margin-top: 12px;
  }

  .ms-span {
    margin: 0px 10px 10px;
  }

  .ms-header-right {
    float: right;
    margin-bottom: 10px;
    margin-top: 10px;
    margin-right: 20px;
  }

  .alt-ico {
    font-size: 18px;
    margin: 10px 0px 0px;
  }

  .alt-ico:hover {
    color: black;
    cursor: pointer;
    font-size: 18px;
  }

  .ms-font {
    color: #303133;
    font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
    font-size: 13px;
  }

  .ms-col-one {
    margin-top: 5px;
  }
</style>
