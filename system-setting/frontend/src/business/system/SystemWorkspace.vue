<template>
  <div>
    <el-card class="table-card" v-loading="loading">
      <template v-slot:header>
        <ms-table-header
          :create-permission="['SYSTEM_WORKSPACE:READ+CREATE']"
          :condition.sync="condition"
          @search="list" @create="create"
          :create-tip="$t('workspace.create')" :title="$t('commons.workspace')"/>
      </template>
      <!-- workspace table start -->
      <el-table border class="adjust-table ws-table" :data="workspaces" :height="screenHeight">
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column :label="$t('commons.member')">
          <template v-slot:default="scope">
            <el-link
              type="primary"
              class="member-size"
              :disabled="disabledEditWorkspaceMember"
              @click="cellClick(scope.row)">
              {{ scope.row.memberSize }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator
                :edit-permission="['SYSTEM_WORKSPACE:READ+EDIT']"
                :delete-permission="['SYSTEM_WORKSPACE:READ+DELETE']"
                :show-delete="workspaceId !== scope.row.id"
                @editClick="edit(scope.row)"
                @deleteClick="handleDelete(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <!-- workspace table end -->
      <ms-table-pagination
        :change="list"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="workspaceTotal"/>
    </el-card>

    <!-- add workspace dialog start -->
    <el-dialog :close-on-click-modal="false" :title="$t('workspace.create')" :visible.sync="dialogWsAddVisible"
               width="30%" @close="close" v-loading="workspaceAddLoading">
      <el-form :model="form" :rules="rules" ref="form" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off" class="form-input"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input type="textarea" v-model="form.description" class="form-input"></el-input>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogWsAddVisible = false"
          @confirm="createWorkspace('form')"/>
      </template>
    </el-dialog>
    <!-- add workspace dialog end -->

    <!-- update workspace dialog start -->
    <el-dialog :close-on-click-modal="false" :title="$t('workspace.update')" :visible.sync="dialogWsUpdateVisible"
               width="30%" @close="close" v-loading="workspaceUpdateLoading">
      <el-form :model="form" :rules="rules" ref="updateForm" label-position="right" label-width="100px" size="small">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off" class="form-input"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input type="textarea" v-model="form.description" class="form-input"></el-input>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogWsUpdateVisible = false"
          @confirm="_updateWorkspace('updateForm')"/>
      </template>
    </el-dialog>
    <!-- update workspace dialog end -->

    <!-- dialog of workspace member start -->
    <el-dialog :close-on-click-modal="false" :visible.sync="dialogWsMemberVisible" width="70%" :destroy-on-close="true"
               @close="closeWsMemberDialog" class="dialog-css" top="15vh">
      <template v-slot:title>
        <ms-table-header
          :condition.sync="dialogCondition" @create="addMember" @search="dialogSearch"
          :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      </template>
      <el-table :data="memberLineData" class="workspace-member-table" v-loading="workspaceMemberLoading">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column :label="$t('commons.group')" width="150">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.groups" type="success"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator
                :tip2="$t('commons.remove')"
                @editClick="editMember(scope.row)"
                @deleteClick="delMember(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination
        :change="dialogSearch"
        :current-page.sync="dialogCurrentPage"
        :page-size.sync="dialogPageSize"
        :total="dialogTotal"/>
    </el-dialog>
    <!-- dialog of workspace member end -->

    <add-member
      :group-type="GROUP_TYPE.WORKSPACE"
      :group-scope-id="groupScopeId"
      @submit="_addMember"
      v-loading="workspaceMemberAddLoading"
      ref="addMember"/>

    <!-- update workspace member dialog start -->
    <el-dialog
      :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="dialogWsMemberUpdateVisible"
      width="30%" :destroy-on-close="true" @close="handleClose" v-loading="workspaceMemberUpdateLoading">
      <el-form :model="memberForm" label-position="right" label-width="100px" size="small" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="memberForm.id" autocomplete="off" :disabled="true" class="form-input"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="memberForm.name" autocomplete="off" :disabled="true" class="form-input"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="memberForm.email" autocomplete="off" :disabled="true" class="form-input"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="memberForm.phone" autocomplete="off" :disabled="true" class="form-input"/>
        </el-form-item>
        <el-form-item :label="$t('commons.group')" prop="groupIds"
                      :rules="{required: true, message: $t('group.please_select_group'), trigger: 'change'}">
          <el-select filterable v-model="memberForm.groupIds" multiple :placeholder="$t('group.please_select_group')"
                     class="select-width">
            <el-option
              v-for="item in memberForm.workspaceGroups"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogWsMemberUpdateVisible = false"
          @confirm="_updateWorkspaceMember('updateUserForm')"/>
      </template>
    </el-dialog>
    <!-- update workspace member dialog end -->

    <ms-delete-confirm :title="$t('workspace.delete')" @delete="_handleDelete" ref="deleteConfirm"/>
  </div>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsRolesTag from "metersphere-frontend/src/components/MsRolesTag";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import {getCurrentWorkspaceId,} from "metersphere-frontend/src/utils/token";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import {GROUP_TYPE} from "metersphere-frontend/src/utils/constants";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import AddMember from "../common/AddMember";
import {
  addWorkspaceSpecial,
  delWorkspaceSpecial,
  getWorkspaceList,
  updateWorkspaceMember,
  updateWorkspaceSpecial,
} from "../../api/workspace";
import {getUserGroupList, getWorkspaceMemberGroup} from "../../api/user-group";
import {
  addWorkspaceMemberSpecial,
  delWorkspaceMemberSpecialById,
  getWorkspaceMemberListSpecial,
  getWorkspaceMemberSpecial
} from "../../api/user";
import {operationConfirm} from "metersphere-frontend/src/utils";

export default {
  name: "MsSystemWorkspace",
  components: {
    MsDeleteConfirm,
    MsTablePagination,
    MsTableHeader,
    MsRolesTag,
    MsTableOperator,
    MsDialogFooter,
    MsTableOperatorButton,
    AddMember
  },
  computed: {
    workspaceId() {
      return getCurrentWorkspaceId();
    },
    disabledEditWorkspaceMember() {
      return !hasPermission('SYSTEM_WORKSPACE:READ+EDIT');
    }
  },
  data() {
    return {
      loading: false,
      workspaceAddLoading: false,
      workspaceUpdateLoading: false,
      workspaceMemberLoading: false,
      workspaceMemberAddLoading: false,
      workspaceMemberUpdateLoading: false,
      dialogWsAddVisible: false,
      dialogWsUpdateVisible: false,
      dialogWsMemberVisible: false,
      dialogWsMemberAddVisible: false,
      dialogWsMemberUpdateVisible: false,
      condition: {},
      dialogCondition: {},
      workspaces: [],
      currentPage: 1,
      pageSize: 10,
      workspaceTotal: 0,
      dialogCurrentPage: 1,
      dialogPageSize: 5,
      dialogTotal: 0,
      memberLineData: [],
      memberForm: {},
      screenHeight: 'calc(100vh - 155px)',
      form: {},
      rules: {
        name: [
          {required: true, message: this.$t('workspace.input_name'), trigger: 'blur'},
          {min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur'},
          {
            required: true,
            pattern: /^(?!-)(?!.*?-$)[a-zA-Z0-9\u4e00-\u9fa5-]+$/,
            message: this.$t('workspace.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        description: [
          {max: 50, message: this.$t('commons.input_limit', [0, 50]), trigger: 'blur'}
        ],
      },
      wsMemberRule: {
        userIds: [
          {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
        ],
        groupIds: [
          {required: true, message: this.$t('group.please_select_group'), trigger: ['blur']}
        ]
      },
      currentWorkspaceRow: {},
      groupScopeId: "",
      GROUP_TYPE,
    };
  },
  activated() {
    this.list();
  },
  inject: [
    'reloadTopMenus',
  ],
  methods: {
    create() {
      this.dialogWsAddVisible = true;
      this.form = {};
      listenGoBack(this.close);
    },
    dataFilter(val) {
      if (!val) {
        this.memberForm.userList = this.memberForm.copyUserList;
        return;
      }
      this.memberForm.userList = this.memberForm.copyUserList.filter((item) => {
        if (!!~item.id.indexOf(val) || !!~item.id.toUpperCase().indexOf(val.toUpperCase())) {
          return true;
        }
      });
    },
    createWorkspace(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.workspaceAddLoading = addWorkspaceSpecial(this.form).then(() => {
            this.dialogWsAddVisible = false;
            this.list();
            this.$success(this.$t('commons.save_success'));
          })
        }
      });
    },
    _updateWorkspace(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.workspaceUpdateLoading = updateWorkspaceSpecial(this.form).then(() => {
            this.$success(this.$t('commons.modify_success'));
            this.dialogWsUpdateVisible = false;
            this.list();
          })
        }
      });
    },
    addMember() {
      this.$refs.addMember.open();
      listenGoBack(this.handleClose);
    },
    _addMember(params) {
      params.workspaceId = this.currentWorkspaceRow.id;
      this.workspaceMemberAddLoading = addWorkspaceMemberSpecial(params).then(() => {
        this.cellClick(this.currentWorkspaceRow);
        this.$refs.addMember.close();
      });
    },
    cellClick(row) {
      this.currentWorkspaceRow = row;
      this.dialogWsMemberVisible = true;
      this.groupScopeId = row.id;
      let param = {name: '', workspaceId: row.id};
      this._getWorkspaceMemberListSpecial(param);
      listenGoBack(this.closeWsMemberDialog);
    },
    dialogSearch() {
      let row = this.currentWorkspaceRow;
      this.dialogWsMemberVisible = true;
      let param = this.dialogCondition;
      this.$set(param, 'workspaceId', row.id);
      this._getWorkspaceMemberListSpecial(param);
    },
    _getWorkspaceMemberListSpecial(param) {
      this.workspaceMemberLoading = getWorkspaceMemberListSpecial(this.dialogCurrentPage, this.dialogPageSize, param)
        .then(res => {
          let {listObject, itemCount} = res.data;
          this.memberLineData = listObject;
          this.dialogTotal = itemCount;
          for (let i = 0; i < this.memberLineData.length; i++) {
            getWorkspaceMemberGroup(param.workspaceId, encodeURIComponent(this.memberLineData[i].id))
              .then(res => {
                let groups = res.data;
                this.$set(this.memberLineData[i], "groups", groups);
              });
          }
        });
    },
    edit(row) {
      this.dialogWsUpdateVisible = true;
      this.form = Object.assign({}, row);
      listenGoBack(this.close);
    },
    close() {
      this.dialogWsAddVisible = false;
      this.dialogWsUpdateVisible = false;
      removeGoBackListener(this.close);
    },
    handleClose() {
      this.memberForm = {};
      this.dialogWsMemberAddVisible = false;
      this.dialogWsMemberUpdateVisible = false;
      removeGoBackListener(this.handleClose);
    },
    closeWsMemberDialog() {
      this.memberLineData = [];
      this.list();
      removeGoBackListener(this.closeWsMemberDialog);
      this.dialogWsMemberVisible = false;
    },
    list() {
      this.loading = getWorkspaceList(this.currentPage, this.pageSize, this.condition)
        .then(res => {
          let {listObject, itemCount} = res.data;
          this.workspaces = listObject;
          this.workspaceTotal = itemCount;
          for (let i = 0; i < this.workspaces.length; i++) {
            getWorkspaceMemberSpecial({name: '', workspaceId: this.workspaces[i].id})
              .then(res => {
                let member = res.data;
                this.$set(this.workspaces[i], "memberSize", member.length);
              });
          }
        });
    },
    editMember(row) {
      this.dialogWsMemberUpdateVisible = true;
      this.memberForm = Object.assign({}, row);
      this.$set(this.memberForm, 'groupIds', this.memberForm.groups.map(r => r.id));
      this.workspaceMemberUpdateLoading = getUserGroupList({type: GROUP_TYPE.WORKSPACE, resourceId: this.groupScopeId})
        .then(res => {
          this.$set(this.memberForm, "workspaceGroups", res.data);
        })
      listenGoBack(this.handleClose);
    },
    handleDelete(workspace) {
      this.$refs.deleteConfirm.open(workspace);
    },
    _handleDelete(workspace) {
      operationConfirm(this.$t('workspace.delete_confirm'), () => {
        this.loading = delWorkspaceSpecial(workspace.id).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.list();
        })
      }, () => {
        this.$info(this.$t('commons.delete_cancelled'));
      })
    },
    delMember(row) {
      this.$confirm(this.$t('member.remove_member').toString(), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.workspaceMemberLoading = delWorkspaceMemberSpecialById(this.currentWorkspaceRow.id, encodeURIComponent(row.id))
          .then(() => {
            this.$success(this.$t('commons.remove_success'));
            this.cellClick(this.currentWorkspaceRow);
          });
      }).catch(() => {
        this.$info(this.$t('commons.remove_cancel'));
      });
    },
    _updateWorkspaceMember(formName) {
      this.$refs[formName].validate((valid) => {
        if (!valid) {
          return;
        }
        let param = {
          id: this.memberForm.id,
          name: this.memberForm.name,
          email: this.memberForm.email,
          phone: this.memberForm.phone,
          groupIds: this.memberForm.groupIds,
          workspaceId: this.currentWorkspaceRow.id
        };
        this.workspaceMemberUpdateLoading = updateWorkspaceMember(param).then(() => {
          this.$success(this.$t('commons.modify_success'));
          this.dialogWsMemberUpdateVisible = false;
          this.cellClick(this.currentWorkspaceRow);
        });
      });
    },
  }
};
</script>

<style scoped>

.el-table__row:hover .edit {
  opacity: 1;
}

.ws-table {
  width: 100%;
}

.member-size {
  cursor: pointer;
}

.select-width {
  width: 80%;
}

.form-input {
  width: 80%;
}

.dialog-css :deep(.el-dialog__body) {
  padding-top: 0;
}

.workspace-member-table {
  width: 100%;
  margin-top: 5px;
}
</style>

