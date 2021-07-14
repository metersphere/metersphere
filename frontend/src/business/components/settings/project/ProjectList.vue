<template>
  <div v-loading="result.loading">
    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :show-create="false" :condition.sync="condition"
                         @search="search" @create="create"
                         :create-tip="btnTips" :title="$t('commons.project')">
          <template v-slot:button>
            <ms-table-button icon="el-icon-box"
                             v-permission="['PROJECT_MANAGER:READ+EDIT']"
                             :content="$t('api_test.jar_config.title')" @click="openJarConfig"/>
          </template>
        </ms-table-header>
      </template>
      <el-table border class="adjust-table" :data="items" style="width: 100%"
                @sort-change="sort"
                @filter-change="filter"
                :height="screenHeight"
      >
        <el-table-column prop="name" :label="$t('commons.name')" min-width="100" show-overflow-tooltip/>
        <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip>
          <template v-slot:default="scope">
            <pre>{{ scope.row.description }}</pre>
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
                :edit-permission="['PROJECT_MANAGER:READ+EDIT']"
                :delete-permission="['PROJECT_MANAGER:READ+DELETE']"
                @editClick="edit(scope.row)"
                :show-delete="false"
                @deleteClick="handleDelete(scope.row)">
                <template v-slot:behind>
                  <ms-table-operator-button
                    v-permission="['PROJECT_MANAGER:READ+EDIT']"
                    :tip="$t('api_test.environment.environment_config')" icon="el-icon-setting"
                    type="info" @exec="openEnvironmentConfig(scope.row)"/>
                  <ms-table-operator-button
                    v-permission="['PROJECT_MANAGER:READ+EDIT']"
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
        <el-form-item :label-width="labelWidth"  label="测试用例自定义ID" prop="customNum">
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

    <ms-delete-confirm :title="$t('project.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

    <api-environment-config ref="environmentConfig"/>

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
import {PROJECT_ID} from "@/common/js/constants";
import MsJarConfig from "../../api/test/components/jar/JarConfig";
import MsTableButton from "../../common/components/MsTableButton";
import {_filter, _sort} from "@/common/js/tableUtils";
import MsResourceFiles from "@/business/components/performance/test/components/ResourceFiles";
import TemplateSelect from "@/business/components/settings/workspace/template/TemplateSelect";
import {PROJECT_CONFIGS} from "@/business/components/common/components/search/search-components";
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
    MsMainContainer,
    MsContainer, MsTableOperator, MsCreateBox, MsTablePagination, MsTableHeader, MsDialogFooter
  },
  data() {
    return {
      createVisible: false,
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
          {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'}
        ],
        description: [
          {max: 250, message: this.$t('commons.input_limit', [0, 250]), trigger: 'blur'}
        ],
        // caseTemplateId: [{required: true}],
        // issueTemplateId: [{required: true}],
      },
      screenHeight: 'calc(100vh - 195px)',
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
    }
  },
  destroyed() {
    this.createVisible = false;
  },
  methods: {
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
      this.$get("/service/integration/all/" + getCurrentUser().lastOrganizationId, response => {
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
            this.list();
            Message.success(this.$t('commons.save_success'));
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
      this.condition.projectId = getCurrentProjectID();
      let url = "/project/list/" + this.currentPage + '/' + this.pageSize;
      this.result = this.$post(url, this.condition, (response) => {
        let data = response.data;
        this.items = data.listObject;
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
  },
  created() {
    document.addEventListener('keydown', this.handleEvent);
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.handleEvent);
  }
};
</script>

<style scoped>
pre {
  margin: 0 0;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
}

.el-input,.el-textarea {
  width: 95%;
}
</style>
