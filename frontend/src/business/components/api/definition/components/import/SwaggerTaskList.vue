<template>
  <el-table border
            v-loading="result.loading"
            highlight-current-row
            @row-click="handleRowClick"
            :data="tableData"
            class="adjust-table table-content"
            height="300px">
    <el-table-column prop="index"
                     width="60"
                     :label="$t('api_test.home_page.running_task_list.table_coloum.index')"
                     show-overflow-tooltip/>
<!--    <el-table-column prop="SwaggerUrlId">-->
<!--    </el-table-column>-->
    <el-table-column
      prop="swaggerUrl"
      :label="$t('swaggerUrl')"
      min-width="170" show-overflow-tooltip>
    </el-table-column>
    <el-table-column prop="modulePath" :label="$t('导入模块')"
                     min-width="100"
                     show-overflow-tooltip/>
    <el-table-column prop="rule" label="同步规则"
                     min-width="140"
                     show-overflow-tooltip/>
    <el-table-column width="100" :label="$t('api_test.home_page.running_task_list.table_coloum.task_status')">
      <template v-slot:default="scope">
        <div>
          <el-switch
            v-model="scope.row.enable"
            class="captcha-img"
            @click.native="closeTaskConfirm(scope.row)"
          ></el-switch>
        </div>
      </template>
    </el-table-column>
    <el-table-column width="170" :label="$t('api_test.home_page.running_task_list.table_coloum.next_execution_time')">
      <template v-slot:default="scope">
        <span>{{ scope.row.nextExecutionTime | timestampFormatDate }}</span>
      </template>
    </el-table-column>
    <el-table-column width="100" label="操作">
      <template v-slot:default="scope">
        <el-button
          type="danger"
          icon="el-icon-delete"
          size="mini"
          @click.native.prevent="deleteRowTask(scope.row)"
        ></el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
import {getCurrentProjectID} from "../../../../../../common/js/utils";

export default {
  name: "SwaggerTaskList",
  data() {
    return {
      tableData: [],
      result: {}
    };
  },
  props: {
    param: Object
  },
  methods: {
    search() {
      let projectId = getCurrentProjectID();
      this.result = this.$get("/api/definition/scheduleTask/" + projectId, response => {
        this.tableData = response.data;
        response.data.forEach(item => {
          if (item.taskId === this.param.taskID) {
            this.handleRowClick(item);
          }
        });
      });
    },
    handleRowClick(row) {
      this.$emit('rowClick', row);
    },
    closeTaskConfirm(row) {
      let flag = row.enable;
      row.enable = !flag;
      if (row.enable) {
        this.$confirm(this.$t('api_test.home_page.running_task_list.confirm.close_title'), this.$t('commons.prompt'), {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          row.enable = !row.enable
          this.updateTask(row);
        }).catch(() => {
        });
      } else {
        row.enable = !row.enable
        this.updateTask(row);
      }

    },
    updateTask(taskRow) {
      let schedule = {
        resourceId: taskRow.id,
        id: taskRow.taskId,
        enable: taskRow.enable,
        value: taskRow.rule
      }
      this.result = this.$post('/api/definition/schedule/switch', schedule, response => {
        this.search();
      });
    },
    deleteRowTask(row) {
      this.result = this.$post('/api/definition/schedule/delete', row, response => {
        this.search();
        this.$emit('clear');
      });
    }

  },
  mounted() {
    this.search()
  },
}
</script>


<style scoped>

</style>
