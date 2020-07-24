<template>
  <el-dialog :title="$t('api_report.title')" :visible.sync="reportVisible">
    <el-table border class="adjust-table" :data="tableData" v-loading="result.loading">
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
</template>

<script>
  import MsApiReportStatus from "../report/ApiReportStatus";

  export default {
    name: "MsApiReportDialog",

    components: {MsApiReportStatus},

    props: ["testId"],

    data() {
      return {
        reportVisible: false,
        result: {},
        tableData: [],
        loading: false
      }
    },

    methods: {
      open() {
        this.reportVisible = true;

        let url = "/api/report/list/" + this.testId;
        this.result = this.$get(url, response => {
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
</style>
