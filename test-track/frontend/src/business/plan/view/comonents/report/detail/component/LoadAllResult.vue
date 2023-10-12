<template>
  <el-container>
    <ms-aside-container width="500px" :default-hidden-bottom-top="200" :enable-auto-height="true">
      <load-failure-result :class="{'init-height': !showResponse}"
                           :is-db="isDb"
                           :is-all="isAll"
                           :share-id="shareId"
                           :is-share="isShare"
                           :is-template="isTemplate"
                           :report="report"
                           :plan-id="planId"
                           @rowClick="getReport"
                           @setSize="setAllSize"/>
    </ms-aside-container>
    <ms-main-container>

      <div v-if="showResponse">
        <div v-if="!isTemplate && reportShow">
          <micro-app v-if="isShare"
                     route-name="sharePerReportView"
                     service="performance"
                     :route-params="{
                       reportId,
                       isShare,
                       shareId,
                       isPlanReport: true,
                     }"/>
          <micro-app v-else
                     route-name="perReportView"
                     service="performance"
                     :route-params="{
                        reportId,
                      }"/>
        </div>


        <load-case-report-view v-else
                               :is-plan-report="true"
                               :share-id="shareId"
                               :is-share="isShare"
                               :plan-report-template="response"
                               :report-id="reportId"
                               ref="loadCaseReportView"/>

      </div>
      <div class="empty" v-show="!showResponse">{{ $t('test_track.plan.load_case.content_empty') }}</div>
    </ms-main-container>
  </el-container>
</template>

<script>
import LoadCaseReportView from "../load/LoadCaseReportView";

import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {checkoutLoadReport, shareCheckoutLoadReport} from "@/api/remote/plan/test-plan";
import LoadFailureResult from "@/business/plan/view/comonents/report/detail/component/LoadFailureResult";

import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MicroApp from "metersphere-frontend/src/components/MicroApp";

export default {
  name: "LoadAllResult",
  components: {
    MsMainContainer,
    MsAsideContainer,
    LoadFailureResult, StatusTableItem, MethodTableItem, TypeTableItem,
    LoadCaseReportView,
    MicroApp
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
      response: null,
      reportShow: true
    }
  },
  methods: {
    getReport(row) {
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
          this.showResponse = false;
          return;
        }
        if (this.isShare) {
          shareCheckoutLoadReport(this.shareId, param)
              .then(r => {
                this.openReport(r.data, row.loadReportId);
              });
        } else {
          checkoutLoadReport(param)
              .then(r => {
                this.openReport(r.data, row.loadReportId);
              });
        }
      }
    },
    openReport(exist, loadReportId) {
      if (exist) {

        if (this.reportId === loadReportId) {
          return;
        }

        this.reportId = loadReportId;
        this.showResponse = true;
        this.reportShow = false;
        this.$nextTick(() => {
          this.reportShow = true;
        });
      } else {
        this.showResponse = false;
        this.$message.warning(this.$t('test_track.plan.load_case.report_not_found'));
      }
    },
    setAllSize(size) {
      this.$emit('setSize', size);
    }
  }
}
</script>

<style scoped>
.init-height :deep(.el-card__body) {
  height: 600px !important;
}

.ms-aside-container {
  border: 0px;
  padding: 10px 0px 0px 10px;
}
</style>
