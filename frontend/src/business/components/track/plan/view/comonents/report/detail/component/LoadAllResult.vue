<template>
  <el-container>
    <ms-aside-container width="500px" :default-hidden-bottom-top="200" :enable-auto-height="true">
      <load-failure-result :class="{'init-height': !showResponse}" :is-db="isDb" @rowClick="getReport" :is-all="isAll"
                           :share-id="shareId" :is-share="isShare" :is-template="isTemplate"
                           :report="report" :plan-id="planId" @setSize="setAllSize"/>
    </ms-aside-container>
    <ms-main-container>
      <load-case-report-view :is-plan-report="true" :share-id="shareId" :is-share="isShare" v-show="showResponse"
                             :plan-report-template="response" :report-id="reportId" ref="loadCaseReportView"/>
      <div class="empty" v-show="!showResponse">{{ $t('test_track.plan.load_case.content_empty') }}</div>
    </ms-main-container>
  </el-container>
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
import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";

export default {
  name: "LoadAllResult",
  components: {
    MsMainContainer,
    MsAsideContainer,
    LoadCaseReportView, LoadCaseReport, LoadFailureResult, StatusTableItem, MethodTableItem, TypeTableItem
  },
  props: {
    planId: String,
    report: Object,
    isTemplate: Boolean,
    isShare: Boolean,
    shareId: String,
    isDb: Boolean,
    isAll: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      failureTestCases: [],
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
.init-height >>> .el-card__body {
  height: 600px !important;
}

.ms-aside-container {
  border: 0px;
  padding: 10px 0px 0px 10px;
}
</style>
