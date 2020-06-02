<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <div>
            <el-row type="flex" justify="space-between" align="middle">
              <span class="title">{{$t('commons.test')}}</span>
              <span class="search">
            <el-input type="text" size="small" :placeholder="$t('load_test.search_by_name')"
                      prefix-icon="el-icon-search"
                      maxlength="60"
                      v-model="condition.name" @change="search" clearable/>
          </span>
            </el-row>
          </div>
        </template>

        <el-table :data="tableData" class="test-content"
                  @sort-change="sort"
                  @filter-change="filter"
        >
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            width="150"
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <el-link type="info" @click="link(scope.row)">{{ scope.row.name }}</el-link>
            </template>
          </el-table-column>
          <el-table-column
            prop="projectName"
            :label="$t('load_test.project_name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="userName"
            :label="$t('load_test.user_name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            width="250"
            sortable
            prop="createTime"
            :label="$t('commons.create_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="250"
            sortable
            prop="updateTime"
            :label="$t('commons.update_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            column-key="status"
            :filters="statusFilters"
            :label="$t('commons.status')">
            <template v-slot:default="{row}">
              <ms-performance-test-status :row="row"/>
            </template>
          </el-table-column>
          <el-table-column
            width="150"
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <ms-table-operators :buttons="buttons" :row="scope.row"/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import MsContainer from "../../common/components/MsContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";
  import MsPerformanceTestStatus from "./PerformanceTestStatus";
  import MsTableOperators from "../../common/components/MsTableOperators";
  import {_filter, _sort} from "../../../../common/js/utils";

  export default {
    components: {
      MsPerformanceTestStatus,
      MsTablePagination,
      MsTableOperator,
      MsContainer,
      MsMainContainer,
      MsTableOperators
    },
    data() {
      return {
        result: {},
        queryPath: "/performance/list",
        deletePath: "/performance/delete",
        condition: {},
        projectId: null,
        tableData: [],
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        loading: false,
        testId: null,
        buttons: [
          {
            tip: this.$t('commons.edit'), icon: "el-icon-edit",
            exec: this.handleEdit
          }, {
            tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "success",
            exec: this.handleCopy
          }, {
            tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
            exec: this.handleDelete
          }
        ],
        statusFilters: [
          {text: 'Saved', value: 'Saved'},
          {text: 'Starting', value: 'Starting'},
          {text: 'Running', value: 'Running'},
          {text: 'Reporting', value: 'Reporting'},
          {text: 'Completed', value: 'Completed'},
          {text: 'Error', value: 'Error'}
        ]
      }
    },
    watch: {
      '$route'(to) {
        this.projectId = to.params.projectId;
        this.initTableData();
      }
    },
    created: function () {
      this.projectId = this.$route.params.projectId;
      this.initTableData();
    },
    methods: {
      initTableData() {

        if (this.projectId !== 'all') {
          this.condition.projectId = this.projectId;
        }

        this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
        });
      },
      search() {
        this.initTableData();
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      handleEdit(testPlan) {
        this.$router.push({
          path: '/performance/test/edit/' + testPlan.id,
        })
      },
      handleCopy(testPlan) {
        this.result = this.$post("/performance/copy", {id: testPlan.id}, () => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        });
      },
      handleDelete(testPlan) {
        this.$alert(this.$t('load_test.delete_confirm') + testPlan.name + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this._handleDelete(testPlan);
            }
          }
        });
      },
      _handleDelete(testPlan) {
        let data = {
          id: testPlan.id
        };

        this.result = this.$post(this.deletePath, data, () => {
          this.$success(this.$t('commons.delete_success'));
          this.initTableData();
        });
      },
      sort(column) {
        _sort(column, this.condition);
        this.initTableData();
      },
      filter(filters) {
        _filter(filters, this.condition);
        this.initTableData();
      },
      link(row) {
        this.$router.push({
          path: '/performance/test/edit/' + row.id,
        })
      }
    }
  }
</script>

<style scoped>

  .test-content {
    width: 100%;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }
</style>
