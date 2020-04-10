<template>
  <div>
    <el-card v-loading="result.loading">
      <template v-slot:header>
        <div>
          <el-row type="flex" justify="space-between" align="middle">
          <span class="title">
            {{$t('commons.workspace')}}
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
            <span class="search">
            <el-input type="text" size="small"
                      :placeholder="$t('workspace.search_by_name')"
                      prefix-icon="el-icon-search" @change="search"
                      maxlength="60" v-model="condition" clearable/>
          </span>
          </el-row>
        </div>
      </template>
      <!-- workspace table -->
      <el-table :data="items" style="width: 100%">
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column prop="organizationName" :label="$t('workspace.organization_name')"/>
        <el-table-column :label="$t('commons.member')">
          <template v-slot:default="scope">
            <el-button type="text" class="member-size" @click="cellClick(scope.row)">{{scope.row.memberSize}}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column>
          <template v-slot:default="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="del(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="list" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <!-- add workspace dialog -->
    <el-dialog :title="$t('workspace.create')" :visible.sync="createVisible" width="30%">
      <el-form :model="form" :rules="rules" ref="form" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item :label="$t('workspace.organization_name')" prop="organizationId">
          <el-select v-model="form.organizationId" :placeholder="$t('organization.select_organization')"
                     class="select-width">
            <el-option
              v-for="item in form.orgList"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <span class="dialog-footer">
          <el-button type="primary" onkeydown="return false;" @click="submit('form')" size="medium">{{$t('commons.save')}}</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- update workspace dialog -->
    <el-dialog :title="$t('workspace.update')" :visible.sync="updateVisible" width="30%">
      <el-form :model="form" :rules="rules" ref="updateForm" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item :label="$t('workspace.organization_name')" prop="organizationId">
          <el-select v-model="form.organizationId" :placeholder="$t('organization.select_organization')"
                     class="select-width">
            <el-option
              v-for="item in form.orgList1"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="updateWorkspace('updateForm')" onkeydown="return false;"
                     size="medium">{{$t('commons.save')}}</el-button>
        </span>
      </template>

    </el-dialog>

    <!-- dialog of workspace member -->
    <el-dialog :visible.sync="memberVisible" width="70%" :destroy-on-close="true" @close="closeMemberFunc">
      <el-row type="flex" justify="space-between" align="middle">
        <span class="member-title">{{$t('commons.member')}}
          <ms-create-box :tips="addTips" :exec="addMember"/>
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
          <template v-slot:default="scope">
            <el-tag v-for="(role, index) in scope.row.roles" :key="index" size="mini" effect="dark" type="success">
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <el-button @click="editMember(scope.row)" onkeydown="return false;" type="primary" icon="el-icon-edit"
                       size="mini" circle/>
            <el-button @click="delMember(scope.row)" onkeydown="return false;" type="danger" icon="el-icon-delete"
                       size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="wsMemberList" :current-page.sync="currentMemberPage" :page-size.sync="pageMemberSize"
                           :total="memberTotal"/>
    </el-dialog>

    <!-- add workspace member dialog -->
    <el-dialog :title="$t('member.create')" :visible.sync="addMemberVisible" width="30%" :destroy-on-close="true"
               @close="closeFunc">
      <el-form :model="memberForm" ref="form" :rules="wsMemberRule" label-position="right" label-width="100px"
               size="small">
        <el-form-item :label="$t('commons.member')" prop="userIds">
          <el-select v-model="memberForm.userIds" multiple :placeholder="$t('member.please_choose_member')"
                     class="select-width">
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
          <el-select v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')"
                     class="select-width">
            <el-option
              v-for="item in memberForm.roles"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <span class="dialog-footer">
          <el-button type="primary" onkeydown="return false;" @click="submitForm('form')" size="medium">{{$t('commons.save')}}</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- update workspace member dialog -->
    <el-dialog :title="$t('member.modify')" :visible.sync="updateMemberVisible" width="30%" :destroy-on-close="true"
               @close="closeFunc">
      <el-form :model="memberForm" label-position="right" label-width="100px" size="small" ref="updateUserForm">
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
          <el-select v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')"
                     class="select-width">
            <el-option
              v-for="item in memberForm.allroles"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="updateOrgMember('updateUserForm')" onkeydown="return false;"
                     size="medium">{{$t('commons.save')}}</el-button>
        </span>
      </template>

    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import {Message} from "element-ui";
  import MsTablePagination from "../../common/pagination/TablePagination";

  export default {
    name: "MsSystemWorkspace",
    components: {MsCreateBox, MsTablePagination},
    mounted() {
      this.list();
    },
    methods: {
      create() {
        this.createVisible = true;
        this.form = {};
        this.$get("/organization/list", response => {
          this.$set(this.form, "orgList", response.data);
        })
      },
      submit(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let saveType = 'special/add';
            if (this.form.id) {
              saveType = 'update'
            }
            this.result = this.$post("/workspace/" + saveType, this.form, () => {
              this.createVisible = false;
              this.list();
              Message.success(this.$t('commons.save_success'));
            });
          } else {
            return false;
          }
        });
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
      cellClick(row) {
        // 保存当前点击的组织信息到currentRow
        this.currentWorkspaceRow = row;
        this.memberVisible = true;
        let param = {
          name: '',
          workspaceId: row.id
        };
        let path = "/user/special/ws/member/list";
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
      wsMemberList() {
        let row = this.currentWorkspaceRow;
        this.memberVisible = true;
        let param = {
          name: '',
          workspaceId: row.id
        };
        let path = "/user/special/ws/member/list";
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
      edit(row) {
        this.updateVisible = true;
        // copy user
        this.form = Object.assign({}, row);
        this.$get("/organization/list", response => {
          this.$set(this.form, "orgList1", response.data);
        })
      },
      updateWorkspace(updateForm) {
        this.$refs[updateForm].validate(valide => {
          if (valide) {
            this.result = this.$post("/workspace/special/update", this.form, () => {
              this.$message({
                type: 'success',
                message: this.$t('commons.modify_success')
              });
              this.updateVisible = false;
              this.list();
            });
          } else {
            return false;
          }
        })
      },
      del(row) {
        this.$confirm(this.$t('workspace.delete_confirm'), this.$t('commons.prompt'), {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.$get('/workspace/special/delete/' + row.id, () => {
            Message.success(this.$t('commons.delete_success'));
            this.list();
          });
        }).catch(() => {

        });
      },
      closeFunc() {
        this.form = {};
      },
      closeMemberFunc() {
        this.memberLineData = [];
        this.list();
      },
      search() {
        this.list();
      },
      list() {
        let url = '/workspace/list/all/' + this.currentPage + '/' + this.pageSize;
        this.result = this.$post(url, {name: this.condition}, response => {
          let data = response.data;
          this.items = data.listObject;
          for (let i = 0; i < this.items.length; i++) {
            let param = {
              name: '',
              workspaceId: this.items[i].id
            }
            let path = "user/special/ws/member/list/all";
            this.$post(path, param, res => {
              let member = res.data;
              this.$set(this.items[i], "memberSize", member.length);
            })
          }
          this.total = data.itemCount;
        });
      },
      buildPagePath(path) {
        return path + "/" + this.currentMemberPage + "/" + this.pageMemberSize;
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let param = {
              userIds: this.memberForm.userIds,
              roleIds: this.memberForm.roleIds,
              workspaceId: this.currentWorkspaceRow.id
            };
            this.result = this.$post("user/special/ws/member/add", param, () => {
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
          this.result = this.$get('/user/special/ws/member/delete/' + this.currentWorkspaceRow.id + '/' + row.id, () => {
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
        this.result = this.$post("/workspace/member/update", param, () => {
          this.$message({
            type: 'success',
            message: this.$t('commons.modify_success')
          });
          this.updateMemberVisible = false;
          this.cellClick(this.currentWorkspaceRow);
        });
      },
    },
    data() {
      return {
        result: {},
        createVisible: false,
        updateVisible: false,
        memberVisible: false,
        addMemberVisible: false,
        updateMemberVisible: false,
        btnTips: this.$t('workspace.add'),
        addTips: this.$t('member.create'),
        condition: "",
        items: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
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
          ],
          organizationId: [
            {required: true, message: this.$t('organization.select_organization'), trigger: ['blur']}
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
    text-decoration: underline;
    cursor: pointer;
  }

  .ws-member-name {
    float: left;
  }

  .ws-member-email {
    float: right;
    color: #8492a6;
    font-size: 13px;
  }

  .select-width {
    width: 100%;
  }

  .member-title {
    margin-bottom: 30px;
  }

</style>

