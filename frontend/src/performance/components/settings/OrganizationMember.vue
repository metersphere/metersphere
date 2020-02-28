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
            <el-tag v-for="(role, index) in scope.row.roles" :key="index" size="mini" effect="dark">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column>
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

    <el-dialog title="添加成员" :visible.sync="createVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" ref="form" :rules="rules" label-position="left" label-width="100px" size="small">
        <el-form-item label="成员" prop="userIds">
          <el-select v-model="form.userIds" multiple placeholder="请选择成员" class="select-width">
            <el-option
              v-for="item in form.userList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
              <span class="org-member-name">{{ item.name }}</span>
              <span class="org-member-email">{{ item.email }}</span>
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

    <el-dialog title="修改成员" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" label-position="left" label-width="100px" size="small" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item label="用户名" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" class="select-width">
            <el-option
              v-for="item in form.allroles"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateOrgMember('updateUserForm')" size="medium">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  import MsCreateBox from "./CreateBox";
  import Cookies from 'js-cookie';
  import {TokenKey} from "../../../common/constants";
  export default {
    name: "MsOrganizationMember",
    components: {MsCreateBox},
    created() {
      this.initTableData();
    },
    data() {
      return {
        result: {},
        btnTips: "添加组织成员",
        createVisible: false,
        updateVisible: false,
        form: {},
        queryPath: "/user/orgmember/list",
        condition: "",
        tableData: [],
        rules: {
          userIds: [
            {required: true, message: '请选择成员', trigger: ['blur']}
          ],
          roleIds: [
            {required: true, message: '请选择角色', trigger: ['blur']}
          ]
        },
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
      }
    },
    methods: {
      currentUser: () => {
        let user = Cookies.get(TokenKey);
        return JSON.parse(user);
      },
      initTableData() {
        let param = {
          name: this.condition,
          organizationId: this.currentUser().lastOrganizationId
        };
        this.result = this.$post(this.buildPagePath(this.queryPath), param, response => {
          let data = response.data;
          this.tableData = data.listObject;
          let url = "/userrole/list/org/" + this.currentUser().lastOrganizationId;
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
      closeFunc() {
        this.form = {};
        this.initTableData();
      },
      handleSizeChange(size) {
        this.pageSize = size;
        this.initTableData();
      },
      handleCurrentChange(current) {
        this.currentPage = current;
        this.initTableData();
      },
      edit(row) {
        this.updateVisible = true;
        this.form = row;
        let roleIds = this.form.roles.map(r => r.id);
        this.result = this.$get('/role/list/org', response => {
          this.$set(this.form, "allroles", response.data);
        })
        // 编辑使填充角色信息
        this.$set(this.form, 'roleIds', roleIds);
      },
      updateOrgMember() {
        let param = {
          id: this.form.id,
          name: this.form.name,
          email: this.form.email,
          phone: this.form.phone,
          roleIds: this.form.roleIds,
          organizationId: this.currentUser().lastOrganizationId
        }
        this.result = this.$post("/organization/member/update", param,() => {
          this.$message({
            type: 'success',
            message: '修改成功!'
          });
          this.updateVisible = false;
          this.initTableData();
        });
      },
      del(row) {
        this.$confirm('是否删除用户 ' + row.name + ' ?', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.result = this.$get('/user/orgmember/delete/' + this.currentUser().lastOrganizationId + '/' + row.id, () => {
            this.$message({
              type: 'success',
              message: '删除成功!'
            });
            this.initTableData();
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });
      },
      create() {
        this.form = {};
        this.result = this.$get('/user/besideorg/list/' + this.currentUser().lastOrganizationId, response => {
          this.createVisible = true;
          this.$set(this.form, "userList", response.data);
        });
        this.result = this.$get('/role/list/org', response => {
          this.$set(this.form, "roles", response.data);
        })
      },
      submitForm(formName) {
        this.loading = true;
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let param = {
              userIds: this.form.userIds,
              roleIds: this.form.roleIds,
              organizationId: this.currentUser().lastOrganizationId
            };
            this.result = this.$post("user/orgmember/add", param,() => {
              this.initTableData();
              this.createVisible = false;
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
  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }

  .org-member-name {
    float: left;
  }

  .org-member-email {
    float: right;
    color: #8492a6;
    font-size: 13px;
  }

  .select-width {
    width: 100%;
  }
</style>
