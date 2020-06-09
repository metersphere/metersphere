<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">{{$t('api_report.title')}}</span>
    </template>
    <el-table :data="tableData" class="table-content ms-cell-tooltip" @row-click="link">
      <el-table-column prop="name"  :label="$t('commons.name')" show-overflow-tooltip/>
      <el-table-column width="200" :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column width="200" :label="$t('commons.update_time')">
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
  </el-card>
</template>

<script>
  import MsApiReportStatus from "../report/ApiReportStatus";

  export default {
    name: "MsApiReportRecentList",

    components: {MsApiReportStatus},

    data() {
      return {
        result: {},
        tableData: [],
        loading: false
      }
    },

    methods: {
      search() {
        this.result = this.$get("/api/report/recent/5", response => {
          this.tableData = response.data;
        });
      },
      link(row) {
        this.$router.push({
          path: '/api/report/view/' + row.id,
        })
      }
    },

    created() {
      this.search();
    }
  }
</script>

<style scoped>

  .el-table {
    cursor:pointer;
  }

</style>
