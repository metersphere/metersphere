<template>
  <div>
    <div style="width: 500px">
      <div style="margin-top: 20px;margin-bottom: 10px">{{ $t('organization.integration.basic_auth_info') }}</div>
      <el-form :model="form" ref="form" label-width="100px" size="small" :disabled="show" :rules="rules">
        <el-form-item :label="$t('organization.integration.api_account')" prop="account">
          <el-input v-model="form.account" :placeholder="$t('organization.integration.input_api_account')"/>
        </el-form-item>
        <el-form-item :label="$t('organization.integration.api_password')" prop="password">
          <el-input v-model="form.password" auto-complete="new-password" v-if="showInput"
                    :placeholder="$t('organization.integration.input_api_password')" show-password/>
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
        1. {{ $t('organization.integration.use_tip_tapd') }}
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
import {TAPD} from "@/common/js/constants";

export default {
  name: "TapdSetting.vue",
  components: {
    BugManageBtn
  },
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
        }
      },
    }
  },
  methods: {
    init() {
      const {lastOrganizationId} = getCurrentUser();
      let param = {};
      param.platform = TAPD;
      param.orgId = lastOrganizationId;
      this.$parent.result = this.$post("service/integration/type", param, response => {
        let data = response.data;
        if (data.configuration) {
          let config = JSON.parse(data.configuration);
          this.$set(this.form, 'account', config.account);
          this.$set(this.form, 'password', config.password);
        } else {
          this.clear();
        }
      })
    },
    save() {
      this.$refs['form'].validate(valid => {
        if (valid) {

          let param = {};
          let auth = {
            account: this.form.account,
            password: this.form.password,
          };
          const {lastOrganizationId} = getCurrentUser();
          param.organizationId = lastOrganizationId;
          param.platform = TAPD;
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
      this.$nextTick(() => {
        if (this.$refs.form) {
          this.$refs.form.clearValidate();
        }
      });
    },
    testConnection() {
      if (this.form.account && this.form.password) {
        this.$parent.result = this.$get("issues/auth/" + getCurrentOrganizationId() + '/' + TAPD, () => {
          this.$success(this.$t('organization.integration.verified'));
        });
      } else {
        this.$warning(this.$t('organization.integration.not_integrated'));
        return false;
      }
    },
    cancelIntegration() {
      if (this.form.account && this.form.password) {
        this.$alert(this.$t('organization.integration.cancel_confirm') + TAPD + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              const {lastOrganizationId} = getCurrentUser();
              let param = {};
              param.orgId = lastOrganizationId;
              param.platform = TAPD;
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

</style>
