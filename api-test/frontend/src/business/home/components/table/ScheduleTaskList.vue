<template>
  <div style="margin: 24px" class="update-api-table">
    <span class="table-title">
      {{ $t('api_test.home_page.running_task_list.title') }}
    </span>
    <div style="margin-top: 16px;" v-loading="loading" element-loading-background="#FFFFFF">
      <div v-show="loadError"
           style="width: 100%; height: 300px; display: flex; flex-direction: column;     justify-content: center;align-items: center">
        <img style="height: 100px;width: 100px;"
             src="/assets/figma/icon_load_error.svg"/>
        <span class="addition-info-title" style="color: #646A73">{{ $t("home.dashboard.public.load_error") }}</span>
      </div>
      <div v-show="!loadError">
        <el-table border :data="tableData" class="adjust-table table-content" style="min-height: 228px"
                  @row-click="clickRow"
                  header-cell-class-name="home-table-cell"
                  element-loading-background="#FFFFFF">
          <!--序号-->
          <el-table-column prop="index" :label="$t('home.new_case.index')" width="50"
                           show-overflow-tooltip>
            <template v-slot:default="{row}">
              {{ row.index }}
            </template>
          </el-table-column>
          <!--名称-->
          <el-table-column prop="name" :label="$t('commons.name')"
                           show-overflow-tooltip min-width="200">
            <template v-slot:default="{row}">
              <span class="redirectColumn">
                {{ row.name }}
              </span>
            </template>
          </el-table-column>
          <!--任务类型-->
          <el-table-column prop="taskGroup" :label="$t('home.table.task_type')" min-width="100"
                           show-overflow-tooltip>
            <template v-slot:default="scope">
              <span class="el-dropdown-link">
                <basic-status-label :value="scope.row.taskGroup"/>
              </span>
            </template>
          </el-table-column>

          <!--任务状态-->
          <el-table-column prop="status" :label="$t('home.table.task_status')" width="100">
            <template v-slot:default="scope">
              <div>
                <el-switch
                  v-model="scope.row.taskStatus"
                  class="captcha-img"
                  @change="closeTaskConfirm(scope.row)"
                ></el-switch>
              </div>
            </template>
          </el-table-column>
          <!--创建人-->
          <el-table-column prop="creator" :label="$t('home.table.create_user')"
                           width="100" show-overflow-tooltip/>
          <!--运行规则-->
          <el-table-column prop="rule" :label="$t('home.table.run_rule')" min-width="150"
                           show-overflow-tooltip>
          </el-table-column>
          <!--下次更新时间-->
          <el-table-column :label="$t('home.table.update_time')" width="170" align="right">
            <template v-slot:default="scope">
              {{ scope.row.updateTime | datetimeFormat }}
            </template>
          </el-table-column>
          <!--更新时间-->
          <el-table-column :label="$t('home.table.next_execution_time')" width="170" align="right">
            <template v-slot:default="scope">
              {{ scope.row.updateTime | datetimeFormat }}
            </template>
          </el-table-column>

          <template #empty>
            <div
              style="width: 100%; height: 144px;display: flex; flex-direction: column;     justify-content: center;align-items: center">
              <img style="height: 100px;width: 100px;margin-bottom: 8px"
                   src="/assets/figma/icon_none.svg"/>
              <span class="addition-info-title">{{ $t("home.dashboard.public.no_data") }}</span>
            </div>
          </template>
        </el-table>
        <home-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                               layout="prev, pager, next, sizes"
                               :total="total"/>
      </div>
    </div>
  </div>
</template>

<script>
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import ApiStatus from "@/business/definition/components/list/ApiStatus";
import HomeTablePagination from "@/business/home/components/table/HomeTablePagination";
import BasicStatusLabel from "metersphere-frontend/src/components/BasicStatusLabel";
import {operationConfirm} from "metersphere-frontend/src/utils";
import {getRunningTask} from "@/api/home";
import {scheduleDisable} from "@/api/schedule";

export default {
  name: "ScheduleTask",
  components: {
    BasicStatusLabel, ApiStatus, HomeTablePagination
  },
  data() {
    return {
      result: false,
      loadError: false,
      loading: false,
      tableData: [],
      currentPage: 1,
      pageSize: 5,
      total: 0,
      condition: {
        filters: {}
      }
    }
  },
  activated() {
    this.search();
  },
  methods: {
    clickRow(row, column) {
      if (column.property !== 'status') {
        if (row.taskGroup === 'API_SCENARIO_TEST') {
          this.redirectPage('scenario', 'scenario', 'edit:' + row.scenarioId);
        } else if (row.taskGroup === 'SWAGGER_IMPORT') {
          this.redirectPage('api', 'swagger');
        }
      }
    },
    search() {
      let projectId = getCurrentProjectID();
      this.loading = true;
      this.loadError = false;
      this.condition.filters.task_type = ['SWAGGER_IMPORT', 'API_SCENARIO_TEST'];
      this.result = getRunningTask(projectId, this.currentPage, this.pageSize, this.condition).then(response => {
        this.total = response.data.itemCount;
        this.tableData = response.data.listObject;
        this.loading = false;
        this.loadError = false;
      }).catch(() => {
        this.loading = false;
        this.loadError = true;
      });
    },
    redirectPage(redirectPage, dataType, selectRange) {
      this.$emit('redirectPage', redirectPage, dataType, selectRange, null);
    },
    closeTaskConfirm(row) {
      let flag = row.taskStatus;
      row.taskStatus = !flag; //保持switch点击前的状态
      operationConfirm(this, this.$t('api_test.home_page.running_task_list.confirm.close_title'), () => {
        this.updateTask(row);
      });
    },
    updateTask(taskRow) {
      this.loading = true;
      this.loadError = false;
      this.result = scheduleDisable(taskRow).then(() => {
        this.search();
      });
    },
  }
}
</script>

<style scoped>
.update-api-table :deep(.el-link--inner) {
  width: 100%;
  float: left;
}

.update-api-table :deep(.status-label) {
  width: 68px;
}
</style>
