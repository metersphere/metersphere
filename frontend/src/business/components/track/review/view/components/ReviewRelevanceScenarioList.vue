<template>
  <div>
    <el-card class="table-card" v-loading="result.loading">
      <env-popover :env-map="projectEnvMap" :project-ids="projectIds" @setProjectEnvMap="setProjectEnvMap"
                   :project-list="projectList" ref="envPopover" class="env-popover"/>

      <el-table ref="scenarioTable" border :data="tableData" class="adjust-table" @select-all="handleSelectAll"
                @select="handleSelect">
        <el-table-column type="selection"/>

        <el-table-column prop="name" :label="$t('api_test.automation.scenario_name')"
                         show-overflow-tooltip/>
        <el-table-column prop="level" :label="$t('api_test.automation.case_level')"
                         show-overflow-tooltip>
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.level" ref="level"/>
          </template>

        </el-table-column>
        <el-table-column prop="tagNames" :label="$t('api_test.automation.tag')" min-width="120">
          <template v-slot:default="scope">
            <ms-tag v-for="itemName in scope.row.tags" :key="itemName" type="success" effect="plain" :content="itemName"
                    style="margin-left: 5px"/>
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
      </el-table>
      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

  </div>
</template>

<script>
import {_handleSelect, _handleSelectAll} from "@/common/js/tableUtils";
import MsTag from "@/business/components/common/components/MsTag";
import EnvPopover from "@/business/components/track/common/EnvPopover";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import PriorityTableItem from "@/business/components/track/common/tableItems/planview/PriorityTableItem";

export default {
  name: "ReviewRelevanceScenarioList",
  components: {PriorityTableItem, MsTablePagination, EnvPopover, MsTag},
  props: {
    referenced: {
      type: Boolean,
      default: false,
    },
    selectNodeIds: Array,
    projectId: String,
    reviewId: String,
  },
  data() {
    return {
      result: {},
      condition: {},
      currentScenario: {},
      schedule: {},
      selectAll: false,
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      reportId: "",
      infoDb: false,
      selectRows: new Set(),
      projectEnvMap: new Map(),
      projectList: [],
      projectIds: new Set(),
    }
  },
  watch: {
    selectNodeIds() {
      this.search();
    },
    projectId() {
      this.search();
    },
  },
  created() {
    this.getWsProjects();
  },
  methods: {
    search() {
      this.projectEnvMap.clear();
      this.projectIds.clear();
      if (!this.projectId) {
        return;
      }
      this.selectRows = new Set();
      this.loading = true;

      this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};

      this.condition.moduleIds = this.selectNodeIds;

      if (this.projectId != null) {
        this.condition.projectId = this.projectId;
      }

      if (this.reviewId != null) {
        this.condition.reviewId = this.reviewId;
      }

      let url = "/test/case/review/scenario/case/relevance/list/" + this.currentPage + "/" + this.pageSize;
      this.result = this.$post(url, this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.tableData.forEach(item => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
      });
    },
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.tableData, this.selectRows);
      this.initProjectIds();
    },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      this.initProjectIds();
    },
    setProjectEnvMap(projectEnvMap) {
      this.projectEnvMap = projectEnvMap;
    },
    getWsProjects() {
      this.$get("/project/listAll", res => {
        this.projectList = res.data;
      })
    },
    initProjectIds() {
      this.projectIds.clear();
      this.selectRows.forEach(row => {
        row.projectIds.forEach(id => {
          this.projectIds.add(id);
        })
      })
    },
    checkEnv() {
      return this.$refs.envPopover.checkEnv();
    }
  }
}
</script>

<style scoped>
/deep/ .el-drawer__header {
  margin-bottom: 0px;
}

.env-popover {
  float: right;
  margin-top: 4px;
}
</style>
