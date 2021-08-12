<template>
  <test-plan-report-container :title="'概览'">
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
            {{ (report.executeRate * 100).toFixed(0) + '%'}}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="'通过率' + ':'">
            {{ (report.passRate * 100).toFixed(0) + '%'}}
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item :label="'报告总结'">
        <el-link @click="isEdit = true">
          编辑
        </el-link>
      </el-form-item>
      <div v-if="!isEdit">
        {{report.summary}}
      </div>
      <div v-else>
        <el-input
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 4}"
          placeholder="请输入内容"
          v-model="report.summary" @blur="saveSummary"/>
      </div>
    </el-form>
  </test-plan-report-container>

</template>

<script>
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import {editPlanReport} from "@/network/test-plan";
import TestPlanReportContainer
  from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContainer";
import {timestampFormatDate} from "@/common/js/filter";
export default {
  name: "TestPlanReportHeader",
  components: {TestPlanReportContainer, MsFormDivider},
  props: {
    planId: String,
    report: Object
  },
  data() {
    return {
      result: {},
      isEdit: false
    }
  },
  computed: {
    showTime() {
      let startTime = 'NaN';
      let endTime = 'NaN';
      if (this.report.startTime) {
        startTime = timestampFormatDate(this.report.startTime, false).substring(0, 10);
      }
      if (this.report.endTime) {
        endTime = timestampFormatDate(this.report.endTime, false).substring(0, 10);
      }
      return startTime + ' ~ ' + endTime;
    }
  },
  methods: {
    saveSummary() {
      editPlanReport({
        id: this.planId,
        reportSummary: this.report.summary ? this.report.summary : ''
      });
      this.isEdit = false;
    }
  }
}
</script>

<style scoped>
.el-link >>> .el-link--inner {
  line-height: 40px;
  font-size: 14px;
  height: 42.8px;
}

.form-info {
  padding: 20px;
}
</style>
