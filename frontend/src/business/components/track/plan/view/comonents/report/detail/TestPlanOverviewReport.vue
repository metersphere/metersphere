<template>
  <test-plan-report-container id="overview" :title="$t('test_track.report.overview')">
    <el-form class="form-info" v-loading="result.loading">
      <el-form-item :label="$t('test_track.report.testing_time') + ':'">
        {{showTime}}
      </el-form-item>
      <el-row type="flex" justify="space-between" class="select-time">
        <el-col :span="8">
          <el-form-item :label="$t('test_track.report.total_number_tests') + ':'">
            {{report.caseCount}}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="$t('test_track.report.exacutive_rate') + ':'">
            {{ (report.executeRate ? (report.executeRate * 100).toFixed(1) : 0) + '%'}}
            <ms-instructions-icon :content="$t('test_track.report.exacutive_rate_tip')"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="$t('test_track.report.passing_rate') + ':'">
            {{ (report.passRate ? (report.passRate  * 100 ).toFixed(1) : 0) + '%'}}
            <ms-instructions-icon :content="$t('test_track.report.passing_rate_tip')"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import {timestampFormatDate} from "@/common/js/filter";
import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";
export default {
  name: "TestPlanOverviewReport",
  components: {MsInstructionsIcon, TestPlanReportContainer, MsFormDivider},
  props: {
    report: Object,
  },
  data() {
    return {
      result: {},
      isEdit: false
    }
  },
  computed: {
    showTime() {
      let startTime = '';
      let endTime = '';
      if (this.report.startTime) {
        startTime = timestampFormatDate(this.report.startTime, false);
      }
      if (this.report.endTime) {
        endTime = timestampFormatDate(this.report.endTime, false);
      }
      return startTime + ' ~ ' + endTime;
    }
  },
}
</script>

<style scoped>
.form-info {
  padding: 20px;
}

.el-form-item:first-child {
  margin-bottom: 0;
  height: 60px;
}
</style>
