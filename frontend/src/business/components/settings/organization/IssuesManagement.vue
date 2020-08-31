<template>
  <div class="header-title" v-loading="result.loading">
    <div>
      <div>{{$t('organization.integration.select_defect_platform')}}</div>
      <el-radio-group v-model="platform" style="margin-top: 10px" @change="change">
        <el-radio label="Tapd">
          <img class="platform" src="../../../../assets/tapd.png" alt="Tapd"/>
        </el-radio>
        <el-radio label="Jira">
          <img class="platform" src="../../../../assets/jira.png" alt="Jira"/>
        </el-radio>
      </el-radio-group>
    </div>

    <div style="width: 500px">
      <div style="margin-top: 20px;margin-bottom: 10px">{{$t('organization.integration.basic_auth_info')}}</div>
      <el-form :model="form" ref="form" label-width="120px" size="small" :disabled="show" :rules="rules">
        <el-form-item :label="$t('organization.integration.api_account')" prop="account">
          <el-input v-model="form.account" :placeholder="$t('organization.integration.input_api_account')"/>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.api_password')" prop="password">
          <el-input v-model="form.password" auto-complete="new-password"
                    :placeholder="$t('organization.integration.input_api_password')" show-password/>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.jira_url')" prop="url" v-if="platform === 'Jira'">
          <el-input v-model="form.url" :placeholder="$t('organization.integration.input_jira_url')"/>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.jira_issuetype')" prop="issuetype" v-if="platform === 'Jira'">
          <el-input v-model="form.issuetype" :placeholder="$t('organization.integration.input_jira_issuetype')"/>
        </el-form-item>
      </el-form>
    </div>

    <div style="margin-left: 120px">
      <el-button type="primary" size="mini" :disabled="!show" @click="testConnection">{{$t('ldap.test_connect')}}
      </el-button>
      <el-button v-if="showEdit" size="mini" @click="edit">{{$t('commons.edit')}}</el-button>
      <el-button type="primary" v-if="showSave" size="mini" @click="save('form')">{{$t('commons.save')}}</el-button>
      <el-button v-if="showCancel" size="mini" @click="cancelEdit">{{$t('organization.integration.cancel_edit')}}
      </el-button>
      <el-button type="info" size="mini" @click="cancelIntegration('form')" :disabled="!show">
        {{$t('organization.integration.cancel_integration')}}
      </el-button>
    </div>

    <div class="defect-tip">
      <div>{{$t('organization.integration.use_tip')}}</div>
      <div>
        1. {{$t('organization.integration.use_tip_tapd')}}
      </div>
      <div>
        2. {{$t('organization.integration.use_tip_jira')}}
      </div>
      <div>
        3. {{$t('organization.integration.use_tip_two')}}
        <router-link to="/track/project/all" style="margin-left: 5px">
          {{$t('organization.integration.link_the_project_now')}}
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
  import {getCurrentUser} from "../../../../common/js/utils";

  export default {
    name: "IssuesManagement",
    data() {
      return {
        form: {},
        result: {},
        platform: '',
        orgId: '',
        show: true,
        showEdit: true,
        showSave: false,
        showCancel: false,
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
          }
        },
      }
    },
    created() {
      this.init(this.platform);
    },
    methods: {
      init(platform) {
        let param = {};
        param.platform = platform;
        param.orgId = getCurrentUser().lastOrganizationId;
        this.result = this.$post("service/integration/type", param, response => {
          let data = response.data;
          this.platform = data.platform;
          if (data.configuration) {
            let config = JSON.parse(data.configuration);
            this.$set(this.form, 'account', config.account);
            this.$set(this.form, 'password', config.password);
            this.$set(this.form, 'url', config.url);
            this.$set(this.form, 'issuetype', config.issuetype);
          } else {
            this.clear();
          }
        })
      },
      edit() {
        this.show = false;
        this.showEdit = false;
        this.showSave = true;
        this.showCancel = true;
      },
      cancelEdit() {
        this.showEdit = true;
        this.showCancel = false;
        this.showSave = false;
        this.show = true;
        this.init(this.platform);
      },
      cancelIntegration() {
        if (this.form.account && this.form.password && this.platform) {

          this.$alert(this.$t('organization.integration.cancel_confirm') + this.platform + "？", '', {
            confirmButtonText: this.$t('commons.confirm'),
            callback: (action) => {
              if (action === 'confirm') {
                let param = {};
                param.orgId = getCurrentUser().lastOrganizationId;
                param.platform = this.platform;
                this.result = this.$post("service/integration/delete", param, () => {
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
      save(form) {
        if (!this.platform) {
          this.$warning(this.$t('organization.integration.choose_platform'));
          return;
        }

        this.$refs[form].validate(valid => {
          if (valid) {

            let formatUrl = this.form.url;
            if (!formatUrl.endsWith('/')) {
              formatUrl = formatUrl + '/';
            }

            let param = {};
            let auth = {
              account: this.form.account,
              password: this.form.password,
              url: formatUrl,
              issuetype: this.form.issuetype
            };
            param.organizationId = getCurrentUser().lastOrganizationId;
            param.platform = this.platform;
            param.configuration = JSON.stringify(auth);

            this.result = this.$post("service/integration/save", param, () => {
              this.show = true;
              this.showEdit = true;
              this.showSave = false;
              this.showCancel = false;
              this.init(this.platform);
              this.$success(this.$t('commons.save_success'));
            });
          } else {
            return false;
          }
        })
      },
      change(platform) {
        this.show = true;
        this.showEdit = true;
        this.showCancel = false;
        this.showSave = false;
        let param = {};
        param.orgId = getCurrentUser().lastOrganizationId;
        param.platform = platform;
        this.result = this.$post("service/integration/type", param, response => {
          let data = response.data;
          if (data.configuration) {
            let config = JSON.parse(data.configuration);
            this.$set(this.form, 'account', config.account);
            this.$set(this.form, 'password', config.password);
            this.$set(this.form, 'url', config.url);
            this.$set(this.form, 'issuetype', config.issuetype);
          } else {
            this.clear();
          }
        })
      },
      clear() {
        this.$set(this.form, 'account', '');
        this.$set(this.form, 'password', '');
        this.$set(this.form, 'url', '');
        this.$set(this.form, 'issuetype', '');
        this.$nextTick(() => {
          this.$refs.form.clearValidate();
        });
      },
      testConnection() {
        if (this.form.account && this.form.password && this.platform) {
          this.result = this.$get("issues/auth/" + this.platform, () => {
            this.$success(this.$t('organization.integration.verified'));
          });
        } else {
          this.$warning(this.$t('organization.integration.not_integrated'));
          return false;
        }
      }
    }
  }
</script>

<style scoped>
  .header-title {
    padding: 10px 30px;
  }

  .defect-tip {
    background: #EDEDED;
    border: solid #E1E1E1 1px;
    margin: 10px 0;
    padding: 10px;
    border-radius: 3px;
  }

  .platform {
    height: 90px;
    vertical-align: middle
  }
</style>
