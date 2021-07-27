<template>
  <div v-loading="result.loading">
    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :create-permission="['WORKSPACE_PROJECT_MANAGER:READ+CREATE']" :condition.sync="condition"
                         @search="search" @create="create"
                         :create-tip="btnTips" :title="$t('commons.project')">
          <template v-slot:button>
            <ms-table-button icon="el-icon-box"
                             :content="$t('api_test.jar_config.title')" @click="openJarConfig"/>
          </template>
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
        <!--<el-table-column prop="workspaceName" :label="$t('project.owning_workspace')"/>-->
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
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column min-width="100"
                         sortable
                         prop="updateTime"
                         :label="$t('commons.update_time')"
                         show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
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
                    v-permission="['WORKSPACE_PROJECT_MANAGER:READ+EDIT']"
                    :tip="$t('api_test.environment.environment_config')" icon="el-icon-setting"
                    type="info" @exec="openEnvironmentConfig(scope.row)"/>
                  <ms-table-operator-button
                    v-permission="['WORKSPACE_PROJECT_MANAGER:READ+EDIT']"
                    :tip="$t('load_test.other_resource')"
                    icon="el-icon-files"
                    type="success" @exec="openFiles(scope.row)"/>
                </template>
              </ms-table-operator>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="list" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="createVisible" destroy-on-close
               @close="handleClose">
      <el-form :model="form" :rules="rules" ref="form" label-position="right" label-width="80px" size="small">
        <el-form-item :label-width="labelWidth" :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"></el-input>
        </el-form-item>


        <el-form-item :label-width="labelWidth" :label="$t('用例模板')" prop="caseTemplateId">
          <template-select :data="form" scene="API_CASE" prop="caseTemplateId" ref="caseTemplate"/>
        </el-form-item>
        <el-form-item :label-width="labelWidth" :label="$t('缺陷模板')" prop="issueTemplateId">
          <template-select :data="form" scene="ISSUE" prop="issueTemplateId" ref="issueTemplate"/>
        </el-form-item>


        <el-form-item :label-width="labelWidth" :label="$t('commons.description')" prop="description">
          <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item :label-width="labelWidth" :label="$t('project.tapd_id')" v-if="tapd">
          <el-input v-model="form.tapdId" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label-width="labelWidth" :label="$t('project.jira_key')" v-if="jira">
          <el-input v-model="form.jiraKey" autocomplete="off"/>
          <ms-instructions-icon effect="light">
            <template>
              <img class="jira-image" src="../../../../assets/jira-key.png"/>
            </template>
          </ms-instructions-icon>
        </el-form-item>
        <el-form-item :label-width="labelWidth" :label="$t('project.zentao_id')" v-if="zentao">
          <el-input v-model="form.zentaoId" autocomplete="off"></el-input>
          <ms-instructions-icon effect="light">
            <template>
              禅道流程：产品-项目 | 产品-迭代 | 产品-冲刺 | 项目-迭代 | 项目-冲刺 <br/><br/>
              根据 "后台 -> 自定义 -> 流程" 查看对应流程，根据流程填写ID <br/><br/>
              产品-项目 | 产品-迭代 | 产品-冲刺 需要填写产品ID <br/><br/>
              项目-迭代 | 项目-冲刺 需要填写项目ID
            </template>
          </ms-instructions-icon>
        </el-form-item>
        <el-form-item :label-width="labelWidth" :label="$t('project.repeatable')" prop="repeatable">
          <el-switch v-model="form.repeatable"></el-switch>
        </el-form-item>
        <el-form-item :label-width="labelWidth" label="测试用例自定义ID" prop="customNum">
          <el-switch v-model="form.customNum"></el-switch>
        </el-form-item>
        <el-form-item :label-width="labelWidth" label="场景自定义ID" prop="scenarioCustomNum">
          <el-switch v-model="form.scenarioCustomNum"></el-switch>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <div class="dialog-footer">
          <ms-dialog-footer
            @cancel="createVisible = false"
            @confirm="submit('form')"/>
        </div>
      </template>
    </el-dialog>

    <el-dialog :close-on-click-modal="false" :visible.sync="memberVisible" width="70%" :destroy-on-close="true"
               @close="close"
               class="dialog-css">
      <template v-slot:title>
        <ms-table-header :condition.sync="dialogCondition" @create="open" @search="list" :have-search="false"
                         :create-permission="['WORKSPACE_USER:READ+CREATE']"
                         :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      </template>
      <div>
        <!-- organization member table -->
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
                                   :edit-permission="['WORKSPACE_USER:READ+EDIT']"
                                   :delete-permission="['WORKSPACE_USER:READ+DELETE']"
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

    <el-dialog :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="updateVisible" width="30%"
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
        <el-form-item :label="$t('commons.group')" prop="groupIds" :rules="{required: true, message: $t('group.please_select_group'), trigger: 'change'}">
          <el-select v-model="form.groupIds" multiple :placeholder="$t('group.please_select_group')" style="width: 100%">
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

    <add-member :group-type="'PROJECT'" :group-scope-id="orgId" ref="addMember" @submit="submitForm"/>

    <ms-delete-confirm :title="$t('project.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

    <api-environment-config ref="environmentConfig" :type="'workspace'"/>

    <ms-jar-config ref="jarConfig"/>

    <ms-resource-files ref="resourceFiles"/>
  </div>
</template>

<script>
import MsCreateBox from "../CreateBox";
import {Message} from "element-ui";
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsDialogFooter from "../../common/components/MsDialogFooter";
import {
  getCurrentOrganizationId,
  getCurrentProjectID,
  getCurrentUser, getCurrentUserId,
  getCurrentWorkspaceId,
  listenGoBack,
  removeGoBackListener
} from "@/common/js/utils";
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import MsDeleteConfirm from "../../common/components/MsDeleteConfirm";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import ApiEnvironmentConfig from "../../api/test/components/ApiEnvironmentConfig";
import TemplateComponent from "../../track/plan/view/comonents/report/TemplateComponent/TemplateComponent";
import {GROUP_PROJECT, PROJECT_ID} from "@/common/js/constants";
import MsJarConfig from "../../api/test/components/jar/JarConfig";
import MsTableButton from "../../common/components/MsTableButton";
import {_filter, _sort} from "@/common/js/tableUtils";
import MsResourceFiles from "@/business/components/performance/test/components/ResourceFiles";
import TemplateSelect from "@/business/components/settings/workspace/template/TemplateSelect";
import {PROJECT_CONFIGS} from "@/business/components/common/components/search/search-components";
import MsRolesTag from "@/business/components/common/components/MsRolesTag";
import AddMember from "@/business/components/settings/common/AddMember";
import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";

export default {
  name: "MsProject",
  components: {
    MsInstructionsIcon,
    TemplateSelect,
    MsResourceFiles,
    MsTableButton,
    MsJarConfig,
    TemplateComponent,
    ApiEnvironmentConfig,
    MsTableOperatorButton,
    MsDeleteConfirm,
    MsMainContainer, MsRolesTag,
    MsContainer, MsTableOperator, MsCreateBox, MsTablePagination, MsTableHeader, MsDialogFooter,
    AddMember
  },
  inject: [
    'reloadTopMenus'
  ],
  data() {
    return {
      createVisible: false,
      updateVisible: false,
      dialogMemberVisible: false,
      result: {},
      btnTips: this.$t('project.create'),
      title: this.$t('project.create'),
      condition: {components: PROJECT_CONFIGS},
      items: [],
      tapd: false,
      jira: false,
      zentao: false,
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
        // caseTemplateId: [{required: true}],
        // issueTemplateId: [{required: true}],
      },
      screenHeight: 'calc(100vh - 195px)',
      dialogCondition: {},
      memberVisible: false,
      memberLineData: [],
      memberForm: {},
      dialogCurrentPage: 1,
      dialogPageSize: 5,
      dialogTotal: 0,
      currentProjectId: "",
      userList: [],
      labelWidth: '150px'
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
      this.create();
      this.$router.replace('/setting/project/all');
    }
    this.list();
    this.getMaintainerOptions();
  },
  activated() {
    this.list();
  },
  computed: {
    currentUser: () => {
      return getCurrentUser();
    },
    projectId() {
      return getCurrentProjectID();
    },
    orgId() {
      return getCurrentOrganizationId();
    }
  },
  destroyed() {
    this.createVisible = false;
  },
  methods: {
    jumpPage(row) {
      this.currentWorkspaceRow = row;
      this.currentProjectId = row.id;
      let param = {
        name: '',
        projectId: row.id
      };
      this.wsId = row.id;
      let path = "/user/project/member/list";
      this.result = this.$post("/user/project/member/list", param, res => {
        let data = res.data;
        this.memberLineData = data;
        let arr = this.memberLineData.filter(item => item.id == getCurrentUserId());
        if (arr.length > 0) {
          window.sessionStorage.setItem(PROJECT_ID, row.id);
          this.$router.push('/track/home');
        } else {
          this.$message(this.$t("commons.project_permission"));
        }
      });

    },
    getMaintainerOptions() {
      let workspaceId = getCurrentWorkspaceId();
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id};
        });
      });
    },
    create() {
      let workspaceId = getCurrentWorkspaceId();
      this.getOptions();
      if (!workspaceId) {
        this.$warning(this.$t('project.please_choose_workspace'));
        return false;
      }
      this.title = this.$t('project.create');
      // listenGoBack(this.handleClose);
      this.createVisible = true;
      this.form = {};
    },
    getOptions() {

      if (this.$refs.issueTemplate) {
        this.$refs.issueTemplate.getTemplateOptions();
      }
      if (this.$refs.caseTemplate) {
        this.$refs.caseTemplate.getTemplateOptions();
      }
    },
    edit(row) {
      this.title = this.$t('project.edit');
      this.getOptions();
      this.createVisible = true;
      listenGoBack(this.handleClose);
      this.form = Object.assign({}, row);
      this.$get("/service/integration/all/" + getCurrentOrganizationId(), response => {
        let data = response.data;
        let platforms = data.map(d => d.platform);
        if (platforms.indexOf("Tapd") !== -1) {
          this.tapd = true;
        }
        if (platforms.indexOf("Jira") !== -1) {
          this.jira = true;
        }
        if (platforms.indexOf("Zentao") !== -1) {
          this.zentao = true;
        }
      });
    },
    submit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let saveType = "add";
          if (this.form.id) {
            saveType = "update";
          }
          var protocol = document.location.protocol;
          protocol = protocol.substring(0, protocol.indexOf(":"));
          this.form.protocal = protocol;
          this.form.workspaceId = getCurrentWorkspaceId();
          this.form.createUser = getCurrentUserId();
          this.result = this.$post("/project/" + saveType, this.form, () => {
            this.createVisible = false;
            Message.success(this.$t('commons.save_success'));
            if (saveType === 'add') {
              this.reloadTopMenus();
            } else {
              this.list();
            }
          });
        } else {
          return false;
        }
      });
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
      this.$confirm(this.$t('project.delete_tip'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.$get('/project/delete/' + project.id, () => {
          if (project.id === getCurrentProjectID()) {
            localStorage.removeItem(PROJECT_ID);
            this.$post("/user/update/current", {id: getCurrentUser().id, lastProjectId: ''});
          }
          Message.success(this.$t('commons.delete_success'));
          this.list();
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$t('commons.delete_cancelled')
        });
      });
    },
    handleClose() {
      removeGoBackListener(this.handleClose);
      this.createVisible = false;
      this.tapd = false;
      this.jira = false;
      this.zentao = false;
    },
    search() {
      this.list();
    },
    list() {
      this.condition.workspaceId = getCurrentWorkspaceId();
      let url = "/project/list/" + this.currentPage + '/' + this.pageSize;
      this.result = this.$post(url, this.condition, (response) => {
        let data = response.data;
        this.items = data.listObject;
        for (let i = 0; i < this.items.length; i++) {
          let param = {
            name: '',
            workspaceId: this.items[i].id
          };
          let path = "user/ws/member/list/all";
          this.$post(path, param, res => {
            let member = res.data;
            this.$set(this.items[i], "memberSize", member.length);
          });
        }
        this.total = data.itemCount;
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
    buildPagePath(path) {
      return path + "/" + this.dialogCurrentPage + "/" + this.dialogPageSize;
    },
    cellClick(row) {
      // 保存当前点击的组织信息到currentRow
      this.currentWorkspaceRow = row;
      this.currentProjectId = row.id;
      this.memberVisible = true;
      let param = {
        name: '',
        projectId: row.id
      };
      this.wsId = row.id;
      let path = "/user/project/member/list";
      this.result = this.$post(this.buildPagePath(path), param, res => {
        let data = res.data;
        this.memberLineData = data.listObject;
        let url = "/user/group/list/project/" + row.id;
        // 填充角色信息
        for (let i = 0; i < this.memberLineData.length; i++) {
          this.$get(url + "/" + encodeURIComponent(this.memberLineData[i].id), response => {
            let groups = response.data;
            this.$set(this.memberLineData[i], "groups", groups);
          });
        }
        this.dialogTotal = data.itemCount;
      });
    },
    addMember() {

    },
    dialogSearch() {
      let row = this.currentWorkspaceRow;
      this.dialogWsMemberVisible = true;
      let param = this.dialogCondition;
      this.$set(param, 'projectId', row.id);
      let path = "/user/project/member/list";
      this.result = this.$post(this.buildPagePath(path), param, res => {
        let data = res.data;
        this.memberLineData = data.listObject;
        let url = "/user/group/list/project/" + row.id;
        // 填充角色信息
        for (let i = 0; i < this.memberLineData.length; i++) {
          this.$get(url + "/" + encodeURIComponent(this.memberLineData[i].id), response => {
            let groups = response.data;
            this.$set(this.memberLineData[i], "groups", groups);
          });
        }
        this.dialogTotal = data.itemCount;
      });
    },
    editMember(row) {
      this.updateVisible = true;
      this.form = Object.assign({}, row);
      let groupIds = this.form.groups.map(r => r.id);
      this.result = this.$post('/user/group/list', {type: GROUP_PROJECT, resourceId: getCurrentOrganizationId()}, response => {
        this.$set(this.form, "allgroups", response.data);
      });
      // 编辑使填充角色信息
      this.$set(this.form, 'groupIds', groupIds);
    },
    delMember(row) {
      this.$confirm(this.$t('member.remove_member'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get('/user/project/member/delete/' + this.currentProjectId + '/' + encodeURIComponent(row.id), () => {
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
          this.result = this.$post("/project/member/update", param, () => {
            this.$success(this.$t('commons.modify_success'));
            this.updateVisible = false;
            this.dialogSearch();
          });
        }
      });
    },
    submitForm(param) {
      param['projectId'] = this.currentProjectId;
      this.result = this.$post("user/project/member/add", param, () => {
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

/*.dialog-css >>> .el-dialog__header {*/
/*  padding: 0px;*/
/*}*/

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

.el-input,.el-textarea {
  width: 95%;
}
</style>
