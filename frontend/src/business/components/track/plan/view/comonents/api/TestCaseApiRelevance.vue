<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    ref="baseRelevance">

    <template v-slot:aside>
      <ms-api-module
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
        :type="'edit'"
        ref="nodeTree"/>
    </template>

    <!-- 列表集合 -->
    <api-list
      v-if="isApiListEnable"
      :current-protocol="currentProtocol"
      :currentRow="currentRow"
      :select-node-ids="selectNodeIds"
      :trash-enable="trashEnable"
      :is-api-list-enable="isApiListEnable"
      :is-case-relevance="true"
      :relevance-project-id="projectId"
      :is-relevance="true"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiList"/>

    <!--测试用例列表-->
    <api-case-simple-list
      v-if="!isApiListEnable"
      :current-protocol="currentProtocol"
      :currentRow="currentRow"
      :select-node-ids="selectNodeIds"
      :trash-enable="trashEnable"
      :is-api-list-enable="isApiListEnable"
      :is-case-relevance="true"
      :relevance-project-id="projectId"
      :plan-id="planId"
      :model="'relevance'"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiCaseList"/>


  </test-case-relevance-base>

</template>

<script>

  import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
  import MsApiModule from "../../../../../api/definition/components/module/ApiModule";
  import {getCurrentProjectID} from "../../../../../../../common/js/utils";
  import ApiList from "../../../../../api/definition/components/list/ApiList";
  import ApiCaseSimpleList from "../../../../../api/definition/components/list/ApiCaseSimpleList";

  export default {
    name: "TestCaseApiRelevance",
    components: {
      ApiCaseSimpleList,
      ApiList,
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
        this.$refs.baseRelevance.open();
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

        let param = {};
        let url = '';
        let environmentId = undefined;
        let selectIds = [];
        if (this.isApiListEnable) {
          url = '/api/definition/relevance';
          environmentId = this.$refs.apiList.environmentId;
          selectIds = Array.from(this.$refs.apiList.selectRows).map(row => row.id);
        } else {
          url = '/api/testcase/relevance';
          environmentId = this.$refs.apiCaseList.environmentId;
          selectIds = Array.from(this.$refs.apiCaseList.selectRows).map(row => row.id);
        }

        if (!environmentId) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }

        param.planId = this.planId;
        param.selectIds = selectIds;
        param.environmentId = environmentId;
        // param.request = this.condition;
        // 选择全选则全部加入到评审，无论是否加载完全部
        // if (this.testCases.length === param.testCaseIds.length) {
        //   param.testCaseIds = ['all'];
        // }

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
