<template>
  <div v-loading="result.loading">
    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">成员
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
                    <el-input type="text" size="small" placeholder="根据用户名搜索" prefix-icon="el-icon-search"
                              maxlength="60" v-model="condition" @change="search" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" label="用户名"/>
        <el-table-column prop="email" label="邮箱"/>
        <el-table-column prop="phone" label="电话"/>
        <el-table-column prop="roles" label="角色" width="140">
          <template slot-scope="scope">
            <el-tag v-for="(role, index) in scope.row.roles" :key="index" size="mini" effect="dark" type="success">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column>
          <template slot-scope="scope">
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

    <el-dialog title="添加成员" :visible.sync="createVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" ref="form" :rules="rules" label-position="left" label-width="100px" size="small">
        <el-form-item label="成员" prop="userIds">
          <el-select v-model="form.userIds" multiple placeholder="请选择成员" class="select-width">
            <el-option
              v-for="item in form.userList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" class="select-width">
            <el-option
              v-for="item in form.roles"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm('form')" size="medium">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  import MsCreateBox from "./CreateBox";
  import Cookies from 'js-cookie';
  import {TokenKey} from "../../../common/constants";

  export default {
    name: "MsMember",
    components: {MsCreateBox},
    data() {
      return {
        result: {},
        form: {},
        btnTips: "添加成员",
        createVisible: false,
        queryPath: "/user/member/list",
        condition: "",
        tableData: [],
        rules: {
          userIds: [
            {required: true, message: '请选择成员', trigger: ['blur', 'change']}
          ],
          roleIds: [
            {required: true, message: '请选择角色', trigger: ['blur', 'change']}
          ]
        },
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
      }
    },
    created: function () {
      this.initTableData();
    },
    methods: {
      currentUser: () => {
        let user = Cookies.get(TokenKey);
        return JSON.parse(user);
      },
      initTableData() {
        if (this.currentUser.lastWorkspaceId === null) {
          return false;
        }
        this.loading = true;
        let param = {
          name: this.condition,
          workspaceId: this.currentUser().lastWorkspaceId
        };

        this.result = this.$post(this.buildPagePath(this.queryPath), param, response => {
          let data = response.data;
          this.tableData = data.listObject;
          let url = "/userrole/list/ws/" + this.currentUser().lastWorkspaceId;
          for (let i = 0; i < this.tableData.length; i++) {
            this.$get(url + "/" + this.tableData[i].id, response => {
              let roles = response.data;
              this.$set(this.tableData[i], "roles", roles);
            })
          }
          this.total = data.itemCount;
        })

      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      search() {
        this.initTableData();
      },
      handleSizeChange(size) {
        this.pageSize = size;
      },
      handleCurrentChange(current) {
        this.currentPage = current;
      },
      closeFunc() {
        this.form = {};
        this.initTableData();
      },
      del(row) {
        this.$confirm('移除该成员, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.loading = true;
          this.$get('/user/member/delete/' + this.currentUser().lastWorkspaceId + '/' + row.id).then(() => {
            this.initTableData();
            this.loading = false;
          });
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
        }).catch(() => {
          this.loading = false;
          this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });
      },
      create() {
        this.loading = true;
        this.$get('/user/list').then(response => {
          if (response.data.success) {
            this.createVisible = true;
            this.form = {userList: response.data.data};
          } else {
            this.$message.error(response.message);
          }
          this.loading = false;
        }).catch(() => {
          this.loading = false;
          this.$message({
            type: 'error',
            message: '获取用户列表失败'
          });
        });
        this.result = this.$get('/role/list/test', response => {
          this.$set(this.form, "roles", response.data);
        })
      },
      submitForm(formName) {
        this.loading = true;
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let param = {
              userIds: this.form.userIds,
              workspaceId: this.currentUser().lastWorkspaceId
            };
            this.$post("user/member/add", param).then(() => {
              this.initTableData();
              this.createVisible = false;
              this.loading = false;
            }).catch(() => {
              this.loading = false;
            })
          } else {
            return false;
          }
        });
      }
    }
  }
</script>

<style scoped>
  .search {
    width: 240px;
  }

  .el-table__row:hover .edit {
    opacity: 1;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }

  .select-width {
    width: 100%;
  }
</style>
