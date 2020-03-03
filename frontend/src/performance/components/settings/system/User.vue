<template>
  <div v-loading="result.loading">

    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">{{$t('commons.member')}}
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
            <el-input type="text" size="small" :placeholder="$t('member.search_by_name')" prefix-icon="el-icon-search" maxlength="60" v-model="condition" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')" width="120"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column prop="status" :label="$t('commons.status')" width="100">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.status"
                       active-color="#13ce66"
                       inactive-color="#ff4949"
                       active-value="1"
                       inactive-value="0"
                       @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')" width="180">
          <template slot-scope="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
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

    <el-dialog :title="$t('user.create')" :visible.sync="createVisible" width="30%" @closed="closeFunc" :destroy-on-close="true">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="createUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="createUser('createUserForm')" size="medium">创建</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="$t('user.modify')" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateUser('updateUserForm')" size="medium">{{$t('commons.save')}}</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";

  export default {
    data() {
      return {
        queryPath: '/user/list',
        deletePath: '/user/delete/',
        createPath: '/user/add',
        updatePath: '/user/update',
        result: {},
        createVisible: false,
        updateVisible: false,
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        btnTips: this.$t('user.create'),
        condition: "",
        tableData: [],
        form: {},
        rule: {
          id: [
            { required: true, message: this.$t('user.input_id'), trigger: 'blur'},
            { min: 2, max: 10, message: this.$t('commons.input_limit', [2, 10]), trigger: 'blur' }
          ],
          name: [
            {required: true, message: this.$t('user.input_name'), trigger: 'blur'},
            { min: 2, max: 10, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur' },
            {
              required: true,
              pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
              message: this.$t('user.special_characters_are_not_supported'),
              trigger: 'blur'
            }
          ],
          phone: [
            {
              required: false,
              pattern: '^1(3|4|5|7|8)\\d{9}$',
              message: this.$t('user.mobile_number_format_is_incorrect'),
              trigger: 'blur'
            }
          ],
          email: [
            { required: true, message: this.$t('user.input_email'), trigger: 'blur' },
            {
              required: true,
              pattern: /^([A-Za-z0-9_\-.])+@([A-Za-z0-9]+\.)+[A-Za-z]{2,6}$/,
              message: this.$t('user.email_format_is_incorrect'),
              trigger: 'blur'
            }
          ]
        }
      }
    },
    name: "MsUser",
    components: {MsCreateBox},
    created() {
      this.initTableData();
    },
    methods: {
      create() {
        this.createVisible = true;
      },
      edit(row) {
        this.updateVisible = true;
        this.form = row;
      },
      del(row) {
        this.$confirm(this.$t('user.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get(this.deletePath + row.id, () => {
            this.$message({
              type: 'success',
              message: this.$t('commons.delete_success')
            });
            this.initTableData();
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancel')
          });
        });
      },
      createUser(createUserForm) {
        this.$refs[createUserForm].validate(valide => {
          if (valide) {
            this.result = this.$post(this.createPath, this.form, () => {
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
      updateUser(updateUserForm) {
        this.$refs[updateUserForm].validate(valide => {
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
      changeSwitch(row) {
        this.$post(this.updatePath, row,() =>{
          this.$message({
            type: 'success',
            message: this.$t('commons.modify_success')
          });
        })
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
