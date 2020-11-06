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
          prop="name"
          :label="$t('api_test.delimit.api_name')"
          show-overflow-tooltip/>

        <el-table-column
          prop="status"
          column-key="api_status"
          :label="$t('api_test.delimit.api_status')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
          <span class="el-dropdown-link">
           <template>
              <div>
                <ms-tag v-if="scope.row.status == 'Prepare'" type="info"
                        :content="$t('test_track.plan.plan_status_prepare')"/>
                <ms-tag v-if="scope.row.status == 'Underway'" type="primary"
                        :content="$t('test_track.plan.plan_status_running')"/>
                <ms-tag v-if="scope.row.status == 'Completed'" type="success"
                        :content="$t('test_track.plan.plan_status_completed')"/>
              </div>
          </template>
          </span>
          </template>
        </el-table-column>

        <el-table-column
          prop="path"
          :label="$t('api_test.delimit.api_type')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
          <span class="el-dropdown-link">
           <template>
             <div class="request-method">
                 <el-tag size="mini"
                         :style="{'background-color': getColor(true, scope.row.path)}" class="api-el-tag"> {{ scope.row.path }}</el-tag>
            </div>
          </template>
          </span>
          </template>

        </el-table-column>

        <el-table-column
          prop="url"
          :label="$t('api_test.delimit.api_path')"
          show-overflow-tooltip/>

        <el-table-column
          prop="userName"
          :label="$t('api_test.delimit.api_principal')"
          show-overflow-tooltip/>

        <el-table-column width="200" :label="$t('api_test.delimit.api_last_time')" prop="updateTime">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="caseTotal"
          :label="$t('api_test.delimit.api_case_number')"
          show-overflow-tooltip/>

        <el-table-column
          prop="caseStatus"
          :label="$t('api_test.delimit.api_case_status')"
          show-overflow-tooltip/>

        <el-table-column
          prop="casePassingRate"
          :label="$t('api_test.delimit.api_case_passing_rate')"
          show-overflow-tooltip/>


        <el-table-column
          :label="$t('commons.operating')" min-width="100">
          <template v-slot:default="scope">
            <el-button type="text" @click="editApi(scope.row)">编辑</el-button>
            <el-button type="text" @click="handleTestCase(scope.row)">用例</el-button>
            <el-button type="text" @click="handleDelete(scope.row)" style="color: #F56C6C">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

    </el-card>

    <ms-bottom-container v-bind:enableAsideHidden="isHide">
      <ms-api-case-list @apiCaseClose="apiCaseClose" :api="selectApi" :current-project="currentProject"/>
    </ms-bottom-container>

  </div>
</template>

<script>

  import MsTableHeader from '../../../../components/common/components/MsTableHeader';
  import MsTableOperator from "../../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../../common/components/MsTableOperatorButton";
  import MsTableButton from "../../../common/components/MsTableButton";
  import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
  import MsTablePagination from "../../../common/pagination/TablePagination";
  import MsTag from "../../../common/components/MsTag";
  import MsApiCaseList from "./ApiCaseList";
  import MsContainer from "../../../common/components/MsContainer";
  import MsBottomContainer from "./BottomContainer";
  import {Message} from "element-ui";

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
        moduleId: "",
        deletePath: "/test/case/delete",
        methodColorMap: new Map([
          ['GET', "#61AFFE"], ['POST', '#49CC90'], ['PUT', '#fca130'],
          ['PATCH', '#E2EE11'], ['DELETE', '#f93e3d'], ['OPTIONS', '#0EF5DA'],
          ['HEAD', '#8E58E7'], ['CONNECT', '#90AFAE'],
          ['DUBBO', '#C36EEF'], ['SQL', '#0AEAD4'], ['TCP', '#0A52DF'],
        ]),
        tableData: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
      }
    },
    props: {
      currentProject: {
        type: Object
      },
      currentModule: Object
    },
    created: function () {
      this.initTableData();
    },
    watch: {
      currentProject() {
        this.initTableData();
      },
      currentModule() {
        this.initTableData();
      }
    },
    methods: {
      initTableData() {
        if (this.currentModule != null) {
          if (this.currentModule.id == "rootID") {
            this.condition.moduleIds = [];
          } else {
            this.condition.moduleIds = this.currentModule.ids;
          }
        }
        if (this.currentProject != null) {
          this.condition.projectId = this.currentProject.id;
        }
        this.result = this.$post("/api/delimit/list/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
          this.total = response.data.itemCount;
          this.tableData = response.data.listObject;
        });
      },
      search() {
        this.initTableData();
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },

      editApi(row) {
        this.$emit('editApi', row);
      },
      handleTestCase(testCase) {
        this.selectApi = testCase;
        this.isHide = false;
      },
      handleDelete(testCase) {
        this.$get('/api/delimit/delete/' + testCase.id, () => {
          Message.success(this.$t('commons.delete_success'));
          this.initTableData();
        });
      },
      apiCaseClose() {
        this.isHide = true;
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
