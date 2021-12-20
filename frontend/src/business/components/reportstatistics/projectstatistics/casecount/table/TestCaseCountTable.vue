<template>
  <div v-loading="loading" class="ms-div">
    <el-card :style="{ width: w+'px'}">
      <el-row style="padding-top: 10px">
        <p class="tip"><span style="margin-left: 5px"></span>{{$t('commons.report_statistics.excel')}} </p>
      </el-row>
      <el-row>
        <el-table
          :data="tableData"
          :max-height="tableHeight"
          :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
          row-key="id"
          border
          class="ms-table">
          <el-table-column
            prop="name"
            :label="groupName">
          </el-table-column>
          <el-table-column
            prop="allCount"
            :label="$t('commons.report_statistics.count')">
          </el-table-column>
          <el-table-column
              prop="testCaseCount"
              :label="$t('api_test.home_page.failed_case_list.table_value.case_type.functional')"
              v-if="isShowColumn('testCase')"
              >
          </el-table-column>
          <el-table-column
              prop="apiCaseCount"
              :label="$t('api_test.home_page.failed_case_list.table_value.case_type.api')"
              v-if="isShowColumn('apiCase')"
              >
          </el-table-column>
          <el-table-column
              prop="scenarioCaseCount"
              :label="$t('api_test.home_page.failed_case_list.table_value.case_type.scene')"
              v-if="isShowColumn('scenarioCase')"
              >
          </el-table-column>
          <el-table-column
              prop="loadCaseCount"
              :label="$t('api_test.home_page.failed_case_list.table_value.case_type.load')"
              v-if="isShowColumn('loadCase')"
              >
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
      groupName: String,
      showColoums: Array,
      fullScreen: {
        type: Boolean,
        default(){
          return false;
        }
      }
    },
    data() {
      return {
        tableHeight : "100px",
        w: document.documentElement.clientWidth - 760,
        loading: false,
      }
    },
    created() {
      this.getTableHeight();
      if(this.fullScreen){
        this.w = document.documentElement.clientWidth;
      }
    },
    methods: {
      isShowColumn(type){
        if(this.showColoums){
          return this.showColoums.findIndex(item => item=== type) >= 0;
        }else {
          return false;
        }

      },
      getTableHeight(){
        let countNumber = document.documentElement.clientHeight * 0.4 /1 - 140;
        countNumber = Math.ceil(countNumber);
        this.tableHeight = countNumber + 'px';
      }
    },
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
