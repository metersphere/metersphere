<template>
  <div>
    <el-card v-loading="result.loading">
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">{{$t('commons.workspace')}}
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
            <el-input type="text" size="small" :placeholder="$t('workspace.search_by_name')"
                      prefix-icon="el-icon-search"
                      maxlength="60" v-model="condition" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="items" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column :label="$t('commons.member')">
          <template slot-scope="scope">
            <el-button type="text" class="member-size" @click="cellClick(scope.row)">{{scope.row.memberSize}}</el-button>
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

    <el-dialog :title="$t('workspace.create')" :visible.sync="createVisible" width="30%">
      <el-form :model="form" :rules="rules" ref="form" label-position="left" label-width="100px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submit('form')" size="medium">{{$t('commons.save')}}</el-button>
      </span>
    </el-dialog>

    <!-- dialog of workspace member -->
    <el-dialog :visible.sync="memberVisible" width="70%" :destroy-on-close="true" @close="closeMemberFunc">
      <el-row type="flex" justify="space-between" align="middle">
        <span class="member-title">{{$t('commons.member')}}
          <ms-create-box :tips="addTips" :exec="addMember" v-permission="['admin','org_admin']"/>
        </span>
        <span class="search">
          <el-input type="text" size="small"
                    :placeholder="$t('organization.search_by_name')"
                    prefix-icon="el-icon-search"
                    maxlength="60" v-model="condition" clearable/>
        </span>
      </el-row>
      <!-- organization member table -->
      <el-table :data="memberLineData" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column :label="$t('commons.role')" width="120">
          <template slot-scope="scope">
            <el-tag v-for="(role, index) in scope.row.roles" :key="index" size="mini" effect="dark" type="success">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template slot-scope="scope">
            <el-button @click="editMember(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="delMember(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>
      <div>
        <el-row>
          <el-col :span="22" :offset="1">
            <div class="table-page">
              <el-pagination
                @size-change="handleMemberSizeChange"
                @current-change="handleMemberCurrentChange"
                :current-page.sync="currentMemberPage"
                :page-sizes="[5, 10, 20, 50, 100]"
                :page-size="pageMemberSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="memberTotal">
              </el-pagination>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>

    <!-- add workspace member dialog -->
    <el-dialog :title="$t('member.create')" :visible.sync="addMemberVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="memberForm" ref="form" :rules="wsMemberRule" label-position="left" label-width="100px" size="small">
        <el-form-item :label="$t('commons.member')" prop="userIds">
          <el-select v-model="memberForm.userIds" multiple :placeholder="$t('member.please_choose_member')" class="select-width">
            <el-option
              v-for="item in memberForm.userList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
              <span class="ws-member-name">{{ item.name }}</span>
              <span class="ws-member-email">{{ item.email }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('commons.role')" prop="roleIds">
          <el-select v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')" class="select-width">
            <el-option
              v-for="item in memberForm.roles"
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

    <!-- update workspace member dialog -->
    <el-dialog :title="$t('member.modify')" :visible.sync="updateMemberVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="memberForm" label-position="left" label-width="100px" size="small" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="memberForm.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="memberForm.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="memberForm.email" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="memberForm.phone" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.role')" prop="roleIds">
          <el-select v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')" class="select-width">
            <el-option
              v-for="item in memberForm.allroles"
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
  import {Message} from "element-ui";
  import {TokenKey} from "../../../../common/constants";

  export default {
    name: "MsOrganizationWorkspace",
    components: {MsCreateBox},
    mounted() {
      this.list();
    },
    computed: {
      currentUser: () => {
        let user = localStorage.getItem(TokenKey);
        return JSON.parse(user);
      }
    },
    methods: {
      create() {
        this.createVisible = true;
        this.form = {};
      },
      submit(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let saveType = 'add';
            if (this.form.id) {
              saveType = 'update'
            }
            this.$post("/workspace/" + saveType, this.form, () => {
              this.createVisible = false;
              this.list();
              Message.success(this.$t('commons.save_success'));
            });
          } else {
            return false;
          }
        });
      },
      edit(row) {
        this.createVisible = true;
        this.form = row;

        // let self = this;
        // let getUser1 = this.$get("/test/user");
        // let getUser2 = this.$get("/test/sleep");
        // this.$all([getUser1, getUser2], function (r1, r2) {
        //   window.console.log(r1.data.data, r2.data.data);
        //   self.loading = false;
        // });
      },
      del(row) {
        this.$confirm(this.$t('workspace.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.$get('/workspace/delete/' + row.id, () => {
            Message.success(this.$t('commons.delete_success'));
            this.list();
          });
        }).catch(() => {

        });
      },
      list() {
        let url = '/workspace/list/' + this.currentPage + '/' + this.pageSize;
        let lastOrganizationId = this.currentUser.lastOrganizationId;
        let userRole = this.currentUser.userRoles.filter(r => r.sourceId === lastOrganizationId);
        if (userRole.length > 0) {
          if (userRole[0].roleId === "org_admin") {
            this.result = this.$post(url, {name: this.condition}, response => {
              let data = response.data;
              this.items = data.listObject;
              for (let i = 0; i < this.items.length; i++) {
                let param = {
                  name: '',
                  workspaceId: this.items[i].id
                }
                let path = "user/ws/member/list/all";
                this.$post(path, param, res => {
                  let member = res.data;
                  this.$set(this.items[i], "memberSize", member.length);
                })
              }
              this.total = data.itemCount;
            });
          } else {
            this.items = [];
            this.total = 0;
          }
        }

      },
      handleSizeChange(size) {
        this.pageSize = size;
        this.list();
      },
      handleCurrentChange(current) {
        this.currentPage = current;
        this.list();
      },
      addMember() {
        this.addMemberVisible = true;
        this.memberForm = {};
        this.result = this.$get('/user/list/', response => {
          this.$set(this.memberForm, "userList", response.data);
        });
        this.result = this.$get('/role/list/test', response => {
          this.$set(this.memberForm, "roles", response.data);
        })
      },
      cellClick(row){
        // 保存当前点击的组织信息到currentRow
        this.currentWorkspaceRow = row;
        this.memberVisible = true;
        let param = {
          name: '',
          workspaceId: row.id
        };
        let path = "/user/ws/member/list";
        this.result = this.$post(this.buildPagePath(path), param, res => {
          let data = res.data;
          this.memberLineData = data.listObject;
          let url = "/userrole/list/ws/" + row.id;
          // 填充角色信息
          for (let i = 0; i < this.memberLineData.length; i++) {
            this.$get(url + "/" + this.memberLineData[i].id, response => {
              let roles = response.data;
              this.$set(this.memberLineData[i], "roles", roles);
            })
          }
          this.memberTotal = data.itemCount;
        });
      },
      closeFunc() {
        this.form = {};
      },
      closeMemberFunc() {
        this.memberLineData = [];
        this.list();
      },
      handleMemberSizeChange(size) {
        this.pageMemberSize = size;
        this.cellClick(this.currentWorkspaceRow);
      },
      handleMemberCurrentChange(current) {
        this.currentMemberPage = current;
        this.cellClick(this.currentWorkspaceRow);
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let param = {
              userIds: this.memberForm.userIds,
              roleIds: this.memberForm.roleIds,
              workspaceId: this.currentWorkspaceRow.id
            };
            this.result = this.$post("user/ws/member/add", param,() => {
              this.cellClick(this.currentWorkspaceRow);
              this.addMemberVisible = false;
            })
          } else {
            return false;
          }
        });
      },
      editMember(row) {
        this.updateMemberVisible = true;
        this.memberForm = row;
        let roleIds = this.memberForm.roles.map(r => r.id);
        this.result = this.$get('/role/list/test', response => {
          this.$set(this.memberForm, "allroles", response.data);
        })
        // 编辑时填充角色信息
        this.$set(this.memberForm, 'roleIds', roleIds);
      },
      delMember(row) {
        this.$confirm(this.$t('member.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get('/user/ws/member/delete/' + this.currentWorkspaceRow.id + '/' + row.id, () => {
            this.$message({
              type: 'success',
              message: this.$t('commons.delete_success')
            });
            this.cellClick(this.currentWorkspaceRow);
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancel')
          });
        });
      },
      updateOrgMember() {
        let param = {
          id: this.memberForm.id,
          name: this.memberForm.name,
          email: this.memberForm.email,
          phone: this.memberForm.phone,
          roleIds: this.memberForm.roleIds,
          workspaceId: this.currentWorkspaceRow.id
        }
        this.result = this.$post("/workspace/member/update", param,() => {
          this.$message({
            type: 'success',
            message: this.$t('commons.modify_success')
          });
          this.updateMemberVisible = false;
          this.cellClick(this.currentWorkspaceRow);
        });
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
    },
    data() {
      return {
        result: {},
        loading: false,
        createVisible: false,
        btnTips: this.$t('workspace.add'),
        addTips: this.$t('member.create'),
        condition: "",
        items: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        updateVisible: false,
        memberVisible: false,
        addMemberVisible: false,
        updateMemberVisible: false,
        currentMemberPage: 1,
        pageMemberSize: 5,
        memberTotal: 0,
        memberLineData: [],
        memberForm: {},
        form: {
          // name: "",
          // description: ""
        },
        rules: {
          name: [
            {required: true, message: this.$t('workspace.input_name'), trigger: 'blur'},
            {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'}
          ]
        },
        wsMemberRule: {
          userIds: [
            {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
          ],
          roleIds: [
            {required: true, message: this.$t('role.please_choose_role'), trigger: ['blur']}
          ]
        },
        currentWorkspaceRow: {},
      }
    }
  }
</script>

<style scoped>
  .search {
    width: 240px;
  }

  .edit {
    opacity: 0;
  }

  .el-table__row:hover .edit {
    opacity: 1;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }

  .member-size {
    text-decoration:underline;
    cursor:pointer;
  }

  .member-title {
    margin-bottom: 30px;
  }

  .select-width {
    width: 100%;
  }

  .ws-member-name {
    float: left;
  }

  .ws-member-email {
    float: right;
    color: #8492a6;
    font-size: 13px;
  }

</style>

