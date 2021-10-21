<template>
  <div v-loading="result.loading">
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

        <el-form-item :label-width="labelWidth" label="TCP Mock Port">
          <el-input-number v-model="form.mockTcpPort" :controls="false" style="width: 30%;margin-right: 30px"></el-input-number>
          <el-switch v-model="form.isMockTcpOpen" @change="chengeMockTcpSwitch"></el-switch>
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
        <el-form-item :label-width="labelWidth" :label="$t('project.azureDevops_id')" v-if="azuredevops">
          <el-input v-model="form.azureDevopsId" autocomplete="off"></el-input>
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
  </div>
</template>

<script>

import {
  getCurrentProjectID,
  getCurrentUser, getCurrentUserId,
  getCurrentWorkspaceId,
  listenGoBack,
  removeGoBackListener
} from "@/common/js/utils";

import {PROJECT_ID} from "@/common/js/constants";
import {PROJECT_CONFIGS} from "@/business/components/common/components/search/search-components";
import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";
import TemplateSelect from "@/business/components/settings/workspace/template/TemplateSelect";
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
import MsCreateBox from "@/business/components/settings/CreateBox";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";

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
      azuredevops: false,
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
  },
  computed: {
    currentUser: () => {
      return getCurrentUser();
    }
  },
  inject: [
    'reload'
  ],
  destroyed() {
    this.createVisible = false;
  },
  methods: {
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
      this.$get("/service/integration/all/" + getCurrentUser().lastWorkspaceId, response => {
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
        if (platforms.indexOf("AzureDevops") !== -1) {
          this.azuredevops = true;
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
          let protocol = document.location.protocol;
          protocol = protocol.substring(0, protocol.indexOf(":"));
          this.form.protocal = protocol;
          this.form.workspaceId = getCurrentWorkspaceId();
          this.form.createUser = getCurrentUserId();
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
      this.tapd = false;
      this.jira = false;
      this.zentao = false;
      this.azuredevops = false;
    },
    chengeMockTcpSwitch(value){
      if(value && this.form.mockTcpPort === 0){
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

.el-input,.el-textarea {
  width: 95%;
}
</style>
