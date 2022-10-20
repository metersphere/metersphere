<template>
  <el-card class="table-card" v-loading="result" body-style="padding:10px;">
    <template v-slot:header>
      <span class="title">
        {{ $t('api_test.home_page.running_task_list.title') }}
      </span>
    </template>
    <ms-table
      :enable-selection="false"
      :condition="condition"
      :data="tableData"
      @refresh="search"
      screen-height="300px">
      <el-table-column prop="index" :label="$t('home.table.index')" width="80"
                       show-overflow-tooltip/>
      <el-table-column prop="name" :label="$t('commons.name')" width="200">
        <template v-slot:default="{row}">
          <!-- 若为只读用户不可点击之后跳转-->
          <span v-if="isReadOnly">
            {{ row.name }}
          </span>
          <el-link v-else type="info" @click="redirect(row)">
            {{ row.name }}
          </el-link>
        </template>
      </el-table-column>
      <ms-table-column
        prop="taskType"
        :filters="typeFilters"
        :label="$t('home.table.task_type')" width="120">
        <template v-slot:default="scope">
          <ms-tag v-if="scope.row.taskGroup == 'API_SCENARIO_TEST'" type="success" effect="plain"
                  :content="$t('api_test.home_page.running_task_list.scenario_schedule')"/>
          <ms-tag v-if="scope.row.taskGroup == 'TEST_PLAN_TEST'" type="warning" effect="plain"
                  :content="$t('api_test.home_page.running_task_list.test_plan_schedule')"/>
          <ms-tag v-if="scope.row.taskGroup == 'SWAGGER_IMPORT'" type="danger" effect="plain"
                  :content="$t('api_test.home_page.running_task_list.swagger_schedule')"/>
        </template>
      </ms-table-column>
      <el-table-column prop="rule" :label="$t('home.table.run_rule')" width="120"
                       show-overflow-tooltip/>
      <el-table-column width="100" :label="$t('home.table.task_status')">
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
      <el-table-column width="170" :label="$t('home.table.next_execution_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.nextExecutionTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="creator" :label="$t('home.table.create_user')"
                       width="100" show-overflow-tooltip/>
      <el-table-column width="170" :label="$t('home.table.update_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </el-table-column>

    </ms-table>
  </el-card>
</template>

<script>
import {apiRunningTask} from "@/api/home";
import {scheduleDisable} from "@/api/schedule";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {operationConfirm} from "metersphere-frontend/src/utils";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";

export default {
  name: "MsRunningTaskList",
  components: {
    MsTableColumn,
    MsTable,
    MsTag
  },
  props: {
    callFrom: String,
  },
  data() {
    return {
      value: '100',
      result: false,
      tableData: [],
      visible: false,
      loading: false,
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
      this.typeFilters = [
        {text: this.$t('api_test.home_page.running_task_list.test_plan_schedule'), value: 'TEST_PLAN_TEST'}
      ];
    }
  },
  methods: {
    search() {
      if (!this.condition.filters.task_type) {
        if (this.callFrom === 'api_test') {
          this.condition.filters.task_type = ['SWAGGER_IMPORT', 'API_SCENARIO_TEST'];
        } else {
          this.condition.filters.task_type = ['TEST_PLAN_TEST'];
        }
      }
      if (this.projectId) {
        this.result = apiRunningTask(this.projectId, this.condition).then(response => {
          this.tableData = response.data;
        });
      }
    },

    closeTaskConfirm(row) {
      let flag = row.taskStatus;
      row.taskStatus = !flag; //保持switch点击前的状态
      operationConfirm(this, this.$t('api_test.home_page.running_task_list.confirm.close_title'), () => {
        this.updateTask(row);
      });
    },

    updateTask(taskRow) {

      this.result = scheduleDisable(taskRow).then(response => {
        this.search();
      });
    },
    redirect(param) {
      if (param.taskGroup === 'TEST_PLAN_TEST') {
        this.$emit('redirectPage', 'testPlanEdit', '', param.scenarioId);
      } else if (param.taskGroup === 'API_SCENARIO_TEST') {
        this.$emit('redirectPage', 'scenario', 'scenario', 'edit:' + param.scenarioId);
      } else if (param.taskGroup === 'SWAGGER_IMPORT') {
        this.$emit('redirectPage', 'api', 'api', {param});
      }
    }
  },

  created() {
    this.search();
  },
  activated() {
    this.search();
  },
}
</script>

<style scoped>

.el-table {
  cursor: pointer;
}

.el-card :deep( .el-card__header) {
  border-bottom: 0px solid #EBEEF5;
}

</style>
