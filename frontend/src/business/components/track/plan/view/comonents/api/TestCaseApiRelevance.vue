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
      :versionFilters="versionFilters"
      :version-enable="versionEnable"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiList">
      <template v-slot:version>
        <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion($event,'api')" margin-left="10"/>
      </template>
    </relevance-api-list>

    <relevance-case-list
      v-if="!isApiListEnable"
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :is-api-list-enable="isApiListEnable"
      :project-id="projectId"
      :is-test-plan="true"
      :versionFilters="versionFilters"
      :version-enable="versionEnable"
      :plan-id="planId"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiCaseList">
      <template v-slot:version>
        <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion($event, 'case')"
                        margin-left="10"/>
      </template>
    </relevance-case-list>

  </test-case-relevance-base>

</template>

<script>

  import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
  import MsApiModule from "../../../../../api/definition/components/module/ApiModule";
  import RelevanceApiList from "../../../../../api/automation/scenario/api/RelevanceApiList";
  import RelevanceCaseList from "../../../../../api/automation/scenario/api/RelevanceCaseList";
  import {getCurrentProjectID, hasLicense} from "@/common/js/utils";
  const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
  const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};

  export default {
    name: "TestCaseApiRelevance",
    components: {
      RelevanceCaseList,
      RelevanceApiList,
      MsApiModule,
      TestCaseRelevanceBase,
      'VersionSelect': VersionSelect.default,
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
        projectId: "",
        versionFilters: [],
      };
    },
    props: {
      planId: {
        type: String
      },
      versionEnable: {
        type: Boolean,
        default: false
      }
    },
    watch: {
      planId() {
        this.condition.planId = this.planId;
      },
    },
    mounted() {
      this.getVersionOptions();
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
        // 切换项目 清空环境和选中行
        if (this.$refs.apiList) {
          this.$refs.apiList.clearEnvAndSelect();
        }
        if (this.$refs.apiCaseList) {
          this.$refs.apiCaseList.clearEnvAndSelect();
        }
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
            let protocol = this.$refs.apiList.currentProtocol;
            this.postRelevance(url, environmentId, selectIds, protocol);
          });
        } else {
          let params = this.$refs.apiCaseList.getConditions();
          this.result = this.$post("/api/testcase/get/caseBLOBs/request", params, (response) => {
            let apiCases = response.data;
            url = '/api/testcase/relevance';
            environmentId = this.$refs.apiCaseList.environmentId;
            selectIds = Array.from(apiCases).map(row => row.id);
            let protocol = this.$refs.apiCaseList.currentProtocol;
            this.postRelevance(url, environmentId, selectIds, protocol);
          });
        }

      },

      postRelevance(url, environmentId, selectIds, protocol) {
        let param = {};
        if (protocol !== 'DUBBO') {
          if (!environmentId) {
            this.$warning(this.$t('api_test.environment.select_environment'));
            return;
          }
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
      getVersionOptions() {
        if (hasLicense()) {
          this.$get('/project/version/get-project-versions/' + getCurrentProjectID(), response => {
            this.versionOptions = response.data;
            this.versionFilters = response.data.map(u => {
              return {text: u.name, value: u.id};
            });
          });
        }
      },
      changeVersion(currentVersion, type) {
        if (type == 'api') {
          this.$refs.apiList.condition.versionId = currentVersion || null;
          this.$refs.apiList.initTable();
        } else {
          this.$refs.apiCaseList.condition.versionId = currentVersion || null;
          this.$refs.apiCaseList.initTable();
        }
      }
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
