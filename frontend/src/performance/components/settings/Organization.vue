<template>
  <div v-loading="result.loading">

    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">{{$t('commons.organization')}}
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
            <el-input type="text" size="small" :placeholder="$t('organization.search_by_name')" prefix-icon="el-icon-search"
                              maxlength="60" v-model="condition" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column :label="$t('commons.operating')">
          <template slot-scope="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="del(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>

      <div>
        <el-row>
          <el-col :span="22" :offset="1">
            <div class="table-page">
              <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page.sync="currentPage"
                :page-sizes="[5, 10, 20, 50, 100]"
                :page-size="pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total">
              </el-pagination>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-dialog :title="$t('organization.create')" :visible.sync="createVisible" width="30%" @closed="closeFunc" :destroy-on-close="true">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="createOrganization">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="createOrganization('createOrganization')" size="medium">{{$t('commons.save')}}</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="$t('organization.modify')" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="updateOrganizationForm">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateOrganization('updateOrganizationForm')" size="medium">{{$t('organization.modify')}}</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "./CreateBox";

  export default {
    name: "MsOrganization",
    components: {MsCreateBox},
    data() {
      return {
        queryPath: '/organization/list',
        deletePath: '/organization/delete/',
        createPath: '/organization/add',
        updatePath: '/organization/update',
        result: {},
        createVisible: false,
        updateVisible: false,
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        btnTips: this.$t('organization.create'),
        condition: "",
        tableData: [],
        form: {},
        rule: {
          name: [
            {required: true, message: this.$t('organization.input_name'), trigger: 'blur'},
            { min: 2, max: 10, message: this.$t('organization.input_name_2_50'), trigger: 'blur' },
            {
              required: true,
              pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
              message: this.$t('organization.special_characters_are_not_supported'),
              trigger: 'blur'
            }
          ],
          description: [
            { max: 50, message: this.$t('organization.input_name_2_50'), trigger: 'blur'}
          ]
        }
      }
    },
    created() {
      this.initTableData();
    },
    methods: {
      create() {
        this.createVisible = true;
      },
      edit(row) {
        // this.loading = true;
        this.updateVisible = true;
        this.form = row;
      },
      del(row) {
        this.$confirm(this.$t('organization.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get(this.deletePath + row.id,() => {
            this.$message({
              type: 'success',
              message: this.$t('commons.delete_success')
            });
            this.initTableData()
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancel')
          });
        });
      },
      createOrganization(createOrganizationForm) {
        this.$refs[createOrganizationForm].validate( valide => {
          if (valide) {
            this.result = this.$post(this.createPath, this.form,() => {
              this.$message({
                type: 'success',
                message: this.$t('commons.save_success')
              });
              this.initTableData();
              this.createVisible = false;
              });
          } else {
            return false;
          }
        })
      },
      updateOrganization(udpateOrganizationForm) {
        this.$refs[udpateOrganizationForm].validate(valide => {
          if (valide) {
            this.result = this.$post(this.updatePath, this.form,() => {
              this.$message({
                type: 'success',
                message: this.$t('commons.modify_success')
              });
              this.updateVisible = false;
              this.initTableData();
              });
          } else {
            return false;
          }
        })
      },
      initTableData() {
        this.result = this.$post(this.buildPagePath(this.queryPath),{},response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
        })
      },
      closeFunc() {
        this.form = {};
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      handleSizeChange(size) {
        this.pageSize = size;
        this.initTableData();
      },
      handleCurrentChange(current) {
        this.currentPage = current;
        this.initTableData();
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      }
    }

  }
</script>

<style scoped>
  .search {
    width: 240px;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }
</style>
