<template>
  <test-plan-report-container :title="'接口用例统计分析'">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane v-if="resultEnable" label="测试结果" name="first">
        <api-result :api-result="report.apiResult"/>
      </el-tab-pane>
      <el-tab-pane v-if="failureEnable" label="失败用例" name="second">
        <api-cases :is-db="isDb" :share-id="shareId" :is-share="isShare" :report="report" :is-template="isTemplate" :plan-id="planId"/>
      </el-tab-pane>
      <el-tab-pane label="所有用例" name="third" v-if="allEnable">
        <api-cases :is-db="isDb" :is-all="true" :share-id="shareId" :is-share="isShare" :report="report" :is-template="isTemplate" :plan-id="planId"/>
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
export default {
  name: "TestPlanApiReport",
  components: {ApiCases, TestPlanReportContainer, ApiResult, MsFormDivider},
  data() {
    return {
      activeName: 'first'
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
      } else if (this.allEnable) {
        this.activeName = 'third';
      }
    },
    handleClick(tab, event) {
    }
  }

}
</script>

<style scoped>

</style>
