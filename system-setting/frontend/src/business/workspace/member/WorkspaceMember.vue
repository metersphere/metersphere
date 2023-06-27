<template>
  <div v-loading="loading">
    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :create-permission="['WORKSPACE_USER:READ+CREATE']" :condition.sync="condition"
                         @search="initTableData" @create="create"
                         :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      </template>
      <el-table border class="adjust-table" :data="tableData" style="width: 100%"
                :height="screenHeight"
                @select-all="handleSelectAll"
                @select="handleSelect"
                ref="userTable">
        <el-table-column v-if="hasPermission('WORKSPACE_PROJECT_MANAGER:READ+ADD_USER')" type="selection" width="50"/>
        <el-table-column width="30" min-width="30" :resizable="false" align="center">
          <template v-slot:default="scope">
            <show-more-btn v-permission="['WORKSPACE_PROJECT_MANAGER:READ+ADD_USER']"
                           :is-show="scope.row.showMore" :buttons="buttons" :size="selectDataCounts"/>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column prop="groups" :label="$t('commons.group')" width="160">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.groups" type="success"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator :edit-permission="['WORKSPACE_USER:READ+EDIT']"
                                 :delete-permission="['WORKSPACE_USER:READ+DELETE']"
                                 :tip2="$t('commons.remove')" @editClick="edit(scope.row)"
                                 @deleteClick="del(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <add-member :group-type="'WORKSPACE'" :group-scope-id="workspaceId" ref="addMember" @submit="submitForm"/>

    <el-dialog :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="updateVisible" width="40%"
               :destroy-on-close="true"
               @close="handleClose">
      <el-form :model="form" label-position="right" label-width="100px" size="small" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.group')" prop="groupIds"
                      :rules="{required: true, message: $t('group.please_select_group'), trigger: 'change'}">
          <el-select v-model="form.groupIds" multiple :placeholder="$t('group.please_select_group')"
                     class="select-width">
            <el-option
              v-for="item in form.allgroups"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="updateVisible = false"
          @confirm="updateWorkspaceMember('updateUserForm')"/>
      </template>
    </el-dialog>
    <user-cascader :lable="batchAddLable" :title="batchAddTitle" @confirm="cascaderConfirm"
                   ref="cascaderDialog"></user-cascader>
    <batch-to-project-group-cascader :title="$t('user.add_project_batch')" @confirm="cascaderConfirm"
                                     :cascader-level="1" ref="cascaderDialog"/>
  </div>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsRolesTag from "metersphere-frontend/src/components/MsRolesTag";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import {getCurrentProjectID, getCurrentUser, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import MsTableHeaderSelectPopover from "metersphere-frontend/src/components/table/MsTableHeaderSelectPopover";
import {_handleSelect, _handleSelectAll, getSelectDataCounts, setUnSelectIds, toggleAllSelection} from "metersphere-frontend/src/utils/tableUtils";
import UserCascader from "../../system/components/UserCascader";
import ShowMoreBtn from "metersphere-frontend/src/components/table/ShowMoreBtn";
import {GROUP_WORKSPACE} from "metersphere-frontend/src/utils/constants";
import AddMember from "../../common/AddMember";
import BatchToProjectGroupCascader from "../../system/components/BatchToProjectGroupCascader";
import GroupCascader from "../../system/components/GroupCascader";
import {addWorkspaceMember, specialBatchProcessUser, delWorkspaceMemberById, getWorkspaceMemberPages} from "../../../api/user";
import {getUserGroupList, getWorkspaceMemberGroup} from "../../../api/user-group";
import {updateWorkspaceMember as _updateWorkspaceMember} from "@/api/workspace";

export default {
  name: "MsMember",
  components: {
    BatchToProjectGroupCascader, GroupCascader,
    AddMember, MsTablePagination, MsTableHeader, MsRolesTag, MsTableOperator, MsDialogFooter,
    MsTableHeaderSelectPopover, UserCascader, ShowMoreBtn
  },
  data() {
    return {
      loading: false,
      form: {},
      createVisible: false,
      updateVisible: false,
      queryPath: "/user/ws/member/list",
      condition: {},
      tableData: [],
      userList: [],
      rules: {
        userIds: [
          {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
        ],
        groupIds: [
          {required: true, message: this.$t('group.please_select_group'), trigger: ['blur']}
        ]
      },
      screenHeight: 'calc(100vh - 155px)',
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      selectDataCounts: 0,
      batchAddLable: this.$t('project.please_choose_workspace'),
      batchAddTitle: this.$t('project.batch_choose_workspace'),
      selectRows: new Set(),
      referenced: false,
      batchAddUserRoleOptions: [],
      buttons: [
        {
          name: this.$t('user.add_project_batch'), handleClick: this.addToProjectBatch
        },
      ],
    };
  },
  computed: {
    workspaceId() {
      return getCurrentWorkspaceId();
    }
  },
  activated: function () {
    this.initTableData();
  },
  methods: {
    hasPermission,
    currentUser: () => {
      return getCurrentUser();
    },
    initTableData() {
      this.selectRows = new Set();
      if (getCurrentWorkspaceId() === null) {
        return false;
      }
      let param = {
        name: this.condition.name,
        workspaceId: getCurrentWorkspaceId()
      };
      this.loading = getWorkspaceMemberPages(this.currentPage, this.pageSize, param).then(res => {
        let {listObject, itemCount} = res.data;
        this.tableData = listObject;
        this.total = itemCount;
        for (let i = 0; i < this.tableData.length; i++) {
          getWorkspaceMemberGroup(getCurrentWorkspaceId(), encodeURIComponent(this.tableData[i].id))
            .then(res => {
              this.$set(this.tableData[i], "groups", res.data);
            });
        }
        this.$nextTick(function () {
          this.checkTableRowIsSelect();
        });
      });
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
        });
      }
    },
    handleClose() {
      this.form = {};
      this.createVisible = false;
      this.updateVisible = false;
      removeGoBackListener(this.handleClose);
    },
    del(row) {
      this.$confirm(this.$t('member.remove_member').toString(), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.loading = delWorkspaceMemberById(getCurrentWorkspaceId(), encodeURIComponent(row.id)).then(() => {
          this.$success(this.$t('commons.remove_success'));
          this.initTableData();
        });
      }).catch(() => {
        this.$info(this.$t('commons.remove_cancel'));
      });
    },
    edit(row) {
      this.updateVisible = true;
      this.form = Object.assign({}, row);
      let groupIds = this.form.groups.map(r => r.id);
      this.loading = getUserGroupList({
        type: GROUP_WORKSPACE,
        resourceId: getCurrentWorkspaceId()}).then(res => {
        this.$set(this.form, "allgroups", res.data);
      })
      // 编辑使填充角色信息
      this.$set(this.form, 'groupIds', groupIds);
      listenGoBack(this.handleClose);
    },
    updateWorkspaceMember(formName) {
      let param = {
        id: this.form.id,
        name: this.form.name,
        email: this.form.email,
        phone: this.form.phone,
        groupIds: this.form.groupIds,
        workspaceId: getCurrentWorkspaceId()
      };
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.loading = _updateWorkspaceMember(param).then(() => {
            this.$success(this.$t('commons.modify_success'));
            this.updateVisible = false;
            this.initTableData();
          });
        }
      });
    },
    addToProjectBatch() {
      this.$refs.cascaderDialog.open();
    },
    create() {
      let wsId = getCurrentWorkspaceId();
      if (typeof wsId == "undefined" || wsId == null || wsId == "") {
        this.$warning(this.$t('workspace.please_select_a_workspace_first'));
        return false;
      }
      this.$refs.addMember.open();
      listenGoBack(this.handleClose);
    },
    submitForm(param) {
      param['workspaceId'] = getCurrentWorkspaceId();
      this.loading = addWorkspaceMember(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.initTableData();
        this.selectRows.clear();
        this.$refs.addMember.close();
      });
    },
    querySearch(queryString, cb) {
      var userList = this.userList;
      var results = queryString ? userList.filter(this.createFilter(queryString)) : userList;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    createFilter(queryString) {
      return (user) => {
        return (user.email.indexOf(queryString.toLowerCase()) === 0 || user.id.indexOf(queryString.toLowerCase()) === 0);
      };
    },
    handleSelectAll(selection) {
      _handleSelectAll(this, selection, this.tableData, this.selectRows, this.condition);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.$emit('selection', selection);
    },
    handleSelect(selection, row) {
      let selectRowMap = new Map();
      for (let selectRow of this.selectRows) {
        selectRowMap.set(selectRow.id, selectRow);
      }
      _handleSelect(this, selection, row, selectRowMap);
      let selectRow = Array.from(selectRowMap.values());
      this.selectRows = new Set(selectRow);
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      this.$emit('selection', selection);
      this.$set(this.form, "userId", selection.id);
    },
    isSelectDataAll(data) {
      this.condition.selectAll = data;
      setUnSelectIds(this.tableData, this.condition, this.selectRows);
      this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
      toggleAllSelection(this.$refs.userTable, this.tableData, this.selectRows);
    },
    cascaderConfirm(batchProcessTypeParam, selectValueArr, selectUserGroupId) {
      if (selectValueArr.length === 0) {
        this.$success(this.$t('commons.modify_success'));
      }
      let params = {};
      params = this.buildBatchParam(params);
      params.workspaceId = getCurrentWorkspaceId();
      params.batchType = batchProcessTypeParam;
      params.batchProcessValue = selectValueArr;
      params.selectUserGroupId = selectUserGroupId;
      specialBatchProcessUser(params).then(() => {
        this.$success(this.$t('commons.modify_success'));
        this.initTableData();
        this.$refs.cascaderDialog.close();
      }).catch(() => {
        if (this.$refs.cascaderDialog) {
          this.$refs.cascaderDialog.loading = false;
        }
      });
    },
    buildBatchParam(param) {
      param.ids = Array.from(this.selectRows).map(row => row.id);
      param.projectId = getCurrentProjectID();
      param.condition = this.condition;
      return param;
    },
  }
};
</script>

<style scoped>

.el-table__row:hover .edit {
  opacity: 1;
}

.select-width {
  width: 80%;
}

:deep(.ms-select-all-fixed th:nth-child(2) .el-icon-arrow-down) {
  top: -5px;
}

.el-input {
  width: 80%;
}
</style>
