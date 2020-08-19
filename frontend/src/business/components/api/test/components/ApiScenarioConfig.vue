<template>
  <el-container>
    <el-aside class="scenario-aside">
      <div class="scenario-list">
        <ms-api-collapse v-model="activeName" @change="handleChange" accordion>
          <draggable :list="scenarios" group="Scenario" class="scenario-draggable" ghost-class="scenario-ghost">
            <ms-api-collapse-item v-for="(scenario, index) in scenarios" :key="index"
                                  :title="scenario.name" :name="index" :class="{'disable-scenario': !scenario.enable}">
              <template slot="title">
                <div class="scenario-name">
                  {{scenario.name}}
                  <span id="hint" v-if="!scenario.name">
                    {{$t('api_test.scenario.config')}}
                  </span>
                </div>
                <el-dropdown trigger="click" @command="handleCommand">
                  <span class="el-dropdown-link el-icon-more scenario-btn"/>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item :disabled="isReadOnly" :command="{type: 'copy', index: index}">
                      {{$t('api_test.scenario.copy')}}
                    </el-dropdown-item>
                    <el-dropdown-item :disabled="isReadOnly" :command="{type:'delete', index:index}">
                      {{$t('api_test.scenario.delete')}}
                    </el-dropdown-item>
                    <el-dropdown-item v-if="scenario.enable" :disabled="isReadOnly" :command="{type:'disable', index:index}">
                      {{$t('api_test.scenario.disable')}}
                    </el-dropdown-item>
                    <el-dropdown-item v-if="!scenario.enable" :disabled="isReadOnly" :command="{type:'enable', index:index}">
                      {{$t('api_test.scenario.enable')}}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </template>
              <ms-api-request-config :is-read-only="isReadOnly" :scenario="scenario" @select="select"/>
            </ms-api-collapse-item>
          </draggable>
        </ms-api-collapse>
      </div>
      <el-button :disabled="isReadOnly" class="scenario-create" type="primary" size="mini" icon="el-icon-plus" plain @click="createScenario"/>
    </el-aside>

    <el-main class="scenario-main">
      <div class="scenario-form">
        <ms-api-scenario-form :is-read-only="isReadOnly" :scenario="selected" :project-id="projectId" v-if="isScenario"/>
        <ms-api-request-form :debug-report-id="debugReportId" @runDebug="runDebug" :is-read-only="isReadOnly"
                             :request="selected" :scenario="currentScenario" v-if="isRequest"/>
      </div>
    </el-main>
  </el-container>
</template>

<script>

import MsApiCollapseItem from "./collapse/ApiCollapseItem";
import MsApiCollapse from "./collapse/ApiCollapse";
import MsApiRequestConfig from "./request/ApiRequestConfig";
import MsApiRequestForm from "./request/ApiRequestForm";
import MsApiScenarioForm from "./ApiScenarioForm";
import {Request, Scenario} from "../model/ScenarioModel";
import draggable from 'vuedraggable';

export default {
  name: "MsApiScenarioConfig",

  components: {
    MsApiRequestConfig,
    MsApiScenarioForm,
    MsApiRequestForm,
    MsApiCollapse,
    MsApiCollapseItem,
    draggable
  },

    props: {
      scenarios: Array,
      projectId: String,
      isReadOnly: {
        type: Boolean,
        default: false
      },
      debugReportId: String
    },

    data() {
      return {
        activeName: 0,
        selected: [Scenario, Request],
        currentScenario: {}
      }
    },

    watch: {
      projectId() {
        this.initScenarioEnvironment();
      }
    },

    activated() {
      this.initScenarioEnvironment();
    },

    methods: {
      createScenario: function () {
        this.scenarios.push(new Scenario());
      },
      copyScenario: function (index) {
        let scenario = this.scenarios[index];
        this.scenarios.push(new Scenario(scenario));
      },
      deleteScenario: function (index) {
        this.scenarios.splice(index, 1);
        if (this.scenarios.length === 0) {
          this.createScenario();
          this.select(this.scenarios[0]);
        }
      },
      disableScenario: function (index) {
        this.scenarios[index].enable = false;
      },
      enableScenario: function (index) {
        this.scenarios[index].enable = true;
      },
      handleChange: function (index) {
        this.select(this.scenarios[index]);
      },
      handleCommand: function (command) {
        switch (command.type) {
          case "copy":
            this.copyScenario(command.index);
            break;
          case "delete":
            this.deleteScenario(command.index);
            break;
          case "disable":
            this.disableScenario(command.index);
            break;
          case "enable":
            this.enableScenario(command.index);
            break;
        }
      },
      select: function (obj, scenario) {
        this.selected = null;
        this.$nextTick(function () {
          if (obj instanceof Scenario) {
            this.currentScenario = obj;
          } else {
            this.currentScenario = scenario;
          }
          this.selected = obj;
        });
      },
      reset: function () {
        this.$nextTick(function () {
          this.activeName = 0;
          this.select(this.scenarios[0]);
        });
      },
      initScenarioEnvironment: function ()  {
        if (this.projectId) {
          this.result = this.$get('/api/environment/list/' + this.projectId, response => {
            let environments = response.data;
            let environmentMap = new Map();
            environments.forEach(environment => {
              environmentMap.set(environment.id, environment);
            });
            this.scenarios.forEach(scenario => {
              if (scenario.environmentId) {
                scenario.environment = environmentMap.get(scenario.environmentId);
              }
            });
          });
        }
      },
      runDebug(request) {
        let scenario =  new Scenario();
        Object.assign(scenario, this.currentScenario);
        scenario.requests = [];
        scenario.requests.push(request);
        this.$emit('runDebug', scenario);
      }
    },

    computed: {
      isScenario() {
        return this.selected instanceof Scenario;
      },
      isRequest() {
        return this.selected instanceof Request;
      }
    },

    created() {
      this.select(this.scenarios[0]);
    }
  }
</script>

<style scoped>
  .scenario-aside {
    position: relative;
    border-radius: 4px;
    border: 1px solid #EBEEF5;
    box-sizing: border-box;
  }

  .scenario-list {
    overflow-y: auto;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 28px;
  }

  .scenario-name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 14px;
    width: 100%;
  }

  /*.scenario-name > #hint {*/
    /*color: #8a8b8d;*/
  /*}*/

  .scenario-btn {
    text-align: center;
    padding: 13px;
  }

  .scenario-create {
    position: absolute;
    bottom: 0;
    width: 100%;
  }

  .scenario-main {
    position: relative;
    margin-left: 20px;
    border: 1px solid #EBEEF5;
  }

  .scenario-form {
    padding: 20px;
    overflow-y: auto;
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
  }

  .scenario-ghost {
    opacity: 0.5;
  }

  .scenario-draggable {
    background-color: #909399;
  }

  .disable-scenario >>> .el-collapse-item__header {
    border-right: 2px solid #909399;
    color: #8a8b8d;
  }
</style>
