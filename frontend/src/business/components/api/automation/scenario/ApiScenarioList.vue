<template>
  <div>
    <el-card class="table-card" v-loading="loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="selectByParam" title=""
                         :show-create="false"/>
      </template>

      <el-table ref="scenarioTable" border :data="tableData" class="adjust-table" @select-all="select" @select="select"
                v-loading="loading">
        <el-table-column type="selection" width="38"/>
        <el-table-column v-if="!referenced" width="40" :resizable="false" align="center">
          <el-dropdown slot="header" style="width: 14px">
              <span class="el-dropdown-link" style="width: 14px">
                <i class="el-icon-arrow-down el-icon--right" style="margin-left: 0px"></i>
              </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native.stop="isSelectDataAll(true)">
                {{ $t('api_test.batch_menus.select_all_data', [total]) }}
              </el-dropdown-item>
              <el-dropdown-item @click.native.stop="isSelectDataAll(false)">
                {{ $t('api_test.batch_menus.select_show_data', [tableData.length]) }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>

          <template v-slot:default="{row}">
            <show-more-btn :is-show="isSelect(row)" :buttons="buttons" :size="selectDataCounts"/>
          </template>
        </el-table-column>
        <el-table-column prop="num" label="ID"
                         show-overflow-tooltip/>
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
        <el-table-column prop="tags" :label="$t('api_test.automation.tag')" width="200px">
          <template v-slot:default="scope">
            <div v-for="(itemName,index)  in scope.row.tags" :key="index">
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
            <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'Success'">
              {{ $t('api_test.automation.success') }}
            </el-link>
            <el-link type="danger" @click="showReport(row)" v-if="row.lastResult === 'Fail'">
              {{ $t('api_test.automation.fail') }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" :label="$t('api_test.automation.passing_rate')"
                         show-overflow-tooltip/>
        <el-table-column :label="$t('commons.operating')" width="200px" v-if="!referenced">
          <template v-slot:default="{row}">
            <div v-if="trashEnable">
              <el-button type="text" @click="reductionApi(row)" v-tester>{{ $t('commons.reduction') }}</el-button>
              <el-button type="text" @click="remove(row)" v-tester>{{ $t('api_test.automation.remove') }}</el-button>
            </div>
            <div v-else>
              <el-button type="text" @click="edit(row)" v-tester>{{ $t('api_test.automation.edit') }}</el-button>
              <el-button type="text" @click="execute(row)" v-tester>{{ $t('api_test.automation.execute') }}</el-button>
              <el-button type="text" @click="copy(row)" v-tester>{{ $t('api_test.automation.copy') }}</el-button>
              <el-button type="text" @click="remove(row)" v-tester>{{ $t('api_test.automation.remove') }}</el-button>
              <ms-scenario-extend-buttons :row="row"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
      <div>
        <!-- 执行结果 -->
        <el-drawer :visible.sync="runVisible" :destroy-on-close="true" direction="ltr" :withHeader="true" :modal="false"
                   size="90%">
          <ms-api-report-detail @refresh="search" :infoDb="infoDb" :report-id="reportId" :currentProjectId="projectId"/>
        </el-drawer>
        <!--测试计划-->
        <el-drawer :visible.sync="planVisible" :destroy-on-close="true" direction="ltr" :withHeader="false"
                   :title="$t('test_track.plan_view.test_result')" :modal="false" size="90%">
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
  components: {
    MsTablePagination,
    MsTableMoreBtn,
    ShowMoreBtn,
    MsTableHeader,
    MsTag,
    MsApiReportDetail,
    MsScenarioExtendButtons,
    MsTestPlanList
  },
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
      selection: [],
      tableData: [],
      selectDataRange: 'all',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: "",
      batchReportId: "",
      content: {},
      infoDb: false,
      runVisible: false,
      planVisible: false,
      projectId: "",
      runData: [],
      report: {},
      selectDataSize: 0,
      selectAll: false,
      buttons: [
        {
          name: this.$t('api_test.automation.batch_add_plan'), handleClick: this.handleBatchAddCase
        }, {
          name: this.$t('api_test.automation.batch_execute'), handleClick: this.handleBatchExecute
        }
      ],
      isSelectAllDate: false,
      unSelection: [],
      selectDataCounts: 0,
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
    batchReportId() {
      this.loading = true;
      this.getReport();
    }
  },
  computed: {
    isNotRunning() {
      return "Running" !== this.report.status;
    }
  },
  methods: {
    selectByParam() {
      this.changeSelectDataRangeAll();
      this.search();
    },
    search() {
      this.condition.filters = ["Prepare", "Underway", "Completed"];
      this.condition.moduleIds = this.selectNodeIds;
      if (this.trashEnable) {
        this.condition.filters = ["Trash"];
        this.condition.moduleIds = [];
      }

      if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }

      //检查是否只查询本周数据
      this.condition.selectThisWeedData = false;
      this.condition.executeStatus = null;
      this.isSelectThissWeekData();
      switch (this.selectDataRange){
        case 'thisWeekCount':
          this.condition.selectThisWeedData = true;
          break;
        case 'unExecute':
          this.condition.executeStatus = 'unExecute';
          break;
        case 'executeFailed':
          this.condition.executeStatus = 'executeFailed';
          break;
        case 'executePass':
          this.condition.executeStatus = 'executePass';
          break;
      }
      this.selection = [];

      this.selectAll = false;
      this.unSelection = [];
      this.selectDataCounts = 0;
      let url = "/api/automation/list/" + this.currentPage + "/" + this.pageSize;
      if (this.condition.projectId) {
        this.loading = true;
        this.$post(url, this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          this.loading = false;
          this.unSelection = data.listObject.map(s => s.id);
        });
      }
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

      obj.projectId = getCurrentProjectID();
      obj.selectAllDate = this.isSelectAllDate;
      obj.unSelectIds = this.unSelection;
      obj = Object.assign(obj, this.condition);

      this.planVisible = false;
      this.$post("/api/automation/scenario/plan", obj, response => {
        this.$success(this.$t("commons.save_success"));
      });
    },
    getReport() {
      if (this.batchReportId) {
        let url = "/api/scenario/report/get/" + this.batchReportId;
        this.$get(url, response => {
          this.report = response.data || {};
          if (response.data) {
            if (this.isNotRunning) {
              try {
                this.content = JSON.parse(this.report.content);
              } catch (e) {
                throw e;
              }
              this.loading = false;
              this.$success("批量执行成功，请到报告页面查看详情！");
            } else {
              setTimeout(this.getReport, 2000)
            }
          } else {
            this.loading = false;
            this.$error(this.$t('api_report.not_exist'));
          }
        });
      }
    },
    handleBatchExecute() {
      this.infoDb = false;
      let url = "/api/automation/run/batch";
      let run = {};
      let scenarioIds = this.selection;
      run.id = getUUID();
      run.scenarioIds = scenarioIds;
      run.projectId = getCurrentProjectID();
      run.selectAllDate = this.isSelectAllDate;
      run.unSelectIds = this.unSelection;

      run = Object.assign(run, this.condition);
      this.$post(url, run, response => {
        let data = response.data;
        this.runVisible = false;
        this.batchReportId = run.id;
      });
    },
    select(selection) {
      this.selection = selection.map(s => s.id);

      //统计应当展示选择了多少行
      this.selectRowsCount(this.selection)

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
      row.tags = null;
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
    //是否选择了全部数据
    isSelectDataAll(dataType) {
      this.isSelectAllDate = dataType;
      this.selectRowsCount(this.selection);
      //如果已经全选，不需要再操作了
      if (this.selection.length != this.tableData.length) {
        this.$refs.scenarioTable.toggleAllSelection(true);
      }
    },
    //选择数据数量统计
    selectRowsCount(selection) {
      let selectedIDs = selection;
      let allIDs = this.tableData.map(s => s.id);
      this.unSelection = allIDs.filter(function (val) {
        return selectedIDs.indexOf(val) === -1
      });
      if (this.isSelectAllDate) {
        this.selectDataCounts = this.total - this.unSelection.length;
      } else {
        this.selectDataCounts = this.selection.length;
      }
    },
    //判断是否只显示本周的数据。  从首页跳转过来的请求会带有相关参数
    isSelectThissWeekData() {
      let dataRange = this.$route.params.dataSelectRange;
      this.selectDataRange = dataRange;
    },
    changeSelectDataRangeAll() {
      this.$emit("changeSelectDataRangeAll");
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
