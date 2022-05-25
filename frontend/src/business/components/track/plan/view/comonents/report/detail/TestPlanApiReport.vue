<template>
  <test-plan-report-container id="api" :title="$t('test_track.report.analysis_api')">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane v-if="resultEnable" :label="$t('test_track.report.test_result')" name="first">
        <api-result :api-result="report.apiResult"/>
      </el-tab-pane>
      <el-tab-pane v-if="failureEnable" name="second">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.fail_case')" :count="failureSize"/>
        </template>
        <api-cases :is-db="isDb" :share-id="shareId" :is-share="isShare" :report="report" :is-template="isTemplate"
                   :plan-id="planId" @setSize="setFailureSize"/>
      </el-tab-pane>
      <el-tab-pane style="min-height: 500px" name="third" v-if="errorReportEnable">
        <template v-slot:label>
          <tab-pane-count :title="$t('error_report_library.option.name')" :count="errorReportSize"/>
        </template>
        <api-cases :is-db="isDb" :is-error-report="true" :share-id="shareId" :is-share="isShare" :report="report"
                   :is-template="isTemplate" :plan-id="planId" @setSize="setErrorReportSize"/>
      </el-tab-pane>
      <el-tab-pane style="min-height: 500px" name="fourth" v-if="unExecuteEnable">
        <template v-slot:label>
          <tab-pane-count :title="$t('api_test.home_page.detail_card.unexecute')" :count="unExecuteSize"/>
        </template>
        <api-cases :is-db="isDb" :is-un-execute="true" :share-id="shareId" :is-share="isShare" :report="report"
                   :is-template="isTemplate" :plan-id="planId" @setSize="setUnExecuteSize"/>
      </el-tab-pane>

      <el-tab-pane style="min-height: 500px" name="fifth" v-if="allEnable">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.all_case')" :count="allSize"/>
        </template>
        <api-cases :is-db="isDb" :is-all="true" :share-id="shareId" :is-share="isShare" :report="report"
                   :is-template="isTemplate" :plan-id="planId" @setSize="setAllSize"/>
      </el-tab-pane>
    </el-tabs>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import ApiResult from "@/business/components/track/plan/view/comonents/report/detail/component/ApiResult";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import ApiCases from "@/business/components/track/plan/view/comonents/report/detail/component/ApiCases";
import TabPaneCount from "@/business/components/track/plan/view/comonents/report/detail/component/TabPaneCount";

export default {
  name: "TestPlanApiReport",
  components: {TabPaneCount, ApiCases, TestPlanReportContainer, ApiResult, MsFormDivider},
  data() {
    return {
      activeName: 'first',
      failureSize: 0,
      errorReportSize: 0,
      unExecuteSize:0,
      allSize: 0,
    };
  },
  props: [
    'report', 'planId', 'isTemplate', 'isShare', 'shareId', 'isDb'
  ],
  computed: {
    resultEnable() {
      let disable = this.report.config && this.report.config.api.children.result.enable === false;
      return !disable;
    },
    failureEnable() {
      let disable = this.report.config && this.report.config.api.children.failure.enable === false;
      return !disable;
    },
    errorReportEnable() {
      let disable = this.report.config && this.report.config.api.children.errorReport && this.report.config.api.children.errorReport.enable === false;
      return !disable;
    },
    unExecuteEnable() {
      let disable = this.report.config && this.report.config.api.children.unExecute && this.report.config.api.children.unExecute.enable === false;
      return !disable;
    },
    allEnable() {
      let disable = this.report.config && this.report.config.api.children.all.enable === false;
      return !disable;
    },
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
    setUnExecuteSize(size){
      this.unExecuteSize = size;
    },
    setAllSize(size) {
      this.allSize = size;
    },
    handleClick(tab, event) {
    }
  }

}
</script>

<style scoped>

</style>
