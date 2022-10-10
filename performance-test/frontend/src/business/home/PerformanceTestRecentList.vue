<template>
  <el-card class="table-card" v-loading="loading">
    <template v-slot:header>
      <span class="title">{{ $t('commons.test') }}</span>
    </template>
    <el-table border :data="tableData" class="adjust-table table-content" @row-click="link" height="300px">
      <el-table-column prop="name" :label="$t('commons.name')" width="150" show-overflow-tooltip/>
      <el-table-column width="180" :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <span>{{ datetimeFormat(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column width="180" :label="$t('commons.update_time')">
        <template v-slot:default="scope">
          <span>{{ datetimeFormat(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" :label="$t('commons.status')">
        <template v-slot:default="{row}">
          <ms-performance-test-status :row="row"/>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>

import MsPerformanceTestStatus from "../test/PerformanceTestStatus";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getRecentTests} from "@/api/performance";
import {datetimeFormat} from "fit2cloud-ui/src/filters/time";


export default {
  name: "MsPerformanceTestRecentList",
  components: {MsPerformanceTestStatus},
  data() {
    return {
      loading: false,
      tableData: []
    };
  },

  methods: {
    search() {
      let condition = {
        projectId: getCurrentProjectID()
      };
      this.loading = getRecentTests(condition)
        .then(response => {
          this.tableData = response.data;
        })
    },
    link(row) {
      this.$router.push({path: `/performance/test/edit/${row.id}`});
    },
    datetimeFormat
  },

  created() {
    this.search();
  },
};
</script>

<style scoped>

.el-table {
  cursor: pointer;
}

</style>
