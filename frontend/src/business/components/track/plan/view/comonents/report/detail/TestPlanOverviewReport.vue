<template>
  <test-plan-report-container id="overview" :title="'概览'">
    <el-form class="form-info" v-loading="result.loading">
      <el-form-item :label="$t('测试时间') + ':'">
        {{showTime}}
      </el-form-item>
      <el-row type="flex" justify="space-between" class="select-time">
        <el-col :span="8">
          <el-form-item :label="'测试总数' + ':'">
            {{report.caseCount}}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="'执行率' + ':'">
            {{ (report.executeRate ? (report.executeRate * 100).toFixed(1) : 0) + '%'}}
            <ms-instructions-icon content="执行过的用例/所有用例 * 100%"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="'通过率' + ':'">
            {{ (report.passRate ? (report.passRate  * 100 ).toFixed(1) : 0) + '%'}}
            <ms-instructions-icon content="执行通过用例/所有用例 * 100%"/>
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
