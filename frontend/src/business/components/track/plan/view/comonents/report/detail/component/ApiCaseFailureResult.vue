<template>
  <div>
    <el-row class="scenario-info">
      <el-col :span="7">

        <ms-table v-loading="result.loading"
                  :show-select-all="false"
                  :screen-height="null"
                  :enable-selection="false"
                  :highlight-current-row="true"
                  @refresh="getScenarioApiCase"
                  @handleRowClick="rowClick"
                  :data="apiCases">

          <ms-table-column
            :width="80"
            :label="$t('commons.id')"
            prop="num">
          </ms-table-column>

          <ms-table-column
            :label="$t('commons.name')"
            prop="name">
          </ms-table-column>

          <ms-table-column
            :label="'创建人'"
            prop="creatorName"/>

          <ms-table-column
            :label="$t('test_track.case.priority')"
            :width="80"
            prop="priority">
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.priority" ref="priority"/>
            </template>
          </ms-table-column>

          <ms-table-column
            :width="80"
            :label="'执行结果'"
            prop="lastResult">
            <template v-slot:default="scope">
              <status-table-item v-if="scope.row.execResult === 'success'" :value="'Pass'"/>
              <status-table-item v-if="scope.row.execResult === 'error'" :value="'Failure'"/>
            </template>
          </ms-table-column>
        </ms-table>
      </el-col>
      <el-col :span="17" v-if="apiCases.length > 0">
        <el-card>
          <ms-request-result-tail v-if="showResponse" :response="response" ref="debugResult"/>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {
  getPlanApiAllCase,
  getPlanApiFailureCase,
  getSharePlanApiAllCase,
  getSharePlanApiFailureCase
} from "@/network/test-plan";
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import {getApiReport, getShareApiReport} from "@/network/api";
import MsRequestResultTail from "@/business/components/api/definition/components/response/RequestResultTail";
export default {
  name: "ApiCaseFailureResult",
  components: {
    MsRequestResultTail,
    MsTableColumn, MsTable, StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem},
  props: {
    planId: String,
    isTemplate: Boolean,
    report: Object,
    isShare: Boolean,
    shareId: String,
    isAll: Boolean
  },
  data() {
    return {
      apiCases:  [],
      result: {},
      response: {},
      showResponse: true
    }
  },
  mounted() {
    this.getScenarioApiCase();
  },
  methods: {
    getScenarioApiCase() {
      if (this.isTemplate) {
        if (this.isAll) {
          this.apiCases = this.report.apiAllCases;
        } else {
          this.apiCases = this.report.apiFailureResult;
        }
        this.handleDefaultClick();
      } else if (this.isShare) {
        if (this.isAll) {
          this.result = getSharePlanApiAllCase(this.shareId, this.planId, (data) => {
            this.apiCases = data;
            this.handleDefaultClick();
          });
        } else {
          this.result = getSharePlanApiFailureCase(this.shareId, this.planId, (data) => {
            this.apiCases = data;
            this.handleDefaultClick();
          });
        }
      } else {
        if (this.isAll) {
          this.result = getPlanApiAllCase(this.planId, (data) => {
            this.apiCases = data;
            this.handleDefaultClick();
          });
        } else {
          this.result = getPlanApiFailureCase(this.planId, (data) => {
            this.apiCases = data;
            this.handleDefaultClick();
          });
        }
      }
    },
    handleDefaultClick() {
      let data = this.apiCases;
      if (data && data.length > 0) {
        this.rowClick(data[0]);
      }
    },
    rowClick(row) {
      this.showResponse = true;
      if (this.isTemplate) {
        if (!row.response) {
          this.showResponse = false;
        } else {
          this.response = JSON.parse(row.response);
        }
      } else if (this.isShare) {
        getShareApiReport(this.shareId, row.id, (data) => {
          if (!data || !data.content) {
            this.showResponse = false;
          } else {
            this.response = JSON.parse(data.content);
          }
        });
      } else {
        getApiReport(row.id, (data) => {
          if (!data || !data.content) {
            this.showResponse = false;
          } else {
            this.response = JSON.parse(data.content);
          }
        });
      }
    }
  }
}
</script>

<style scoped>
</style>
