<template>
  <el-container class="scenario-info">
    <ms-aside-container width="500px" :default-hidden-bottom-top="200" :enable-auto-height="true">
      <el-card>
        <el-scrollbar>
          <ms-table v-loading="loading"
                    :show-select-all="false"
                    :screen-height="null"
                    :enable-selection="false"
                    :highlight-current-row="true"
                    @refresh="getScenarioApiCase"
                    @handleRowClick="rowClick"
                    :data="scenarioCases">

            <ms-table-column
                :width="80"
                :label="$t('commons.id')"
                prop="num">
            </ms-table-column>
            <ms-table-column
                :label="$t('commons.name')"
                prop="name">
            </ms-table-column>
            <el-table-column
              prop="principalName"
              :label="$t('test_track.plan.plan_principal')"/>
            <ms-table-column
                :label="$t('test_track.case.priority')"
                :width="80">
              <template v-slot:default="scope">
                <priority-table-item :value="scope.row.level" ref="priority"/>
              </template>
            </ms-table-column>
            <ms-table-column
                :width="70"
                :label="$t('api_test.automation.step')"
                prop="stepTotal">
            </ms-table-column>
            <ms-table-column
                :width="80"
                :label="$t('test_track.plan_view.execute_result')"
                prop="lastResult">
              <template v-slot:default="{row}">
                <ms-test-plan-api-status :status="row.lastResult"/>
              </template>
            </ms-table-column>
          </ms-table>
        </el-scrollbar>
      </el-card>
    </ms-aside-container>
    <el-main>
      <div v-if="showResponse">
        <micro-app v-show="!isTemplate" service="ui" route-name="ApiReportView" :route-params="routeParams"/>
        <UiShareReportDetail
          v-show="isTemplate"
          :report-id="reportId"
          :share-id="shareId"
          :is-share="isShare"
          :template-report="response"
          :is-template="true"
          :is-plan="true"
          :show-cancel-button="false"/>
      </div>
      <div class="empty" v-else>{{ $t('test_track.plan.load_case.content_empty') }}</div>
    </el-main>
  </el-container>
</template>

<script>
import UiShareReportDetail from "../ui/UiShareReportDetail"
import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MicroApp from "metersphere-frontend/src/components/MicroApp";
import MsTestPlanApiStatus from "@/business/plan/view/comonents/api/TestPlanApiStatus";

export default {
  name: "UiScenarioResult",
  components: {
    MsTestPlanApiStatus,
    MsMainContainer,
    MsAsideContainer,
    MicroApp,
    MsTableColumn, MsTable, StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem,
    UiShareReportDetail
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
    uiAllCases: Array,
    filterStatus: Array,
  },
  data() {
    return {
      scenarioCases: [],
      loading: false,
      reportId: null,
      response: {},
      showResponse: false,
      resultMap: {
        'Success' : 'Pass',
        'Error' : 'Failure',
        'STOP' : 'STOP',
        'Running' : 'Running',
        'UnExecute' : 'Prepare',
      },
      routeParams: {
        reportId: this.reportId,
        isShare: this.isShare,
        shareId: this.shareId,
        isPlanReport: true,
        isTemplate: this.isTemplate,
        response: this.response,
        showCancelButton: false,
        showReportNameButton: false
      },
    }
  },
  mounted() {
    this.getScenarioApiCase();
  },
  watch: {
    scenarioCases() {
      if (this.scenarioCases) {
        this.$emit('setSize', this.scenarioCases.length);
      }
    },
    uiAllCases() {
      this.getScenarioApiCase();
    }
  },
  methods: {
    getScenarioApiCase() {
      this.scenarioCases = [];
      if (this.filterStatus && this.filterStatus.length > 0) {
        this.uiAllCases.forEach(item => {
          if (this.filterStatus.indexOf(item.lastResult) > -1) {
            this.scenarioCases.push(item);
          }
        });
      } else {
        this.scenarioCases = this.uiAllCases;
      }
    },
    rowClick(row) {
      this.$nextTick(()=>{
        if (this.isTemplate) {
          if (row.response) {
            this.response = row.response;
            this.showResponse = true;
          }
        } else {
          if (row.reportId && row.lastResult !== "Running" && row.lastResult !== "Waiting") {
            this.reportId = row.reportId;
            this.showResponse = true;
          }
       }
      })
    }
  }
}
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
