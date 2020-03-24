<template>
  <div v-loading="result.loading">
    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">{{$t('commons.member')}}
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
            <el-input type="text" size="small" :placeholder="$t('member.search_by_name')"
                      prefix-icon="el-icon-search"
                      maxlength="60" v-model="condition" @change="search" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column prop="roles" :label="$t('commons.role')" width="140">
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

    <el-dialog :title="$t('member.create')" :visible.sync="createVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" ref="form" :rules="rules" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.member')" prop="userIds">
          <el-select v-model="form.userIds" multiple :placeholder="$t('member.please_choose_member')" class="select-width">
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
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm('form')" size="medium">{{$t('commons.save')}}</el-button>
      </span>
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
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateOrgMember('updateUserForm')" size="medium">{{$t('commons.save')}}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import {TokenKey} from "../../../../common/constants";

  export default {
    name: "MsOrganizationMember",
    components: {MsCreateBox},
    created() {
      this.initTableData();
    },
    data() {
      return {
        result: {},
        btnTips: this.$t('member.create'),
        createVisible: false,
        updateVisible: false,
        form: {},
        queryPath: "/user/org/member/list",
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
    methods: {
      currentUser: () => {
        let user = localStorage.getItem(TokenKey);
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
            message: this.$t('commons.modify_success')
          });
          this.updateVisible = false;
          this.initTableData();
        });
      },
      del(row) {
        this.$confirm(this.$t('member.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get('/user/org/member/delete/' + this.currentUser().lastOrganizationId + '/' + row.id, () => {
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
      create() {
        let orgId = this.currentUser().lastOrganizationId;
        if (!orgId) {
          this.$message({
            type: 'warning',
            message: this.$t('organization.select_organization')
          })
          return false;
        }
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
        this.$refs[formName].validate((valid) => {
          let orgId = this.currentUser().lastOrganizationId;
          if (valid) {
            let param = {
              userIds: this.form.userIds,
              roleIds: this.form.roleIds,
              organizationId: orgId
            };
            this.result = this.$post("user/org/member/add", param,() => {
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
