<template>
  <test-plan-report-container id="ui" :title="$t('test_track.report.analysis_ui')">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane v-if="resultEnable" :label="$t('test_track.report.test_result')" name="first">
        <ui-result :ui-result="report.uiResult"/>
      </el-tab-pane>
      <el-tab-pane v-if="failureEnable" name="second">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.fail_case')" :count="failureSize"/>
        </template>
        <ui-scenario-result :is-db="isDb" :share-id="shareId" :is-share="isShare"
                            :report="report" :is-template="isTemplate" :plan-id="planId"
                            :ui-all-cases="uiAllCases"
                            :filter-status="['Error']"
                            @setSize="setFailureSize"/>
      </el-tab-pane>
      <el-tab-pane style="min-height: 500px" name="third" v-if="unExecuteEnable">
        <template v-slot:label>
          <tab-pane-count :title="$t('api_test.home_page.detail_card.unexecute')" :count="unExecuteSize"/>
        </template>
        <ui-scenario-result :is-db="isDb" :share-id="shareId" :is-share="isShare"
                            :report="report" :is-template="isTemplate" :plan-id="planId"
                            :ui-all-cases="uiAllCases"
                            :filter-status="['unexecute', 'STOP']"
                            @setSize="setUnExecuteSize"/>
      </el-tab-pane>

      <el-tab-pane style="min-height: 500px" name="fifth" v-if="allEnable">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.all_case')" :count="allSize"/>
        </template>
        <ui-scenario-result :is-db="isDb" :share-id="shareId" :is-share="isShare"
                            :report="report" :is-template="isTemplate" :plan-id="planId"
                            :ui-all-cases="uiAllCases"
                            @setSize="setAllSize"/>
      </el-tab-pane>
    </el-tabs>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import UiResult from "@/business/components/track/plan/view/comonents/report/detail/component/UiResult";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import TabPaneCount from "@/business/components/track/plan/view/comonents/report/detail/component/TabPaneCount";
import {hasLicense} from "@/common/js/utils";
import {
  getPlanUiScenarioAllCase,
  getSharePlanUiScenarioAllCase,
} from "@/network/test-plan";
import UiScenarioResult from "@/business/components/track/plan/view/comonents/report/detail/component/UiScenarioResult";

export default {
  name: "TestPlanUiReport",
  components: {UiScenarioResult, TabPaneCount, TestPlanReportContainer, UiResult, MsFormDivider},
  data() {
    return {
      activeName: 'first',
      failureSize: 0,
      unExecuteSize: 0,
      allSize: 0,
      uiAllCases: []
    };
  },
  props: [
    'report', 'planId', 'isTemplate', 'isShare', 'shareId', 'isDb'
  ],
  computed: {
    resultEnable() {
      let disable = this.report.config && this.report.config.ui.children.result.enable === false;
      return !disable;
    },
    failureEnable() {
      let disable = this.report.config && this.report.config.ui.children.failure.enable === false;
      return !disable;
    },
    unExecuteEnable() {
      let disable = this.report.config && this.report.config.ui.children.unExecute && this.report.config.ui.children.unExecute.enable === false;
      return !disable;
    },
    allEnable() {
      let disable = this.report.config && this.report.config.ui.children.all.enable === false;
      return !disable;
    }
  },
  watch: {
    resultEnable() {
      this.initActiveName();
    },
    failureEnable() {
      this.initActiveName();
    },
    allEnable() {
      this.initActiveName();
    },
    'report.config'() {
      this.getAllUiCase();
    }
  },
  mounted() {
    this.initActiveName();
    this.getAllUiCase();
  },
  methods: {
    initActiveName() {
      if (this.resultEnable) {
        this.activeName = 'first';
      } else if (this.failureEnable) {
        this.activeName = 'second';
      } else if (this.allEnable) {
        this.activeName = 'third';
      }
    },
    setFailureSize(size) {
      this.failureSize = size;
    },
    setUnExecuteSize(size) {
      this.unExecuteSize = size;
    },
    setAllSize(size) {
      this.allSize = size;
    },
    handleClick(tab, event) {
    },
    getAllUiCase() {
      if (this.isTemplate || this.isDb) {
        this.uiAllCases = this.report.uiAllCases ? this.report.uiAllCases : [];
      } else if (this.isShare) {
        let param = this.getStatusList();
        if (param) {
          getSharePlanUiScenarioAllCase(this.shareId, this.planId, param, (data) => {
            this.uiAllCases = data;
          });
        }
      } else {
        let param = this.getStatusList();
        if (param) {
          this.result = getPlanUiScenarioAllCase(this.planId, param, (data) => {
            this.uiAllCases = data;
          });
        }
      }
    },
    getStatusList() {
      let statusList = [];
      if (this.allEnable) {
        return statusList;
      }
      if (this.failureEnable) {
        statusList.push('Error');
      }
      if (this.unExecuteEnable) {
        statusList.push('UnExecute');
      }
      return statusList.length > 0 ? statusList : null;
    }
  }
}
</script>

<style scoped>
</style>
