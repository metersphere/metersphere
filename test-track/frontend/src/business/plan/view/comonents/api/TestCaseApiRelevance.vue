<template>
  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    @close="close"
    :plan-id="planId"
    :is-saving="isSaving"
    ref="baseRelevance"
  >
    <template v-slot:aside>
      <ms-api-module
        class="node-tree"
        :relevance-project-id="projectId"
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :show-case-num="false"
        :is-read-only="true"
        :is-relevance="true"
        ref="nodeTree"
      />
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
      @selectCountChange="setSelectCounts"
      ref="apiList"
    >
      <template v-slot:version>
        <mx-version-select
          v-xpack
          :project-id="projectId"
          @changeVersion="changeVersion($event, 'api')"
          margin-left="10"
        />
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
      @selectCountChange="setSelectCounts"
      ref="apiCaseList"
    >
      <template v-slot:version>
        <mx-version-select
          v-xpack
          :project-id="projectId"
          @changeVersion="changeVersion($event, 'case')"
          margin-left="10"
        />
      </template>
    </relevance-case-list>
  </test-case-relevance-base>
</template>

<script>
import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";
import {
  apiDefinitionListBatch,
  apiDefinitionRelevance,
} from "@/api/remote/api/api-definition";
import {
  apiTestCaseListBlobs,
  apiTestCaseRelevance,
} from "@/api/remote/api/api-case";
import RelevanceApiList from "@/business/plan/view/comonents/api/RelevanceApiList";
import RelevanceCaseList from "@/business/plan/view/comonents/api/RelevanceCaseList";
import MsApiModule from "@/business/plan/view/comonents/api/module/ApiModule";
import { getVersionFilters } from "@/business/utils/sdk-utils";

export default {
  name: "TestCaseApiRelevance",
  components: {
    MsApiModule,
    RelevanceCaseList,
    RelevanceApiList,
    TestCaseRelevanceBase,
    MxVersionSelect,
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
      isSaving: false,
    };
  },
  props: {
    planId: {
      type: String,
    },
    versionEnable: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    planId() {
      this.condition.planId = this.planId;
    },
    projectId() {
      this.getVersionOptions();
    },
  },
  mounted() {
    this.getVersionOptions();
  },
  methods: {
    close() {
      this.projectId = "";
    },
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
      if (this.$refs.apiList) {
        this.$refs.apiList.condition.selectAll = false;
      }
      if (this.$refs.apiCaseList) {
        this.$refs.apiCaseList.condition.selectAll = false;
      }
      this.condition.selectAll = false;
      this.selectNodeIds = nodeIds;
    },
    handleProtocolChange(protocol) {
      this.currentProtocol = protocol;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    },

    saveCaseRelevance() {
      let url = "";
      let environmentId = undefined;
      let selectIds = [];
      this.isSaving = true;
      if (this.isApiListEnable) {
        //查找所有数据
        let params = this.$refs.apiList.getConditions();
        apiDefinitionListBatch(params)
          .then((response) => {
            let apis = response.data;
            environmentId = this.$refs.apiList.environmentId;
            selectIds = Array.from(apis).map((row) => row.id);
            let protocol = this.$refs.apiList.currentProtocol;
            this.postRelevance(
              apiDefinitionRelevance,
              environmentId,
              selectIds,
              protocol
            );
          })
          .catch(() => {
            this.isSaving = false;
          });
      } else {
        let params = this.$refs.apiCaseList.getConditions();
        apiTestCaseListBlobs(params)
          .then((response) => {
            let apiCases = response.data;
            environmentId = this.$refs.apiCaseList.environmentId;
            selectIds = Array.from(apiCases).map((row) => row.id);
            let protocol = this.$refs.apiCaseList.currentProtocol;
            this.postRelevance(
              apiTestCaseRelevance,
              environmentId,
              selectIds,
              protocol
            );
          })
          .catch(() => {
            this.isSaving = false;
          });
      }
    },

    postRelevance(relevanceList, environmentId, selectIds, protocol) {
      let param = {};
      if (protocol !== "DUBBO") {
        if (!environmentId) {
          this.isSaving = false;
          this.$warning(this.$t("api_test.environment.select_environment"));
          return;
        }
      }
      if (selectIds.length < 1) {
        this.isSaving = false;
        this.$warning(this.$t("test_track.plan_view.please_choose_test_case"));
        return;
      }
      param.planId = this.planId;
      param.selectIds = selectIds;
      param.environmentId = environmentId;
      relevanceList(param)
        .then(() => {
          this.$success(this.$t("plan.relevance_case_success"));
          this.$emit("refresh");
          this.refresh();
          this.isSaving = false;
        })
        .catch(() => {
          this.isSaving = false;
        });
    },
    getVersionOptions() {
      getVersionFilters(this.projectId).then(
        (r) => (this.versionFilters = r.data)
      );
    },
    changeVersion(currentVersion, type) {
      if (type == "api") {
        this.$refs.apiList.condition.versionId = currentVersion || null;
        this.$refs.apiList.initTable();
      } else {
        this.$refs.apiCaseList.condition.versionId = currentVersion || null;
        this.$refs.apiCaseList.initTable();
      }
    },
    setSelectCounts(data) {
      this.$refs.baseRelevance.selectCounts = data;
    },
  },
};
</script>

<style scoped>
:deep(.select-menu) {
  margin-bottom: 15px;
}

:deep(.environment-select) {
  float: right;
  margin-right: 10px;
}

.node-tree {
  max-height: calc(75vh - 120px);
  overflow-y: auto;
}
</style>
