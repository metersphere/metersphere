<template>
  <div v-loading="result.loading">
    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle" v-permission="['test_manager']">
          <span class="title">{{$t('commons.member')}}
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
                    <el-input type="text" size="small" placeholder="根据用户名搜索" prefix-icon="el-icon-search"
                              maxlength="60" v-model="condition" @change="search" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column prop="roles" label="角色" width="120">
          <template slot-scope="scope">
            <el-tag v-for="(role, index) in scope.row.roles" :key="index" size="mini" effect="dark" type="success">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column>
          <template slot-scope="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle v-permission="['test_manager']"/>
            <el-button @click="del(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle v-permission="['test_manager']"/>
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
      <el-form :model="form" ref="form" :rules="rules" label-position="right" label-width="100px" size="small">
        <el-form-item label="成员" prop="userIds">
          <el-select v-model="form.userIds" multiple :placeholder="$t('member.please_choose_member')" class="select-width">
            <el-option
              v-for="item in form.userList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
              <span class="workspace-member-name">{{ item.name }}</span>
              <span class="workspace-member-email">{{ item.email }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple :placeholder="$t('role.please_choose_role')" class="select-width">
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
        <el-button type="primary" @click="submitForm('form')" size="medium">{{$t('commons.save')}}</el-button>
      </span>
    </el-dialog>

    <el-dialog title="修改成员" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" label-position="right" label-width="100px" size="small" ref="updateUserForm">
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
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple :placeholder="$t('role.please_choose_role')" class="select-width">
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
        <el-button type="primary" @click="updateWorkspaceMember('updateUserForm')" size="medium">{{$t('commons.save')}}</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import {TokenKey} from "../../../../common/constants";

  export default {
    name: "MsMember",
    components: {MsCreateBox},
    data() {
      return {
        result: {},
        form: {},
        btnTips: "添加工作空间成员",
        createVisible: false,
        updateVisible: false,
        queryPath: "/user/ws/member/list",
        condition: "",
        tableData: [],
        rules: {
          userIds: [
            {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
          ],
          roleIds: [
            {required: true, message: this.$t('role.please_choose_role'), trigger: ['blur']}
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
        let user = localStorage.getItem(TokenKey);
        return JSON.parse(user);
      },
      initTableData() {
        if (this.currentUser().lastWorkspaceId === null) {
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
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.loading = true;
          this.$get('/user/ws/member/delete/' + this.currentUser().lastWorkspaceId + '/' + row.id).then(() => {
            this.initTableData();
            this.loading = false;
          });
          this.$message({
            type: 'success',
            message: this.$t('commons.delete_success')
          });
        }).catch(() => {
          this.loading = false;
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancel')
          });
        });
      },
      edit(row) {
        this.updateVisible = true;
        this.form = row;
        let roleIds = this.form.roles.map(r => r.id);
        this.result = this.$get('/role/list/test', response => {
          this.$set(this.form, "allroles", response.data);
        })
        // 编辑使填充角色信息
        this.$set(this.form, 'roleIds', roleIds);
      },
      updateWorkspaceMember() {
        let param = {
          id: this.form.id,
          name: this.form.name,
          email: this.form.email,
          phone: this.form.phone,
          roleIds: this.form.roleIds,
          workspaceId: this.currentUser().lastWorkspaceId
        }
        this.result = this.$post("/workspace/member/update", param,() => {
          this.$message({
            type: 'success',
            message: this.$t('commons.modify_success')
          });
          this.updateVisible = false;
          this.initTableData();
        });
      },
      create() {
        this.form = {};
        let param = {
          name: this.condition,
          organizationId: this.currentUser().lastOrganizationId
        };
        let wsId = this.currentUser().lastWorkspaceId;
        if (typeof wsId == "undefined" || wsId == null || wsId == "") {
          this.$message({
            message:'请先选择工作空间！',
            type: 'warning'
          });
          return false;
        }
        this.$post('/user/org/member/list/all', param,response => {
          this.createVisible = true;
          this.$set(this.form, "userList", response.data);
        })
        this.result = this.$get('/role/list/test', response => {
          this.$set(this.form, "roles", response.data);
        })
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let param = {
              userIds: this.form.userIds,
              roleIds: this.form.roleIds,
              workspaceId: this.currentUser().lastWorkspaceId
            };
            this.result = this.$post("user/ws/member/add", param, () => {
              this.initTableData();
              this.createVisible = false;
            })
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

  .workspace-member-name {
    float: left;
  }

  .workspace-member-email {
    float: right;
    color: #8492a6;
    font-size: 13px;
  }

</style>
