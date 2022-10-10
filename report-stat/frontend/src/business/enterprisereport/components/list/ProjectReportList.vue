<template>
  <div>
    <el-card class="table-card" v-loading="result.loading">
      <ms-table-header :condition.sync="condition" @search="initTableData"
                       style="margin-bottom: 10px"
                       v-permission="['PROJECT_ENTERPRISE_REPORT:READ+CREATE']"
                       @create="createProjectReport"
                       :createTip="$t('commons.report_statistics.project_report.create_report')"/>
      <ms-table
        :data="tableData"
        :condition="condition"
        :page-size="page.pageSize"
        :operators="operators"
        :batch-operators="buttons"
        :total="page.total"
        operator-width="200"
        row-key="id"
        @refresh="initTableData"
        @filter="initTableData"
        ref="msTable">

        <el-table-column min-width="300" prop="name"
                         :label="$t('commons.report_statistics.project_report.report_name')"
                         show-overflow-tooltip/>

        <el-table-column prop="status" :label="$t('commons.status')">
          <template v-slot:default="scope">
            <ms-tag v-if="scope.row.status == 'NEW'" effect="plain"
                    :content="$t('commons.report_statistics.table.draft')"/>
            <ms-tag v-else-if="scope.row.status == 'SENDED'" type="success" effect="plain"
                    :content="$t('commons.report_statistics.table.sended')"/>
            <ms-tag v-else-if="scope.row.status == 'SEND_FAILD'" type="error" effect="plain"
                    :content="$t('commons.report_statistics.table.send_error')"/>
            <ms-tag v-else type="effect" effect="plain" :content="scope.row.status"/>
          </template>
        </el-table-column>

        <el-table-column prop="sendTime"
                         :label="$t('commons.report_statistics.project_report.report_send_time' )"
                         show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.lastSendTime | datetimeFormat }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime"
                         :label="$t('commons.create_time' )"
                         show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="scheduleStatus"
          :filters="scheduleFilters"
          column-key="scheduleStatus"
          :label="$t('commons.trigger_mode.schedule')">
          <template v-slot="scope">
            <span v-if="scope.row.scheduleStatus === 'OPEN'">
              <el-tooltip placement="bottom-start" effect="light">
                <div slot="content">
                  {{ $t('api_test.home_page.running_task_list.table_coloum.run_rule') }}: {{
                    scope.row.scheduleCorn
                  }}<br/>
                  {{ $t('test_track.plan.next_run_time') }}：<span>{{
                    scope.row.scheduleExecuteTime | datetimeFormat
                  }}</span>
                </div>
                <el-switch
                  @click.stop.native
                  v-model="scope.row.scheduleIsOpen"
                  inactive-color="#DCDFE6"
                  @change="scheduleChange(scope.row)"
                  :disabled="!hasSchedulePermission"/>
                </el-tooltip>
            </span>
            <span v-else-if="scope.row.scheduleStatus === 'SHUT'">
              <el-switch
                @click.stop.native
                v-model="scope.row.scheduleIsOpen"
                inactive-color="#DCDFE6"
                @change="scheduleChange(scope.row)"
                :disabled="!hasSchedulePermission">
                </el-switch>
            </span>
            <span v-else>
             <span style="color:#783887;">{{
                 $t('schedule.not_set')
               }}</span>
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createUser" :label="$t('commons.create_user')" show-overflow-tooltip/>
      </ms-table>
      <table-pagination :change="initTableData" :current-page.sync="page.currentPage" :page-size.sync="page.pageSize"
                        :total="page.total"/>
    </el-card>
    <email-preview-dialog @refresh="initTableData" :show-button="true" ref="emailPreviewDialog"/>
    <send-report-schedule ref="scheduleMaintain" @refreshTable="initTableData"/>
  </div>
</template>
<script>
import {_filter, _sort, getPageInfo} from "metersphere-frontend/src/utils/tableUtils";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import ShowMoreBtn from "@/business/compnent/button/ShowMoreBtn";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import {operationConfirm} from "metersphere-frontend/src/utils";
import {getCurrentProjectID, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import {hasPermission} from "metersphere-frontend/src/utils/permission"
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTag from "metersphere-frontend/src/components/MsTag";
import EmailPreviewDialog from "@/business/enterprisereport/components/dialog/EmailPreviewDialog";
import SendReportSchedule from "@/business/enterprisereport/components/schedule/SendReportSchedule";
import TablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {
  copyEnterpriseReport,
  deleteEnterpriseReport,
  getEnterpriseReport,
  getEnterpriseReportByParam,
  updateScheduleTask
} from "@/api/enterprise-report";

export default {
  name: "ProjectReportList",
  components: {
    SendReportSchedule,
    MsTable,
    MsTableColumn,
    ShowMoreBtn,
    MsTableHeader,
    MsTableOperatorButton,
    MsTag,
    EmailPreviewDialog,
    TablePagination
  },
  data() {
    return {
      result: {},
      page: getPageInfo(),
      condition: {
        filters: {},
        orders: [{name: "create_time", type: "desc"}]
      },
      tableData: [],
      buttons: [{
        name: this.$t('api_report.batch_delete'),
        handleClick: this.handleBatchDelete,
        permissions: ['PROJECT_ENTERPRISE_REPORT:READ+DELETE']
      }],
      operators: [
        {
          tip: this.$t('commons.preview'), icon: "el-icon-document",
          type: 'default',
          class: 'run-button',
          exec: this.previewReport
        },
        {
          tip: this.$t('commons.copy'), icon: "el-icon-document",
          exec: this.copyProjectReport,
          permissions: ['PROJECT_ENTERPRISE_REPORT:READ+COPY']
        },
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit", type: "primary",
          isDisable: this.isRowDisabled,
          exec: this.editProjectReport,
          permissions: ['PROJECT_ENTERPRISE_REPORT:READ+EDIT']
        }, {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.deleteReport,
          permissions: ['PROJECT_ENTERPRISE_REPORT:READ+DELETE']
        }, {
          tip: this.$t('api_test.automation.schedule'), icon: "el-icon-timer", type: "default",
          exec: this.scheduleReport,
          permissions: ['PROJECT_ENTERPRISE_REPORT:READ+SCHEDULE']
        }
      ],
      scheduleFilters: [
        {text: this.$t('test_track.plan.schedule_enabled'), value: 'OPEN'},
        {text: this.$t('test_track.issue.status_closed'), value: 'SHUT'},
        {text: this.$t('schedule.not_set'), value: 'NOTSET'}
      ],
      selectRows: new Set(),
      selectDataCounts: 0,
      hasSchedulePermission: false,
    }
  },
  created() {
    this.hasSchedulePermission = hasPermission('PROJECT_ENTERPRISE_REPORT:READ+SCHEDULE');
    this.initTableData();
  },
  methods: {
    isRowDisabled(row) {
      return row.status === 'SENDED';
    },
    scheduleChange(row) {
      let titles = this.$t('api_test.home_page.running_task_list.confirm.open_title');
      if (row.scheduleIsOpen) {
        this.$t('api_test.home_page.running_task_list.confirm.close_title');
      }
      let param = {id: row.scheduleId, enable: row.scheduleIsOpen};
      row.scheduleIsOpen = !row.scheduleIsOpen;

      operationConfirm(titles, () => {
        row.scheduleIsOpen = !row.scheduleIsOpen;
        this.updateTask(param);
      });
    },
    updateTask(param) {
      this.result = updateScheduleTask(param).then(() => {
        this.initTableData();
      });
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    sort(column) {
      _sort(column, this.condition);
      this.initTableData();
    },
    initTableData() {
      if (!getCurrentProjectID()) {
        return;
      }
      this.selectDataCounts = 0;
      this.condition.projectId = getCurrentProjectID();
      this.result = getEnterpriseReportByParam(this.page.currentPage, this.page.pageSize, this.condition).then(response => {
        if (response.data) {
          this.page.total = response.data.itemCount;
          this.tableData = response.data.listObject;
        }

      });
    },
    createProjectReport() {
      this.$emit("createProjectReport");
    },
    previewReport(row) {
      getEnterpriseReport(row.id).then(response => {
        let reportData = response.data;
        if (reportData.addressee) {
          reportData.addressee = JSON.parse(reportData.addressee);
        } else {
          reportData.addressee = [];
        }

        if (reportData.duplicated) {
          reportData.duplicated = JSON.parse(reportData.duplicated);
        } else {
          reportData.duplicated = [];
        }

        if (reportData.reportContent) {
          reportData.reportContent = JSON.parse(reportData.reportContent);
        } else {
          reportData.reportContent = [];
        }
        this.$refs.emailPreviewDialog.open(reportData);
      });
    },
    deleteReport(row) {
      this.$alert(this.$t('commons.confirm') + this.$t('commons.delete') + row.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            let param = {id: row.id, projectId: getCurrentProjectID()};
            deleteEnterpriseReport(param).then(() => {
              this.$success(this.$t('commons.delete_success'));
              this.initTableData();
            });
          }
        }
      });
    },
    handleBatchDelete() {
      this.$alert(this.$t('api_report.delete_batch_confirm') + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            if (!this.condition.selectAll) {
              let selectRows = this.$refs.msTable.selectRows;
              let ids = Array.from(selectRows).map(row => row.id);
              this.condition.ids = ids;
            }
            deleteEnterpriseReport(this.condition).then(() => {
              this.condition.ids = [];
              this.$success(this.$t('commons.delete_success'));
              this.initTableData();
            });
          }
        }
      });
    },
    copyProjectReport(row) {
      let param = {id: row.id, createUser: getCurrentUserId()};
      copyEnterpriseReport(param).then(() => {
        this.$success(this.$t('commons.copy_success'));
        this.initTableData();
      });
    },
    editProjectReport(row) {
      getEnterpriseReport(row.id).then(
        response => {
          let reportData = response.data;
          if (reportData.addressee) {
            reportData.addressee = JSON.parse(reportData.addressee);
          } else {
            reportData.addressee = [];
          }

          if (reportData.duplicated) {
            reportData.duplicated = JSON.parse(reportData.duplicated);
          } else {
            reportData.duplicated = [];
          }

          if (reportData.reportContent) {
            reportData.reportContent = JSON.parse(reportData.reportContent);
          } else {
            reportData.reportContent = [];
          }

          this.$emit("editProjectReport", reportData);
        }
      );
    },
    scheduleReport(row) {
      let openRow = row;
      openRow.redirectFrom == 'enterpriseReport';
      this.$refs.scheduleMaintain.open(openRow);
    }

  }
}
</script>

<style scoped>

.table-page {
  padding-top: 20px;
  margin-right: -9px;
  float: right;
}

.el-table {
  cursor: pointer;
}

.operate-button > div {
  display: inline-block;
  margin-left: 10px;
}

.request-method {
  padding: 0 5px;
  color: #1E90FF;
}

</style>
