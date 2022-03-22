<template>
  <div v-loading="result.loading">
    <el-dialog :close-on-click-modal="false" :title="title" :visible.sync="createVisible" destroy-on-close
               @close="handleClose">
      <el-form :model="form" :rules="rules" ref="form" label-position="right" label-width="80px" size="small">
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
          <template-select :data="form" scene="API_CASE" prop="caseTemplateId" ref="caseTemplate" :project-id="form.id"/>
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

        <el-form-item :label-width="labelWidth" :label="$t('commons.description')" prop="description">
          <el-input :autosize="{ minRows: 2, maxRows: 4}" type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item :label-width="labelWidth" :label="$t('project.tapd_id')" v-if="tapd">
          <el-input v-model="form.tapdId" autocomplete="off"></el-input>
          <el-button @click="check" type="primary" class="checkButton">{{ $t('test_track.issue.check_id_exist') }}</el-button>
        </el-form-item>

        <project-jira-config v-if="jira" :label-width="labelWidth" :form="form">
          <template #checkBtn>
            <el-button @click="check" type="primary" class="checkButton">{{ $t('test_track.issue.check_id_exist') }}</el-button>
          </template>
        </project-jira-config>
        <el-form-item :label-width="labelWidth" :label="$t('project.zentao_id')" v-if="zentao">
          <el-input v-model="form.zentaoId" autocomplete="off"></el-input>
          <el-button @click="check" type="primary" class="checkButton">{{ $t('test_track.issue.check_id_exist') }}</el-button>
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

import {
  getCurrentProjectID,
  getCurrentUser,
  getCurrentUserId,
  getCurrentWorkspaceId,
  listenGoBack,
  removeGoBackListener
} from "@/common/js/utils";

import {AZURE_DEVOPS, JIRA, PROJECT_ID, TAPD, ZEN_TAO} from "@/common/js/constants";
import {PROJECT_CONFIGS} from "@/business/components/common/components/search/search-components";
import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";
import TemplateSelect from "@/business/components/project/template/TemplateSelect";
import MsResourceFiles from "@/business/components/performance/test/components/ResourceFiles";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsJarConfig from "@/business/components/api/test/components/jar/JarConfig";
import TemplateComponent
  from "@/business/components/track/plan/view/comonents/report/TemplateComponent/TemplateComponent";
import ApiEnvironmentConfig from "@/business/components/api/test/components/ApiEnvironmentConfig";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import MsDeleteConfirm from "@/business/components/common/components/MsDeleteConfirm";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsTableOperator from "@/business/components/common/components/MsTableOperator";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import {ISSUE_PLATFORM_OPTION} from "@/common/js/table-constants";
import ProjectJiraConfig from "@/business/components/project/menu/components/ProjectJiraConfig";

export default {
  name: "EditProject",
  components: {
    ProjectJiraConfig,
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
    MsContainer, MsTableOperator, MsTablePagination, MsTableHeader, MsDialogFooter
  },
  data() {
    return {
      createVisible: false,
      result: {},
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
        // caseTemplateId: [{required: true}],
        // issueTemplateId: [{required: true}],
      },
      screenHeight: 'calc(100vh - 195px)',
      labelWidth: '150px',
      platformOptions: [],
      issueOptions: []
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
      return this.form.platform === TAPD && this.platformOptions.map(i => i.value).indexOf(TAPD) > -1;
    },
    jira() {
      return this.form.platform === JIRA && this.platformOptions.map(i => i.value).indexOf(JIRA) > -1;
    },
    zentao() {
      return this.form.platform === ZEN_TAO && this.platformOptions.map(i => i.value).indexOf(ZEN_TAO) > -1;
    },
    azuredevops() {
      return this.form.platform === AZURE_DEVOPS && this.platformOptions.map(i => i.value).indexOf(AZURE_DEVOPS) > -1;
    },
  },
  inject: [
    'reload'
  ],
  destroyed() {
    this.createVisible = false;
  },
  methods: {
    check() {
      if (!this.form.id) {
        this.$warning(this.$t("test_track.issue.save_project_first"));
        return;
      }
      this.$post("/project/check/third/project", this.form, () => {
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
        row.issueConfigObj = row.issueConfig ? JSON.parse(row.issueConfig) : {};
        this.form = Object.assign({}, row);
      } else {
        this.form = {issueConfigObj: {}};
      }
      this.platformOptions = [];
      this.platformOptions.push(...ISSUE_PLATFORM_OPTION);
      this.$get("/service/integration/all/" + getCurrentUser().lastWorkspaceId, response => {
        let data = response.data;
        let platforms = data.map(d => d.platform);
        this.filterPlatformOptions(platforms, TAPD);
        this.filterPlatformOptions(platforms, JIRA);
        this.filterPlatformOptions(platforms, ZEN_TAO);
        this.filterPlatformOptions(platforms, AZURE_DEVOPS);
        this.issueOptions = this.platformOptions;
      });
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
        if (valid) {
          let saveType = "add";
          if (this.form.id) {
            saveType = "update";
          }
          let protocol = document.location.protocol;
          protocol = protocol.substring(0, protocol.indexOf(":"));
          this.form.protocal = protocol;
          this.form.workspaceId = getCurrentWorkspaceId();
          this.form.createUser = getCurrentUserId();
          this.form.issueConfig = JSON.stringify(this.form.issueConfigObj);
          this.result = this.$post("/project/" + saveType, this.form, () => {
            this.createVisible = false;
            this.reload();
            this.$success(this.$t('commons.save_success'));
          });
        } else {
          return false;
        }
      });
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
          this.$success(this.$t('commons.delete_success'));
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
    },
    chengeMockTcpSwitch(value) {
      if (value && this.form.mockTcpPort === 0) {
        this.result = this.$get('/project/genTcpMockPort/' + this.form.id, res => {
          let port = res.data;
          this.form.mockTcpPort = port;
        })
      }
    }
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
