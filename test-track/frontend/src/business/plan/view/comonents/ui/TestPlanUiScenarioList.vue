<template>
  <div class="card-container">
    <el-card class="card-content" v-loading="loading">
      <template v-slot:header>
        <test-plan-scenario-list-header
          :condition="condition"
          :projectId="projectId"
          :plan-status="planStatus"
          @refresh="filterSearch"
          @relevanceCase="$emit('relevanceCase', 'scenario')"
        />
      </template>

      <ms-table
        :data="tableData"
        :condition="condition"
        :total="total"
        :page-size.sync="pageSize"
        :operators="operators"
        :screen-height="screenHeight"
        :batch-operators="buttons"
        @handlePageChange="search"
        :fields.sync="fields"
        :field-key="tableHeaderKey"
        :enable-order-drag="enableOrderDrag"
        row-key="id"
        :row-order-func="editTestPlanUiScenarioCaseOrder"
        :row-order-group-id="planId"
        @order="search"
        @filter="filterSearch"
        ref="table"
      >
        <span v-for="item in fields" :key="item.key">
          <ms-table-column
            v-if="item.id == 'num'"
            :fields-width="fieldsWidth"
            sortable
            prop="num"
            min-width="80px"
            label="ID"
          >
            <template v-slot:default="scope">
              <el-link @click="openById(scope.row)">
                <span>
                  {{ scope.row.num }}
                </span>
              </el-link>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="name"
            :label="$t('api_test.automation.scenario_name')"
            min-width="120px"
            sortable
          />

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="level"
            :label="$t('api_test.automation.case_level')"
            min-width="120px"
            column-key="level"
            sortable
            :filters="apiscenariofilters.LEVEL_FILTERS"
          >
            <template v-slot:default="scope">
              <priority-table-item :value="scope.row.level" ref="level" />
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="envs"
            :label="$t('commons.environment')"
            min-width="150"
          >
            <template v-slot:default="{ row }">
              <div v-if="row.envs && JSON.stringify(row.envs) !== '{}'">
                <span v-for="(k, v, index) in row.envs" :key="index">
                  <span v-if="index === 0 || index === 1">
                    <span class="project-name" :title="v">{{ v }}</span
                    >:
                    <el-tag type="success" size="mini" effect="plain">
                      {{ k }}
                    </el-tag>
                    <br />
                  </span>
                  <el-popover placement="top" width="350" trigger="click">
                    <div v-for="(k, v, index) in row.envs" :key="index">
                      <span class="plan-case-env"
                        >{{ v }}:
                        <el-tag type="success" size="mini" effect="plain">{{
                          k
                        }}</el-tag
                        ><br />
                      </span>
                    </div>
                    <el-link
                      v-if="index === 2"
                      slot="reference"
                      type="info"
                      :underline="false"
                      icon="el-icon-more"
                    />
                  </el-popover>
                </span>
              </div>
              <div v-else>
                {{ $t('api_test.environment.default_environment') }}
              </div>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="tagNames"
            :label="$t('commons.tag')"
            width="120px"
            :show-overflow-tooltip="false"
          >
            <template v-slot:default="scope">
              <el-tooltip class="item" effect="dark" placement="top">
                <div
                  v-html="getTagToolTips(scope.row.tags)"
                  slot="content"
                ></div>
                <div class="oneLine">
                  <ms-tag
                    v-for="(itemName, index) in scope.row.tags"
                    :key="index"
                    type="success"
                    effect="plain"
                    :show-tooltip="
                      scope.row.tags.length === 1 && itemName.length * 12 <= 100
                    "
                    :content="itemName"
                    style="margin-left: 0px; margin-right: 2px"
                  />
                </div>
              </el-tooltip>
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="createUser"
            :label="$t('api_test.automation.creator')"
            min-width="100px"
          >
            <template v-slot:default="scope">
              {{ scope.row.creatorName }}
            </template>
          </ms-table-column>

          <ms-table-column
            v-if="item.id == 'maintainer'"
            prop="userId"
            :fields-width="fieldsWidth"
            :label="$t('custom_field.case_maintainer')"
            min-width="120"
          >
            <template v-slot:default="scope">
              {{ scope.row.principalName }}
            </template>
          </ms-table-column>

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="principal"
            :label="$t('custom_field.case_maintainer')"
            min-width="120"
          />

          <ms-update-time-column :field="item" :fields-width="fieldsWidth" />
          <ms-create-time-column :field="item" :fields-width="fieldsWidth" />

          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="stepTotal"
            :label="$t('api_test.automation.step')"
            min-width="80px"
          />
          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="lastResult"
            min-width="100px"
            :filters="apiscenariofilters.RESULT_FILTERS"
            :label="$t('api_test.automation.last_result')"
          >
            <template v-slot:default="{ row }">
              <el-link
                @click="showReport(row)"
                :disabled="
                  !row.lastResult ||
                  row.lastResult === 'PENDING' ||
                  row.lastResult === 'UnExecute'
                "
              >
                <ms-test-plan-api-status
                  :status="
                    row.lastResult === 'UnExecute' ? 'PENDING' : row.lastResult
                  "
                />
              </el-link>
            </template>
          </ms-table-column>
          <ms-table-column
            :field="item"
            :fields-width="fieldsWidth"
            prop="passRate"
            min-width="80px"
            :label="$t('api_test.automation.passing_rate')"
          />
        </span>
      </ms-table>

      <ms-table-pagination
        :change="search"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"
      />
      <div>
        <!-- 执行结果 -->
        <el-drawer
          :visible.sync="runVisible"
          :destroy-on-close="true"
          direction="ltr"
          :withHeader="true"
          :modal="false"
          size="90%"
          @close="search"
        >
          <micro-app
            :to="`/ui/report/view/${reportId}`"
            route-name="ApiReportView"
            :route-params="{ showCancelButton: false, reportId, view: true }"
            service="ui"
          />
        </el-drawer>
      </div>
    </el-card>

    <!-- 批量编辑 -->
    <batch-edit
      :dialog-title="$t('test_track.case.batch_edit_case')"
      :type-arr="typeArr"
      :value-arr="valueArr"
      :select-row="this.$refs.table ? this.$refs.table.selectRows : new Set()"
      ref="batchEdit"
      @batchEdit="batchEdit"
    />

    <ui-run-mode
      @handleRunBatch="handleRunBatch"
      ref="runMode"
      :custom-run-mode="true"
      :custom-serial-on-sample-error="true"
      :request="conditionRequest"
      :plan-case-ids="planCaseIds"
    />

    <ms-task-center ref="taskCenter" :show-menu="false" />
  </div>
</template>

<script>
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {getCurrentProjectID, getCurrentWorkspaceId,} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {getUUID, strMapToObj} from "metersphere-frontend/src/utils";
import TestPlanScenarioListHeader from "../ui/TestPlanUiScenarioListHeader";
import MsPlanRunMode from "@/business/plan/common/PlanRunModeWithEnv";
import {
  buildBatchParam,
  getCustomTableHeader,
  getCustomTableWidth,
  initCondition,
} from "metersphere-frontend/src/utils/tableUtils";
import {TEST_PLAN_UI_SCENARIO_CASE} from "metersphere-frontend/src/utils/constants";
import HeaderLabelOperate from "metersphere-frontend/src/components/head/HeaderLabelOperate";
import BatchEdit from "@/business/case/components/BatchEdit";
import PriorityTableItem from "@/business/common/tableItems/planview/PriorityTableItem";
import MsTaskCenter from "metersphere-frontend/src/components/task/TaskCenter";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import MsUpdateTimeColumn from "metersphere-frontend/src/components/table/MsUpdateTimeColumn";
import MsCreateTimeColumn from "metersphere-frontend/src/components/table/MsCreateTimeColumn";
import {editTestPlanUiScenarioCaseOrder} from "@/api/remote/plan/test-plan";
import {
  testPlanUiScenarioCaseBatchDelete,
  testPlanUiScenarioCaseBatchUpdateEnv,
  testPlanUiScenarioCaseDelete,
  testPlanUiScenarioCaseRun,
  testPlanUiScenarioCaseSelectAllTableRows,
  testPlanUiScenarioList,
} from "@/api/remote/ui/test-plan-ui-scenario-case";
import {getVersionFilters} from "@/business/utils/sdk-utils";
import {uiAutomationReduction, uiAutomationVerifySeleniumServer,} from "@/api/remote/ui/api-scenario";
import i18n from "@/i18n";
import MicroApp from "metersphere-frontend/src/components/MicroApp";
import MsTestPlanApiStatus from "@/business/plan/view/comonents/api/TestPlanApiStatus";
import UiRunMode from "@/business/plan/view/comonents/ui/UiRunMode";
import {baseSocket} from "@/api/base-network";

export default {
  name: "MsTestPlanUiScenarioList",
  components: {
    UiRunMode,
    MsTestPlanApiStatus,
    MsCreateTimeColumn,
    MsUpdateTimeColumn,
    MsTableColumn,
    MsTable,
    PriorityTableItem,
    HeaderLabelOperate,
    TestPlanScenarioListHeader,
    MsTablePagination,
    MsTableHeader,
    MsTag,
    MicroApp,
    BatchEdit,
    MsTaskCenter,
    MsPlanRunMode,
  },
  props: {
    referenced: {
      type: Boolean,
      default: false,
    },
    selectNodeIds: Array,
    planId: String,
    planStatus: String,
    clickType: String,
    versionEnable: Boolean,
  },
  data() {
    return {
      type: TEST_PLAN_UI_SCENARIO_CASE,
      tableHeaderKey: "TEST_PLAN_SCENARIO_CASE",
      fields: getCustomTableHeader("TEST_PLAN_UI_SCENARIO_CASE"),
      fieldsWidth: getCustomTableWidth("TEST_PLAN_UI_SCENARIO_CASE"),
      screenHeight: "calc(100vh - 250px)", //屏幕高度
      tableLabel: [],
      loading: false,
      condition: {},
      currentScenario: {},
      schedule: {},
      selectAll: false,
      tableData: [],
      currentPage: 1,
      selectDataCounts: 0,
      pageSize: 10,
      total: 0,
      reportId: "",
      status: "default",
      infoDb: false,
      runVisible: false,
      runData: [],
      enableOrderDrag: true,
      operators: [
        {
          tip: this.$t("api_test.run"),
          icon: "el-icon-video-play",
          exec: this.execute,
          class: this.planStatus === "Archived" ? "disable-run" : "run-button",
          isDisable: this.planStatus === "Archived",
          permissions: ["PROJECT_TRACK_PLAN:READ+RUN"],
        },
        {
          tip: this.$t("test_track.plan_view.cancel_relevance"),
          icon: "el-icon-unlock",
          exec: this.remove,
          type: "danger",
          isDisable: this.planStatus === "Archived",
          permissions: ["PROJECT_TRACK_PLAN:READ+RELEVANCE_OR_CANCEL"],
        },
      ],
      buttons: [
        {
          name: this.$t("test_track.case.batch_unlink"),
          handleClick: this.handleDeleteBatch,
          isDisable: this.planStatus === "Archived",
          permissions: ["PROJECT_TRACK_PLAN:READ+CASE_BATCH_DELETE"],
        },
        {
          name: this.$t("api_test.automation.batch_execute"),
          handleClick: this.handleBatchExecute,
          isDisable: this.planStatus === "Archived",
          permissions: ["PROJECT_TRACK_PLAN:READ+CASE_BATCH_RUN"],
        },
      ],
      typeArr: [
        {
          id: "projectEnv",
          name: this.$t("api_test.definition.request.run_env"),
        },
      ],
      valueArr: {
        projectEnv: [],
      },
      planCaseIds: [],
      apiscenariofilters: {
        LEVEL_FILTERS: [
          { text: "P0", value: "P0" },
          { text: "P1", value: "P1" },
          { text: "P2", value: "P2" },
          { text: "P3", value: "P3" },
        ],
        RESULT_FILTERS: [
          { text: i18n.t("Error"), value: "ERROR" },
          { text: i18n.t("Success"), value: "SUCCESS" },
          { text: i18n.t("Pending"), value: "PENDING" },
        ],
      },
      versionFilters: [],
      //
      conditionRequest: {},
    };
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    editTestPlanUiScenarioCaseOrder() {
      return editTestPlanUiScenarioCaseOrder;
    },
  },
  beforeDestroy() {
    if (this.websocket && this.websocket.close instanceof Function) {
      this.websocket.close();
    }
  },
  created() {
    this.initWebSocket();
    this.search();
    this.getVersionOptions();
  },
  watch: {
    selectNodeIds() {
      this.condition.selectAll = false;
      this.search();
    },
    planId() {
      this.search();
    },
  },
  methods: {
    getTagToolTips(tags) {
      try {
        let showTips = "";
        tags.forEach((item) => {
          showTips += item + ",";
        });
        return showTips.substr(0, showTips.length - 1);
      } catch (e) {
        return "";
      }
    },
    initWebSocket() {
      this.websocket = baseSocket("/plan/ui/" + this.planId);
      this.websocket.onmessage = this.onMessage;
      this.websocket.onopen = this.onOpen;
      this.websocket.onerror = this.onError;
      this.websocket.onclose = this.onClose;
    },
    onOpen() {},
    onError() {},
    onMessage(e) {
      if (e.data === "CONN_SUCCEEDED") {
        return;
      }
      try {
        let obj = JSON.parse(e.data);
        let { planCaseId, planCaseStatus } = obj;
        let data = this.tableData.filter((d) => d.id === planCaseId);
        if (data.length > 0) {
          data[0]["lastResult"] = planCaseStatus;
        }
      } catch (ex) {
        // nothing
      }
    },
    onClose() {},
    filterSearch() {
      // 添加搜索条件时，当前页设置成第一页
      this.currentPage = 1;
      this.search();
    },
    search() {
      initCondition(this.condition, this.condition.selectAll);
      if (
        this.condition &&
        this.condition.filters &&
        this.condition.filters.last_result
      ) {
        if (this.condition.filters.last_result.length > 0) {
          //校验是否含有PENDING
          if (this.condition.filters.last_result.includes("PENDING")) {
            this.condition.filters.last_result = [
              ...this.condition.filters.last_result,
              "UnExecute",
            ];
          }
          //校验是否含有ERROR
          if (this.condition.filters.last_result.includes("ERROR")) {
            this.condition.filters.last_result = [
              ...this.condition.filters.last_result,
              "FAIL",
            ];
          }
        }
      }
      this.loading = true;
      this.condition.moduleIds = this.selectNodeIds;
      if (this.clickType) {
        if (this.status == "default") {
          this.condition.status = this.clickType;
        } else {
          this.condition.status = null;
        }
        this.status = "all";
      }
      this.enableOrderDrag =
        (this.condition.orders && this.condition.orders.length) > 0
          ? false
          : true;

      if (this.planId) {
        this.condition.planId = this.planId;
        testPlanUiScenarioList(
          { pageNum: this.currentPage, pageSize: this.pageSize },
          this.condition
        ).then((response) => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.tableData.forEach((item) => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          this.tableData.forEach((item) => {
            try {
              let data = item.environmentMap;
              if (data) {
                this.$set(item, "envs", data);
              }
            } catch (error) {
              this.$set(item, "envs", {});
            }
          });
          this.loading = false;
        });
      }
    },
    reductionApi(row) {
      row.scenarioDefinition = null;
      let rows = [row];
      uiAutomationReduction(rows).then(() => {
        this.$success(this.$t("commons.save_success"));
        this.search();
      });
    },
    handleBatchExecute() {
      let rows = this.orderBySelectRows(this.$refs.table.selectRows);
      this.planCaseIds = [];
      rows.forEach((row) => {
        this.planCaseIds.push(row.id);
      });
      this.conditionRequest.id = getUUID();
      this.conditionRequest.ids = this.planCaseIds;
      this.conditionRequest.projectId = this.projectId;
      this.conditionRequest.condition = this.condition;
      this.$refs.runMode.open();
    },
    orderBySelectRows(rows) {
      let selectIds = this.$refs.table.selectIds;
      if (rows) {
        selectIds = Array.from(rows).map((row) => row.id);
      }
      let array = [];
      for (let i in this.tableData) {
        if (selectIds.indexOf(this.tableData[i].id) !== -1) {
          array.push(this.tableData[i]);
        }
      }
      return array;
    },
    handleRunBatch(config) {
      let rows = this.orderBySelectRows(this.$refs.table.selectRows);
      if (this.planId) {
        let selectParam = buildBatchParam(this);
        let param = { config: config, planCaseIds: [] };
        param.ids = rows.map((r) => r.id);
        rows.forEach((row) => {
          this.buildExecuteParam(param, row);
        });
        param.condition = selectParam.condition;
        param.triggerMode = "BATCH";
        param.requestOriginator = "TEST_PLAN";
        testPlanUiScenarioCaseRun(param).then(() => {
          this.$message(this.$t("commons.run_message"));
          this.$refs.taskCenter.open();
          this.search();
        });
      }
    },
    async validateSeleniumSetting() {
      //选择本地调试默认取消性能模式
      const h = this.$createElement;
      //检测是否配置了 个人地址, 检测配置的ip端口 是否正常
      let validate = true;
      await uiAutomationVerifySeleniumServer().then((res) => {
        if (res.data === "ok") {
          validate = true;
        } else if (res.data === "connectionErr") {
          this.showServerMessageBox(
            h("p", null, [
              h("span", null, this.$t("ui.check_grid")),
              h(
                "p",
                {
                  style: "color: #aeb0b3;cursor:pointer;font-size: 10px;",
                  on: {
                    click: (value) => {
                      this.redirectSetting();
                    },
                  },
                },
                this.$t("ui.view_config")
              ),
            ])
          );
          validate = false;
        } else {
          this.showServerMessageBox(
            h("p", null, [
              h("span", null, this.$t("ui.check_grid_ip")),
              h(
                "p",
                {
                  style: "color: #aeb0b3;cursor:pointer;font-size: 10px;",
                  on: {
                    click: (value) => {
                      this.redirectSetting();
                    },
                  },
                },
                this.$t("ui.view_config")
              ),
            ])
          );
          validate = false;
        }
      });
      return validate;
    },
    redirectSetting() {
      window.open("/#/setting/systemparametersetting");
    },
    showServerMessageBox(msg) {
      this.$msgbox({
        title: "",
        message: msg,
        confirmButtonText: this.$t("commons.confirm"),
        cancelButtonText: this.$t("commons.cancel"),
      })
        .then((action) => {})
        .catch(() => {});
    },
    async doExecute(row) {
      //校验selenium
      let serverResult = await this.validateSeleniumSetting();
      if (!serverResult) {
        return;
      }
      this.infoDb = false;
      let param = { planCaseIds: [] };
      this.reportId = "";
      this.buildExecuteParam(param, row);
      param.triggerMode = "MANUAL";
      param.requestOriginator = "TEST_PLAN";
      param.config = {
        mode: "serial",
      };
      if (this.planId) {
        testPlanUiScenarioCaseRun(param).then((response) => {
          this.runVisible = true;
          if (response.data && response.data.length > 0) {
            this.reportId = response.data[0].reportId;
          }
        });
      }
    },
    execute(row) {
      this.doExecute(row);
    },
    buildExecuteParam(param, row) {
      // param.id = row.id;
      param.id = getUUID();
      param.planScenarioId = row.id;
      param.projectId = row.projectId;
      param.planCaseIds.push(row.id);
      return param;
    },
    showReport(row) {
      this.runVisible = true;
      this.infoDb = true;
      this.reportId = getUUID() + "[TEST-PLAN-REDIRECT]" + row.reportId;
    },
    remove(row) {
      if (this.planId) {
        testPlanUiScenarioCaseDelete(row.id).then(() => {
          this.$success(this.$t("test_track.cancel_relevance_success"));
          this.$emit("refresh");
          this.search();
        });
      }
    },
    openById(item) {
      let automationData = this.$router.resolve({
        path: "/ui/automation",
        query: {
          redirectID: getUUID(),
          dataType: "scenario",
          dataSelectRange: "edit:" + item.caseId,
          projectId: item.projectId,
          workspaceId: getCurrentWorkspaceId(),
        },
      });
      window.open(automationData.href, "_blank");
    },
    handleDeleteBatch() {
      this.$alert(
        this.$t("test_track.plan_view.confirm_cancel_relevance") + "？",
        "",
        {
          confirmButtonText: this.$t("commons.confirm"),
          callback: (action) => {
            if (action === "confirm") {
              let param = buildBatchParam(this);
              param.ids = this.$refs.table.selectIds;
              if (this.planId) {
                param.planId = this.planId;
                testPlanUiScenarioCaseBatchDelete(param).then(() => {
                  this.$refs.table.selectRows.clear();
                  this.search();
                  this.$success(this.$t("test_track.cancel_relevance_success"));
                  this.$emit("refresh");
                });
              }
            }
          },
        }
      );
    },
    handleBatchEdit() {
      if (this.condition != null && this.condition.selectAll) {
        let selectAllRowParams = buildBatchParam(this);
        selectAllRowParams.ids = this.$refs.table.selectIds;
        testPlanUiScenarioCaseSelectAllTableRows(selectAllRowParams).then(
          (response) => {
            let dataRows = response.data;
            this.$refs.batchEdit.open(dataRows.size);
            this.$refs.batchEdit.setScenarioSelectRows(
              dataRows,
              "planScenario"
            );
          }
        );
      } else {
        this.$refs.batchEdit.open(this.$refs.table.selectRows.size);
        this.$refs.batchEdit.setScenarioSelectRows(
          this.$refs.table.selectRows,
          "planScenario"
        );
      }
    },
    batchEdit(form) {
      let param = {};
      param.mapping = strMapToObj(form.map);
      param.envMap = strMapToObj(form.projectEnvMap);
      param.environmentType = form.environmentType;
      param.envGroupId = form.envGroupId;
      if (this.planId) {
        testPlanUiScenarioCaseBatchUpdateEnv(param).then(() => {
          this.$success(this.$t("commons.save_success"));
          this.search();
        });
      }
    },
    getVersionOptions() {
      if (hasLicense()) {
        getVersionFilters(getCurrentProjectID()).then(
          (r) => (this.versionFilters = r.data)
        );
      }
    },
  },
};
</script>

<style scoped>
.plan-case-env {
  display: inline-block;
  padding: 0 0;
  max-width: 350px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 2px;
  margin-left: 5px;
}

.project-name {
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 80px;
  vertical-align: middle;
}

:deep(.el-drawer__wrapper) {
  z-index: 2000;
}

.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
