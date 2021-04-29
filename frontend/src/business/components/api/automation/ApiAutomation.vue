<template>
  <ms-container v-if="renderComponent" v-loading="loading">
    <ms-aside-container>
      <ms-api-scenario-module
        :show-operator="true"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @saveAsEdit="editScenario"
        @setModuleOptions="setModuleOptions"
        @setNodeTree="setNodeTree"
        @enableTrash="enableTrash"
        @exportAPI="exportAPI"
        @exportJmx="exportJmx"
        @refreshAll="refreshAll"
        :type="'edit'"
        ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container>
      <el-tabs v-model="activeName" @tab-click="addTab" @tab-remove="removeTab">
        <el-tab-pane name="default" :label="$t('api_test.automation.scenario_list')">
          <ms-api-scenario-list
            :module-tree="nodeTree"
            :module-options="moduleOptions"
            :select-node-ids="selectNodeIds"
            :trash-enable="trashEnable"
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            :is-read-only="isReadOnly"
            @openScenario="editScenario"
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
            <ms-edit-api-scenario @refresh="refresh" @openScenario="editScenario" @closePage="closePage" :currentScenario="item.currentScenario"
                                  :moduleOptions="moduleOptions" ref="autoScenarioConfig"/>
          </div>
        </el-tab-pane>

        <el-tab-pane name="add">
          <template v-slot:label>
            <el-dropdown @command="handleCommand" v-tester>
              <el-button type="primary" plain icon="el-icon-plus" size="mini" v-tester/>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="ADD">{{ $t('api_test.automation.add_scenario') }}</el-dropdown-item>
                <el-dropdown-item command="CLOSE_ALL">{{ $t('api_test.definition.request.close_all_label') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
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
  import {getUUID, downloadFile, checkoutTestManagerOrTestUser, getCurrentUser} from "@/common/js/utils";
  import MsApiScenarioModule from "@/business/components/api/automation/scenario/ApiScenarioModule";
  import MsEditApiScenario from "./scenario/EditApiScenario";

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
      },
      isReadOnly() {
        return !checkoutTestManagerOrTestUser();
      },
      projectId() {
        return this.$store.state.projectId
      },
    },
    data() {
      return {
        redirectID: '',
        renderComponent: true,
        isHide: true,
        activeName: 'default',
        redirectFlag: 'none',
        currentModule: null,
        moduleOptions: [],
        tabs: [],
        loading: false,
        trashEnable: false,
        selectNodeIds: [],
        nodeTree: [],
        currentModulePath: "",
      }
    },
    watch: {
      redirectID() {
        this.renderComponent = false;
        this.$nextTick(() => {
          // 在 DOM 中添加 my-component 组件
          this.renderComponent = true;
        });
      },
      '$route'(to, from) {  //  路由改变时，把接口定义界面中的 ctrl s 保存快捷键监听移除
        if (to.path.indexOf('/api/automation') == -1) {
          if (this.$refs && this.$refs.autoScenarioConfig) {
            this.$refs.autoScenarioConfig.forEach(item => {
              item.removeListener();
            });
          }
        }
      },
      selectNodeIds() {
        this.activeName = "default";
      }
    },
    methods: {
      exportAPI() {
        this.$refs.apiScenarioList.exportApi();
      },
      exportJmx() {
        this.$refs.apiScenarioList.exportJmx();
      },
      checkRedirectEditPage(redirectParam) {
        if (redirectParam != null) {
          let selectParamArr = redirectParam.split("edit:");
          if (selectParamArr.length == 2) {
            let scenarioId = selectParamArr[1];
            let projectId = this.projectId;
            //查找单条数据，跳转修改页面
            let url = "/api/automation/list/" + 1 + "/" + 1;
            this.$post(url, {id: scenarioId, projectId: projectId}, response => {
              let data = response.data;
              if (data != null) {
                //如果树未加载
                if (JSON.stringify(this.moduleOptions) === '{}') {
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
        if (redirectIDParam != null) {
          if (this.redirectFlag == "none") {
            this.activeName = "default";
            this.addListener();
            this.redirectFlag = "redirected";
          }
        } else {
          this.redirectFlag = "none";
        }
      },
      getPath(id, arr) {
        if (id === null) {
          return null;
        }
        if(arr) {
          arr.forEach(item => {
            if (item.id === id) {
              this.currentModulePath = item.path;
            }
            if (item.children && item.children.length > 0) {
              this.getPath(id, item.children);
            }
          });
        }
      },
      addTab(tab) {
        if (tab.name === 'default') {
          this.$refs.apiScenarioList.search();
        }
        if (!this.projectId) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        this.currentModulePath = "";
        if (tab.name === 'add') {
          let label = this.$t('api_test.automation.add_scenario');
          let name = getUUID().substring(0, 8);
          this.activeName = name;
          let currentScenario = {
            status: "Underway", principal: getCurrentUser().id,
            apiScenarioModuleId: "default-module", id: getUUID(),
            modulePath: "/" + this.$t("commons.module_title")
          };
          if (this.nodeTree && this.nodeTree.length > 0) {
            currentScenario.apiScenarioModuleId = this.nodeTree[0].id;
            this.getPath(this.nodeTree[0].id, this.moduleOptions);
            currentScenario.modulePath = this.currentModulePath;
          }

          if (this.selectNodeIds && this.selectNodeIds.length > 0) {
            currentScenario.apiScenarioModuleId = this.selectNodeIds[0];
            this.getPath(this.selectNodeIds[0], this.moduleOptions);
            currentScenario.modulePath = this.currentModulePath;
          }
          this.tabs.push({label: label, name: name, currentScenario: currentScenario});
        }
        if (tab.name === 'edit') {
          let label = this.$t('api_test.automation.add_scenario');
          let name = getUUID().substring(0, 8);
          this.activeName = name;
          label = tab.currentScenario.name;
          this.tabs.push({label: label, name: name, currentScenario: tab.currentScenario});
        }
        if (this.$refs && this.$refs.autoScenarioConfig) {
          this.$refs.autoScenarioConfig.forEach(item => {
            item.removeListener();
          });  //  删除所有tab的 ctrl + s 监听
          this.addListener();
        }
      },
      addListener() {
        let index = this.tabs.findIndex(item => item.name === this.activeName); //  找到当前选中tab的index
        if (index != -1) {   //  为当前选中的tab添加监听
          this.$nextTick(() => {
            this.$refs.autoScenarioConfig[index].addListener();
          });
        }
      },
      handleTabClose() {
        this.tabs = [];
        this.activeName = "default";
        this.refresh();
      },
      handleCommand(e) {
        switch (e) {
          case "ADD":
            this.addTab({name: 'add'});
            break;
          case "CLOSE_ALL":
            this.handleTabClose();
            break;
          default:
            this.addTab({name: 'add'});
            break;
        }
      },
      closePage(targetName) {
        this.tabs = this.tabs.filter(tab => tab.label !== targetName);
        if (this.tabs.length > 0) {
          this.activeName = this.tabs[this.tabs.length - 1].name;
          this.addListener(); //  自动切换当前标签时，也添加监听
        } else {
          this.activeName = "default"
        }
      },
      removeTab(targetName) {
        this.tabs = this.tabs.filter(tab => tab.name !== targetName);
        if (this.tabs.length > 0) {
          this.activeName = this.tabs[this.tabs.length - 1].name;
          this.addListener(); //  自动切换当前标签时，也添加监听
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
        this.setTabTitle(data);
        this.$refs.apiScenarioList.search(data);
      },
      refreshAll() {
        this.$refs.nodeTree.list();
        this.$refs.apiScenarioList.search();
      },
      setTabTitle(data) {
        for (let index in this.tabs) {
          let tab = this.tabs[index];
          if (tab.name === this.activeName) {
            tab.label = data.name;
            break;
          }
        }
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
      setNodeTree(data) {
        this.nodeTree = data;
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
  /deep/ .el-tabs__header {
    margin: 0 0 0px;
  }
</style>
