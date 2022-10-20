<template>
  <div>
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="createVisible" v-if="createVisible"
               @close="handleClose">
      <el-form v-loading="loading" :model="form" :rules="rules" ref="form" label-position="right" label-width="80px"
               size="small">
        <el-form-item :label-width="labelWidth" :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"></el-input>
        </el-form-item>

        <el-form-item v-if="platformOptions.length >= 1" :label-width="labelWidth"
                      :label="$t('test_track.issue.third_party_integrated')"
                      prop="platform">
          <el-select filterable v-model="form.platform">
            <el-option v-for="item in platformOptions" :key="item.value" :label="item.text" :value="item.value">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item :label-width="labelWidth" :label="$t('workspace.case_template_manage')" prop="caseTemplateId">
          <template-select :data="form" scene="API_CASE" prop="caseTemplateId" ref="caseTemplate"
                           :project-id="form.id"/>
        </el-form-item>

        <el-form-item :label-width="labelWidth"
                      :label="$t('workspace.issue_template_manage')" prop="issueTemplateId">
          <template-select :platform="form.platform" :data="form" scene="ISSUE" prop="issueTemplateId"
                           :disabled="form.platform === 'Jira' && form.thirdPartTemplate"
                           :platformOptions="issueOptions" :project-id="form.id"
                           ref="issueTemplate"/>

          <el-checkbox @change="thirdPartTemplateChange" v-if="form.platform === 'Jira'"
                       v-model="form.thirdPartTemplate" style="margin-left: 10px">
            {{ $t('test_track.issue.use_third_party') }}
          </el-checkbox>
        </el-form-item>


        <el-form-item :label="$t('system_custom_template.api_template')" :label-width="labelWidth" prop="apiTemplateId">
          <template-select ref="apiTemplate" :data="form" :project-id="form.id" prop="apiTemplateId"
                           scene="API"/>
        </el-form-item>

        <el-form-item :label-width="labelWidth" :label="$t('commons.description')" prop="description">
          <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item :label-width="labelWidth" :label="$t('project.tapd_id')" v-if="tapd">
          <el-input v-model="form.tapdId" autocomplete="off"></el-input>
          <el-button @click="check" type="primary" class="checkButton">
            {{ $t('test_track.issue.check_id_exist') }}
          </el-button>
        </el-form-item>

        <project-jira-config :result="jiraResult" v-if="jira" :label-width="labelWidth" :form="form" ref="jiraConfig">
          <template #checkBtn>
            <el-button @click="check" type="primary" class="checkButton">
              {{ $t('test_track.issue.check_id_exist') }}
            </el-button>
          </template>
        </project-jira-config>
        <el-form-item :label-width="labelWidth" :label="$t('project.zentao_id')" v-if="zentao">
          <el-input v-model="form.zentaoId" autocomplete="off"></el-input>
          <el-button @click="check" type="primary" class="checkButton">
            {{ $t('test_track.issue.check_id_exist') }}
          </el-button>
          <ms-instructions-icon effect="light">
            <template>
              禅道流程：产品-项目 | 产品-迭代 | 产品-冲刺 | 项目-迭代 | 项目-冲刺 <br/><br/>
              根据 "后台 -> 自定义 -> 流程" 查看对应流程，根据流程填写ID <br/><br/>
              产品-项目 | 产品-迭代 | 产品-冲刺 需要填写产品ID <br/><br/>
              项目-迭代 | 项目-冲刺 需要填写项目ID
            </template>
          </ms-instructions-icon>
        </el-form-item>
        <el-form-item :label-width="labelWidth" :label="$t('project.azureDevops_id')" v-if="azuredevops">
          <el-input v-model="form.azureDevopsId" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item :label-width="labelWidth" :label="$t('project.azureDevops_filter_id')" v-if="azuredevops">
          <el-input v-model="form.azureFilterId" autocomplete="off"/>
          <ms-instructions-icon content="非必填项，用例关联需求时，可以只筛选出，所填的 workItem 下的选项" effect="light"/>
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
  </div>
</template>

<script>

import {listenGoBack, removeGoBackListener} from "metersphere-frontend/src/utils";
import {operationConfirm} from "metersphere-frontend/src/utils";
import {
  getCurrentProjectID,
  getCurrentUser,
  getCurrentUserId,
  getCurrentWorkspaceId
} from "metersphere-frontend/src/utils/token";
import {AZURE_DEVOPS, JIRA, PROJECT_ID, TAPD, ZEN_TAO} from "metersphere-frontend/src/utils/constants";
import {PROJECT_CONFIGS} from "metersphere-frontend/src/components/search/search-components";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import TemplateSelect from "./TemplateSelect";
import MsTableButton from "metersphere-frontend/src/components/MsTableButton";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {ISSUE_PLATFORM_OPTION} from "metersphere-frontend/src/utils/table-constants";
import ProjectJiraConfig from "./ProjectJiraConfig";
import {
  getAllServiceIntegration,
  checkThirdPlatformProject,
  deleteProjectById,
  modifyProject,
  saveProject
} from "../../../api/project";
import {updateInfo} from "metersphere-frontend/src/api/user";

export default {
  name: "EditProject",
  components: {
    ProjectJiraConfig,
    MsInstructionsIcon,
    TemplateSelect,
    MsTableButton,
    MsTableOperatorButton,
    MsDeleteConfirm,
    MsTableOperator,
    MsTablePagination,
    MsTableHeader,
    MsDialogFooter
  },
  data() {
    return {
      screenHeight: 'calc(100vh - 155px)',
      labelWidth: '150px',
      createVisible: false,
      loading: false,
      jiraResult: {
        loading: false
      },
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
          {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'}
        ],
        description: [
          {max: 250, message: this.$t('commons.input_limit', [0, 250]), trigger: 'blur'}
        ],
      },
      platformOptions: [],
      issueOptions: [],
      issueTemplateId: "",
      ableEdit: true,
    };
  },
  props: {
    baseUrl: {
      type: String
    },
    isShowApp: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    currentUser: () => {
      return getCurrentUser();
    },
    tapd() {
      return this.showPlatform(TAPD);
    },
    jira() {
      return this.showPlatform(JIRA);
    },
    zentao() {
      return this.showPlatform(ZEN_TAO);
    },
    azuredevops() {
      return this.showPlatform(AZURE_DEVOPS);
    },
  },
  inject: ['reload'],
  destroyed() {
    this.createVisible = false;
  },
  methods: {
    showPlatform(platform) {
      return this.form.platform === platform
        && this.platformOptions.map(i => i.value).indexOf(platform) > -1;
    },
    check() {
      if (!this.form.id) {
        this.$warning(this.$t("test_track.issue.save_project_first"));
        return;
      }
      checkThirdPlatformProject(this.form).then(() => {
        this.$success("OK");
      });
    },
    getOptions() {
      if (this.$refs.issueTemplate) {
        this.$refs.issueTemplate.getTemplateOptions();
      }
      if (this.$refs.caseTemplate) {
        this.$refs.caseTemplate.getTemplateOptions();
      }
      if (this.$refs.apiTemplate) {
        this.$refs.apiTemplate.getTemplateOptions();
      }
    },
    thirdPartTemplateChange(val) {
      if (val)
        this.form.issueTemplateId = '';
    },
    edit(row) {
      this.getOptions();
      this.createVisible = true;
      listenGoBack(this.handleClose);
      if (row) {
        this.title = this.$t('project.edit');
        row.issueConfigObj = row.issueConfig ? JSON.parse(row.issueConfig) : {
          jiraIssueTypeId: null,
          jiraStoryTypeId: null
        };
        // 兼容性处理
        if (!row.issueConfigObj.jiraIssueTypeId) {
          row.issueConfigObj.jiraIssueTypeId = null;
        }
        if (!row.issueConfigObj.jiraStoryTypeId) {
          row.issueConfigObj.jiraStoryTypeId = null;
        }
        this.form = Object.assign({}, row);
        this.issueTemplateId = row.issueTemplateId;
      } else {
        this.form = {issueConfigObj: {jiraIssueTypeId: null, jiraStoryTypeId: null}};
      }
      if (this.$refs.jiraConfig) {
        this.$refs.jiraConfig.getIssueTypeOption(this.form);
      }
      this.platformOptions = [];
      this.platformOptions.push(...ISSUE_PLATFORM_OPTION);
      this.loading = getAllServiceIntegration().then(res => {
        let data = res.data;
        let platforms = data.map(d => d.platform);
        this.filterPlatformOptions(platforms, TAPD);
        this.filterPlatformOptions(platforms, JIRA);
        this.filterPlatformOptions(platforms, ZEN_TAO);
        this.filterPlatformOptions(platforms, AZURE_DEVOPS);
        this.issueOptions = this.platformOptions;
      }).catch(() => {
        this.ableEdit = false;
      })
    },
    filterPlatformOptions(platforms, platform) {
      if (platforms.indexOf(platform) === -1) {
        for (let i = 0; i < this.platformOptions.length; i++) {
          if (this.platformOptions[i].value === platform) {
            this.platformOptions.splice(i, 1);
            break;
          }
        }
      }
    },
    submit(formName) {
      this.$refs[formName].validate((valid) => {
        if (!valid || !this.ableEdit) {
          return false;
        }

        let protocol = document.location.protocol;
        protocol = protocol.substring(0, protocol.indexOf(":"));
        this.form.protocal = protocol;
        this.form.workspaceId = getCurrentWorkspaceId();
        this.form.createUser = getCurrentUserId();
        this.form.issueConfig = JSON.stringify(this.form.issueConfigObj);
        if (this.issueTemplateId !== this.form.issueTemplateId) {
          // 更换缺陷模版移除字段
          localStorage.removeItem("ISSUE_LIST");
        }

        let promise = this.form.id ? modifyProject(this.form) : saveProject(this.form);
        this.loading = promise.then(() => {
          this.createVisible = false;
          this.$success(this.$t('commons.save_success'));
          if (!this.form.id) {
            setTimeout(() => {
              window.location.reload();
            }, 400);
            return;
          }
          this.reload();
        });
      });
    },
    handleDelete(project) {
      this.$refs.deleteConfirm.open(project);
    },
    _handleDelete(project) {
      operationConfirm(this, this.$t('project.delete_tip'), () => {
        deleteProjectById(project.id).then(() => {
          if (project.id === getCurrentProjectID()) {
            localStorage.removeItem(PROJECT_ID);
            updateInfo({id: getCurrentUser().id, lastProjectId: ''});
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
      this.createVisible = false;
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

.el-input, .el-textarea {
  width: 80%;
}

.checkButton {
  margin-left: 5px;
}
</style>
