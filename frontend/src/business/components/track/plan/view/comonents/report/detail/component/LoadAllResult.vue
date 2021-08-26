<template>
    <div class="container">
      <el-row class="scenario-info">
        <el-col :span="7">
          <load-failure-result :is-db="isDb" @rowClick="getReport" :is-all="true" :share-id="shareId" :is-share="isShare" :is-template="isTemplate"
                               :report="report" :plan-id="planId" @setSize="setAllSize"/>
        </el-col>
        <el-col :span="17" >
          <el-card v-show="showResponse">
            <load-case-report-view :is-plan-report="true" :share-id="shareId" :is-share="isShare"
                                   :plan-report-template="response" :report-id="reportId" ref="loadCaseReportView"/>
          </el-card>
          <div class="empty" v-show="!showResponse">内容为空</div>
        </el-col>
      </el-row>
    </div>
</template>

<script>
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {checkoutLoadReport, shareCheckoutLoadReport} from "@/network/test-plan";
import LoadFailureResult
  from "@/business/components/track/plan/view/comonents/report/detail/component/LoadFailureResult";
import LoadCaseReport from "@/business/components/track/plan/view/comonents/load/LoadCaseReport";
import LoadCaseReportView from "@/business/components/track/plan/view/comonents/load/LoadCaseReportView";
export default {
  name: "LoadAllResult",
  components: {LoadCaseReportView, LoadCaseReport, LoadFailureResult, StatusTableItem, MethodTableItem, TypeTableItem},
  props: {
    planId: String,
    report: Object,
    isTemplate: Boolean,
    isShare: Boolean,
    shareId: String,
    isDb: Boolean
  },
  data() {
    return {
      failureTestCases:  [],
      showResponse: false,
      reportId: "",
      response: null
    }
  },
  methods: {
    getReport(row) {
      this.showResponse = false;
      if (this.isTemplate) {
        if (row.response) {
          this.showResponse = true;
          this.response = row.response;
        } else {
          this.response = null;
        }
      } else {
        let param = {
          testPlanLoadCaseId: row.id,
          reportId: row.loadReportId
        }
        if (!row.loadReportId) {
          return;
        }
        this.showResponse = true;
        if (this.isShare) {
          shareCheckoutLoadReport(this.shareId, param, data => {
            this.openReport(data, row.loadReportId);
          });
        } else {
          checkoutLoadReport(param, data => {
            this.openReport(data, row.loadReportId);
          });
        }
      }
    },
    openReport(exist, loadReportId) {
      if (exist) {
        this.reportId = loadReportId;
      } else {
        this.showResponse = false;
        this.$warning(this.$t('test_track.plan.load_case.report_not_found'));
      }
    },
    setAllSize(size) {
      this.$emit('setSize', size);
    }
  }
}
</script>

<style scoped>

</style>
