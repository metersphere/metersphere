<template>
  <div v-loading="loading">
    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :create-permission="['WORKSPACE_PROJECT_MANAGER:READ+CREATE']" :condition.sync="condition"
                         @search="search" @create="create"
                         :create-tip="btnTips" :title="$t('project.manager')">
        </ms-table-header>
      </template>
      <el-table border class="adjust-table" :data="items" style="width: 100%"
                @sort-change="sort"
                @filter-change="filter"
                :height="screenHeight"
      >
        <el-table-column prop="name" :label="$t('commons.name')" min-width="100" show-overflow-tooltip>
          <template v-slot:default="scope">
            <el-link type="primary" class="member-size" @click="jumpPage(scope.row)">
              {{ scope.row.name }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip>
          <template v-slot:default="scope">
            <pre>{{ scope.row.description }}</pre>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.member')">
          <template v-slot:default="scope">
            <el-link type="primary" class="member-size" @click="cellClick(scope.row)">
              {{ scope.row.memberSize }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column
          prop="createUser"
          :label="$t('commons.create_user')"
          :filters="userFilters"
          column-key="create_user"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.createUserName }}</span>
          </template>
        </el-table-column>
        <el-table-column min-width="100"
                         sortable
                         prop="createTime"
                         :label="$t('commons.create_time')"
                         show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column min-width="100"
                         sortable
                         prop="updateTime"
                         :label="$t('commons.update_time')"
                         show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')" width="180">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator
                :edit-permission="['WORKSPACE_PROJECT_MANAGER:READ+EDIT']"
                :delete-permission="['WORKSPACE_PROJECT_MANAGER:READ+DELETE']"
                :show-delete="projectId !== scope.row.id"
                @editClick="edit(scope.row)"
                @deleteClick="handleDelete(scope.row)">
                <template v-slot:behind>
                  <ms-table-operator-button
                    v-permission="['WORKSPACE_PROJECT_MANAGER:READ+ENVIRONMENT_CONFIG']"
                    :tip="$t('api_test.environment.environment_config')" icon="el-icon-setting"
                    type="info" @exec="openEnvironmentConfig(scope.row)"/>
                </template>
              </ms-table-operator>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="list" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <edit-project ref="editProject"/>

    <el-dialog
      v-loading="memberTableLoading"
      :close-on-click-modal="false" :visible.sync="memberVisible" width="70%" :destroy-on-close="true"
      @close="close"
      class="dialog-css">
      <template v-slot:title>
        <ms-table-header :condition.sync="dialogCondition" @create="open" @search="list" :have-search="false"
                         :create-permission="['WORKSPACE_PROJECT_MANAGER:READ+ADD_USER']"
                         :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      </template>
      <div>
        <el-table :data="memberLineData" style="width: 100%;margin-top: 5px;">
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
                <ms-table-operator :tip2="$t('commons.remove')"
                                   :edit-permission="['WORKSPACE_PROJECT_MANAGER:READ+EDIT_USER']"
                                   :delete-permission="['WORKSPACE_PROJECT_MANAGER:READ+DELETE_USER']"
                                   @editClick="editMember(scope.row)"
                                   @deleteClick="delMember(scope.row)"/>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="dialogSearch" :current-page.sync="dialogCurrentPage"
                             :page-size.sync="dialogPageSize"
                             :total="dialogTotal"/>
      </div>
    </el-dialog>

    <el-dialog
      v-loading="memberDialogLoading"
      :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="updateVisible" width="30%"
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
                     style="width: 100%">
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
          @confirm="updateProjectMember('updateUserForm')"/>
      </template>
    </el-dialog>

    <add-member
      :group-type="'PROJECT'"
      :group-scope-id="workspaceId"
      ref="addMember"
      :project-id="rowProjectId"
      :user-resource-url="'user/add/project/member/option/' + rowProjectId"
      @submit="submitForm"/>

    <ms-delete-confirm :title="$t('project.delete')" @delete="_handleDelete" ref="deleteConfirm"/>
    <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>
  </div>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {
  fullScreenLoading,
  operationConfirm,
  removeGoBackListener,
  stopFullScreenLoading
} from "metersphere-frontend/src/utils";
import {
  getCurrentProjectID,
  getCurrentUser,
  getCurrentUserId,
  getCurrentWorkspaceId
} from "metersphere-frontend/src/utils/token";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import {GROUP_PROJECT, PROJECT_ID} from "metersphere-frontend/src/utils/constants";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import {_filter, _sort} from "metersphere-frontend/src/utils/tableUtils";
import {PROJECT_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import MsRolesTag from "metersphere-frontend/src/components/MsRolesTag";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import AddMember from "../../common/AddMember";
import {isSuperUser} from "metersphere-frontend/src/api/user.js";
import {
  addProjectMember,
  delProjectMember,
  getCurrentProjectUserList,
  getProjectMemberPages,
  updateCurrentUser
} from "../../../api/user";
import {delProjectById, getProjectPages, modifyProjectMember} from "../../../api/project";
import {getProjectMemberGroup, getUserGroupList} from "../../../api/user-group";
import EditProject from "./EditProject";
import ApiEnvironmentConfig from "metersphere-frontend/src/components/environment/ApiEnvironmentConfig";
import {switchProject} from "metersphere-frontend/src/api/project";


export default {
  name: "MsProject",
  components: {
    MsInstructionsIcon,
    MsTableButton,
    MsTableOperatorButton,
    MsDeleteConfirm,
    MsRolesTag,
    EditProject,
    MsTableOperator,
    MsTablePagination,
    MsTableHeader,
    MsDialogFooter,
    AddMember,
    ApiEnvironmentConfig
  },
  inject: [
    'reload',
    'reloadTopMenus'
  ],
  data() {
    return {
      updateVisible: false,
      dialogMemberVisible: false,
      loading: false,
      memberDialogLoading: false,
      memberTableLoading: false,
      btnTips: this.$t('project.create'),
      title: this.$t('project.create'),
      condition: {components: PROJECT_CONFIGS},
      items: [],
      form: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      userFilters: [],
      rules: {
        name: [
          {required: true, message: this.$t('project.input_name'), trigger: 'blur'},
          {min: 2, max: 60, message: this.$t('commons.input_limit', [2, 60]), trigger: 'blur'}
        ],
        description: [
          {max: 250, message: this.$t('commons.input_limit', [0, 250]), trigger: 'blur'}
        ],
      },
      screenHeight: 'calc(100vh - 155px)',
      dialogCondition: {},
      memberVisible: false,
      memberLineData: [],
      memberForm: {},
      dialogCurrentPage: 1,
      dialogPageSize: 5,
      dialogTotal: 0,
      currentProjectId: "",
      userList: [],
      labelWidth: '150px',
      rowProjectId: ""
    };
  },
  props: {
    baseUrl: {
      type: String
    }
  },
  mounted() {
    if (this.$route.path.split('/')[2] === 'project' &&
      this.$route.path.split('/')[3] === 'create') {
      this.$router.replace('/setting/project/all');
      setTimeout(() => {
        this.create();
      }, 200)
    }
    this.list();
    this.getMaintainerOptions();
  },
  activated() {
    this.list();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    workspaceId() {
      return getCurrentWorkspaceId();
    }
  },
  methods: {
    jumpPage(row) {
      this.currentWorkspaceRow = row;
      this.currentProjectId = row.id;
      let param = {
        projectId: row.id
      };
      this.loading = getProjectMemberPages(1, 10000, row.workspaceId, param).then(res => {
        let {listObject} = res.data;
        this.memberLineData = listObject;
        let arr = this.memberLineData.filter(item => item.id === getCurrentUserId());
        if (arr.length > 0) {
          this.doJump(row);
        } else {
          isSuperUser(getCurrentUserId()).then(r => {
            if (r && r.data) {
              this.doJump(row);
            } else {
              this.$warning(this.$t("commons.project_permission"));
            }
          });
        }
      });
    },
    doJump(row) {
      // 跳转的时候更新用户的last_project_id
      sessionStorage.setItem(PROJECT_ID, row.id);
      const loading = fullScreenLoading(this);
      switchProject({id: getCurrentUserId(), lastProjectId: row.id}).then(() => {
        this.$router.push('/track/home').then(() => {
          location.reload();
          stopFullScreenLoading(loading);
        });
      })
    },
    getMaintainerOptions() {
      getCurrentProjectUserList().then(res => {
        this.userFilters = res.data.map(u => {
          return {text: u.name, value: u.id};
        });
      });
    },
    create() {
      let workspaceId = getCurrentWorkspaceId();
      if (!workspaceId) {
        this.$warning(this.$t('project.please_choose_workspace'));
        return false;
      }
      this.title = this.$t('project.create');
      // listenGoBack(this.handleClose);
      this.form = {};
      this.$refs.editProject.edit();
    },
    edit(row) {
      this.$refs.editProject.edit(row);
    },
    openJarConfig() {
      this.$refs.jarConfig.open();
    },
    openFiles(project) {
      this.$refs.resourceFiles.open(project);
    },
    handleDelete(project) {
      this.$refs.deleteConfirm.open(project);
    },
    _handleDelete(project) {
      operationConfirm(this, this.$t('project.delete_tip'), () => {
        delProjectById(project.id).then(() => {
          if (project.id === getCurrentProjectID()) {
            localStorage.removeItem(PROJECT_ID);
            updateCurrentUser({id: getCurrentUser().id, lastProjectId: ''});
          }
          this.$success(this.$t('commons.delete_success'));
          this.list();
        });
      }, () => {
        this.$info(this.$t('commons.delete_cancelled'));
      })
    },
    handleClose() {
      removeGoBackListener(this.handleClose);
    },
    search() {
      this.currentPage = 1;
      this.list();
    },
    list() {
      this.condition.workspaceId = getCurrentWorkspaceId();
      this.loading = getProjectPages(this.currentPage, this.pageSize, this.condition).then(res => {
        let data = res.data;
        let {listObject, itemCount} = data;
        this.items = listObject;
        this.total = itemCount;
        for (let i = 0; i < this.items.length; i++) {
          let param = {
            projectId: this.items[i].id
          };
          getProjectMemberPages(1, 10000, this.condition.workspaceId, param).then(res => {
            let {listObject} = res.data
            this.$set(this.items[i], "memberSize", listObject.length);
          });
        }
      });
    },
    sort(column) {
      _sort(column, this.condition);
      this.list();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.list();
    },
    openEnvironmentConfig(project) {
      this.$refs.environmentConfig.open(project.id);
    },
    environmentConfigClose() {
      // do nothing
    },
    cellClick(row) {
      this.rowProjectId = row.id;
      // 保存当前点击的组织信息到currentRow
      this.currentWorkspaceRow = row;
      this.currentProjectId = row.id;
      let param = {
        name: '',
        projectId: row.id
      };
      this.memberTableLoading = getProjectMemberPages(this.dialogCurrentPage, this.dialogPageSize, row.workspaceId, param).then(res => {
        console.log(111)
        let data = res.data;
        let {listObject, itemCount} = data;
        this.memberLineData = listObject;
        this.dialogTotal = itemCount;
        let memberArr = this.memberLineData.filter(item => item.id === getCurrentUserId());
        if (memberArr.length === 0) {
          isSuperUser(getCurrentUserId()).then(r => {
            if (r && r.data) {
              // 非项目成员但拥有超级管理员用户组
              this.setMemberGroup(row);
              this.memberVisible = true;
            } else {
              this.$warning(this.$t("commons.project_permission"));
            }
          });
        } else {
          this.setMemberGroup(row);
          this.memberVisible = true;
        }
      });
    },
    setMemberGroup(row) {
      for (let i = 0; i < this.memberLineData.length; i++) {
        getProjectMemberGroup(row.id, encodeURIComponent(this.memberLineData[i].id)).then(res => {
          this.$set(this.memberLineData[i], "groups", res.data);
        });
      }
    },
    dialogSearch() {
      let row = this.currentWorkspaceRow;
      this.dialogWsMemberVisible = true;
      let param = this.dialogCondition;
      this.$set(param, 'projectId', row.id);
      getProjectMemberPages(this.dialogCurrentPage, this.dialogPageSize, row.workspaceId, param).then(res => {
        let data = res.data;
        let {listObject, itemCount} = data;
        this.memberLineData = listObject;
        this.dialogTotal = itemCount;
        // 填充角色信息
        for (let i = 0; i < this.memberLineData.length; i++) {
          getProjectMemberGroup(row.id, encodeURIComponent(this.memberLineData[i].id)).then(res => {
            let groups = res.data;
            this.$set(this.memberLineData[i], "groups", groups);
          });
        }
      });
    },
    editMember(row) {
      this.updateVisible = true;
      this.form = Object.assign({}, row);
      let groupIds = this.form.groups.map(r => r.id);
      let param = {
        type: GROUP_PROJECT,
        resourceId: getCurrentWorkspaceId(),
        projectId: this.currentProjectId
      };
      // 编辑使填充角色信息
      this.$set(this.form, 'groupIds', groupIds);
      this.memberDialogLoading = getUserGroupList(param).then(res => {
        this.$set(this.form, "allgroups", res.data);
      });
    },
    delMember(row) {
      this.$confirm(this.$t('member.remove_member').toString(), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.loading = delProjectMember(this.currentProjectId, encodeURIComponent(row.id))
          .then(() => {
            this.$success(this.$t('commons.remove_success'));
            this.dialogSearch();
          });
      }).catch(() => {
        this.$info(this.$t('commons.remove_cancel'));
      });
    },
    close: function () {
      this.memberVisible = false;
      this.memberLineData = [];
      this.list();
    },
    updateProjectMember(formName) {
      let param = {
        id: this.form.id,
        name: this.form.name,
        email: this.form.email,
        phone: this.form.phone,
        groupIds: this.form.groupIds,
        projectId: this.currentProjectId
      };
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.loading = modifyProjectMember(param).then(() => {
            this.$success(this.$t('commons.modify_success'));
            this.updateVisible = false;
            this.dialogSearch();
          });
        }
      });
    },
    submitForm(param) {
      param['projectId'] = this.currentProjectId;
      this.loading = addProjectMember(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.dialogSearch();
        this.$refs.addMember.close();
      });
    },
    open() {
      this.$refs.addMember.open();
    },
    handleMemberClose() {
      this.dialogMemberVisible = false;
      this.memberForm = {};
    },
    querySearch(queryString, cb) {
      let userList = this.userList;
      let results = queryString ? userList.filter(this.createFilter(queryString)) : userList;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    createFilter(queryString) {
      return (user) => {
        return (user.email.indexOf(queryString.toLowerCase()) === 0 || user.id.indexOf(queryString.toLowerCase()) === 0);
      };
    },
  },
  created() {
    document.addEventListener('keydown', this.handleEvent);
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.handleEvent);
  },

};
</script>

<style scoped>
pre {
  margin: 0 0;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
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

.el-input, .el-textarea {
  width: 80%;
}

</style>
