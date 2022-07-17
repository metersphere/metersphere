<template>
  <test-plan-report-container id="ui" :title="$t('test_track.report.analysis_ui')">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane v-if="resultEnable" :label="$t('test_track.report.test_result')" name="first">
        <ui-result :api-result="report.uiResult"/>
      </el-tab-pane>
      <el-tab-pane v-if="failureEnable" name="second">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.fail_case')" :count="failureSize"/>
        </template>
        <api-cases :is-db="isDb" :share-id="shareId" :is-share="isShare" :report="report" :is-template="isTemplate"
                   :plan-id="planId" @setSize="setFailureSize" :is-ui="true"/>
        <el-button class="rerun-button" plain size="mini" v-if="showRerunBtn && (failureSize > 0 || unExecuteSize > 0) && isRerun" @click="rerun">
          {{ $t('api_test.automation.rerun') }}
        </el-button>
      </el-tab-pane>
      <el-tab-pane style="min-height: 500px" name="fourth" v-if="unExecuteEnable">
        <template v-slot:label>
          <tab-pane-count :title="$t('api_test.home_page.detail_card.unexecute')" :count="unExecuteSize"/>
        </template>
        <api-cases :is-db="isDb" :is-un-execute="true" :share-id="shareId" :is-share="isShare" :report="report"
                   :is-template="isTemplate" :plan-id="planId" @setSize="setUnExecuteSize" :is-ui="true"/>

        <el-button class="rerun-button" plain size="mini" v-if="showRerunBtn && (failureSize > 0 || unExecuteSize > 0) && isRerun" @click="rerun">
          {{ $t('api_test.automation.rerun') }}
        </el-button>
      </el-tab-pane>

      <el-tab-pane style="min-height: 500px" name="fifth" v-if="allEnable">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.all_case')" :count="allSize"/>
        </template>
        <api-cases :is-db="isDb" :is-all="true" :share-id="shareId" :is-share="isShare" :report="report"
                   :is-template="isTemplate" :plan-id="planId" @setSize="setAllSize" :is-ui="true"/>
        <el-button class="rerun-button" plain size="mini" v-if="showRerunBtn && (failureSize > 0 || unExecuteSize > 0) && isRerun" @click="rerun">
          {{ $t('api_test.automation.rerun') }}
        </el-button>

      </el-tab-pane>
    </el-tabs>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import UiResult from "@/business/components/track/plan/view/comonents/report/detail/component/UiResult";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import ApiCases from "@/business/components/track/plan/view/comonents/report/detail/component/ApiCases";
import TabPaneCount from "@/business/components/track/plan/view/comonents/report/detail/component/TabPaneCount";
import {hasLicense} from "@/common/js/utils";

export default {
  name: "TestPlanUiReport",
  components: {TabPaneCount, ApiCases, TestPlanReportContainer, UiResult, MsFormDivider},
  data() {
    return {
      activeName: 'first',
      failureSize: 0,
      errorReportSize: 0,
      unExecuteSize: 0,
      allSize: 0,
      showRerunBtn: true,
    };
  },
  created() {
    this.showRerunBtn = !this.isShare && hasLicense();
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
    errorReportEnable() {
      let disable = this.report.config && this.report.config.ui.children.errorReport && this.report.config.ui.children.errorReport.enable === false;
      return !disable;
    },
    unExecuteEnable() {
      let disable = this.report.config && this.report.config.ui.children.unExecute && this.report.config.ui.children.unExecute.enable === false;
      return !disable;
    },
    allEnable() {
      let disable = this.report.config && this.report.config.ui.children.all.enable === false;
      return !disable;
    },
    isRerun() {
      return ((this.report && this.report.apiFailureCases)
        || (this.report && this.report.unExecuteCases)
        || (this.report && this.report.scenarioFailureCases)
        || (this.report && this.report.unExecuteScenarios)
        || (this.report && this.report.loadFailureCases));
    }
  },
  watch: {
    resultEnable() {
      this.initActiveName();
    },
    failureEnable() {
      this.initActiveName();
    },
    errorReportEnable() {
      this.initActiveName();
    },
    allEnable() {
      this.initActiveName();
    },
  },
  mounted() {
    this.initActiveName();
  },
  methods: {
    initActiveName() {
      if (this.resultEnable) {
        this.activeName = 'first';
      } else if (this.failureEnable) {
        this.activeName = 'second';
      } else if (this.errorReportEnable) {
        this.activeName = 'third';
      } else if (this.allEnable) {
        this.activeName = 'fourth';
      }
    },
    setFailureSize(size) {
      this.failureSize = size;
    },
    setErrorReportSize(size) {
      this.errorReportSize = size;
    },
    setUnExecuteSize(size) {
      this.unExecuteSize = size;
    },
    setAllSize(size) {
      this.allSize = size;
    },
    handleClick(tab, event) {
    },
    rerun() {
      let type = "TEST_PLAN";
      let scenarios = [];
      let cases = [];
      let performanceCases = [];
      let rerunObj = {
        type: type,
        reportId: this.report.id,
        scenarios: scenarios,
        cases: cases,
        performanceCases: performanceCases
      }
      // 获取需要重跑的用例
      if (this.report && this.report.apiFailureCases) {
        this.format(cases, this.report.apiFailureCases);
      }
      if (this.report && this.report.unExecuteCases) {
        this.format(cases, this.report.unExecuteCases);
      }
      // 获取需要重跑的场景
      if (this.report && this.report.scenarioFailureCases) {
        this.format(scenarios, this.report.scenarioFailureCases);
      }
      if (this.report && this.report.unExecuteScenarios) {
        this.format(scenarios, this.report.unExecuteScenarios);
      }
      // 获取需要重跑的性能用例
      if (this.report && this.report.loadFailureCases) {
        this.format(performanceCases, this.report.loadFailureCases);
      }
      this.$post('/ui/test/exec/rerun', rerunObj, res => {
        if (res.data !== 'SUCCESS') {
          this.$error(res.data);
        } else {
          this.$success(this.$t('api_test.automation.rerun_success'));
        }
      });
    },
    format(cases, datas) {
      if (this.report && datas) {
        datas.forEach(item => {
          if (item) {
            let obj = {id: item.id, reportId: item.reportId, userId: item.createUser};
            cases.push(obj);
          }
        });
      }
    }
  }
}
</script>

<style scoped>
.rerun-button {
  position: absolute;
  top: 10px;
  right: 10px;
  margin-right: 10px;
  z-index: 1100;
  background-color: #F2F9EF;
  color: #87C45D;
}
</style>
