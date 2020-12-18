<template>
  <ms-container>
    <ms-aside-container>
      <ms-node-tree @nodeSelectEvent="selectModule" @getApiModuleTree="initTree" @protocolChange="changeProtocol"
                    @refresh="refresh" @saveAsEdit="editApi" @exportAPI="exportAPI"/>
    </ms-aside-container>

    <ms-main-container>
      <!-- 主框架列表 -->
      <el-tabs v-model="apiDefaultTab">
        <el-tab-pane
          :key="item.name"
          v-for="(item) in apiTabs"
          :label="item.title"
          :closable="item.closable"
          :name="item.name">
          <!-- 列表集合 -->
          <ms-api-list
            v-if="item.type === 'list'"
            :current-protocol="currentProtocol"
            :current-module="currentModule"
            @editApi="editApi"
            @handleCase="handleCase"
            :visible="visible"
            :currentRow="currentRow"
            ref="apiList"/>
        </el-tab-pane>
      </el-tabs>
    </ms-main-container>
  </ms-container>

</template>

<script>
  import MsNodeTree from '../../../definition/components/module/ApiModule';
  import MsApiList from './ApiList';
  import MsContainer from "../../../../common/components/MsContainer";
  import MsMainContainer from "../../../../common/components/MsMainContainer";
  import MsAsideContainer from "../../../../common/components/MsAsideContainer";
  import {downloadFile, getCurrentUser, getUUID, getCurrentProjectID} from "@/common/js/utils";

  export default {
    name: "ApiDefinition",
    components: {
      MsNodeTree,
      MsApiList,
      MsMainContainer,
      MsContainer,
      MsAsideContainer,
    },
    props: {
      visible: {
        type: String,
      },
      currentRow: {
        type: Object,
      }
    },
    data() {
      return {
        isHide: true,
        apiDefaultTab: 'default',
        currentProtocol: null,
        currentModule: null,
        currentApi: {},
        moduleOptions: {},
        runTestData: {},
        apiTabs: [{
          title: this.$t('api_test.definition.api_title'),
          name: 'default',
          type: "list",
          closable: false
        }],
      }
    },
    methods: {
      editApi(row) {
        this.currentApi = row;
      },
      handleCase(testCase) {
        this.currentApi = testCase;
        this.isHide = false;
      },
      apiCaseClose() {
        this.isHide = true;
      },
      selectModule(data) {
        this.currentModule = data.data;
      },
      exportAPI() {
        if (!this.$refs.apiList[0].tableData) {
          return;
        }
        let obj = {projectName: getCurrentProjectID(), protocol: this.currentProtocol, data: this.$refs.apiList[0].tableData}
        downloadFile("导出API.json", JSON.stringify(obj));
      },
      refresh(data) {
        this.$refs.apiList[0].initApiTable(data);
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
      saveApi(data) {
        this.setTabTitle(data);
        this.$refs.apiList[0].initApiTable(data);
      },
      initTree(data) {
        this.moduleOptions = data;
      },
      changeProtocol(data) {
        this.currentProtocol = data;
      }
    }
  }
</script>

<style scoped>
  .ms-api-buttion {
    position: absolute;
    top: 100px;
    right: 4px;
    padding: 0;
    background: 0 0;
    border: none;
    outline: 0;
    cursor: pointer;
    margin-right: 10px;
    font-size: 16px;
  }

  .ms-api-div {
    overflow-y: auto;
    height: calc(100vh - 155px)
  }

  /deep/ .el-tabs__header {
    margin: 0 0 5px;
  }

  /deep/ .el-main {
    overflow: hidden;
  }
</style>
