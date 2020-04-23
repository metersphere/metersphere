<template>
  <div class="container" v-loading="result.loading">
    <div class="main-content">
      <el-card>
        <template v-slot:header>
          <ms-table-header :condition.sync="condition" @search="search" :title="$t('commons.test')"
                           @create="create"/>
        </template>
        <el-table :data="tableData" class="test-content">
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="description"
            :label="$t('commons.description')"
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
            width="150"
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <el-button @click="handleEdit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
              <el-button @click="handleDelete(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </div>
  </div>
</template>

<script>
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";

  export default {
    components: {MsTableHeader, MsTablePagination},
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
      this.search();
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
                this.$message({
                  message: this.$t('commons.delete_success'),
                  type: 'success'
                });
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
  .test-content {
    width: 100%;
  }
</style>
