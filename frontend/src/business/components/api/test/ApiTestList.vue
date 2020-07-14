<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="search"
                           :title="$t('commons.test')"
                           @create="create" :createTip="$t('load_test.create')"/>
        </template>
        <el-table :data="tableData" class="table-content" @sort-change="sort" @row-click="handleView"
                  @filter-change="filter">
          <el-table-column prop="name" :label="$t('commons.name')" width="250" show-overflow-tooltip>
          </el-table-column>
          <el-table-column prop="projectName" :label="$t('load_test.project_name')" width="200" show-overflow-tooltip/>
          <el-table-column prop="userName" :label="$t('api_test.creator')" width="150" show-overflow-tooltip/>
          <el-table-column width="250" :label="$t('commons.create_time')" sortable
                           prop="createTime">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column width="250" :label="$t('commons.update_time')" sortable
                           prop="updateTime">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" :label="$t('commons.status')"
                           column-key="status"
                           :filters="statusFilters">
            <template v-slot:default="{row}">
              <ms-api-test-status :row="row"/>
            </template>
          </el-table-column>
          <el-table-column width="150" :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <ms-table-operators :buttons="buttons" :row="scope.row"/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import MsContainer from "../../common/components/MsContainer";
  import MsMainContainer from "../../common/components/MsMainContainer";
  import MsApiTestStatus from "./ApiTestStatus";
  import MsTableOperators from "../../common/components/MsTableOperators";
  import {_filter, _sort} from "../../../../common/js/utils";
  import {getTestConfigs} from "../../common/components/search/search-components";

  export default {
    components: {
      MsTableOperators,
      MsApiTestStatus, MsMainContainer, MsContainer, MsTableHeader, MsTablePagination, MsTableOperator
    },
    data() {
      return {
        result: {},
        condition: {
          components: getTestConfigs()
        },
        projectId: null,
        tableData: [],
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        loading: false,
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
      '$route': 'init'
    },

    methods: {
      create() {
        this.$router.push('/api/test/create');
      },
      search(combine) {
        // 只有在点击高级搜索的查询按钮时combine才有值
        let condition = combine ? {combine: combine} : this.condition;
        if (this.projectId !== 'all') {
          condition.projectId = this.projectId;
        }

        let url = "/api/list/" + this.currentPage + "/" + this.pageSize;
        this.result = this.$post(url, condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
        });
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      },
      handleEdit(test) {
        this.$router.push({
          path: '/api/test/edit?id=' + test.id,
        })
      },
      handleView(test) {
        this.$router.push({
          path: '/api/test/view?id=' + test.id,
        })
      },
      handleDelete(test) {
        this.$alert(this.$t('load_test.delete_confirm') + test.name + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              this.result = this.$post("/api/delete", {id: test.id}, () => {
                this.$success(this.$t('commons.delete_success'));
                this.search();
              });
            }
          }
        });
      },
      handleCopy(test) {
        this.result = this.$post("/api/copy", {projectId: test.projectId, id: test.id, name: test.name}, () => {
          this.$success(this.$t('commons.copy_success'));
          this.search();
        });
      },
      init() {
        this.projectId = this.$route.params.projectId;
        if (this.projectId && this.projectId !== "all") {
          this.$store.commit('setProjectId', this.projectId);
        }
        this.search();
      },
      sort(column) {
        _sort(column, this.condition);
        this.init();
      },
      filter(filters) {
        _filter(filters, this.condition);
        this.init();
      },
    },
    created() {
      this.init();
    }

  }
</script>

<style scoped>
  .table-content {
    width: 100%;
  }

  .el-table {
    cursor: pointer;
  }
</style>
