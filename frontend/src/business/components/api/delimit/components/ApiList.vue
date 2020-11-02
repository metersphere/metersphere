<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">

      <template v-slot:header>
        <ms-table-header :showCreate="false" :condition.sync="condition"
                         @search="search"
                         :title="$t('api_test.delimit.api_title')"/>
      </template>
      <el-table
        border
        :data="tableData"
        row-key="id"
        class="test-content adjust-table">

        <el-table-column
          prop="api_name"
          :label="$t('api_test.delimit.api_name')"
          show-overflow-tooltip/>

        <el-table-column
          prop="api_status"
          column-key="api_status"
          :label="$t('api_test.delimit.api_status')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
          <span class="el-dropdown-link">
           <template>
              <div>
                <ms-tag v-if="scope.row.api_status == 'Prepare'" type="info"
                        :content="$t('test_track.plan.plan_status_prepare')"/>
                <ms-tag v-if="scope.row.api_status == 'Underway'" type="primary"
                        :content="$t('test_track.plan.plan_status_running')"/>
                <ms-tag v-if="scope.row.api_status == 'Completed'" type="success"
                        :content="$t('test_track.plan.plan_status_completed')"/>
              </div>
          </template>
          </span>
          </template>
        </el-table-column>

        <el-table-column
          prop="api_type"
          :label="$t('api_test.delimit.api_type')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
          <span class="el-dropdown-link">
           <template>
             <div class="request-method">
                 <el-tag size="mini"
                         :style="{'background-color': getColor(true, scope.row.api_type)}" class="api-el-tag"> {{ scope.row.api_type }}</el-tag>
            </div>
          </template>
          </span>
          </template>

        </el-table-column>

        <el-table-column
          prop="api_path"
          :label="$t('api_test.delimit.api_path')"
          show-overflow-tooltip/>

        <el-table-column
          prop="api_principal"
          :label="$t('api_test.delimit.api_principal')"
          show-overflow-tooltip/>

        <el-table-column
          prop="api_last_time"
          :label="$t('api_test.delimit.api_last_time')"
          show-overflow-tooltip/>

        <el-table-column
          prop="api_case_number"
          :label="$t('api_test.delimit.api_case_number')"
          show-overflow-tooltip/>

        <el-table-column
          prop="api_case_status"
          :label="$t('api_test.delimit.api_case_status')"
          show-overflow-tooltip/>

        <el-table-column
          prop="api_case_passing_rate"
          :label="$t('api_test.delimit.api_case_passing_rate')"
          show-overflow-tooltip/>


        <el-table-column
          :label="$t('commons.operating')" min-width="100">
          <template v-slot:default="scope">
            <el-button type="text" @click="testCaseEdit(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleTestCase(scope.row)">用例</el-button>
            <el-button type="text" @click="handleDelete(scope.row)" style="color: #F56C6C">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

    </el-card>

    <ms-bottom-container v-bind:enableAsideHidden="isHide">
      <ms-api-case-list @apiCaseClose="apiCaseClose" :api="selectApi"></ms-api-case-list>
    </ms-bottom-container>

  </div>
</template>

<script>

  import MsTableHeader from '../../../../components/common/components/MsTableHeader';
  import MsTableOperator from "../../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
  import MsTableButton from "../../../common/components/MsTableButton";
  import {TEST_CASE_CONFIGS} from "../../../common/components/search/search-components";
  import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
  import MsTablePagination from "../../../common/pagination/TablePagination";
  import MsTag from "../../../common/components/MsTag";
  import MsApiCaseList from "./ApiCaseList";
  import MsContainer from "../../../common/components/MsContainer";
  import MsBottomContainer from "./BottomContainer";

  export default {
    name: "ApiList",
    components: {
      MsTableButton,
      MsTableOperatorButton,
      MsTableOperator,
      MsTableHeader,
      MsTablePagination,
      MsTag,
      MsApiCaseList,
      MsContainer,
      MsBottomContainer
    },
    data() {
      return {
        result: {},
        condition: {},
        isHide: true,
        selectApi: {},
        deletePath: "/test/case/delete",
        methodColorMap: new Map([
          ['GET', "#61AFFE"], ['POST', '#49CC90'], ['PUT', '#fca130'],
          ['PATCH', '#E2EE11'], ['DELETE', '#f93e3d'], ['OPTIONS', '#0EF5DA'],
          ['HEAD', '#8E58E7'], ['CONNECT', '#90AFAE'],
          ['DUBBO', '#C36EEF'], ['SQL', '#0AEAD4'], ['TCP', '#0A52DF'],
        ]),
        tableData: [{
          api_name: "接口名称",
          api_status: "Completed",
          api_type: "GET",
          api_path: "/api/common/login.do",
          api_principal: "负责人",
          api_last_time: "最后更新时间",
          api_case_number: "用例数",
          api_case_status: "失败",
          api_case_passing_rate: "100%"
        }, {
          api_name: "接口名称",
          api_status: "Prepare",
          api_type: "GET",
          api_path: "/api/common/login.do",
          api_principal: "负责人",
          api_last_time: "最后更新时间",
          api_case_number: "用例数",
          api_case_status: "通过",
          api_case_passing_rate: "100%"
        }, {
          api_name: "接口名称",
          api_status: "Underway",
          api_type: "POST",
          api_path: "/api/common/login.do",
          api_principal: "负责人",
          api_last_time: "最后更新时间",
          api_case_number: "用例数",
          api_case_status: "通过",
          api_case_passing_rate: "-"
        }],
        currentPage: 1,
        pageSize: 10,
        total: 0,
      }
    },
    props: {
      currentProject: {
        type: Object
      },
      selectParentNodes: {
        type: Array
      }
    },
    created: function () {
      this.initTableData();
    },
    watch: {
      currentProject() {
        this.initTableData();
      },
    },
    methods: {
      initTableData() {
      },
      search() {
        alert("s");
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },

      testCaseEdit(testCase) {
        this.$emit('testCaseEdit', testCase);
      },
      handleTestCase(testCase) {
        this.selectApi = testCase;
        this.isHide = false;
      },
      handleDelete(testCase) {

      },
      apiCaseClose() {
        this.isHide = true;
      },
      refresh() {
        this.condition = {components: TEST_CASE_CONFIGS};
        // this.selectIds.clear();
        this.selectRows.clear();
        this.$emit('refresh');
      },
      getColor(enable, method) {
        if (enable) {
          return this.methodColorMap.get(method);
        }
      },
    }
  }
</script>

<style scoped>
  .operate-button > div {
    display: inline-block;
    margin-left: 10px;
  }

  .request-method {
    padding: 0 5px;
    color: #1E90FF;
  }

  .api-el-tag {
    color: white;
  }

</style>
