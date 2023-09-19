<template>
  <div>
    <div style="width: 500px">
      <div style="margin-top: 20px;margin-bottom: 10px">{{ $t('organization.integration.basic_auth_info') }}</div>
      <el-form :model="form" ref="form" label-width="155px" size="small" :disabled="show" :rules="rules">
        <el-form-item :label="$t('organization.integration.azure_pat')" prop="pat">
          <el-input v-model="form.pat" auto-complete="new-password" v-if="showInput"
                    :placeholder="$t('organization.integration.input_azure_pat')" show-password/>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.azure_devops_url')" prop="url">
          <el-input v-model="form.url" :placeholder="$t('organization.integration.input_azure_url')"/>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.azure_organization_id')" prop="organization">
          <el-input v-model="form.organization"
                    :placeholder="$t('organization.integration.input_azure_organization_id')"/>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.azure_issuetype')" prop="issuetype">
          <el-input v-model="form.issuetype" :placeholder="$t('organization.integration.input_azure_issuetype')"/>
          <ms-instructions-icon effect="light">
            <template>
              <img class="jira-image" src="/assets/azureDevops-type.png"/>
            </template>
          </ms-instructions-icon>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.azure_storytype')" prop="storytype">
          <el-input v-model="form.storytype" :placeholder="$t('organization.integration.input_azure_storytype')"/>
          <ms-instructions-icon effect="light">
            <template>
              <img class="jira-image" src="/assets/azureDevops-type.png"/>
            </template>
          </ms-instructions-icon>
        </el-form-item>
      </el-form>
    </div>

    <bug-manage-btn @save="save"
                    @init="init"
                    :edit-permission="['WORKSPACE_SERVICE:READ+EDIT']"
                    @testConnection="testConnection"
                    @cancelIntegration="cancelIntegration"
                    @reloadPassInput="reloadPassInput"
                    :form="form"
                    :show.sync="show"
                    ref="bugBtn"/>

    <div class="defect-tip">
      <div>{{ $t('organization.integration.use_tip') }}</div>
      <div>
        1. {{ $t('organization.integration.use_tip_azure') }}
      </div>
      <div>
        2. {{ $t('organization.integration.use_tip_two') }}
        <router-link to="/setting/project/all" style="margin-left: 5px;color: #551A8B; text-decoration: underline; cursor: pointer">
          {{ $t('organization.integration.link_the_project_now') }}
        </router-link>
      </div>
      <div>
        3. {{ $t('organization.integration.use_tip_three') }}
        <span style="margin-left: 5px;color: #551A8B; text-decoration: underline; cursor: pointer" @click="resVisible = true">
          {{ $t('organization.integration.link_the_info_now') }}
        </span>
        <el-dialog :close-on-click-modal="false" width="80%"
                   :visible.sync="resVisible" destroy-on-close @close="closeDialog">
          <ms-person-router @closeDialog="closeDialog"/>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script>
import BugManageBtn from "./BugManageBtn";
import {getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {AZURE_DEVOPS} from "metersphere-frontend/src/utils/constants";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import MsPersonRouter from "metersphere-frontend/src/components/personal/PersonRouter";
import {
  authServiceIntegration,
  delServiceIntegration,
  getServiceIntegration,
  saveServiceIntegration
} from "../../../api/workspace";

export default {
  name: "AzureDevopsSetting",
  components: {MsInstructionsIcon, BugManageBtn, MsPersonRouter},
  created() {
    this.init();
  },
  data() {
    return {
      show: true,
      showInput: true,
      resVisible: false,
      form: {},
      rules: {
        pat: {
          required: true,
          message: this.$t('organization.integration.input_azure_pat'),
          trigger: ['change', 'blur']
        },
        url: {
          required: true,
          message: this.$t('organization.integration.input_azure_url'),
          trigger: ['change', 'blur']
        },
        organization: {
          required: true,
          message: this.$t('organization.integration.input_azure_organization_id'),
          trigger: ['change', 'blur']
        },
        issuetype: {
          required: true,
          message: this.$t('organization.integration.input_azure_issuetype'),
          trigger: ['change', 'blur']
        },
        storytype: {
          required: true,
          message: this.$t('organization.integration.input_azure_storytype'),
          trigger: ['change', 'blur']
        }
      },
    };
  },
  methods: {
    init() {
      const workspaceId = getCurrentWorkspaceId();
      let param = {};
      param.platform = AZURE_DEVOPS;
      param.workspaceId = workspaceId;
      this.$parent.loading = getServiceIntegration(param).then(res => {
        let data = res.data;
        if (data.configuration) {
          let config = JSON.parse(data.configuration);
          this.$set(this.form, 'pat', config.pat);
          this.$set(this.form, 'url', config.url);
          this.$set(this.form, 'organization', config.organization);
          this.$set(this.form, 'issuetype', config.issuetype);
          this.$set(this.form, 'storytype', config.storytype);
        } else {
          this.clear();
        }
      });
    },
    save() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          let formatUrl = this.form.url.trim();
          if (!formatUrl.endsWith('/')) {
            formatUrl = formatUrl + '/';
          }
          let param = {};
          let auth = {
            pat: this.form.pat,
            url: formatUrl,
            organization: this.form.organization,
            issuetype: this.form.issuetype,
            storytype: this.form.storytype
          };
          param.workspaceId = getCurrentWorkspaceId();
          param.platform = AZURE_DEVOPS;
          param.configuration = JSON.stringify(auth);
          this.$parent.loading = saveServiceIntegration(param).then(() => {
            this.show = true;
            this.$refs.bugBtn.showEdit = true;
            this.$refs.bugBtn.showSave = false;
            this.$refs.bugBtn.showCancel = false;
            this.reloadPassInput();
            this.init();
            this.$success(this.$t('commons.save_success'));
          });
        } else {
          return false;
        }
      });
    },
    clear() {
      this.$set(this.form, 'pat', '');
      this.$set(this.form, 'url', '');
      this.$set(this.form, 'organization', '');
      this.$set(this.form, 'issuetype', '');
      this.$set(this.form, 'storytype', '');
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate();
        }
      });
    },
    testConnection() {
      if (this.form.pat) {
        this.$parent.loading = authServiceIntegration(getCurrentWorkspaceId(), AZURE_DEVOPS).then(() => {
          this.$success(this.$t('organization.integration.verified'));
        });
      } else {
        this.$warning(this.$t('organization.integration.not_integrated'));
        return false;
      }
    },
    cancelIntegration() {
      if (this.form.url && this.form.pat) {
        this.$alert(this.$t('organization.integration.cancel_confirm') + AZURE_DEVOPS + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              const workspaceId = getCurrentWorkspaceId();
              let param = {};
              param.workspaceId = workspaceId;
              param.platform = AZURE_DEVOPS;
              this.$parent.loading = delServiceIntegration(param).then(() => {
                this.$success(this.$t('organization.integration.successful_operation'));
                this.init('');
              });
            }
          }
        });
      } else {
        this.$warning(this.$t('organization.integration.not_integrated'));
      }
    },
    reloadPassInput() {
      this.showInput = false;
      this.$nextTick(function () {
        this.showInput = true;
      });
    },
    closeDialog() {
      this.resVisible = false;
    }
  }
};
</script>

<style scoped>
.defect-tip {
  background: #EDEDED;
  border: solid #E1E1E1 1px;
  margin: 10px 0;
  padding: 10px;
  border-radius: 3px;
}

.el-input {
  width: 80%;
}
</style>
