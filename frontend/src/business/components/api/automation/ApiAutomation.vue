<template>
  <ms-container v-if="renderComponent">
    <ms-aside-container>
      <ms-api-scenario-module
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @saveAsEdit="editScenario"
        @setModuleOptions="setModuleOptions"
        @enableTrash="enableTrash"
        :type="'edit'"
        ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container>
      <el-tabs v-model="activeName" @tab-click="addTab" @tab-remove="removeTab">
        <el-tab-pane name="default" :label="$t('api_test.automation.scenario_test')">
          <ms-api-scenario-list
            :select-node-ids="selectNodeIds"
            :trash-enable="trashEnable"
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            @edit="editScenario"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            ref="apiScenarioList"/>
        </el-tab-pane>

        <el-tab-pane
          :key="item.name"
          v-for="(item) in tabs"
          :label="item.label"
          :name="item.name"
          closable>
          <div class="ms-api-scenario-div">
            <ms-edit-api-scenario @refresh="refresh" :currentScenario="item.currentScenario"
                                  :moduleOptions="moduleOptions"/>
          </div>
        </el-tab-pane>

        <el-tab-pane name="add">
          <template v-slot:label>
            <el-button type="primary" plain icon="el-icon-plus" size="mini" v-tester/>
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
import {getCurrentProjectID} from "../../../../common/js/utils";

export default {
  name: "ApiAutomation",
  components: {
    MsApiScenarioModule,
    MsApiScenarioList,
    MsMainContainer,
    MsAsideContainer,
    MsContainer,
    MsEditApiScenario
  },
  comments: {},
  computed: {
    checkRedirectID: function () {
      let redirectIDParam = this.$route.params.redirectID;
      this.changeRedirectParam(redirectIDParam);
      return redirectIDParam;
    },
    isRedirectEdit: function () {
      let redirectParam = this.$route.params.dataSelectRange;
      this.checkRedirectEditPage(redirectParam);
      return redirectParam;
    }
  },
  data() {
    return {
      redirectID: '',
      renderComponent: true,
      isHide: true,
      activeName: 'default',
      currentModule: null,
      moduleOptions: [],
      tabs: [],
      trashEnable: false,
      selectNodeIds: [],
    }
  },
  watch: {
    redirectID() {
      this.renderComponent = false;
      this.$nextTick(() => {
        // 在 DOM 中添加 my-component 组件
        this.renderComponent = true;
      });
    }
  },
  methods: {
    checkRedirectEditPage(redirectParam) {
      if(redirectParam!=null){
        let selectParamArr = redirectParam.split("edit:");
        if (selectParamArr.length == 2) {
          let scenarioId = selectParamArr[1];
          let projectId = getCurrentProjectID();
          //查找单条数据，跳转修改页面
          let url = "/api/automation/list/" + 1 + "/" + 1;
          this.$post(url, {id:scenarioId,projectId:projectId}, response => {
            let data = response.data;
            if(data!=null){
              //如果树未加载
              if(JSON.stringify(this.moduleOptions) === '{}' ){
                this.$refs.nodeTree.list();
              }
              let row = data.listObject[0];
              row.tags = JSON.parse(row.tags);
              this.editScenario(row);
            }
          });
        }
      }
    },
    changeRedirectParam(redirectIDParam) {
      this.redirectID = redirectIDParam;
    },
    addTab(tab) {
      if (!getCurrentProjectID()) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      if (tab.name === 'add') {
        let label = this.$t('api_test.automation.add_scenario');
        let name = getUUID().substring(0, 8);
        this.activeName = name;
        this.tabs.push({label: label, name: name, currentScenario: {apiScenarioModuleId: "", id: getUUID()}});
      }
      if (tab.name === 'edit') {
        let label = this.$t('api_test.automation.add_scenario');
        let name = getUUID().substring(0, 8);
        this.activeName = name;
        label = tab.currentScenario.name;
        this.tabs.push({label: label, name: name, currentScenario: tab.currentScenario});
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
    refresh(data) {
      this.$refs.apiScenarioList.search(data);
    },
    editScenario(row) {
      this.addTab({name: 'edit', currentScenario: row});
    },

    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },
    changeSelectDataRangeAll(tableType) {
      this.$route.params.dataSelectRange = 'all';
    },
    enableTrash(data) {
      this.trashEnable = data;
    }
  }
}
</script>

<style scoped>

</style>
