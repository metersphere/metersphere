<template>
  <div class="container">
    <div class="main-content">
      <el-card>
        <el-container class="scenario-container">
          <el-header>
            <span class="scenario-title">场景配置</span>
          </el-header>

          <el-container>
            <el-aside class="scenario-aside">
              <div class="scenario-list">
                <ms-api-collapse v-model="activeName" @change="handleChange" accordion>
                  <ms-api-collapse-item v-for="(scenario, index) in scenarios" :key="index"
                                        :title="scenario.name" :name="index">
                    <template slot="title">
                      <div class="scenario-name">{{scenario.name}}</div>
                      <el-dropdown trigger="click" @command="handleCommand">
                        <span class="el-dropdown-link el-icon-more scenario-btn"/>
                        <el-dropdown-menu slot="dropdown">
                          <el-dropdown-item :command="{type:'delete', index:index}">删除场景</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                    </template>
                    <ms-api-request :requests="scenario.requests" :open="select"/>
                  </ms-api-collapse-item>
                </ms-api-collapse>
              </div>
              <el-button class="scenario-create" type="primary" size="mini" icon="el-icon-plus" plain
                         @click="createScenario"/>
            </el-aside>

            <el-main class="scenario-main">
              <div class="scenario-form">
                <ms-api-scenario-form :scenario="selected" v-if="isScenario"></ms-api-scenario-form>
                <ms-api-request-form :request="selected" v-if="isRequest"></ms-api-request-form>
              </div>
            </el-main>
          </el-container>
        </el-container>
      </el-card>
    </div>
  </div>
</template>

<script>

  import MsApiCollapseItem from "./components/ApiCollapseItem";
  import MsApiCollapse from "./components/ApiCollapse";
  import MsApiRequest from "./components/ApiRequest";
  import MsApiRequestForm from "./components/ApiRequestForm";
  import MsApiScenarioForm from "./components/ApiScenarioForm";
  import {Scenario, Request} from "./model/APIModel";

  export default {
    name: "MsApiScenarioConfig",

    components: {MsApiScenarioForm, MsApiRequestForm, MsApiRequest, MsApiCollapse, MsApiCollapseItem},

    data() {
      return {
        activeName: 0,
        scenarios: [],
        selected: Object
      }
    },

    methods: {
      createScenario: function () {
        let scenario = new Scenario({name: "Scenario"});
        this.scenarios.push(scenario);
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
        this.selected = obj;
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
      if (this.scenarios.length === 0) {
        this.createScenario();
        this.select(this.scenarios[0]);
      }
    }
  }
</script>

<style scoped>
  .scenario-container {
    height: calc(100vh - 150px);
    min-height: 600px;
  }

  .scenario-title {
    font-size: 16px;
    margin-left: -20px;
  }

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
