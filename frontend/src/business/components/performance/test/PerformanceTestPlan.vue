<template>
  <div class="container" v-loading="result.loading">
    <div class="main-content">
      <el-card class="table-card">
        <template v-slot:header>
          <div>
            <el-row type="flex" justify="space-between" align="middle">
              <span class="title">{{$t('commons.test')}}</span>
              <span class="search">
            <el-input type="text" size="small" :placeholder="$t('load_test.search_by_name')"
                      prefix-icon="el-icon-search"
                      maxlength="60"
                      v-model="condition" @change="search" clearable/>
          </span>
            </el-row>
          </div>
        </template>
        <el-table :data="tableData" class="test-content">
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="projectName"
            :label="$t('load_test.project_name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            width="250"
            :label="$t('commons.create_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="250"
            :label="$t('commons.update_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            :label="$t('commons.status')">
            <template v-slot:default="{row}">
              <el-tag size="mini" type="info" v-if="row.status === 'Saved'">
                {{ row.status }}
              </el-tag>
              <el-tag size="mini" type="primary" v-else-if="row.status === 'Starting'">
                {{ row.status }}
              </el-tag>
              <el-tag size="mini" type="success" v-else-if="row.status === 'Running'">
                {{ row.status }}
              </el-tag>
              <el-tag size="mini" type="warning" v-else-if="row.status === 'Reporting'">
                {{ row.status }}
              </el-tag>
              <el-tag size="mini" type="info" v-else-if="row.status === 'Completed'">
                {{ row.status }}
              </el-tag>
              <el-tooltip placement="top" v-else-if="row.status === 'Error'" effect="light">
                <template v-slot:content>
                  <div>{{row.description}}</div>
                </template>
                <el-tag size="mini" type="danger">
                  {{ row.status }}
                </el-tag>
              </el-tooltip>
              <span v-else>
                {{ row.status }}
              </span>
            </template>
          </el-table-column>
          <el-table-column
            width="150"
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <ms-table-operator @editClick="handleEdit(scope.row)" @deleteClick="handleDelete(scope.row)"/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </div>
  </div>
</template>

<script>
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableOperator from "../../common/components/MsTableOperator";

  export default {
    components: {MsTablePagination, MsTableOperator},
    data() {
      return {
        result: {},
        queryPath: "/performance/list",
        deletePath: "/performance/delete",
        condition: "",
        projectId: null,
        tableData: [],
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        loading: false,
        testId: null,
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
        let param = {
          name: this.condition,
        };

        if (this.projectId !== 'all') {
          param.projectId = this.projectId;
        }

        this.result = this.$post(this.buildPagePath(this.queryPath), param, response => {
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
