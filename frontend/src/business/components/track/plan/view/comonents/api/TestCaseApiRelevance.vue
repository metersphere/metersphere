<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    ref="baseRelevance">

    <template v-slot:aside>
      <ms-api-module
        :relevance-project-id="projectId"
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

    <relevance-api-list
      v-if="isApiListEnable"
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :is-api-list-enable="isApiListEnable"
      :project-id="projectId"
      :is-test-plan="true"
      :plan-id="planId"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiList"/>

    <relevance-case-list
      v-if="!isApiListEnable"
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :is-api-list-enable="isApiListEnable"
      :project-id="projectId"
      :is-test-plan="true"
      :plan-id="planId"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiCaseList"/>

  </test-case-relevance-base>

</template>

<script>

  import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
  import MsApiModule from "../../../../../api/definition/components/module/ApiModule";
  import RelevanceApiList from "../../../../../api/automation/scenario/api/RelevanceApiList";
  import RelevanceCaseList from "../../../../../api/automation/scenario/api/RelevanceCaseList";

  export default {
    name: "TestCaseApiRelevance",
    components: {
      RelevanceCaseList,
      RelevanceApiList,
      MsApiModule,
      TestCaseRelevanceBase,
    },
    data() {
      return {
        showCasePage: true,
        currentProtocol: null,
        currentModule: null,
        selectNodeIds: [],
        moduleOptions: {},
        trashEnable: false,
        isApiListEnable: true,
        condition: {},
        currentRow: {},
        projectId: ""
      };
    },
    props: {
      planId: {
        type: String
      }
    },
    watch: {
      planId() {
        this.condition.planId = this.planId;
      },
    },
    methods: {
      open() {
        this.init();
        this.$refs.baseRelevance.open();
        if (this.$refs.apiList) {
          this.$refs.apiList.clear();
        }
        if (this.$refs.apiCaseList) {
          this.$refs.apiCaseList.clear();
        }
      },
      init() {
        if (this.$refs.apiList) {
          this.$refs.apiList.initTable();
        }
        if (this.$refs.apiCaseList) {
          this.$refs.apiCaseList.initTable();
        }
        if (this.$refs.nodeTree) {
          this.$refs.nodeTree.list();
        }
      },
      setProject(projectId) {
        this.projectId = projectId;
      },
      isApiListEnableChange(data) {
        this.isApiListEnable = data;
      },

      refresh(data) {
        if (this.isApiListEnable) {
          this.$refs.apiList.initTable(data);
        } else {
          this.$refs.apiCaseList.initTable(data);
        }
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

      saveCaseRelevance() {


        let url = '';
        let environmentId = undefined;
        let selectIds = [];
        if (this.isApiListEnable) {
          //查找所有数据
          let params = this.$refs.apiList.getConditions();
          this.result = this.$post("/api/definition/list/batch", params, (response) => {
            let apis = response.data;
            url = '/api/definition/relevance';
            environmentId = this.$refs.apiList.environmentId;
            selectIds = Array.from(apis).map(row => row.id);
            this.postRelevance(url, environmentId, selectIds);
          });
        } else {
          let params = this.$refs.apiCaseList.getConditions();
          this.result = this.$post("/api/testcase/get/caseBLOBs/request", params, (response) => {
            let apiCases = response.data;
            url = '/api/testcase/relevance';
            environmentId = this.$refs.apiCaseList.environmentId;
            selectIds = Array.from(apiCases).map(row => row.id);
            this.postRelevance(url, environmentId, selectIds);
          });
        }

      },

      postRelevance(url, environmentId, selectIds) {
        let param = {};
        if (!environmentId) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }
        param.planId = this.planId;
        param.selectIds = selectIds;
        param.environmentId = environmentId;

        this.result = this.$post(url, param, () => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('refresh');
          this.refresh();
          this.$refs.baseRelevance.close();
        });
      },
    }
  }
</script>

<style scoped>

  /deep/ .select-menu {
    margin-bottom: 15px;
  }

  /deep/ .environment-select {
    float: right;
    margin-right: 10px;
  }

</style>
