<template>
  <div style="margin: 24px" class="running-task-table">
    <span class="table-title">
      {{ $t('api_test.home_page.running_task_list.title') }}
    </span>
    <div style="margin-top: 16px" v-loading="loading" element-loading-background="#FFFFFF">
      <div v-if="loadError"
           style="width: 100%; height: 300px; display: flex; flex-direction: column; justify-content: center; align-items: center">
        <img style="height: 100px;width: 100px;"
             src="/assets/module/figma/icon_load_error.svg"/>
        <span class="addition-info-title" style="color: #646A73">{{ $t("home.dashboard.public.load_error") }}</span>
      </div>
      <div v-if="!loadError">
        <el-table
          :enable-selection="false" :condition="condition" :data="tableData" class="adjust-table table-content"
          @refresh="search" header-cell-class-name="home-table-cell" style="min-height: 228px">
          <el-table-column
            type="index"
            :label="$t('home.table.index')"
            show-overflow-tooltip
            width="100px"/>
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            min-width="200px">
            <template v-slot:default="{row}">
              <!-- 若为只读用户不可点击之后跳转-->
              <span v-if="isReadOnly">
                {{ row.name }}
              </span>
              <el-link style="color: #783887;" :underline="false" v-else type="info" @click="redirect(row)">
                {{ row.name }}
              </el-link>
            </template>
          </el-table-column>
          <ms-table-column
            prop="taskType"
            :label="$t('home.table.task_type')"
            :filters="typeFilters"
            width="150px">
            <template v-slot:default="scope">
              <basic-task-type-label :value="scope.row.taskGroup"></basic-task-type-label>
            </template>
          </ms-table-column>
          <el-table-column
            prop="rule"
            :label="$t('home.table.run_rule')"
            show-overflow-tooltip
            width="150px"/>
          <el-table-column
            :label="$t('home.table.task_status')"
            width="150px">
            <template v-slot:default="scope">
              <div>
                <el-switch
                  :disabled="isReadOnly"
                  v-model="scope.row.taskStatus"
                  class="captcha-img"
                  @change="closeTaskConfirm(scope.row)"
                ></el-switch>
              </div>
            </template>
          </el-table-column>
          <el-table-column
            :label="$t('home.table.next_execution_time')"
            width="200px">
            <template v-slot:default="scope">
              <span>{{ scope.row.nextExecutionTime | datetimeFormat }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="creator"
            :label="$t('home.table.create_user')"
            show-overflow-tooltip
            width="150px"/>
          <template #empty>
            <div
              style="width: 100%;height: 238px;display: flex;flex-direction: column;justify-content: center;align-items: center">
              <img style="height: 100px;width: 100px;margin-bottom: 8px"
                   src="/assets/module/figma/icon_none.svg"/>
              <span class="addition-info-title">{{ $t("home.dashboard.public.no_data") }}</span>
            </div>
          </template>
        </el-table>
        <home-pagination v-if="tableData.length > 0" :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize" layout="prev, pager, next, sizes"
                         :total="total"/>
      </div>
    </div>
  </div>
</template>

<script>
import MsTag from "metersphere-frontend/src/components/MsTag";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import {getTrackRunningTask} from "@/api/track";
import {updatePlanSchedule} from "@/api/remote/plan/test-plan";
import HomePagination from "@/business/home/components/pagination/HomePagination";
import BasicTaskTypeLabel from "metersphere-frontend/src/components/BasicTaskTypeLabel";
import {hasPermission} from "@/business/utils/sdk-utils";

export default {
  name: "MsRunningTaskList",
  components: { MsTableColumn, MsTable, MsTag, HomePagination, BasicTaskTypeLabel },
  props: {
    callFrom: String,
  },
  data() {
    return {
      value: '100',
      tableData: [],
      visible: false,
      loading: false,
      loadError: false,
      currentPage: 1,
      pageSize: 5,
      total: 0,
      typeFilters: [],
      condition: {
        filters: {}
      }
    }
  },

  computed: {
    isReadOnly() {
      return false;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  mounted() {
    if (this.callFrom === 'api_test') {
      this.typeFilters = [
        {text: this.$t('api_test.home_page.running_task_list.scenario_schedule'), value: 'API_SCENARIO_TEST'},
        {text: this.$t('api_test.home_page.running_task_list.swagger_schedule'), value: 'SWAGGER_IMPORT'},
      ];
    } else {
      this.typeFilters = null;
    }
  },
  methods: {
    search() {
      if (!this.condition.filters.task_type) {
        this.condition.filters.task_type = ['TEST_PLAN_TEST'];
      }
      if (this.projectId) {
        this.loading = true;
        this.loadError = false;
        getTrackRunningTask(this.projectId, this.currentPage, this.pageSize, this.condition)
          .then((r) => {
            this.loading = false;
            this.loadError = false;
            this.total = r.data.itemCount;
            this.tableData = r.data.listObject;
          }).catch(() => {
            this.loading = false;
            this.loadError = true;
          });
      }
    },
    closeTaskConfirm(row) {
      let flag = row.taskStatus;
      row.taskStatus = !flag; //保持switch点击前的状态
      this.$confirm(this.$t('api_test.home_page.running_task_list.confirm.close_title'), this.$t('commons.prompt'), {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.updateTask(row);
      }).catch(() => {
      });
    },

    updateTask(taskRow) {
      updatePlanSchedule(taskRow)
        .then(() => {
          this.search();
        });
    },
    redirect(param) {
      if (param.taskGroup === 'TEST_PLAN_TEST') {
        if (!hasPermission('PROJECT_TRACK_PLAN:READ')) {
          return;
        }
        this.$emit('redirectPage', 'testPlanEdit', '', param.scenarioId);
      } else if (param.taskGroup === 'API_SCENARIO_TEST') {
        if (!hasPermission('PROJECT_API_SCENARIO:READ')) {
          return;
        }
        this.$emit('redirectPage', 'scenario', 'scenario', 'edit:' + param.scenarioId);
      } else if (param.taskGroup === 'SWAGGER_IMPORT') {
        if (!hasPermission('PROJECT_API_DEFINITION:READ')) {
          return;
        }
        this.$emit('redirectPage', 'api', 'api', {param});
      }
    }
  },
  activated() {
    this.search();
  },
}
</script>

<style scoped>
.running-task-table :deep(.el-link--inner) {
  width: 100%;
  float: left;
}

.running-task-table :deep(.status-label) {
  width: 75px;
  text-align: center;
}
</style>
