<template>
  <div v-loading="result.loading">
    <el-card>
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="initTableData" @create="create"
                         :create-tip="btnTips" :title="$t('commons.member')"/>
      </template>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column prop="roles" :label="$t('commons.role')" width="120">
          <template v-slot:default="scope">
            <el-tag v-for="(role, index) in scope.row.roles" :key="index" size="mini" effect="dark" type="success">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column>
          <template v-slot:default="scope">
            <el-button @click="edit(scope.row)" onkeydown="return false;" type="primary" icon="el-icon-edit" size="mini"
                       circle v-permission="['test_manager']"/>
            <el-button @click="del(scope.row)" onkeydown="return false;" type="danger" icon="el-icon-delete" size="mini"
                       circle v-permission="['test_manager']"/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <el-dialog :title="$t('member.create')" :visible.sync="createVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" ref="form" :rules="rules" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.member')" prop="userIds">
          <el-select v-model="form.userIds" multiple :placeholder="$t('member.please_choose_member')"
                     class="select-width">
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
        <el-form-item :label="$t('commons.role')" prop="roleIds">
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
      <template v-slot:footer>
        <span class="dialog-footer">
          <el-button type="primary" onkeydown="return false;"
                     @click="submitForm('form')" size="medium">{{$t('commons.save')}}</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog :title="$t('member.modify')" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
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
        <el-form-item :label="$t('commons.role')" prop="roleIds">
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
      <template v-slot:footer>
        <span class="dialog-footer">
          <el-button type="primary" onkeydown="return false;"
                     @click="updateWorkspaceMember('updateUserForm')" size="medium">{{$t('commons.save')}}</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import {TokenKey} from "../../../../common/js/constants";
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsTag from "../../common/components/MsTag";

  export default {
    name: "MsMember",
    components: {MsCreateBox, MsTablePagination, MsTableHeader, MsTag},
    data() {
      return {
        result: {},
        form: {},
        btnTips: this.$t('member.create'),
        createVisible: false,
        updateVisible: false,
        queryPath: "/user/ws/member/list",
        condition: {},
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
          name: this.condition.name,
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
      closeFunc() {
        this.form = {};
        this.initTableData();
      },
      del(row) {
        this.$confirm(this.$t('member.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.loading = true;
          this.$get('/user/ws/member/delete/' + this.currentUser().lastWorkspaceId + '/' + row.id).then(() => {
            this.initTableData();
            this.loading = false;
          });
          this.$success(this.$t('commons.delete_success'));
        }).catch(() => {
          this.loading = false;
          this.$info(this.$t('commons.delete_cancel'));
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
        this.result = this.$post("/workspace/member/update", param, () => {
          this.$success(this.$t('commons.modify_success'));
          this.updateVisible = false;
          this.initTableData();
        });
      },
      create() {
        this.form = {};
        let param = {
          name: this.condition.name,
          organizationId: this.currentUser().lastOrganizationId
        };
        let wsId = this.currentUser().lastWorkspaceId;
        if (typeof wsId == "undefined" || wsId == null || wsId == "") {
          this.$warning(this.$t('workspace.please_select_a_workspace_first'));
          return false;
        }
        this.$post('/user/org/member/list/all', param, response => {
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

  .el-table__row:hover .edit {
    opacity: 1;
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
