<template>
  <div>
    <el-card class="table-card" v-loading="loading">
      <ms-table-header
        :condition.sync="condition"
        @search="initTableData"
        style="margin-bottom: 10px"
        v-permission="['PROJECT_ERROR_REPORT_LIBRARY:READ+CREATE']"
        @create="createErrorReport"
        :createTip="$t('error_report_library.option.create')"
      >
        <template v-slot:button>
          <span style="margin-left: 10px">{{
            $t("error_report_library.tips")
          }}</span>
        </template>
      </ms-table-header>

      <ms-table
        v-loading="loading"
        :data="tableData"
        :condition="condition"
        :page-size="page.pageSize"
        :total="page.total"
        :batch-operators="buttons"
        :screenHeight="screenHeight"
        :remember-order="true"
        row-key="id"
        operator-width="190px"
        @callBackSelectAll="callBackSelectAll"
        @callBackSelect="callBackSelect"
        @refresh="initTableData"
        ref="caseTable"
      >
        <ms-table-column
          min-width="300"
          prop="errorCode"
          :label="$t('error_report_library.option.error_code')"
          :show-overflow-tooltip="true"
        />

        <ms-table-column
          prop="matchType"
          :label="$t('error_report_library.option.match_type')"
        >
          <template v-slot:default="scope">
            <ms-tag
              v-if="scope.row.matchType == 'Text'"
              effect="plain"
              :content="$t('error_report_library.match_type.text')"
            />
          </template>
        </ms-table-column>

        <ms-table-column
          width="100"
          :label="$t('error_report_library.option.status')"
        >
          <template v-slot:default="scope">
            <div>
              <el-switch
                v-model="scope.row.status"
                class="captcha-img"
                @change="changeStatus(scope.row)"
              ></el-switch>
            </div>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="createTime"
          :label="$t('commons.create_time')"
          show-overflow-tooltip
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="updateTime"
          :label="$t('commons.update_time')"
          show-overflow-tooltip
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </ms-table-column>

        <ms-table-column
          prop="createUser"
          :label="$t('commons.create_user')"
          show-overflow-tooltip
        />
        <ms-table-column
          prop="description"
          :label="$t('commons.description')"
          show-overflow-tooltip
        />

        <ms-table-column min-width="150" :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator-button
                :tip="$t('commons.edit')"
                icon="el-icon-edit"
                :disabled="scope.row.status === 'SENDED'"
                v-permission="['PROJECT_ERROR_REPORT_LIBRARY:READ+EDIT']"
                @exec="editErrorReport(scope.row)"
              />
              <ms-table-operator-button
                type="danger"
                @exec="deleteReport(scope.row)"
                v-permission="['PROJECT_ERROR_REPORT_LIBRARY:READ+DELETE']"
                :tip="$t('commons.delete')"
                icon="el-icon-delete"
              />
            </div>
          </template>
        </ms-table-column>
      </ms-table>
      <ms-table-pagination
        :change="initTableData"
        :current-page.sync="page.currentPage"
        :page-size.sync="page.pageSize"
        :total="page.total"
      />
    </el-card>
  </div>
</template>
<script>
import { getPageInfo } from "metersphere-frontend/src/utils/tableUtils";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import ShowMoreBtn from "metersphere-frontend/src/components/table/ShowMoreBtn";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import { hasPermission } from "metersphere-frontend/src/utils/permission";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsTag from "metersphere-frontend/src/components/MsTag";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {
  deleteErrorReportLibrary,
  getErrorReportLibraryById,
  getErrorReportLibraryPages,
  modifyErrorReportLibrary,
} from "../../../../../api/error-report-library";
import { operationConfirm } from "metersphere-frontend/src/utils";
export default {
  name: "ProjectReportList",
  components: {
    MsTable,
    MsTableColumn,
    ShowMoreBtn,
    MsTableHeader,
    MsTableOperatorButton,
    MsTag,
    MsTablePagination,
  },
  data() {
    return {
      result: {},
      loading: false,
      page: getPageInfo(),
      condition: {
        filters: {},
        orders: [{ name: "create_time", type: "desc" }],
      },
      tableData: [],
      buttons: [
        {
          name: this.$t("api_test.definition.request.batch_delete"),
          handleClick: this.handleBatchDelete,
          permissions: ["PROJECT_ERROR_REPORT_LIBRARY:READ+BATCH_DELETE"],
        },
      ],
      selectNodeIds: [],
      screenHeight: "calc(100vh - 150px)", //屏幕高度
      selectRows: new Set(),
      selectDataCounts: 0,
    };
  },
  created() {
    this.initTableData();
  },
  methods: {
    callBackSelectAll(selections) {
      this.selectNodeIds = Array.from(selections).map((o) => o.id);
    },
    callBackSelect(selections) {
      this.selectNodeIds = Array.from(selections).map((o) => o.id);
    },
    initTableData() {
      if (!getCurrentProjectID()) {
        return;
      }
      this.selectDataCounts = 0;
      this.selectRows = new Set();
      this.condition.projectId = getCurrentProjectID();
      let selectParam = { condition: this.condition };
      if (this.condition.name) {
        selectParam.errorCode = this.condition.name;
      }
      this.loading = getErrorReportLibraryPages(
        this.page.currentPage,
        this.page.pageSize,
        selectParam
      ).then((response) => {
        let data = response.data;
        this.page.total = data.itemCount;
        this.tableData = data.listObject;
      });
    },
    createErrorReport() {
      this.$emit("createErrorReport");
    },
    deleteReport(row) {
      operationConfirm(
        this,
        this.$t("commons.confirm") +
          this.$t("commons.delete") +
          row.errorCode +
          "？",
        () => {
          let param = { id: row.id, projectId: getCurrentProjectID() };
          deleteErrorReportLibrary(param).then(() => {
            this.$success(this.$t("commons.delete_success"));
            this.initTableData();
          });
        }
      );
    },
    handleBatchDelete() {
      operationConfirm(
        this,
        this.$t("pj_batch_delete.error_library") + "？",
        () => {
          let sendParam = {};
          sendParam.ids = this.selectNodeIds;
          sendParam.projectId = getCurrentProjectID();
          sendParam.condition = this.condition;
          deleteErrorReportLibrary(sendParam).then(() => {
            this.$success(this.$t("commons.delete_success"));
            this.initTableData();
          });
        }
      );
    },
    editErrorReport(row) {
      getErrorReportLibraryById(row.id).then((response) => {
        if (response.data) {
          let libraryData = response.data;
          this.$emit("editErrorReport", libraryData);
        }
      });
    },
    changeStatus(row) {
      if (!hasPermission("PROJECT_ERROR_REPORT_LIBRARY:READ+EDIT")) {
        row.status = !row.status;
        this.$alert(this.$t("commons.project_permission"));
        return false;
      } else {
        let param = { id: row.id, status: row.status };
        this.loading = modifyErrorReportLibrary(param).then(() => {
          this.$success(row.status ? this.$t("commons.enable_success") : this.$t("commons.disable_success"));
        });
      }
    },
  },
};
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
  color: #1e90ff;
}
</style>
