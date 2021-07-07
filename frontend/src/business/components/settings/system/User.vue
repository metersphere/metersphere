<template>
  <div v-loading="result.loading">

    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :create-permission="['SYSTEM_USER:READ+CREATE']" :condition.sync="condition" @search="search"
                         @import="importUserDialogOpen" :show-import="true" :import-tip="$t('commons.import_user')"
                         @create="create" :create-tip="$t('user.create')" :title="$t('commons.user')"/>

      </template>

      <el-table border class="adjust-table ms-select-all-fixed" :data="tableData" style="width: 100%"
                @select-all="handleSelectAll"
                @select="handleSelect"
                :height="screenHeight"
                ref="userTable">
        <el-table-column type="selection" width="50"/>
        <ms-table-header-select-popover v-show="total>0"
                                        :page-size="pageSize>total?total:pageSize"
                                        :total="total"
                                        :select-data-counts="selectDataCounts"
                                        :table-data-count-in-page="tableData.length"
                                        @selectPageAll="isSelectDataAll(false)"
                                        @selectAll="isSelectDataAll(true)"/>
        <el-table-column v-if="!referenced" width="30" min-width="30" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn :is-show="scope.row.showMore" :buttons="buttons" :size="selectDataCounts"/>
          </template>
        </el-table-column>

        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.name')" width="200"/>

        <el-table-column :label="$t('commons.group')" width="150">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.roles"/>
          </template>
        </el-table-column>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="status" :label="$t('commons.status')" width="120">
          <template v-slot:default="scope">
            <el-switch :disabled="currentUserId === scope.row.id" v-model="scope.row.status"
                       inactive-color="#DCDFE6"
                       active-value="1"
                       inactive-value="0"
                       @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="source" :label="$t('user.source')"/>
        <el-table-column :label="$t('commons.operating')" min-width="120px">
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
      </el-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <!--Changing user password in system settings-->
    <el-dialog :close-on-click-modal="false" :title="$t('member.edit_password')" :visible.sync="editPasswordVisible"
               width="30%"
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
    <project-cascader :title="batchAddTitle" @confirm="cascaderConfirm" ref="cascaderDialog"></project-cascader>
    <group-cascader :title="$t('user.add_user_group_batch')" @confirm="cascaderConfirm" ref="groupCascaderDialog"></group-cascader>
    <edit-user ref="editUser" @refresh="search"/>
  </div>
</template>

<script>
import MsCreateBox from "../CreateBox";
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsDialogFooter from "../../common/components/MsDialogFooter";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import {getCurrentProjectID, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import MsRolesTag from "../../common/components/MsRolesTag";
import {getCurrentUser} from "@/common/js/utils";
import {PHONE_REGEX} from "@/common/js/regex";
import UserImport from "@/business/components/settings/system/components/UserImport";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import {
  _handleSelect,
  _handleSelectAll,
  getSelectDataCounts,
  setUnSelectIds,
  toggleAllSelection
} from "@/common/js/tableUtils";
import UserCascader from "@/business/components/settings/system/components/UserCascader";
import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
import EditUser from "@/business/components/settings/system/EditUser";
import ProjectCascader from "@/business/components/settings/system/components/ProjectCascader";
import GroupCascader from "@/business/components/settings/system/components/GroupCascader";

export default {
  name: "MsUser",
  components: {
    GroupCascader,
    EditUser,
    MsCreateBox,
    MsTablePagination,
    MsTableHeader,
    MsTableOperator,
    MsDialogFooter,
    MsTableOperatorButton,
    MsRolesTag,
    UserImport,
    MsTableHeaderSelectPopover,
    UserCascader,
    ProjectCascader,
    ShowMoreBtn
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
      referenced: false,
      queryPath: '/user/special/list',
      deletePath: '/user/special/delete/',
      createPath: '/user/special/add',
      updatePath: '/user/special/update',
      editPasswordPath: '/user/special/password',
      batchAddTitle: this.$t('user.add_project_batch'),
      result: {},
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
      screenHeight: 'calc(100vh - 195px)',
      checkPasswordForm: {},
      ruleForm: {},
      buttons: [
        {
          name: this.$t('user.add_project_batch'), handleClick: this.addToProjectBatch
        },
        {
          name: this.$t('user.add_user_group_batch'), handleClick: this.addUserGroupBatch
        }
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
      userGroup: []
    }
  },
  activated() {
    this.currentUserId = getCurrentUser().id;
    this.search();
  },
  methods: {
    create() {
      this.$refs.editUser.open("Add", this.$t('user.create'));
    },
    edit(row) {
      this.$refs.editUser.open("Edit", this.$t('user.modify'), row);
    },
    editPassword(row) {
      this.editPasswordVisible = true;
      this.ruleForm = Object.assign({}, row);
      listenGoBack(this.handleClose);
    },
    del(row) {
      this.$confirm(this.$t('user.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get(this.deletePath + encodeURIComponent(row.id), () => {
          this.$success(this.$t('commons.delete_success'));
          this.search();
        });
      }).catch(() => {
        this.$info(this.$t('commons.delete_cancel'));
      });
    },
    createUser(createUserForm) {
      this.$refs[createUserForm].validate(valid => {
        if (valid) {
          this.result = this.$post(this.createPath, this.form, () => {
            this.$success(this.$t('commons.save_success'));
            this.search();
            this.createVisible = false;
          });
        } else {
          return false;
        }
      })
    },
    updateUser(updateUserForm) {
      this.$refs[updateUserForm].validate(valid => {
        if (valid) {
          this.result = this.$post(this.updatePath, this.form, () => {
            this.$success(this.$t('commons.modify_success'));
            this.updateVisible = false;
            this.search();
          });
        } else {
          return false;
        }
      })
    },
    editUserPassword(editPasswordForm) {
      this.$refs[editPasswordForm].validate(valid => {
        if (valid) {
          this.result = this.$post(this.editPasswordPath, this.ruleForm, () => {
            this.$success(this.$t('commons.modify_success'));
            this.editPasswordVisible = false;
            this.search();
            this.reload();
          });
        } else {
          return false;
        }
      })
    },
    search() {
      this.selectRows = new Set();
      this.condition.selectAll = false;
      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        let url = "/user/special/user/group";
        for (let i = 0; i < this.tableData.length; i++) {
          if (this.tableData[i].id) {
            this.$get(url + '/' + encodeURIComponent(this.tableData[i].id), result => {
              let data = result.data;
              let groups = data.groups;
              this.$set(this.tableData[i], "roles", groups);
              this.$set(this.tableData[i], "isLocalUser", this.tableData[i].source === 'LOCAL');
            });
          }
        }

        this.$nextTick(function(){
          this.checkTableRowIsSelect();
        });

      })
    },

    checkTableRowIsSelect(){
      //如果默认全选的话，则选中应该选中的行
      if(this.condition.selectAll){
        let unSelectIds = this.condition.unSelectIds;
        this.tableData.forEach(row=>{
          if(unSelectIds.indexOf(row.id)<0){
            this.$refs.userTable.toggleRowSelection(row,true);

            //默认全选，需要把选中对行添加到selectRows中。不然会影响到勾选函数统计
            if (!this.selectRows.has(row)) {
              this.$set(row, "showMore", true);
              this.selectRows.add(row);
            }
          }else{
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
      this.$post('/user/special/update_status', row, () => {
        this.$success(this.$t('commons.modify_success'));
      })
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    getOrgList() {
      this.$get("/organization/list", response => {
        this.$set(this.form, "orgList", response.data);
      })
    },
    getWsList() {
      this.$get("/workspace/list", response => {
        this.$set(this.form, "wsList", response.data);
      })
    },
    importUserDialogOpen(){
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
    addToProjectBatch(){
      this.$refs.cascaderDialog.open();
    },
    addUserGroupBatch(){
      this.$refs.groupCascaderDialog.open();
    },
    cascaderConfirm(batchProcessTypeParam, selectValueArr){
      if(selectValueArr.length === 0){
        this.$success(this.$t('commons.modify_success'));
      }
      let params = {};
      params = this.buildBatchParam(params);
      params.batchType = batchProcessTypeParam;
      params.batchProcessValue = selectValueArr;
      this.$post('/user/special/batchProcessUserInfo', params, () => {
        this.$success(this.$t('commons.modify_success'));
        this.search();
        this.cascaderClose(batchProcessTypeParam);
      }, () => {
        this.cascaderRequestError(batchProcessTypeParam);
      });
    },
    cascaderRequestError(type) {
      type === "ADD_PROJECT" ? this.$refs.cascaderDialog.loading = false :
        this.$refs.groupCascaderDialog.loading = false;
    },
    cascaderClose(type) {
      type === "ADD_PROJECT" ? this.$refs.cascaderDialog.close() :
        this.$refs.groupCascaderDialog.close();
    },
    buildBatchParam(param) {
      param.ids = Array.from(this.selectRows).map(row => row.id);
      param.projectId = getCurrentProjectID();
      param.condition = this.condition;
      return param;
    },
  }
}
</script>

<style scoped>
/*/deep/ .el-table__fixed-right {*/
/*  height: 100% !important;*/
/*}*/

/*/deep/ .el-table__fixed {*/
/*  height: 110px !important;*/
/*}*/

/deep/ .ms-select-all-fixed th:first-child.el-table-column--selection {
  margin-top: 0px;
}
/deep/ .ms-select-all-fixed th:nth-child(2) .table-select-icon {
  top: -8px;
}
</style>
