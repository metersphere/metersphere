<template>
  <div>
    <div style="width: 500px">
      <div style="margin-top: 20px;margin-bottom: 10px">{{ $t('organization.integration.basic_auth_info') }}</div>
      <el-form :model="form" ref="form" label-width="100px" size="small" :disabled="show" :rules="rules">
        <el-form-item :label="$t('organization.integration.account')" prop="account">
          <el-input v-model="form.account" :placeholder="$t('organization.integration.input_api_account')"/>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.password')" prop="password">
          <el-input v-model="form.password" auto-complete="new-password" v-if="showInput"
                    :placeholder="$t('organization.integration.input_api_password')" show-password/>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.jira_url')" prop="url">
          <el-input v-model="form.url" :placeholder="$t('organization.integration.input_jira_url')"/>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.jira_issuetype')" prop="issuetype">
          <el-input v-model="form.issuetype" :placeholder="$t('organization.integration.input_jira_issuetype')"/>
          <ms-instructions-icon effect="light">
            <template>
              <img class="jira-image" src="../../../../../assets/jira-type.png"/>
            </template>
          </ms-instructions-icon>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.jira_storytype')" prop="storytype">
          <el-input v-model="form.storytype" :placeholder="$t('organization.integration.input_jira_storytype')"/>
          <ms-instructions-icon effect="light">
            <template>
              <img class="jira-image" src="../../../../../assets/jira-type.png"/>
            </template>
          </ms-instructions-icon>
        </el-form-item>
      </el-form>
    </div>

    <bug-manage-btn @save="save"
                    @init="init"
                    :edit-permission="['ORGANIZATION_SERVICE:READ+EDIT']"
                    @testConnection="testConnection"
                    @cancelIntegration="cancelIntegration"
                    @reloadPassInput="reloadPassInput"
                    :form="form"
                    :show.sync="show"
                    ref="bugBtn"/>

    <div class="defect-tip">
      <div>{{ $t('organization.integration.use_tip') }}</div>
      <div>
        1. {{ $t('organization.integration.use_tip_jira') }}
      </div>
      <div>
        2. {{ $t('organization.integration.use_tip_two') }}
        <router-link to="/setting/project/all" style="margin-left: 5px">
          {{ $t('organization.integration.link_the_project_now') }}
        </router-link>
      </div>
      <div>
        3. {{ $t('organization.integration.use_tip_three') }}
        <router-link :to="{name: 'PersonSetting', params: { open: true }}" style="margin-left: 5px">
          {{ $t('organization.integration.link_the_info_now') }}
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import BugManageBtn from "@/business/components/settings/organization/components/BugManageBtn";
import {getCurrentOrganizationId, getCurrentUser} from "@/common/js/utils";
import {JIRA} from "@/common/js/constants";
import MsInstructionsIcon from "@/business/components/common/components/MsInstructionsIcon";

export default {
  name: "JiraSetting",
  components: {MsInstructionsIcon, BugManageBtn},
  created() {
    this.init();
  },
  data() {
    return {
      show: true,
      showInput: true,
      form: {},
      rules: {
        account: {
          required: true,
          message: this.$t('organization.integration.input_api_account'),
          trigger: ['change', 'blur']
        },
        password: {
          required: true,
          message: this.$t('organization.integration.input_api_password'),
          trigger: ['change', 'blur']
        },
        url: {
          required: true,
          message: this.$t('organization.integration.input_jira_url'),
          trigger: ['change', 'blur']
        },
        issuetype: {
          required: true,
          message: this.$t('organization.integration.input_jira_issuetype'),
          trigger: ['change', 'blur']
        },
        storytype: {
          required: true,
          message: this.$t('organization.integration.input_jira_storytype'),
          trigger: ['change', 'blur']
        }
      },
    }
  },
  methods: {
    init() {
      const {lastOrganizationId} = getCurrentUser();
      let param = {};
      param.platform = JIRA;
      param.orgId = lastOrganizationId;
      this.$parent.result = this.$post("service/integration/type", param, response => {
        let data = response.data;
        if (data.configuration) {
          let config = JSON.parse(data.configuration);
          this.$set(this.form, 'account', config.account);
          this.$set(this.form, 'password', config.password);
          this.$set(this.form, 'url', config.url);
          this.$set(this.form, 'issuetype', config.issuetype);
          this.$set(this.form, 'storytype', config.storytype);
        } else {
          this.clear();
        }
      })
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
            account: this.form.account,
            password: this.form.password,
            url: formatUrl,
            issuetype: this.form.issuetype,
            storytype: this.form.storytype
          };
          const {lastOrganizationId} = getCurrentUser();
          param.organizationId = lastOrganizationId;
          param.platform = JIRA;
          param.configuration = JSON.stringify(auth);
          this.$parent.result = this.$post("service/integration/save", param, () => {
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
      })
    },
    clear() {
      this.$set(this.form, 'account', '');
      this.$set(this.form, 'password', '');
      this.$set(this.form, 'url', '');
      this.$set(this.form, 'issuetype', '');
      this.$set(this.form, 'storytype', '');
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate();
        }
      });
    },
    testConnection() {
      if (this.form.account && this.form.password) {
        this.$parent.result = this.$get("issues/auth/" + getCurrentOrganizationId() + '/' + JIRA, () => {
          this.$success(this.$t('organization.integration.verified'));
        });
      } else {
        this.$warning(this.$t('organization.integration.not_integrated'));
        return false;
      }
    },
    cancelIntegration() {
      if (this.form.account && this.form.password) {
        this.$alert(this.$t('organization.integration.cancel_confirm') + JIRA + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              const {lastOrganizationId} = getCurrentUser();
              let param = {};
              param.orgId = lastOrganizationId;
              param.platform = JIRA;
              this.$parent.result = this.$post("service/integration/delete", param, () => {
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
    }
  }
}
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
