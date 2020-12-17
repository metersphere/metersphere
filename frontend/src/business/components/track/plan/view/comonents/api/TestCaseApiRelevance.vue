<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    ref="baseRelevance">

    <template v-slot:aside>
      <!--<node-tree class="node-tree"-->
                 <!--@nodeSelectEvent="nodeChange"-->
                 <!--@refresh="refresh"-->
                 <!--:tree-nodes="treeNodes"-->
                 <!--ref="nodeTree"/>-->
      <ms-api-module
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @exportAPI="exportAPI"
        @debug="debug"
        @saveAsEdit="editApi"
        @setModuleOptions="setModuleOptions"
        @enableTrash="enableTrash"
        :is-read-only="true"
        :type="'edit'"
        ref="nodeTree"/>
    </template>

    <ms-table-header :condition.sync="condition" @search="search" title="" :show-create="false"/>

    <api-list
      v-if="isApiListEnable"
      :current-protocol="currentProtocol"
      :currentRow="currentRow"
      :select-node-ids="selectNodeIds"
      :trash-enable="trashEnable"
      :is-api-list-enable="isApiListEnable"
      @editApi="editApi"
      @handleCase="handleCase"
      @showExecResult="showExecResult"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiList"/>


  </test-case-relevance-base>

</template>

<script>

  import NodeTree from '../../../../common/NodeTree';
  import PriorityTableItem from "../../../../common/tableItems/planview/PriorityTableItem";
  import TypeTableItem from "../../../../common/tableItems/planview/TypeTableItem";
  import MsTableSearchBar from "../../../../../common/components/MsTableSearchBar";
  import MsTableAdvSearchBar from "../../../../../common/components/search/MsTableAdvSearchBar";
  import MsTableHeader from "../../../../../common/components/MsTableHeader";
  import elTableInfiniteScroll from 'el-table-infinite-scroll';
  import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
  import ApiList from "../../../../../api/automation/scenario/api/ApiList";
  import MsApiModule from "../../../../../api/definition/components/module/ApiModule";
  import {getCurrentProjectID} from "../../../../../../../common/js/utils";

  export default {
    name: "TestCaseApiRelevance",
    components: {
      MsApiModule,
      ApiList,
      TestCaseRelevanceBase,
      NodeTree,
      PriorityTableItem,
      TypeTableItem,
      MsTableSearchBar,
      MsTableAdvSearchBar,
      MsTableHeader,
    },
    directives: {
      'el-table-infinite-scroll': elTableInfiniteScroll
    },
    data() {
      return {
        showCasePage: true,
        apiDefaultTab: 'default',
        currentProtocol: null,
        currentModule: null,
        selectNodeIds: [],
        currentApi: {},
        moduleOptions: {},
        trashEnable: false,
        isApiListEnable: true,
        condition: {},
        currentRow: {}
      };
    },
    props: {
      planId: {
        type: String
      }
    },
    watch: {
      planId() {
        // this.condition.planId = this.planId;
      },
      projectId() {
        this.condition.projectId = this.projectId;
        // this.getProjectNode();
        // this.search();
      }
    },
    updated() {
      this.toggleSelection(this.testCases);
    },
    methods: {

      open() {
        this.$refs.baseRelevance.open();
      },
      search() {

      },

      setProject(projectId) {
        this.projectId = projectId;
      },

      isApiListEnableChange(data) {
        this.isApiListEnable = data;
      },
      handleCommand(e) {
        switch (e) {
          case "ADD":
            this.handleTabAdd(e);
            break;
          case "TEST":
            this.handleTabsEdit(this.$t("commons.api"), e);
            break;
          case "CLOSE_ALL":
            this.handleTabClose();
            break;
          default:
            this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), "debug");
            break;
        }
      },
      debug(id) {
        this.handleTabsEdit(this.$t('api_test.definition.request.fast_debug'), "debug", id);
      },
      editApi(row) {
        let name = this.$t('api_test.definition.request.edit_api');
        if (row.name) {
          name = this.$t('api_test.definition.request.edit_api') + "-" + row.name;
        }
        // this.handleTabsEdit(name, "ADD", row);
      },
      handleCase(api) {
        this.currentApi = api;
        this.showCasePage = false;
      },
      apiCaseClose() {
        this.showCasePage = true;
      },
      exportAPI() {
        if (!this.$refs.apiList[0].tableData) {
          return;
        }
        let obj = {projectName: getCurrentProjectID(), protocol: this.currentProtocol, data: this.$refs.apiList[0].tableData}
        // downloadFile("导出API.json", JSON.stringify(obj));
      },
      refresh(data) {
        this.$refs.apiList[0].initTable(data);
      },
      setTabTitle(data) {
        for (let index in this.apiTabs) {
          let tab = this.apiTabs[index];
          if (tab.name === this.apiDefaultTab) {
            tab.title = this.$t('api_test.definition.request.edit_api') + "-" + data.name;
            break;
          }
        }
        this.runTestData = data;
      },
      runTest(data) {
        this.setTabTitle(data);
        this.handleCommand("TEST");
      },
      saveApi(data) {
        this.setTabTitle(data);
        this.$refs.apiList[0].initApiTable(data);
      },

      showExecResult(row){
        this.debug(row);
      },

      nodeChange(node, nodeIds, pNodes) {
        this.selectNodeIds = nodeIds;
      },
      handleProtocolChange(protocol) {
        this.currentProtocol = protocol;
      },
      setModuleOptions(data) {
        this.moduleOptions = data;
      },
      enableTrash(data) {
        this.trashEnable = data;
      },

      saveCaseRelevance() {
        let param = {};
        param.planId = this.planId;
        param.testCaseIds = [...this.selectIds];
        param.request = this.condition;
        // 选择全选则全部加入到评审，无论是否加载完全部
        if (this.testCases.length === param.testCaseIds.length) {
          param.testCaseIds = ['all'];
        }
        this.result = this.$post('/test/plan/relevance', param, () => {
          this.selectIds.clear();
          this.$success(this.$t('commons.save_success'));

          this.$refs.baseRelevance.close();

          this.$emit('refresh');
        });
      },
    }
  }
</script>

<style scoped>
</style>
