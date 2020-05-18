<template>
  <div class="relate_report">
    <el-button type="success" plain @click="search">{{$t('api_report.title')}}</el-button>

    <el-dialog :title="$t('api_report.title')" :visible.sync="reportVisible">
      <el-table :data="tableData">
        <el-table-column :label="$t('commons.name')" width="150" show-overflow-tooltip>
          <template v-slot:default="scope">
            <el-link type="info" @click="link(scope.row)">{{ scope.row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column width="250" :label="$t('commons.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column width="250" :label="$t('commons.update_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('commons.status')">
          <template v-slot:default="{row}">
            <ms-api-report-status :row="row"/>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
  import MsApiReportStatus from "../report/ApiReportStatus";

  export default {
    name: "MsApiReportDialog",

    components: {MsApiReportStatus},

    data() {
      return {
        reportVisible: false,
        result: {},
        tableData: [],
        loading: false
      }
    },

    methods: {
      search() {
        this.reportVisible = true;

        this.result = this.$get("/api/report/recent/99", response => {
          this.tableData = response.data;
        });
      },
      link(row) {
        this.reportVisible = false;

        this.$router.push({
          path: '/api/report/view/' + row.id,
        })
      }
    },
  }
</script>

<style scoped>
  .relate_report {
    margin-left: 10px;
  }
</style>
