<template>
  <test-plan-report-container id="load" :title="$t('test_track.report.analysis_load')">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane v-if="resultEnable" :label="$t('test_track.report.test_result')" name="first">
        <load-result :load-result="report.loadResult"/>
      </el-tab-pane>
      <el-tab-pane v-if="failureEnable" name="second">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.fail_case')" :count="failureSize"/>
        </template>
        <load-all-result :is-db="isDb" :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :is-all="false"
                         :report="report" :plan-id="planId" @setSize="setFailureSize"/>
      </el-tab-pane>
      <el-tab-pane name="third" v-if="allEnable" style="min-height: 550px">
        <template v-slot:label>
          <tab-pane-count :title="$t('test_track.report.all_case')" :count="allSize"/>
        </template>
        <load-all-result :is-db="isDb" :share-id="shareId" :is-share="isShare" :is-template="isTemplate" :is-all="true"
                         :report="report" :plan-id="planId" @setSize="setAllSize"/>
      </el-tab-pane>
    </el-tabs>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import LoadResult from "@/business/components/track/plan/view/comonents/report/detail/component/LoadResult";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import LoadFailureResult
  from "@/business/components/track/plan/view/comonents/report/detail/component/LoadFailureResult";
import LoadAllResult from "@/business/components/track/plan/view/comonents/report/detail/component/LoadAllResult";
import TabPaneCount from "@/business/components/track/plan/view/comonents/report/detail/component/TabPaneCount";

export default {
  name: "TestPlanLoadReport",
  components: {
    TabPaneCount,
    LoadAllResult,
    LoadFailureResult,
    TestPlanReportContainer,
    LoadResult, MsFormDivider},
  data() {
    return {
      activeName: 'first',
      failureSize: 0,
      allSize: 0,
    };
  },
  props: [
    'report',
    'planId',
    'isTemplate',
    'isShare',
    'shareId',
    'isDb'
  ],
  computed: {
    resultEnable() {
      let disable = this.report.config && this.report.config.load.children.result.enable === false;
      return !disable;
    },
    failureEnable() {
      let disable = this.report.config && this.report.config.load.children.failure.enable === false;
      return !disable;
    },
    allEnable() {
      let disable = this.report.config && this.report.config.load.children.all.enable === false;
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
    setFailureSize(size) {
      this.failureSize = size;
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
