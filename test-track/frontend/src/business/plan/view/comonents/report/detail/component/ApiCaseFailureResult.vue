<template>
  <el-container>
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
            :data="apiCases"
          >
            <ms-table-column :width="100" :label="$t('commons.id')" prop="num">
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
              prop="priority"
            >
              <template v-slot:default="scope">
                <priority-table-item
                  :value="scope.row.priority"
                  ref="priority"
                />
              </template>
            </ms-table-column>

            <ms-table-column
              :width="80"
              :label="$t('test_track.plan_view.execute_result')"
              prop="lastResult"
            >
              <template v-slot:default="scope">
                <ms-test-plan-api-status :status="scope.row.execResult" />
              </template>
            </ms-table-column>
          </ms-table>
        </el-scrollbar>
      </el-card>
    </ms-aside-container>
    <ms-main-container v-loading="responseLoading">
      <div v-if="showResponse">
        <el-card v-if="!isTemplate">
          <ms-request-result-tail :response="response" :is-test-plan="true" ref="showRspResult"/>
        </el-card>
        <el-card v-else>
          <ms-request-result-tail
            :response="response"
            :is-test-plan="showResponse"
            ref="debugResult"
          />
        </el-card>
      </div>

      <div class="empty" v-else>
        {{ $t("test_track.plan.load_case.content_empty") }}
      </div>
    </ms-main-container>
  </el-container>
</template>

<script>
import MsRequestResultTail from "../api/RequestResultTail";

import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {
  getPlanApiAllCase,
  getPlanApiErrorReportCase,
  getPlanApiFailureCase,
  getPlanApiUnExecuteCase,
  getSharePlanApiAllCase,
  getSharePlanApiErrorReportCase,
  getSharePlanApiFailureCase,
  getSharePlanApiUnExecuteCase,
} from "@/api/remote/plan/test-plan";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {getShareApiReport, getShareApiReportByReportId} from "@/api/share";
import {apiDefinitionReportGet, apiDefinitionReportGetDb,} from "@/api/remote/api/api-definition";
import MsTestPlanApiStatus from "@/business/plan/view/comonents/api/TestPlanApiStatus";

export default {
  name: "ApiCaseFailureResult",
  components: {
    MsTestPlanApiStatus,
    MsMainContainer,
    MsAsideContainer,
    MsTableColumn,
    MsTable,
    StatusTableItem,
    MethodTableItem,
    TypeTableItem,
    PriorityTableItem,
    MsRequestResultTail,
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
      apiCases: [],
      responseLoading: false,
      loading: false,
      response: {},
      showResponse: false,
    };
  },
  watch: {
    apiCases() {
      if (this.apiCases) {
        this.$emit("setSize", this.apiCases.length);
      } else {
        this.apiCases = [];
      }
    },
  },
  mounted() {
    this.getScenarioApiCase();
  },
  methods: {
    getScenarioApiCase() {
      if (this.isTemplate || this.isDb) {
        if (this.isErrorReport) {
          this.apiCases = this.report.errorReportCases
            ? this.report.errorReportCases
            : [];
        } else if (this.isUnExecute) {
          this.apiCases = this.report.unExecuteCases
            ? this.report.unExecuteCases
            : [];
        } else if (this.isAll) {
          this.apiCases = this.report.apiAllCases
            ? this.report.apiAllCases
            : [];
        } else {
          this.apiCases = this.report.apiFailureCases
            ? this.report.apiFailureCases
            : [];
        }
      } else if (this.isShare) {
        if (this.isErrorReport) {
          this.loading = true;
          getSharePlanApiErrorReportCase(this.shareId, this.planId).then(
            (r) => {
              this.loading = false;
              this.apiCases = r.data;
            }
          );
        } else if (this.isUnExecute) {
          this.loading = true;
          getSharePlanApiUnExecuteCase(this.shareId, this.planId).then((r) => {
            this.loading = false;
            this.apiCases = r.data;
          });
        } else if (this.isAll) {
          this.loading = true;
          getSharePlanApiAllCase(this.shareId, this.planId).then((r) => {
            this.loading = false;
            this.apiCases = r.data;
          });
        } else {
          this.loading = true;
          getSharePlanApiFailureCase(this.shareId, this.planId).then((r) => {
            this.loading = false;
            this.apiCases = r.data;
          });
        }
      } else {
        if (this.isErrorReport) {
          this.loading = true;
          getPlanApiErrorReportCase(this.planId).then((r) => {
            this.loading = false;
            this.apiCases = r.data;
          });
        } else if (this.isUnExecute) {
          this.loading = true;
          getPlanApiUnExecuteCase(this.planId).then((r) => {
            this.loading = false;
            this.apiCases = r.data;
          });
        } else if (this.isAll) {
          this.loading = true;
          getPlanApiAllCase(this.planId).then((r) => {
            this.loading = false;
            this.apiCases = r.data;
          });
        } else {
          this.loading = true;
          getPlanApiFailureCase(this.planId).then((r) => {
            this.loading = false;
            this.apiCases = r.data;
          });
        }
      }
    },
    selectReportContentByReportId(reportId) {
      this.responseLoading = true;
      apiDefinitionReportGet(reportId).then((response) => {
        this.responseLoading = false;
        if (response.data) {
          let data = response.data;
          if (data) {
            this.showResponse = true;
            try {
              this.response = JSON.parse(data.content);
            } catch (e) {
              this.response = {};
            }
          }
        }
      });
    },
    selectShareReportContentByReportId(reportId) {
      this.responseLoading = true;
      getShareApiReportByReportId(reportId).then((response) => {
        this.responseLoading = false;
        if (response.data) {
          let data = response.data;
          if (data) {
            this.showResponse = true;
            try {
              this.response = JSON.parse(data.content);
            } catch (e) {
              this.response = {};
            }
          }
        }
      });
    },
    rowClick(row) {
      this.showResponse = false;
      this.$nextTick(() => {
        if (this.isTemplate) {
          if (row.response) {
            this.showResponse = true;
            this.response = JSON.parse(row.response);
          }
        } else if (this.isShare) {
          if (row.reportId) {
            this.selectShareReportContentByReportId(row.reportId);
          } else {
            this.responseLoading = true;
            getShareApiReport(this.shareId, row.id).then((r) => {
              this.responseLoading = false;
              let data = r.data;
              if (data && data.content) {
                this.showResponse = true;
                this.response = JSON.parse(data.content);
              }
            });
          }
        } else {
          if (row.reportId) {
            this.selectReportContentByReportId(row.reportId);
          } else {
            this.responseLoading = true;
            apiDefinitionReportGetDb(row.id).then((r) => {
              this.responseLoading = false;
              let data = r.data;
              if (data && data.content) {
                this.showResponse = true;
                try {
                  this.response = JSON.parse(data.content);
                } catch (e) {
                  this.response = {};
                }
              }
            });
          }
        }
      });
    },
  },
};
</script>

<style scoped>
.el-card :deep(.el-card__body) {
  height: 550px;
}

:deep(.text-container .pane) {
  height: 550px !important;
}

.ms-aside-container {
  border: 0px;
  height: 550px;
  padding: 10px 0px 0px 10px;
}

.ms-main-container {
  height: 580px;
}
</style>
