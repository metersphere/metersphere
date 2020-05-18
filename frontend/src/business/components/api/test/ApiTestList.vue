<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <ms-table-header :condition.sync="condition" @search="search" :title="$t('commons.test')"
                           @create="create" :createTip="$t('load_test.create')"/>
        </template>
        <el-table :data="tableData" class="table-content">
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
<!--          <el-table-column-->
<!--            prop="description"-->
<!--            :label="$t('commons.description')"-->
<!--            show-overflow-tooltip>-->
<!--          </el-table-column>-->
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

  export default {
    components: {MsMainContainer, MsContainer, MsTableHeader, MsTablePagination, MsTableOperator},
    data() {
      return {
        result: {},
        condition: {name: ""},
        projectId: null,
        tableData: [],
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        loading: false
      }
    },

    beforeRouteEnter(to, from, next) {
      next(self => {
        self.projectId = to.params.projectId;
        self.search();
      });
    },

    methods: {
      create() {
        this.$router.push('/api/test/create');
      },
      search() {
        let param = {
          name: this.condition.name,
        };

        if (this.projectId !== 'all') {
          param.projectId = this.projectId;
        }

        let url = "/api/list/" + this.currentPage + "/" + this.pageSize
        this.result = this.$post(url, param, response => {
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
      }
    }
  }
</script>

<style scoped>
  .table-content {
    width: 100%;
  }
</style>
