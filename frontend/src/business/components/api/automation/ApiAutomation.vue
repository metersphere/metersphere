<template>
  <ms-container>
    <ms-aside-container>
      <ms-api-scenario-module @selectModule="selectModule" @getApiModuleTree="initTree" @changeProject="changeProject"
                              @refresh="refresh" @saveAsEdit="editScenario"/>
    </ms-aside-container>
    <ms-main-container>
      <el-tabs v-model="activeName" @tab-click="addTab" @tab-remove="removeTab">
        <el-tab-pane name="default" :label="$t('api_test.automation.scenario_test')">
          <ms-api-scenario-list
            :current-project="currentProject"
            :current-module="currentModule"
            @edit="editScenario"
            ref="apiScenarioList"/>
        </el-tab-pane>

        <el-tab-pane
          :key="item.name"
          v-for="(item) in tabs"
          :label="item.label"
          :name="item.name"
          closable>
          <div class="ms-api-scenario-div">
            <ms-edit-api-scenario :current-project="currentProject" :currentScenario="currentScenario" :moduleOptions="moduleOptions"/>
          </div>
        </el-tab-pane>

        <el-tab-pane name="add">
          <template v-slot:label>
            <el-button type="primary" plain icon="el-icon-plus" size="mini"/>
          </template>
        </el-tab-pane>
      </el-tabs>
    </ms-main-container>
  </ms-container>
</template>

<script>

import MsContainer from "@/business/components/common/components/MsContainer";
import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsApiScenarioList from "@/business/components/api/automation/scenario/ApiScenarioList";
import {getUUID} from "@/common/js/utils";
import MsApiScenarioModule from "@/business/components/api/automation/scenario/ApiScenarioModule";
import MsEditApiScenario from "./scenario/EditApiScenario";

export default {
  name: "ApiAutomation",
  components: {MsApiScenarioModule, MsApiScenarioList, MsMainContainer, MsAsideContainer, MsContainer,MsEditApiScenario},
  comments: {},
  data() {
    return {
      isHide: true,
      activeName: 'default',
      currentProject: null,
      currentModule: null,
      currentScenario: {},
      moduleOptions: {},
      tabs: [],
    }
  },
  watch: {},
  methods: {
    addTab(tab) {
      if (tab.name === 'add') {
        let label = this.$t('api_test.automation.add_scenario');
        let name = getUUID().substring(0, 8);
        this.tabs.push({label: label, name: name});
        this.activeName = name;
      }
    },
    removeTab(targetName) {
      this.tabs = this.tabs.filter(tab => tab.name !== targetName);
      if (this.tabs.length > 0) {
        this.activeName = this.tabs[this.tabs.length - 1].name;
      } else {
        this.activeName = "default"
      }
    },
    setTabLabel(data) {
      for (const tab of this.tabs) {
        if (tab.name === this.activeName) {
          tab.label = data.name;
          break;
        }
      }
    },
    selectModule(data) {
      this.currentModule = data;
    },
    saveScenario(data) {
      this.setTabLabel(data);
      this.$refs.apiScenarioList.search(data);
    },
    initTree(data) {
      this.moduleOptions = data;
    },
    changeProject(data) {
      this.currentProject = data;
    },
    refresh(data) {
      this.$refs.apiScenarioList.search(data);
    },
    editScenario(row) {
      this.currentScenario = row;
      console.log(row)
      this.addTab({name: 'add'});
    },
  }
}
</script>

<style scoped>

</style>
