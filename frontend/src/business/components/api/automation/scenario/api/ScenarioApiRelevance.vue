<template>
  <el-dialog class="api-relevance" :title="'接口导入'"
             :visible.sync="dialogVisible"
             width="60%"
             :close-on-click-modal="false"
             top="50px">

    <ms-container>
      <ms-aside-container :enable-aside-hidden="false">
        <ms-api-module
          @nodeSelectEvent="nodeChange"
          @protocolChange="handleProtocolChange"
          @refreshTable="refresh"
          @setModuleOptions="setModuleOptions"
          :is-read-only="true"
          ref="nodeTree"/>
      </ms-aside-container>

      <ms-main-container>
        <scenario-relevance-api-list
          v-if="isApiListEnable"
          :current-protocol="currentProtocol"
          :select-node-ids="selectNodeIds"
          :is-api-list-enable="isApiListEnable"
          @isApiListEnableChange="isApiListEnableChange"
          ref="apiList"
        />

        <scenario-relevance-case-list
          v-if="!isApiListEnable"
          :current-protocol="currentProtocol"
          :select-node-ids="selectNodeIds"
          :is-api-list-enable="isApiListEnable"
          @isApiListEnableChange="isApiListEnableChange"
          ref="apiCaseList"/>
      </ms-main-container>
    </ms-container>

    <template v-slot:footer>
      <el-button type="primary" @click="copy" @keydown.enter.native.prevent>复制</el-button>
      <el-button v-if="!isApiListEnable" type="primary" @click="reference" @keydown.enter.native.prevent>引用</el-button>
    </template>

  </el-dialog>
</template>

<script>
    import ScenarioRelevanceCaseList from "./ScenarioRelevanceCaseList";
    import MsApiModule from "../../../definition/components/module/ApiModule";
    import MsContainer from "../../../../common/components/MsContainer";
    import MsAsideContainer from "../../../../common/components/MsAsideContainer";
    import MsMainContainer from "../../../../common/components/MsMainContainer";
    import ScenarioRelevanceApiList from "./ScenarioRelevanceApiList";
    export default {
      name: "ScenarioApiRelevance",
      components: {
        ScenarioRelevanceApiList,
        MsMainContainer, MsAsideContainer, MsContainer, MsApiModule, ScenarioRelevanceCaseList},
      data() {
          return {
            dialogVisible: false,
            result: {},
            currentProtocol: null,
            selectNodeIds: [],
            moduleOptions: {},
            isApiListEnable: true,
          }
      },
      methods: {
        reference() {
          this.save('REF');
        },
        copy() {
          this.save('Copy');
        },
        save(reference) {
          if (this.isApiListEnable) {
            this.$emit('save', this.$refs.apiList.selectRows, 'API', reference);
            this.close();
          } else {
            let apiCases = this.$refs.apiCaseList.selectRows;
            let ids = Array.from(apiCases).map(row => row.id);
            this.result = this.$post("/api/testcase/get/request", {ids: ids}, (response) => {
              apiCases.forEach((item) => {
                item.request = response.data[item.id];
              });
              this.$emit('save', apiCases, 'CASE', reference);
              this.close();
            });
          }
        },
        close() {
          this.refresh();
          this.dialogVisible = false;
        },
        open() {
          this.dialogVisible = true;
        },
        isApiListEnableChange(data) {
          this.isApiListEnable = data;
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
        refresh() {
          if (this.isApiListEnable) {
            this.$refs.apiList.initTable();
          } else {
            this.$refs.apiCaseList.initTable();
          }
        },
      }
    }
</script>

<style scoped>

  .ms-aside-container {
    border: 0px;
  }

  .api-relevance >>> .el-dialog__body {
    padding: 10px 20px;
  }

</style>
