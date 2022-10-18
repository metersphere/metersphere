<template>
  <div>
    <el-dialog :close-on-click-modal="false" :visible.sync="visible" width="65%" top="15vh"
               :destroy-on-close="true" @close="close" v-loading="loading" z-index="1000"
               class="group-member">
      <template v-slot:title>
        <ms-table-header :condition.sync="condition" @create="addMemberBtn" @search="search"
                         :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      </template>
      <el-table :border="true" class="adjust-table" :data="memberData" style="width: 100%;margin-top:5px;">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')" show-overflow-tooltip/>
        <el-table-column prop="email" :label="$t('commons.email')" show-overflow-tooltip/>
        <el-table-column prop="phone" :label="$t('commons.phone')" show-overflow-tooltip>
          <template v-slot="scope">
            {{ scope.row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="typeLabel" v-if="showTypeLabel">
          <template v-slot:default="scope">
            <el-popover
              placement="top"
              width="250"
              trigger="click">
              <div v-loading="sourceLoading" style="height: 150px;overflow: auto;">
                <el-tag
                  v-for="item in groupSource"
                  :key="item.id"
                  :type="item.name"
                  size="small"
                  style="margin-left: 5px;margin-top: 5px;">
                  {{ item.name }}
                </el-tag>
              </div>
              <el-link type="primary" @click="getGroupSource(scope.row)" slot="reference">点击查看</el-link>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator :tip2="$t('commons.remove')"
                                 :show-edit="showTypeLabel"
                                 @editClick="editMemberBtn(scope.row)"
                                 @deleteClick="removeMember(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="search" :current-page.sync="currentPage"
                           :page-size.sync="pageSize"
                           :total="total"/>
    </el-dialog>
    <el-dialog :close-on-click-modal="false" :visible.sync="memberVisible" width="45%"
               :title="title" :destroy-on-close="true" v-loading="memberLoading" @close="memberDialogClose">
      <el-form ref="memberFrom" label-position="right" :model="form" size="small" :rules="rules" label-width="120px"
               style="margin-right: 40px;">
        <el-form-item :label="$t('commons.member')" prop="userIds">
          <el-select
            v-model="form.userIds"
            multiple
            filterable
            :popper-append-to-body="false"
            class="member_select"
            :disabled="userSelectDisable"
            :filter-method="filterUserOption"
            @visible-change="resetUserOption"
            :placeholder="$t('member.please_choose_member')">
            <el-option
              v-for="item in users"
              :key="item.id"
              :label="item.id"
              :value="item.id">
              <user-option-item :user="item"/>
            </el-option>
            <div style="text-align: center; color: #8a8b8d;" v-if="showUserSearchGetMore">
              {{ $t('user.search_get_more_tip') }}
            </div>
          </el-select>
        </el-form-item>
        <el-form-item :label="typeLabel" v-if="showTypeLabel" prop="sourceIds">
          <el-select v-model="form.sourceIds" :placeholder="typeLabel"
                     :filter-method="filterResourceOption"
                     @visible-change="resetResourceOption"
                     class="other_source_select"
                     clearable multiple filterable>
            <el-option v-for="item in sourceData" :key="item.id" :label="item.name" :value="item.id"/>
            <div style="text-align: center; color: #8a8b8d;" v-if="showResourceSearchGerMore">
              {{ $t('user.search_get_more_tip') }}
            </div>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button @click="memberVisible = false" size="medium">{{ $t('commons.cancel') }}</el-button>
          <el-button type="primary" @click="addMember" @keydown.enter.native.prevent size="medium">
            {{ $t('commons.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {GROUP_PROJECT, GROUP_SYSTEM, GROUP_WORKSPACE} from "metersphere-frontend/src/utils/constants";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import UserOptionItem from "../../common/UserOptionItem";
import {getCurrentProjectID, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import {getProjectById} from "../../../api/project";
import {
  addUser2Group,
  getUserGroupByResourceUrlAndPage,
  getUserGroupSourceByUserIdAndGroupId,
  modifyUserGroupMember,
  rmUserFromGroup
} from "../../../api/user-group";
import {getUserListByResourceUrl} from "../../../api/user";
import {getGroupResource} from "../../../api/workspace";
import {operationConfirm} from "metersphere-frontend/src/utils";

export default {
  name: "GroupMember",
  components: {
    UserOptionItem,
    MsTableHeader,
    MsTablePagination,
    MsTableOperator
  },
  data() {
    return {
      visible: false,
      memberVisible: false,
      condition: {},
      memberData: [],
      currentPage: 1,
      pageSize: 5,
      total: 0,
      sourceLoading: false,
      memberLoading: false,
      group: {},
      groupSource: [],
      sourceData: [],
      sourceDataCopy: [],
      users: [],
      usersCopy: [],
      currentProject: {
        id: "",
        name: ""
      },
      form: {},
      title: '',
      submitType: '',
      userSelectDisable: false,
      initUserGroupUrl: "/user/group/user/",
      initUserUrl: "/user/list/",
      rules: {
        userIds: {required: true, message: this.$t('member.please_choose_member'), trigger: 'blur'},
        sourceIds: {required: true, message: this.$t('group.select_belong_source'), trigger: 'blur'}
      },
      loading: false,
      limitOptionCount: 400,
      showUserSearchGetMore: false,
      showResourceSearchGerMore: false,
    };
  },
  computed: {
    typeLabel() {
      let type = this.group.type;
      if (type === GROUP_WORKSPACE) {
        return this.$t('group.belong_workspace');
      }
      if (type === GROUP_PROJECT) {
        return this.$t('group.belong_project');
      }
      return '';
    },
    showTypeLabel() {
      return this.group.type !== GROUP_SYSTEM;
    }
  },
  methods: {
    init() {
      this.condition.userGroupId = this.group.id;
      this.loading = getUserGroupByResourceUrlAndPage(this.initUserGroupUrl, this.currentPage, this.pageSize, this.condition)
        .then(res => {
          let data = res.data;
          if (data) {
            let {itemCount, listObject} = data;
            this.total = itemCount;
            this.memberData = listObject;
          }
        })
      if (getCurrentProjectID()) {
        getProjectById(getCurrentProjectID()).then(res => {
          this.currentProject = res.data;
        });
      }
    },
    open(group, initUserGroupUrl, initUserUrl) {
      this.initUserGroupUrl = initUserGroupUrl ? initUserGroupUrl : "/user/group/user/";
      this.initUserUrl = initUserUrl ? initUserUrl : "/user/list/";
      this.visible = true;
      this.group = group;
      this.init();
    },
    close() {
      this.visible = false;
      this.$emit("refresh");
    },
    addMemberBtn() {
      this.title = this.$t('member.create');
      this.memberVisible = true;
      this.submitType = 'ADD';
      this.getUser();
      this.getResource();
    },
    search() {
      this.init();
    },
    editMemberBtn(row) {
      this.title = this.$t('member.modify');
      this.userSelectDisable = true;
      this.memberVisible = true;
      this.submitType = 'EDIT';
      this.getUser();
      this.getResource();
      getUserGroupSourceByUserIdAndGroupId(row.id, this.group.id).then(res => {
        let data = res.data;
        let userIds = [row.id];
        let sourceIds = data.map(d => d.id);
        this.$set(this.form, 'userIds', userIds);
        this.$set(this.form, 'sourceIds', sourceIds);
        this._handleSelectOption(this.form.userIds, this.users);
        this._handleSelectOption(this.form.sourceIds, this.sourceData);
      });
    },
    editMember() {
      this.form.groupId = this.group.id;
      this.$refs['memberFrom'].validate(valid => {
        if (!valid) {
          return false;
        }
        this.memberLoading = modifyUserGroupMember(this.form).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.init();
          this.memberVisible = false;
        });
      });
    },
    getUser() {
      this.memberLoading = getUserListByResourceUrl(this.initUserUrl).then(res => {
        this.handleUserOption(res.data);
        this.usersCopy = res.data;
      })
    },
    removeMember(row) {
      operationConfirm(this.$t('member.remove_member'), () => {
        if (this.initUserUrl === 'user/ws/current/member/list') {
          if (row.id === getCurrentUserId()) {
            this.$warning(this.$t('group.unable_to_remove_current_member'));
            return;
          }
        }
        this.loading = rmUserFromGroup(row.id, this.group.id).then(() => {
          this.$success(this.$t('commons.remove_success'));
          this.init();
        });
      }, () => {
        this.$info(this.$t('commons.remove_cancel'));
      })
    },
    getGroupSource(row) {
      this.groupSource = [];
      this.sourceLoading = getUserGroupSourceByUserIdAndGroupId(row.id, this.group.id).then(res => {
        this.groupSource = res.data;
      });
    },
    addMember() {
      if (this.submitType === 'ADD') {
        this._addMember();
      } else if (this.submitType === 'EDIT') {
        this.editMember();
      }
    },
    _addMember() {
      this.form.groupId = this.group.id;
      this.$refs['memberFrom'].validate(valid => {
        if (!valid) {
          return false;
        }
        this.memberLoading = addUser2Group(this.form).then(() => {
          this.$success(this.$t('commons.save_success'));
          this.memberVisible = false;
          this.init();
        });
      });
    },
    getResource() {
      this.memberLoading = getGroupResource(this.group.id, this.group.type).then(res => {
        let data = res.data;
        if (data) {
          this._setResource(this.group.type, data);
          this.handleResourceOption(this.sourceData);
        }
      });
    },
    _setResource(type, data) {
      switch (type) {
        case GROUP_WORKSPACE:
          this.sourceData = data.workspaces;
          this.sourceDataCopy = data.workspaces;
          break;
        case GROUP_PROJECT:
          if (this.initUserUrl === 'user/ws/current/member/list') {
            if (!this.currentProject.id) {
              this.currentProject.id = sessionStorage.getItem("project_id");
              this.currentProject.name = sessionStorage.getItem("project_name");
            }
            this.sourceData = [this.currentProject];
            this.sourceDataCopy = [this.currentProject];
          } else {
            this.sourceData = data.projects;
            this.sourceDataCopy = data.projects;
          }
          break;
        default:
      }
    },
    memberDialogClose() {
      this.form = {};
      this.memberVisible = false;
      this.userSelectDisable = false;
    },
    handleUserOption(users) {
      if (!users) {
        return;
      }
      this.showUserSearchGetMore = users.length > this.limitOptionCount;
      this.users = users.slice(0, this.limitOptionCount);
      if (!this.form.userIds || this.form.userIds.length === 0) {
        return;
      }
      this._handleSelectOption(this.form.userIds, this.users, this.usersCopy);
    },
    _handleSelectOption(ids, options, origins) {
      for (let id of ids) {
        let index = options.findIndex(o => o.id === id);
        if (index <= -1) {
          let obj = origins.find(d => d.id === id);
          if (obj) {
            options.unshift(obj);
          }
        }
      }
    },
    handleResourceOption(resources) {
      if (!resources) {
        return;
      }
      this.showResourceSearchGerMore = resources.length > this.limitOptionCount;
      this.sourceData = resources.slice(0, this.limitOptionCount);
      if (!this.form.sourceIds || this.form.sourceIds.length === 0) {
        return;
      }
      this._handleSelectOption(this.form.sourceIds, this.sourceData, this.sourceDataCopy);
    },
    filterUserOption(queryString) {
      this.handleUserOption(queryString ? this.usersCopy.filter(this.createFilter(queryString)) : this.usersCopy);
    },
    filterResourceOption(queryString) {
      this.handleResourceOption(queryString ? this.sourceDataCopy.filter(this.createFilter(queryString)) : this.sourceDataCopy);
    },
    createFilter(queryString) {
      return item => (item.name && item.name.toLowerCase().indexOf(queryString.toLowerCase()) !== -1);
    },
    resetUserOption(val) {
      if (val) {
        this.handleUserOption(this.usersCopy);
      }
    },
    resetResourceOption(val) {
      if (val) {
        this.handleResourceOption(this.sourceDataCopy);
      }
    }
  }
};
</script>

<style scoped>
.member_select, .other_source_select {
  display: block;
}

.group-member :deep(.el-dialog__body) {
  padding-top: 0;
}
</style>
