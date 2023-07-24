<template>
  <div>
    <el-card class="table-card" v-loading="loading">
      <template v-slot:header>
        <ms-table-header :create-permission="['SYSTEM_USER:READ+CREATE']" :condition.sync="condition" @search="search"
                         @import="importUserDialogOpen" :show-import="true"
                         :upload-permission="['SYSTEM_USER:READ+IMPORT']"
                         :import-tip="$t('commons.import_user')"
                         :tip="$t('commons.search_by_name_or_id')" @create="create"
                         :create-tip="$t('user.create')" :title="$t('commons.user')"/>
      </template>

      <ms-table border
                :condition="condition"
                :total="total"
                :data="tableData"
                style="width: 100%"
                :batch-operators="buttons"
                :screen-height="screenHeight"
                enableSelection
                @refresh="search"
                ref="userTable">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.name')" max-width="200"/>

        <el-table-column :label="$t('commons.group')" width="150">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.roles"/>
          </template>
        </el-table-column>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="status" :label="$t('commons.status')" width="120" sortable="custom">
          <template v-slot:default="scope">
            <el-switch :disabled="currentUserId === scope.row.id" v-model="scope.row.status"
                       inactive-color="#DCDFE6"
                       active-value="1"
                       inactive-value="0"
                       @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')" sortable="custom">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" :label="$t('user.source')"/>
        <el-table-column :label="$t('commons.operating')" fixed="right" min-width="100px">
          <template v-slot:default="scope">
            <div>

              <ms-table-operator :edit-permission="['SYSTEM_USER:READ+EDIT']"
                                 :delete-permission="['SYSTEM_USER:READ+DELETE']"
                                 @editClick="edit(scope.row)" @deleteClick="del(scope.row)">
                <template v-slot:behind>
                  <ms-table-operator-button :tip="$t('member.edit_password')" icon="el-icon-s-tools"
                                            v-permission="['SYSTEM_USER:READ+EDIT_PASSWORD']" type="success"
                                            @exec="editPassword(scope.row)" v-if="scope.row.isLocalUser"/>
                </template>
              </ms-table-operator>
            </div>
          </template>
        </el-table-column>
      </ms-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <!--Changing user password in system settings-->
    <el-dialog :close-on-click-modal="false" :title="$t('member.edit_password')" :visible.sync="editPasswordVisible"
               width="30%" v-loading="editPassLoading"
               :destroy-on-close="true" @close="handleClose" left>
      <el-form :model="ruleForm" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="editPasswordForm" class="demo-ruleForm">
        <el-form-item :label="$t('member.new_password')" prop="newpassword">
          <el-input type="password" v-model="ruleForm.newpassword" autocomplete="off" show-password></el-input>
        </el-form-item>
        <el-form-item :label="$t('member.repeat_password')" prop="confirmpassword">
          <el-input type="password" v-model="ruleForm.confirmpassword" autocomplete="off" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="ruleForm.id" autocomplete="off" :disabled="true" style="display:none"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <ms-dialog-footer
          @cancel="editPasswordVisible = false"
          @confirm="editUserPassword('editPasswordForm')"/>
      </span>
    </el-dialog>
    <user-import ref="userImportDialog" @refreshAll="search"></user-import>
    <batch-to-project-group-cascader :title="$t('user.add_project_batch')" @confirm="cascaderConfirm"
                                     :cascader-level="2" ref="cascaderDialog"/>
    <workspace-cascader :title="$t('user.add_workspace_batch')" @confirm="cascaderConfirm"
                        ref="workspaceCascader"></workspace-cascader>
    <group-cascader :title="$t('user.add_user_group_batch')" @confirm="cascaderConfirm"
                    ref="groupCascaderDialog"></group-cascader>
    <edit-user ref="editUser" @refresh="search"/>
  </div>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTable from "metersphere-frontend/src/components/table/MsTable";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import {getCurrentProjectID, getCurrentUser} from "metersphere-frontend/src/utils/token";
import MsRolesTag from "metersphere-frontend/src/components/MsRolesTag";
import {PHONE_REGEX} from "metersphere-frontend/src/utils/regex";
import UserImport from "../components/UserImport";
import MsTableHeaderSelectPopover from "metersphere-frontend/src/components/table/MsTableHeaderSelectPopover";
import {
  _handleSelect,
  _handleSelectAll,
  _sort,
  getSelectDataCounts,
  setUnSelectIds,
  toggleAllSelection
} from "metersphere-frontend/src/utils/tableUtils";
import UserCascader from "../components/UserCascader";
import ShowMoreBtn from "metersphere-frontend/src/components/table/ShowMoreBtn";
import EditUser from "./EditUser";
import GroupCascader from "../components/GroupCascader";
import {logout} from "@/api/user";
import WorkspaceCascader from "../components/WorkspaceCascader";
import BatchToProjectGroupCascader from "../components/BatchToProjectGroupCascader";
import {
  specialBatchProcessUser,
  specialCreateUser,
  specialDeleteUserById, specialGetUserGroup,
  specialListUsers,
  specialModifyPassword,
  specialModifyUser, specialModifyUserDisable
} from "../../../api/user";
import {operationConfirm} from "metersphere-frontend/src/utils";

export default {
  name: "MsUser",
  components: {
    BatchToProjectGroupCascader,
    WorkspaceCascader,
    GroupCascader,
    EditUser,
    MsTablePagination,
    MsTableHeader,
    MsTableOperator,
    MsDialogFooter,
    MsTableOperatorButton,
    MsRolesTag,
    UserImport,
    MsTableHeaderSelectPopover,
    UserCascader,
    ShowMoreBtn,
    MsTable
  },
  inject: [
    'reload'
  ],
  data() {
    const validateConfirmPwd = (rule, value, callback) => {
      if (value === '') {
        callback(new Error(this.$t('user.input_password')));
      } else if ((value !== this.ruleForm.newpassword)) {
        callback(new Error(this.$t('member.inconsistent_passwords')));
      } else {
        callback();
      }
    };
    return {
      screenHeight: 'calc(100vh - 155px)',
      referenced: false,
      loading: false,
      currentUserId: '',
      createVisible: false,
      updateVisible: false,
      selectDataCounts: 0,
      editPasswordVisible: false,
      btnAddRole: false,
      multipleSelection: [],
      userRole: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      condition: {},
      selectRows: new Set(),
      tableData: [],
      form: {
        roles: [{
          id: ''
        }]
      },
      changePasswordUser: '',
      checkPasswordForm: {},
      ruleForm: {},
      buttons: [
        {name: this.$t('user.add_project_batch'), handleClick: this.addToProjectBatch, permissions: ['SYSTEM_USER:READ+EDIT']},
        {name: this.$t('user.add_user_group_batch'), handleClick: this.addUserGroupBatch, permissions: ['SYSTEM_USER:READ+EDIT']},
        {name: this.$t('user.add_workspace_batch'), handleClick: this.addToWorkspaceBatch, permissions: ['SYSTEM_USER:READ+EDIT']}
      ],
      rule: {
        id: [
          {required: true, message: this.$t('user.input_id'), trigger: 'blur'},
          {min: 1, max: 50, message: this.$t('commons.input_limit', [1, 50]), trigger: 'blur'},
          {
            required: true,
            pattern: '^[^\u4e00-\u9fa5]+$',
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        name: [
          {required: true, message: this.$t('user.input_name'), trigger: 'blur'},
          {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'},
          {
            required: true,
            message: this.$t('user.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        phone: [
          {
            pattern: PHONE_REGEX,
            message: this.$t('user.mobile_number_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        email: [
          {required: true, message: this.$t('user.input_email'), trigger: 'blur'},
          {
            required: true,
            pattern: /^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/,
            message: this.$t('user.email_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        password: [
          {required: true, message: this.$t('user.input_password'), trigger: 'blur'},
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        newpassword: [
          {required: true, message: this.$t('user.input_password'), trigger: 'blur'},
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        confirmpassword: [
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t('member.password_format_is_incorrect'),
            trigger: 'blur'
          },
          {trigger: ['blur', 'change'], validator: validateConfirmPwd}
        ]
      },
      userGroup: [],
      editPassLoading: false,
    }
  },
  activated() {
    this.currentUserId = getCurrentUser().id;
    this.search();
    if (this.$refs.userTable) {
      this.$refs.userTable.doLayout();
    }
  },
  methods: {
    create() {
      this.$refs.editUser.open("Add", this.$t('user.create'));
    },
    edit(row) {
      this.$refs.editUser.open("Edit", this.$t('user.modify'), row);
    },
    editPassword(row) {
      this.changePasswordUser = row.id;
      this.editPasswordVisible = true;
      this.ruleForm = Object.assign({}, row);
      listenGoBack(this.handleClose);
    },
    del(row) {
      operationConfirm(this, this.$t('user.delete_confirm'), () => {
        this.loading = specialDeleteUserById(encodeURIComponent(row.id)).then(() => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        })
      }, () => {
        this.$info(this.$t('commons.delete_cancel'));
      })
    },
    createUser(createUserForm) {
      this.$refs[createUserForm].validate(valid => {
        if (!valid) {
          return false;
        }
        this.loading = specialCreateUser(this.form).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.search();
          this.createVisible = false;
        })
      })
    },
    updateUser(updateUserForm) {
      this.$refs[updateUserForm].validate(valid => {
        if (!valid) {
          return false;
        }
        this.loading = specialModifyUser(this.form).then(() => {
          this.$success(this.$t('commons.modify_success'));
          this.updateVisible = false;
          this.search();
        })
      })
    },
    editUserPassword(editPasswordForm) {
      this.$refs[editPasswordForm].validate(valid => {
        if (!valid) {
          return false;
        }
        this.editPassLoading = specialModifyPassword(this.ruleForm).then(() => {
          this.$success(this.$t('commons.modify_success'));
          if (this.changePasswordUser === getCurrentUser().id) {
            logout();
          } else {
            this.editPasswordVisible = false;
            this.search();
            this.reload();
          }
        })
      })
    },
    search() {
      this.selectRows = new Set();
      this.condition.selectAll = false;
      this.loading = specialListUsers(this.condition, this.currentPage, this.pageSize)
        .then((response) => {
          let data = response.data;
          let {itemCount, listObject} = data;
          this.total = itemCount;
          this.tableData = listObject;
          for (let i = 0; i < this.tableData.length; i++) {
            if (this.tableData[i].id) {
              specialGetUserGroup(encodeURIComponent(this.tableData[i].id))
                .then(result => {
                  let data = result.data;
                  let groups = data.groups;
                  this.$set(this.tableData[i], "roles", groups);
                  this.$set(this.tableData[i], "isLocalUser", this.tableData[i].source === 'LOCAL');
                })
            }
          }
          this.$nextTick(function () {
            this.checkTableRowIsSelect();
          });
        })
    },

    checkTableRowIsSelect() {
      //如果默认全选的话，则选中应该选中的行
      if (this.condition.selectAll) {
        let unSelectIds = this.condition.unSelectIds;
        this.tableData.forEach(row => {
          if (unSelectIds.indexOf(row.id) < 0) {
            this.$refs.userTable.toggleRowSelection(row, true);

            //默认全选，需要把选中对行添加到selectRows中。不然会影响到勾选函数统计
            if (!this.selectRows.has(row)) {
              this.$set(row, "showMore", true);
              this.selectRows.add(row);
            }
          } else {
            //不勾选的行，也要判断是否被加入了selectRow中。加入了的话就去除。
            if (this.selectRows.has(row)) {
              this.$set(row, "showMore", false);
              this.selectRows.delete(row);
            }
          }
        })
      }
    },

    handleClose() {
      this.form = {roles: [{id: ''}]};
      this.btnAddRole = false;
      removeGoBackListener(this.handleClose);
      this.editPasswordVisible = false;
      this.createVisible = false;
      this.updateVisible = false;
    },
    changeSwitch(row) {
      specialModifyUserDisable(row).then(() => {
        this.$success(this.$t('commons.modify_success'));
      });
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    importUserDialogOpen() {
      this.$refs.userImportDialog.open();
    },
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.tableData, this.selectRows, this.condition);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.$emit('selection', selection);
    },
    handleSelect(selection, row) {
      _handleSelect(this, selection, row, this.selectRows);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.$emit('selection', selection);
    },
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.condition.unSelectIds = [];
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      toggleAllSelection(this.$refs.userTable, this.tableData, this.selectRows);
    },
    addToProjectBatch() {
      this.$refs.cascaderDialog.open();
    },
    addToWorkspaceBatch() {
      this.$refs.workspaceCascader.open();
    },
    addUserGroupBatch() {
      this.$refs.groupCascaderDialog.open();
    },
    cascaderConfirm(batchProcessTypeParam, selectValueArr, selectedUserGroup) {
      if (selectValueArr.length === 0) {
        this.$success(this.$t('commons.modify_success'));
      }
      let params = {};
      params = this.buildBatchParam(params);
      params.batchType = batchProcessTypeParam;
      params.batchProcessValue = selectValueArr;
      params.selectUserGroupId = selectedUserGroup;
      specialBatchProcessUser(params).then(() => {
        this.$success(this.$t('commons.modify_success'));
        this.search();
        this.cascaderClose(batchProcessTypeParam);
      }).catch(() => {
        this.cascaderRequestError(batchProcessTypeParam);
      });
    },
    cascaderRequestError(type) {
      if (type === "ADD_PROJECT") {
        this.$refs.cascaderDialog.loading = false;
      } else if (type === "ADD_WORKSPACE") {
        this.$refs.workspaceCascader.loading = false;
      } else {
        this.$refs.groupCascaderDialog.loading = false;
      }
    },
    cascaderClose(type) {
      if (type === "ADD_PROJECT") {
        this.$refs.cascaderDialog.close();
      } else if (type === "ADD_WORKSPACE") {
        this.$refs.workspaceCascader.close();
      } else {
        this.$refs.groupCascaderDialog.close();
      }
    },
    buildBatchParam(param) {
      param.ids = this.$refs.userTable.selectIds;
      param.projectId = getCurrentProjectID();
      param.condition = this.condition;
      return param;
    },
    sort(column) {
      if (this.condition.orders) {
        let index = this.condition.orders.findIndex(o => o.name.replace('_', '').toLowerCase() === column.column.property.toLowerCase());
        if (index !== -1) {
          this.condition.orders.splice(index, 1);
        }
      }
      _sort(column, this.condition);
      this.clearSelectRows();
      this.search();
    },
    handleHeadAddClass({column}) {
      if (!column || !column.property || !column.sortable || !this.condition.orders) {
        return;
      }
      let order = this.condition.orders.find(o => o.name.replace('_', '').toLowerCase() === column.property.toLowerCase());
      if (!order) {
        return;
      }
      if (!order.type) {
        column.order = '';
      } else {
        column.order = order.type === 'desc' ? 'descending' : 'ascending';
      }
    },
    clearSelectRows() {
      this.selectRows.clear();
      this.selectIds = [];
      if (!this.condition.selectAll) {
        this.condition.selectAll = false;
        this.condition.unSelectIds = [];
      }
      this.selectDataCounts = 0;
      if (this.$refs.userTable) {
        this.$refs.userTable.clearSelection();
      }
    },
  }
}
</script>

<style scoped>
:deep(.ms-select-all-fixed th:first-child.el-table-column--selection) {
  margin-top: 0;
}

:deep(.ms-select-all-fixed th:nth-child(2) .table-select-icon) {
  top: -8px;
}
</style>
