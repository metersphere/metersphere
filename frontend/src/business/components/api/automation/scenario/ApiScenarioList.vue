<template>
  <div>
    <el-card class="table-card" v-loading="loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="search" title=""
                         :show-create="false"/>
      </template>

      <el-table ref="scenarioTable" border :data="tableData" class="adjust-table" @select-all="select" @select="select">
        <el-table-column type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="{row}">
            <show-more-btn :is-show="isSelect(row)" :buttons="buttons" :size="selection.length"/>
          </template>
        </el-table-column>
        <el-table-column prop="name" :label="$t('api_test.automation.scenario_name')"
                         show-overflow-tooltip/>
        <el-table-column prop="level" :label="$t('api_test.automation.case_level')"
                         show-overflow-tooltip>
          <template v-slot:default="scope">
            <ms-tag v-if="scope.row.level == 'P0'" type="info" effect="plain" content="P0"/>
            <ms-tag v-if="scope.row.level == 'P1'" type="warning" effect="plain" content="P1"/>
            <ms-tag v-if="scope.row.level == 'P2'" type="success" effect="plain" content="P2"/>
            <ms-tag v-if="scope.row.level == 'P3'" type="danger" effect="plain" content="P3"/>
          </template>

        </el-table-column>
        <el-table-column prop="tagNames" :label="$t('api_test.automation.tag')" width="200px">
          <template v-slot:default="scope">
            <div v-for="itemName in scope.row.tagNames" :key="itemName">
              <ms-tag type="success" effect="plain" :content="itemName"/>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userId" :label="$t('api_test.automation.creator')" show-overflow-tooltip/>
        <el-table-column prop="updateTime" :label="$t('api_test.automation.update_time')" width="180">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stepTotal" :label="$t('api_test.automation.step')" show-overflow-tooltip/>
        <el-table-column prop="lastResult" :label="$t('api_test.automation.last_result')">
          <template v-slot:default="{row}">
            <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'Success'">{{ $t('api_test.automation.success') }}</el-link>
            <el-link type="danger" @click="showReport(row)" v-if="row.lastResult === 'Fail'">{{ $t('api_test.automation.fail') }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" :label="$t('api_test.automation.passing_rate')"
                         show-overflow-tooltip/>
        <el-table-column :label="$t('commons.operating')" width="200px" v-if="!referenced">
          <template v-slot:default="{row}">
            <div v-if="trashEnable">
              <el-button type="text" @click="reductionApi(row)">恢复</el-button>
              <el-button type="text" @click="remove(row)">{{ $t('api_test.automation.remove') }}</el-button>
            </div>
            <div v-else>
              <el-button type="text" @click="edit(row)">{{ $t('api_test.automation.edit') }}</el-button>
              <el-button type="text" @click="execute(row)">{{ $t('api_test.automation.execute') }}</el-button>
              <el-button type="text" @click="copy(row)">{{ $t('api_test.automation.copy') }}</el-button>
              <el-button type="text" @click="remove(row)">{{ $t('api_test.automation.remove') }}</el-button>
              <ms-scenario-extend-buttons :row="row"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
      <div>
        <!-- 执行结果 -->
        <el-drawer :visible.sync="runVisible" :destroy-on-close="true" direction="ltr" :withHeader="true" :modal="false" size="90%">
          <ms-api-report-detail @refresh="search" :infoDb="infoDb" :report-id="reportId" :currentProjectId="projectId"/>
        </el-drawer>
        <!--测试计划-->
        <el-drawer :visible.sync="planVisible" :destroy-on-close="true" direction="ltr" :withHeader="false" :title="$t('test_track.plan_view.test_result')" :modal="false" size="90%">
          <ms-test-plan-list @addTestPlan="addTestPlan"/>
        </el-drawer>
      </div>
    </el-card>

  </div>
</template>

<script>
  import MsTableHeader from "@/business/components/common/components/MsTableHeader";
  import MsTablePagination from "@/business/components/common/pagination/TablePagination";
  import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
  import MsTag from "../../../common/components/MsTag";
  import {getUUID, getCurrentProjectID} from "@/common/js/utils";
  import MsApiReportDetail from "../report/ApiReportDetail";
  import MsTableMoreBtn from "./TableMoreBtn";
  import MsScenarioExtendButtons from "@/business/components/api/automation/scenario/ScenarioExtendBtns";
  import MsTestPlanList from "./testplan/TestPlanList";

  export default {
    name: "MsApiScenarioList",
    components: {MsTablePagination, MsTableMoreBtn, ShowMoreBtn, MsTableHeader, MsTag, MsApiReportDetail, MsScenarioExtendButtons, MsTestPlanList},
    props: {
      referenced: {
        type: Boolean,
        default: false,
      },
      selectNodeIds: Array,
      trashEnable: {
        type: Boolean,
        default: false,
      }
    },
    data() {
      return {
        loading: false,
        condition: {},
        currentScenario: {},
        schedule: {},
        selectAll: false,
        selection: [],
        tableData: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
        reportId: "",
        infoDb: false,
        runVisible: false,
        planVisible: false,
        projectId: "",
        runData: [],
        buttons: [
          {
            name: this.$t('api_test.automation.batch_add_plan'), handleClick: this.handleBatchAddCase
          }, {
            name: this.$t('api_test.automation.batch_execute'), handleClick: this.handleBatchExecute
          }
        ],
      }
    },
    created() {
      this.projectId = getCurrentProjectID();
      this.search();
    },
    watch: {
      selectNodeIds() {
        this.search();
      },
      trashEnable() {
        if (this.trashEnable) {
          this.search();
        }
      },
    },
    methods: {
      search() {
        this.loading = true;
        this.condition.filters = ["Prepare", "Underway", "Completed"];

        this.condition.moduleIds = this.selectNodeIds;

        if (this.trashEnable) {
          this.condition.filters = ["Trash"];
          this.condition.moduleIds = [];
        }

        if (this.projectId != null) {
          this.condition.projectId = this.projectId;
        }

        let url = "/api/automation/list/" + this.currentPage + "/" + this.pageSize;
        this.$post(url, this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.loading = false;
        });
      },
      handleCommand(cmd) {
        let table = this.$refs.scenarioTable;
        switch (cmd) {
          case "table":
            this.selectAll = false;
            table.toggleAllSelection();
            break;
          case "all":
            this.selectAll = true;
            break
        }
      },
      handleBatchAddCase() {
        this.planVisible = true;
      },
      addTestPlan(plans) {
        let obj = {planIds: plans, scenarioIds: this.selection};
        this.planVisible = false;
        this.$post("/api/automation/scenario/plan", obj, response => {
          this.$success(this.$t("commons.save_success"));
        });
      },
      handleBatchExecute() {
        this.infoDb = false;
        let url = "/api/automation/run";
        let run = {};
        let scenarioIds = this.selection;
        run.id = getUUID();
        run.scenarioIds = scenarioIds;
        run.projectId = getCurrentProjectID();
        this.$post(url, run, response => {
          let data = response.data;
          this.runVisible = true;
          this.reportId = run.id;
        });
      },
      selectAllChange() {
        this.handleCommand("table");
      },
      select(selection) {
        this.selection = selection.map(s => s.id);
        this.$emit('selection', selection);
      },
      isSelect(row) {
        return this.selection.includes(row.id)
      },
      edit(row) {
        this.$emit('edit', row);
      },
      reductionApi(row) {
        row.scenarioDefinition = null;
        let rows = [row];
        this.$post("/api/automation/reduction", rows, response => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        })
      },
      execute(row) {
        this.infoDb = false;
        let url = "/api/automation/run";
        let run = {};
        let scenarioIds = [];
        scenarioIds.push(row.id);
        run.id = getUUID();
        run.projectId = getCurrentProjectID();
        run.scenarioIds = scenarioIds;
        this.$post(url, run, response => {
          let data = response.data;
          this.runVisible = true;
          this.reportId = run.id;
        });
      },
      copy(row) {
        row.copy = true;
        this.$emit('edit', row);
      },
      showReport(row) {
        this.runVisible = true;
        this.infoDb = true;
        this.reportId = row.reportId;
      },
      remove(row) {
        if (this.trashEnable) {
          this.$get('/api/automation/delete/' + row.id, () => {
            this.$success(this.$t('commons.delete_success'));
            this.search();
          });
          return;
        }
        this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + " ？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              let ids = [row.id];
              this.$post('/api/automation/removeToGc/', ids, () => {
                this.$success(this.$t('commons.delete_success'));
                this.search();
              });
            }
          }
        });
      },
    }
  }
</script>

<style scoped>
  /deep/ .el-drawer__header {
    margin-bottom: 0px;
  }
</style>
