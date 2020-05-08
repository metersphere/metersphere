<template>
  <el-container>
    <el-aside class="scenario-aside">
      <div class="scenario-list">
        <ms-api-collapse v-model="activeName" @change="handleChange" accordion>
          <ms-api-collapse-item v-for="(scenario, index) in scenarios" :key="index"
                                :title="scenario.name" :name="index">
            <template slot="title">
              <div class="scenario-name">
                {{scenario.name}}
                <span id="hint" v-if="!scenario.name">
                  {{$t('api_test.scenario.config')}}
                </span>
              </div>
              <!--              暂时去掉，将来再加-->
              <!--              <el-dropdown trigger="click" @command="handleCommand">-->
              <!--                <span class="el-dropdown-link el-icon-more scenario-btn"/>-->
              <!--                <el-dropdown-menu slot="dropdown">-->
              <!--                  <el-dropdown-item :command="{type:'delete', index:index}">删除场景</el-dropdown-item>-->
              <!--                </el-dropdown-menu>-->
              <!--              </el-dropdown>-->
            </template>
            <ms-api-request-config :requests="scenario.requests" :open="select"/>
          </ms-api-collapse-item>
        </ms-api-collapse>
      </div>
      <!--      暂时去掉，将来再加-->
      <!--      <el-button class="scenario-create" type="primary" size="mini" icon="el-icon-plus" plain @click="createScenario"/>-->
    </el-aside>

    <el-main class="scenario-main">
      <div class="scenario-form">
        <ms-api-scenario-form :scenario="selected" v-if="isScenario"/>
        <ms-api-request-form :request="selected" v-if="isRequest"/>
      </div>
    </el-main>
  </el-container>
</template>

<script>

  import MsApiCollapseItem from "./ApiCollapseItem";
  import MsApiCollapse from "./ApiCollapse";
  import MsApiRequestConfig from "./ApiRequestConfig";
  import MsApiRequestForm from "./ApiRequestForm";
  import MsApiScenarioForm from "./ApiScenarioForm";
  import {Scenario, Request} from "../model/ScenarioModel";

  export default {
    name: "MsApiScenarioConfig",

    components: {
      MsApiRequestConfig,
      MsApiScenarioForm,
      MsApiRequestForm,
      MsApiCollapse,
      MsApiCollapseItem
    },

    props: {
      scenarios: Array
    },

    data() {
      return {
        activeName: 0,
        selected: [Scenario, Request]
      }
    },

    methods: {
      createScenario: function () {
        this.scenarios.push(new Scenario());
      },
      deleteScenario: function (index) {
        this.scenarios.splice(index, 1);
        if (this.scenarios.length === 0) {
          this.createScenario();
          this.select(this.scenarios[0]);
        }
      },
      handleChange: function (index) {
        this.select(this.scenarios[index]);
      },
      handleCommand: function (command) {
        switch (command.type) {
          case "delete":
            this.deleteScenario(command.index);
            break;
        }
      },
      select: function (obj) {
        this.selected = null;
        this.$nextTick(function () {
          this.selected = obj;
        });
      },
      reset: function () {
        this.selected = null;
        this.$nextTick(function () {
          this.activeName = 0;
          this.select(this.scenarios[0]);
        });
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

  .scenario-name > #hint {
    color: #8a8b8d;
  }

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
</style>
