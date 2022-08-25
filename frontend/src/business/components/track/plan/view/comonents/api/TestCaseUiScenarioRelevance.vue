<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    ref="baseRelevance">

    <template v-slot:aside>
      <ui-scenario-module
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :relevance-project-id="projectId"
        :is-read-only="true"
        :show-case-num="false"
        ref="nodeTree"/>
    </template>

    <relevance-ui-scenario-list
      :select-node-ids="selectNodeIds"
      :trash-enable="trashEnable"
      :version-enable="versionEnable"
      :plan-id="planId"
      :project-id="projectId"
      @selectCountChange="setSelectCounts"
      ref="apiScenarioList"/>

  </test-case-relevance-base>

</template>

<script>

import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
import {strMapToObj} from "../../../../../../../common/js/utils";
import ApiCaseSimpleList from "../../../../../api/definition/components/list/ApiCaseSimpleList";
import MsApiScenarioList from "../../../../../api/automation/scenario/ApiScenarioList";
import RelevanceUiScenarioList from "@/business/components/track/plan/view/comonents/api/RelevanceUiScenarioList";
import {ENV_TYPE} from "@/common/js/constants";
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const UiScenarioModule = requireComponent.keys().length > 0 ? requireComponent("./ui/automation/scenario/UiScenarioModule.vue") : {};

export default {
  name: "TestCaseUiScenarioRelevance",
  components: {
    RelevanceUiScenarioList,
    MsApiScenarioList,
    ApiCaseSimpleList,
    "UiScenarioModule" : UiScenarioModule.default,
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
      condition: {},
      currentRow: {},
      projectId: ""
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
  methods: {
    open() {
      this.$refs.baseRelevance.open();
      if (this.$refs.apiScenarioList) {
        this.$refs.apiScenarioList.clear();
        this.$refs.apiScenarioList.search();
      }
    },
    setProject(projectId) {
      this.projectId = projectId;
    },

    refresh(data) {
      this.$refs.apiScenarioList.search(data);
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

    postRelevance() {
      let url = '/ui/automation/relevance';
      const envMap = this.$refs.apiScenarioList.projectEnvMap;
      let envType = this.$refs.apiScenarioList.environmentType;
      let map = this.$refs.apiScenarioList.map;
      let envGroupId = this.$refs.apiScenarioList.envGroupId;

      if (envType === ENV_TYPE.JSON && (!envMap || envMap.size < 1)) {
        this.$warning(this.$t("api_test.environment.select_environment"));
        return false;
      } else if (envType === ENV_TYPE.GROUP && !envGroupId) {
        this.$warning(this.$t("api_test.environment.select_environment"));
        return false;
      }
      let param = {};
      param.planId = this.planId;
      param.mapping = strMapToObj(map);
      param.envMap = strMapToObj(envMap);
      param.environmentType = envType;
      param.envGroupId = envGroupId;

      this.result = this.$post(url, param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh');
        this.refresh();
        this.autoCheckStatus();
        this.$refs.baseRelevance.close();
      });
    },
    getAllId(param) {
      return new Promise((resolve) => {
        this.$post("/test/plan/uiScenario/case/relevance/list/ids", param, (data) => {
          resolve(data.data);
        })
      });
    },
    async saveCaseRelevance() {
      let selectIds = [];
      let url = '/ui/automation/relevance';
      let selectRows = this.$refs.apiScenarioList.selectRows;
      const envMap = this.$refs.apiScenarioList.projectEnvMap;
      let map = this.$refs.apiScenarioList.map;
      let envGroupId = this.$refs.apiScenarioList.envGroupId;

      selectRows.forEach(row => {
        selectIds.push(row.id);
      })

      let param = {};
      param.planId = this.planId;
      param.mapping = strMapToObj(map);
      param.envMap = strMapToObj(envMap);
      param.envGroupId = envGroupId;
      param.selectIds = selectIds;

      //查找所有数据
      let params = this.$refs.apiScenarioList.condition;
      if (params.selectAll) {
        let result = await this.getAllId(params);
        param.selectIds = result;
      }

      this.result = this.$post(url, param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh');
        this.refresh();
        this.autoCheckStatus();
        this.$refs.baseRelevance.close();
      });
    },
    autoCheckStatus() { //  检查执行结果，自动更新计划状态
      if (!this.planId) {
        return;
      }
      this.$post('/test/plan/autoCheck/' + this.planId, (response) => {
      });
    },
    setSelectCounts(data) {
      this.$refs.baseRelevance.selectCounts = data;
    }
  }
};
</script>

<style scoped>

/deep/ .select-menu {
  margin-bottom: 15px;
}

/deep/ .environment-select {
  float: right;
  margin-right: 10px;
}

/deep/ .module-input {
  width: 243px;
}

/deep/ .table-select-icon {
  position: absolute;
  display: inline-block;
  top: -13px !important;
  left: -30px;
  width: 30px;
}

</style>
