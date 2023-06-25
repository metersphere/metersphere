<template>
  <el-container class="scenario-info">
    <ms-aside-container
      width="500px"
      :default-hidden-bottom-top="200"
      :enable-auto-height="true"
    >
      <el-card>
        <el-scrollbar>
          <ms-table
            v-loading="loading"
            :show-select-all="false"
            :screen-height="null"
            :enable-selection="false"
            :highlight-current-row="true"
            @refresh="getScenarioApiCase"
            @handleRowClick="rowClick"
            :data="scenarioCases"
          >
            <ms-table-column
              :width="80"
              :label="$t('commons.id')"
              prop="customNum"
            >
            </ms-table-column>
            <ms-table-column :label="$t('commons.name')" prop="name">
            </ms-table-column>
            <el-table-column
              prop="principalName"
              :label="$t('test_track.plan.plan_principal')"
            />
            <ms-table-column
              :label="$t('test_track.case.priority')"
              :width="80"
            >
              <template v-slot:default="scope">
                <priority-table-item :value="scope.row.level" ref="priority" />
              </template>
            </ms-table-column>
            <ms-table-column
              :width="70"
              :label="$t('api_test.automation.step')"
              prop="stepTotal"
            >
            </ms-table-column>
            <ms-table-column
              :width="80"
              :label="$t('test_track.plan_view.execute_result')"
              prop="lastResult"
            >
              <template v-slot:default="{ row }">
                <ms-test-plan-api-status :status="row.lastResult" />
              </template>
            </ms-table-column>
          </ms-table>
        </el-scrollbar>
      </el-card>
    </ms-aside-container>
    <el-main>
      <div v-if="showResponse">
        <ms-api-report
          v-show="isTemplate"
          :report-id="reportId"
          :is-share="isShare"
          :share-id="shareId"
          :is-plan="true"
          :is-template="isTemplate"
          :template-report="response"
        />
        <micro-app
          v-show="!isTemplate"
          route-name="ApiScenarioReportView"
          service="api"
          :route-params="routeParams"
        />
      </div>

      <div class="empty" v-else>
        {{ $t("test_track.plan.load_case.content_empty") }}
      </div>
    </el-main>
  </el-container>
</template>

<script>
import { debounce } from "lodash-es";
import MsApiReport from "../api/ApiReportDetail";

import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {
  getPlanScenarioAllCase,
  getPlanScenarioErrorReportCase,
  getPlanScenarioFailureCase,
  getPlanScenarioUnExecuteCase,
  getSharePlanScenarioAllCase,
  getSharePlanScenarioErrorReportCase,
  getSharePlanScenarioFailureCase,
  getSharePlanScenarioUnExecuteCase,
} from "@/api/remote/plan/test-plan";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MicroApp from "metersphere-frontend/src/components/MicroApp";
import MsTestPlanApiStatus from "@/business/plan/view/comonents/api/TestPlanApiStatus";

export default {
  name: "ApiScenarioFailureResult",
  components: {
    MsTestPlanApiStatus,
    MsMainContainer,
    MsAsideContainer,
    MicroApp,
    MsTableColumn,
    MsTable,
    StatusTableItem,
    MethodTableItem,
    TypeTableItem,
    PriorityTableItem,
    MsApiReport,
  },
  props: {
    planId: String,
    isTemplate: Boolean,
    report: Object,
    isShare: Boolean,
    shareId: String,
    isAll: Boolean,
    isErrorReport: Boolean,
    isUnExecute: Boolean,
    isDb: Boolean,
  },
  data() {
    return {
      scenarioCases: [],
      loading: false,
      reportId: null,
      response: {},
      showResponse: false,
      routeParams: {
        reportId: this.reportId,
        isShare: this.isShare,
        shareId: this.shareId,
        isPlanReport: true,
        isTemplate: this.isTemplate,
      },
      debouncedRenderReport: debounce(this.renderReport, 300), // 300ms防抖
    };
  },
  mounted() {
    this.getScenarioApiCase();
  },
  watch: {
    scenarioCases() {
      if (this.scenarioCases) {
        this.$emit("setSize", this.scenarioCases.length);
      }
    },
  },
  methods: {
    getScenarioApiCase() {
      if (this.isTemplate || this.isDb) {
        if (this.isErrorReport) {
          this.scenarioCases = this.report.errorReportScenarios
            ? this.report.errorReportScenarios
            : [];
        } else if (this.isUnExecute) {
          this.scenarioCases = this.report.unExecuteScenarios
            ? this.report.unExecuteScenarios
            : [];
        } else if (this.isAll) {
          this.scenarioCases = this.report.scenarioAllCases
            ? this.report.scenarioAllCases
            : [];
        } else {
          this.scenarioCases = this.report.scenarioFailureCases
            ? this.report.scenarioFailureCases
            : [];
        }
      } else if (this.isShare) {
        if (this.isErrorReport) {
          this.loading = true;
          getSharePlanScenarioErrorReportCase(this.shareId, this.planId).then(
            (r) => {
              this.loading = false;
              this.scenarioCases = r.data;
            }
          );
        } else if (this.isUnExecute) {
          this.loading = true;
          getSharePlanScenarioUnExecuteCase(this.shareId, this.planId).then(
            (r) => {
              this.loading = false;
              this.scenarioCases = r.data;
            }
          );
        } else if (this.isAll) {
          this.loading = true;
          getSharePlanScenarioAllCase(this.shareId, this.planId).then((r) => {
            this.loading = false;
            this.scenarioCases = r.data;
          });
        } else {
          this.loading = true;
          getSharePlanScenarioFailureCase(this.shareId, this.planId).then(
            (r) => {
              this.loading = false;
              this.scenarioCases = r.data;
            }
          );
        }
      } else {
        if (this.isErrorReport) {
          this.loading = true;
          getPlanScenarioErrorReportCase(this.planId).then((r) => {
            this.loading = false;
            this.scenarioCases = r.data;
          });
        } else if (this.isUnExecute) {
          this.loading = true;
          getPlanScenarioUnExecuteCase(this.planId).then((r) => {
            this.loading = false;
            this.scenarioCases = r.data;
          });
        } else if (this.isAll) {
          this.loading = true;
          getPlanScenarioAllCase(this.planId).then((r) => {
            this.loading = false;
            this.scenarioCases = r.data;
          });
        } else {
          this.loading = true;
          getPlanScenarioFailureCase(this.planId).then((r) => {
            this.loading = false;
            this.scenarioCases = r.data;
          });
        }
      }
    },
    rowClick(row) {
      this.debouncedRenderReport(row);
    },
    renderReport(row) {
      if (this.isTemplate) {
        if (row.response) {
          this.response = row.response;
          this.showResponse = true;
        }
      } else {
        if (
          row.reportId &&
          row.lastResult !== "Running" &&
          row.lastResult !== "Waiting"
        ) {
          this.routeParams.reportId = row.reportId;
          this.showResponse = true;
        }
      }
    },
  },
};
</script>

<style scoped>
.padding-col {
  padding-right: 0px;
}

.el-card :deep(.el-card__body) {
  height: 588px;
}

:deep(.api-report-content) {
  height: auto;
}

.ms-aside-container {
  border: 0px;
  padding: 10px 0px 0px 10px;
}
</style>
