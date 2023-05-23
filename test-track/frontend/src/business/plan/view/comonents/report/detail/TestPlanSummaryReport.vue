<template>
  <test-plan-report-container id='summary' :title="$t('test_track.report.report_summary')">
    <template v-slot:title>
      <el-link class="edit-link" v-if="showEdit && !isEdit" @click="isEdit = true">
        <i class="el-icon-edit">{{ $t('commons.edit') }}</i>
      </el-link>
      <el-link class="edit-link" v-if="showEdit && isEdit" @click="saveSummary">
        <i class="el-icon-circle-check">{{ $t('commons.save') }}</i>
      </el-link>
    </template>
    <el-form class="form-info" v-loading="loading">
      <div v-if="!isEdit">
        <div class="rich-text-content" v-html="report.summary"></div>
      </div>
      <div v-else>
        <ms-rich-text :content.sync="report.summary"/>
      </div>
    </el-form>
  </test-plan-report-container>

</template>

<script>
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import {editPlanReport} from "@/api/remote/plan/test-plan";
import TestPlanReportContainer from "@/business/plan/view/comonents/report/detail/TestPlanReportContainer";
import MsRichText from "@/business/case/components/MsRichText";
import {testPlanDbReportEdit} from "@/api/remote/plan/test-plan-report";
import {hasPermission} from "@/business/utils/sdk-utils";
export default {
  name: "TestPlanSummaryReport",
  components: {MsRichText, TestPlanReportContainer, MsFormDivider},
  props: {
    planId: String,
    report: Object,
    isTemplate: Boolean,
    isShare: Boolean,
    isDb: Boolean,
  },
  data() {
    return {
      loading: false,
      isEdit: false
    }
  },
  computed: {
    showEdit() {
      return !this.isTemplate && !this.isShare && hasPermission("PROJECT_TRACK_REPORT:READ+EDIT");
    }
  },
  methods: {
    saveSummary() {
      if (this.isDb) {
        testPlanDbReportEdit({
          testPlanReportId: this.report.id,
          summary: this.report.summary || ''
        });
      } else {
        editPlanReport({
          id: this.planId,
          reportSummary: this.report.summary || ''
        });
      }
      this.isEdit = false;
    }
  }
}
</script>

<style scoped>
.el-link :deep(.el-link--inner) {
  line-height: 40px;
  font-size: 14px;
  height: 42.8px;
}

.form-info {
  padding: 25px;
}

.edit-link {
  margin-left: 10px;
}

.rich-text-content :deep(.table td) {
  border: solid 1px #e6e6e6;
  min-width: 2em;
  padding: .4em;
}
</style>
