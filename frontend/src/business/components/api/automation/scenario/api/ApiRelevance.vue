<template>
  <test-case-relevance-base
    @setProject="setProject"
    :dialog-title="$t('api_test.definition.api_import')"
    ref="baseRelevance">
    <template v-slot:aside>
      <ms-api-module
        style="margin-top: 5px;"
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

    <scenario-relevance-api-list
      v-if="isApiListEnable"
      :project-id="projectId"
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiList"/>

    <scenario-relevance-case-list
      v-if="!isApiListEnable"
      :project-id="projectId"
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiCaseList"/>

    <template v-slot:footer>
      <el-button type="primary" @click="copy" :loading="buttonIsWorking" @keydown.enter.native.prevent>{{ $t('commons.copy') }}</el-button>
      <el-button v-if="!isApiListEnable" type="primary" :loading="buttonIsWorking" @click="reference" @keydown.enter.native.prevent>
        {{ $t('api_test.scenario.reference') }}
      </el-button>
    </template>
  </test-case-relevance-base>
</template>

<script>
import ScenarioRelevanceCaseList from "./RelevanceCaseList";
import MsApiModule from "../../../definition/components/module/ApiModule";
import MsContainer from "../../../../common/components/MsContainer";
import MsAsideContainer from "../../../../common/components/MsAsideContainer";
import MsMainContainer from "../../../../common/components/MsMainContainer";
import ScenarioRelevanceApiList from "./RelevanceApiList";
import RelevanceDialog from "../../../../track/plan/view/comonents/base/RelevanceDialog";
import TestCaseRelevanceBase from "@/business/components/track/plan/view/comonents/base/TestCaseRelevanceBase";
import {getUUID} from "@/common/js/utils";

export default {
  name: "ApiRelevance",
  components: {
    TestCaseRelevanceBase,
    RelevanceDialog,
    ScenarioRelevanceApiList,
    MsMainContainer, MsAsideContainer, MsContainer, MsApiModule, ScenarioRelevanceCaseList
  },
  data() {
    return {
      buttonIsWorking:false,
      result: {},
      currentProtocol: null,
      selectNodeIds: [],
      moduleOptions: {},
      isApiListEnable: true,
      projectId: ""
    }
  },
  watch: {
    projectId() {
      this.refresh();
      this.$refs.nodeTree.list(this.projectId);
    }
  },
  methods: {
    changeButtonLoadingType(){
      this.refresh();
      this.buttonIsWorking = false;
    },
    reference() {
      this.buttonIsWorking = true;
      this.save('REF');
    },
    copy() {
      this.buttonIsWorking = true;
      this.save('Copy');
    },
    save(reference) {
      if (this.isApiListEnable) {
        let apis = this.$refs.apiList.selectRows;
        apis.forEach(api => {
          api.projectId = this.projectId;
        });
        let params = this.$refs.apiList.getConditions();
        this.result = this.$post("/api/definition/list/batch", params, (response) => {
          let apis = response.data;
          if(apis.length === 0){
            this.$warning('请选择接口');
            this.buttonIsWorking = false;
          }else {
            this.$emit('save', apis, 'API', reference);
            this.$refs.baseRelevance.close();
          }
        },(error) => {
          this.buttonIsWorking = false;
        });

      } else {
        let params = this.$refs.apiCaseList.getConditions();
        this.result = this.$post("/api/testcase/get/caseBLOBs/request", params, (response) => {
          let apiCases = response.data;
          if(apiCases.length === 0) {
            this.$warning('请选择案例');
            this.buttonIsWorking = false;
          }else{
            this.$emit('save', apiCases, 'CASE', reference);
            this.$refs.baseRelevance.close();
          }
        },(error) => {
          this.buttonIsWorking = false;
        });
      }
    },
    close() {
      this.$emit('close');
      this.refresh();
      this.$refs.relevanceDialog.close();
    },
    open() {
      this.buttonIsWorking = false;
      if (this.$refs.apiList) {
        this.$refs.apiList.clearSelection();
      }
      if (this.$refs.apiCaseList) {
        this.$refs.apiCaseList.clearSelection();
      }
      this.$refs.baseRelevance.open();
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
        this.$refs.apiList.initTable(this.projectId);
      } else {
        this.$refs.apiCaseList.initTable(this.projectId);
      }
    },
    setProject(projectId) {
      this.projectId = projectId;
    },
  }
}
</script>

<style scoped>
/deep/ .filter-input {
  width: 140px !important;
}
</style>
