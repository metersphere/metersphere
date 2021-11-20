<template>
  <div v-loading="loading" class="ms-div">
    <el-card :style="{ width: w+'px'}">
      <el-row style="padding-top: 10px">
        <p class="tip"><span style="margin-left: 5px"></span>{{$t('commons.report_statistics.excel')}} </p>
      </el-row>
      <el-row>
        <el-table
          :data="tableData"
          :height="h"
          :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
          row-key="id"
          border
          class="ms-table">
          <el-table-column
            prop="name"
            :label="$t('api_monitor.date')"
            sortable>
          </el-table-column>
          <el-table-column
            prop="createCount"
            :label="$t('commons.report_statistics.add_case')"
            sortable>
          </el-table-column>
          <el-table-column
            prop="updateCount"
            sortable
            :label="$t('commons.report_statistics.change_case')">
          </el-table-column>
        </el-table>
      </el-row>
    </el-card>
  </div>
</template>

<script>
  export default {
    name: "TestAnalysisTable",
    components: {},
    props: {
      tableData: Array,
    },
    data() {
      return {
        w: document.documentElement.clientWidth - 760,
        h: document.body.clientHeight / 2.3 - 20,
        loading: false,
      }
    },
    methods: {},
  }
</script>

<style scoped>

  .tip {
    float: left;
    font-size: 14px;
    border-radius: 2px;
    border-left: 2px solid #783887;
    margin: 0px 20px 0px;
  }

  .ms-div {
    margin-bottom: 20px;
  }

  .ms-table {
    width: 95%;
    margin: 20px;
  }

  /deep/ .el-card__body {
    padding: 0px;
  }

</style>
