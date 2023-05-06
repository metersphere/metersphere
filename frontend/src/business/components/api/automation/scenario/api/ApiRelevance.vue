<template>
  <test-case-relevance-base
    :is-across-space="isAcrossSpace"
    @setProject="setProject"
    :dialog-title="$t('api_test.definition.api_import')"
    ref="baseRelevance">
    <template v-slot:aside>
      <ms-api-module
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :is-relevance="true"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

    <scenario-relevance-api-list
      v-if="isApiListEnable"
      :project-id="projectId"
      :version-filters="versionFilters"
      :current-version="currentVersion"
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange"
      @selectCountChange="setSelectCounts"
      ref="apiList">
      <template v-slot:version>
        <version-select v-xpack :project-id="projectId" :default-version="currentVersion"
                        @changeVersion="currentVersionChange"/>
      </template>
    </scenario-relevance-api-list>

    <scenario-relevance-case-list
      v-if="!isApiListEnable"
      :project-id="projectId"
      :version-filters="versionFilters"
      :current-version="currentVersion"
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange"
      @selectCountChange="setSelectCounts"
      ref="apiCaseList">
      <template v-slot:version>
        <version-select v-xpack :project-id="projectId" :default-version="currentVersion"
                        @changeVersion="currentVersionChange"/>
      </template>
    </scenario-relevance-case-list>

    <template v-slot:headerBtn>
      <!--  显示数量    -->
      <table-select-count-bar :count="selectCounts" style="float: left; margin: 5px;"/>

      <el-button size="mini" icon="el-icon-refresh" @click="refresh"/>
      <el-button type="primary" @click="copy" :loading="buttonIsWorking" @keydown.enter.native.prevent size="mini">
        {{ $t('commons.copy') }}
      </el-button>
      <el-button v-if="!isApiListEnable" type="primary" :loading="buttonIsWorking" @click="reference" size="mini"
                 @keydown.enter.native.prevent>
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
import {hasLicense} from "@/common/js/utils";
import TableSelectCountBar from "@/business/components/api/automation/scenario/api/TableSelectCountBar";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};

export default {
  name: "ApiRelevance",
  components: {
    TableSelectCountBar,
    'VersionSelect': VersionSelect.default,
    TestCaseRelevanceBase,
    RelevanceDialog,
    ScenarioRelevanceApiList,
    MsMainContainer, MsAsideContainer, MsContainer, MsApiModule, ScenarioRelevanceCaseList
  },
  props:{
    isAcrossSpace:{
      type:Boolean,
      default() {
        return false;
      }
    }
  },
  data() {
    return {
      buttonIsWorking: false,
      result: {},
      currentProtocol: null,
      saveOtherPageData: false,
      selectNodeIds: [],
      moduleOptions: {},
      isApiListEnable: true,
      projectId: "",
      versionFilters: [],
      currentVersion: null,
      selectCounts: null,
    };
  },
  watch: {
    projectId() {
      this.refresh();
      this.$refs.nodeTree.list(this.projectId);
      this.getVersionOptions();
    }
  },
  methods: {
    changeButtonLoadingType() {
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
        if (this.$refs.apiList.total > 500) {
          this.$alert(this.$t('api_test.automation.scenario_step_ref_message', [this.$refs.apiList.total]) + '？', '', {
            callback: (action) => {
              if (action === 'confirm') {
                this.getApiList(params,apis, reference,false);
              } else {
                this.buttonIsWorking = false;
              }
            }
          });
        } else  {
          this.getApiList(params,apis, reference, true);
        }
      } else {
        let params = this.$refs.apiCaseList.getConditions();
        let apiCases = this.$refs.apiCaseList.selectRows;
        apiCases.forEach(apiCase => {
          apiCase.projectId = this.projectId;
        });
        if (this.$refs.apiCaseList.total > 500) {
          this.$alert(this.$t('api_test.automation.scenario_step_ref_message', [this.$refs.apiCaseList.total]) + '？', '', {
            callback: (action) => {
              if (action === 'confirm') {
                this.getApiCaseList(params,apiCases, reference, false);
              } else {
                this.buttonIsWorking = false;
              }
            }
          });
        } else {
          this.getApiCaseList(params,apiCases, reference,true);
        }
      }
    },
    getApiList(params,apis, reference, isContinue) {
      this.result = this.$post("/api/definition/list/batch", params, (response) => {
        let apis = response.data;
        if (apis.length === 0) {
          this.$warning('请选择接口');
          this.buttonIsWorking = false;
        } else {
          if (apis.length > 500 && isContinue) {
            this.$alert(this.$t('api_test.automation.scenario_step_ref_message', [apis.length]) + '？', '', {
              callback: (action) => {
                if (action === 'confirm') {
                  this.$emit('save', apis, 'API', reference);
                  this.$refs.baseRelevance.close();
                } else {
                  this.buttonIsWorking = false;
                }
              }
            });
          }else {
            this.$emit('save', apis, 'API', reference);
            this.$refs.baseRelevance.close();
          }
        }
      }, (error) => {
        this.buttonIsWorking = false;
      });
    },
    getApiCaseList(params,apiCases, reference,isContinue) {
      this.result = this.$post("/api/testcase/get/caseBLOBs/request", params, (response) => {
         apiCases = response.data;
        if (apiCases.length === 0) {
          this.$warning('请选择案例');
          this.buttonIsWorking = false;
        } else {
          if (apiCases.length > 500 && isContinue) {
            this.$alert(this.$t('api_test.automation.scenario_step_ref_message', [apiCases.length]) + '？','', {
                callback: (action) => {
                  if (action === 'confirm') {
                    this.$emit('save', apiCases, 'CASE', reference);
                    this.$refs.baseRelevance.close();
                  } else {
                    this.buttonIsWorking = false;
                  }
                }
              }
            );
          } else {
            this.$emit('save', apiCases, 'CASE', reference);
            this.$refs.baseRelevance.close();
          }
        }
      }, (error) => {
        this.buttonIsWorking = false;
      });
    },
    close() {
      this.$emit('close');
      this.refresh();
      this.$refs.relevanceDialog.close();
    },
    open() {
      this.buttonIsWorking = false;
      this.$refs.baseRelevance.open();
      this.getVersionOptions();
    },
    isApiListEnableChange(data) {
      this.isApiListEnable = data;
      this.selectCounts = 0;
    },
    currentVersionChange(currentVersion) {
      this.currentVersion = currentVersion || null;
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
    getVersionOptions(currentVersion) {
      if (hasLicense()) {
        if (!this.projectId) {
          return;
        }
        this.$get('/project/version/get-project-versions/' + this.projectId, response => {
          if (currentVersion) {
            this.versionFilters = response.data.filter(u => u.id === currentVersion).map(u => {
              return {text: u.name, value: u.id};
            });
          } else {
            this.versionFilters = response.data.map(u => {
              return {text: u.name, value: u.id};
            });
          }
        });
      }
    },
    setSelectCounts(data) {
      this.selectCounts = data;
    },
  }
};
</script>

<style scoped>
/deep/ .filter-input {
  width: 140px !important;
}
.version-select {
  padding-left: 10px;
}
</style>
