<template>
  <div v-loading="loading">
    <el-tabs v-model="activeIndex">
      <el-tab-pane v-if="hasPermission('PERSONAL_INFORMATION:READ+EDIT')" name="commons.personal_setting"
                   :label="$t('commons.personal_setting')"
                   class="setting-item"></el-tab-pane>
      <el-tab-pane v-if="hasPermission('PERSONAL_INFORMATION:READ+API_KEYS')" name="commons.api_keys"
                   :label="$t('commons.api_keys')"
                   class="setting-item"></el-tab-pane>
      <el-tab-pane v-if="hasPermission('PERSONAL_INFORMATION:READ+EDIT_PASSWORD')" name="change_password"
                   :label="$t('member.edit_password')"
                   class="setting-item"></el-tab-pane>
      <el-tab-pane
        v-if="hasPermission('PERSONAL_INFORMATION:READ+THIRD_ACCOUNT')
        &&(platformAccountConfigs.length > 0 || hasTapd || hasAzure) && hasPermission('WORKSPACE_SERVICE:READ')"
        name="third_account" :label="$t('commons.third_account')" class="setting-item"></el-tab-pane>
      <el-tab-pane v-if="hasPermission('PERSONAL_INFORMATION:READ+UI_SETTING') && isXpack" name="commons.ui_setting"
                   :label="$t('commons.ui_setting')"
                   class="setting-item"></el-tab-pane>
    </el-tabs>
    <ms-main-container>
      <ms-person-from-setting v-if="activeIndex==='commons.personal_setting'" :form=form
                              @getPlatformInfo="getPlatformInfo" @cancel="cancel"/>
      <ms-api-keys v-if="activeIndex==='commons.api_keys'"/>
      <password-info v-if="activeIndex==='change_password'" :rule-form="ruleForm" @cancel="cancel"></password-info>
      <ui-setting v-if="activeIndex==='commons.ui_setting'" @cancel="cancel"></ui-setting>
      <el-form v-if="activeIndex==='third_account'">
        <div v-for="config in platformAccountConfigs" :key="config.key">
          <platform-account-config
            :config="config"
            :account-config="currentPlatformInfo"
            v-if="showPlatformConfig(config.key)"
          />
        </div>
        <tapd-user-info @auth="handleAuth" v-if="hasTapd" :data="currentPlatformInfo"/>
        <azure-devops-user-info @auth="handleAuth" v-if="hasAzure" :data="currentPlatformInfo"/>
        <el-form-item class="el-form-item-class">
          <el-button size="small" @click="cancel">{{ $t('commons.cancel') }}</el-button>
          <el-button size="small" type="primary" @click="updateUser('updateUserForm')" @keydown.enter.native.prevent>
            {{ $t('commons.confirm') }}
          </el-button>
        </el-form-item>
      </el-form>
    </ms-main-container>

  </div>

</template>

<script>
import MsPersonFromSetting from "./PersonFromSetting";
import MsApiKeys from "./ApiKeys";
import MsMainContainer from "../../components/MsMainContainer";
import PasswordInfo from "./PasswordInfo";
import UiSetting from "./UiSetting";
import {getCurrentUser, getCurrentWorkspaceId} from "../../utils/token";
import {hasLicense, hasPermission} from "../../utils/permission";
import TapdUserInfo from "./TapdUserInfo";
import AzureDevopsUserInfo from "./AzureDevopsUserInfo";
import {getIntegrationService} from "../../api/workspace";
import {useUserStore} from "@/store";
import {handleAuth as _handleAuth, getUserInfo, getWsAndPj, updateInfo} from "../../api/user";
import PlatformAccountConfig from "./PlatformAccountConfig";
import {getPlatformAccountInfo} from "../../api/platform-plugin";
import {ISSUE_PLATFORM_OPTION} from "../../utils/table-constants";


const userStore = useUserStore();

export default {
  name: "MsPersonRouter",
  components: {
    PlatformAccountConfig,
    MsMainContainer,
    MsPersonFromSetting,
    MsApiKeys,
    PasswordInfo,
    TapdUserInfo,
    AzureDevopsUserInfo,
    UiSetting
  },
  inject: [
    'reload',
  ],
  data() {
    return {
      activeIndex: '',
      ruleForm: {},
      hasTapd: false,
      hasAzure: false,
      isXpack: false,
      updatePath: '/user/update/current',
      form: {platformInfo: {}},
      currentPlatformInfo: {
        jiraAccount: '',
        jiraPassword: '',
        tapdUserName: '',
        zentaoUserName: '',
        zentaoPassword: '',
        azureDevopsPat: ''
      },
      platformAccountConfigs: [],
      result: {},
      loading: false,
      workspaceList: [],
      projectList: []
    }
  },
  mounted() {
    this.$EventBus.$on('siwtchActive', (item) => {
      if (item) {
        this.activeIndex = item
      }
    })
    this.isXpack = hasLicense();
  },
  beforeDestroy() {
    this.$EventBus.$off("siwtchActive")
  },
  methods: {
    hasPermission,
    currentUser: () => {
      return getCurrentUser();
    },
    showPlatformConfig(platform) {
      return ISSUE_PLATFORM_OPTION.map(item => item.value).indexOf(platform) < 0;
    },
    handleAuth(type) {
      let param = {...this.currentPlatformInfo};
      if (type === 'AzureDevops') {
        if (!param.azureDevopsPat) {
          this.$error(this.$t('organization.integration.input_azure_pat'));
          return
        }
      }
      param.workspaceId = getCurrentWorkspaceId();
      param.platform = type;
      this.$parent.result = _handleAuth(param).then(() => {
        this.$success(this.$t('organization.integration.verified'));
      })
    },
    getPlatformInfo(row) {
      let orgId = getCurrentWorkspaceId();
      if (row.platformInfo && row.platformInfo !== 'null') {
        this.form = row;
        if (!row.platformInfo[orgId]) {
          this.form.platformInfo = JSON.parse(row.platformInfo);
        }
      } else {
        this.form.platformInfo = {};
      }
      if (!this.form.platformInfo[orgId]) {
        this.form.platformInfo[orgId] = {};
      }
      this.currentPlatformInfo = this.form.platformInfo[orgId];
      this.result = getIntegrationService()
        .then(({data}) => {
          let platforms = data.map(d => d.platform);
          if (platforms.indexOf("Tapd") !== -1) {
            this.hasTapd = true;
          }
          if (platforms.indexOf("AzureDevops") !== -1) {
            this.hasAzure = true;
          }
        });
    },
    cancel() {
      this.$emit('closeDialog', false);
    },
    updateUser(updateUserForm) {
      let param = {};
      Object.assign(param, this.form);
      param.platformInfo = JSON.stringify(this.form.platformInfo);
      this.loading = updateInfo(param).then(response => {
        this.$success(this.$t('commons.modify_success'));
        userStore.$patch(response);
        this.$emit('closeDialog', false);
        this.initTableData()
        this.reload();
      });
    },
    initTableData() {
      getPlatformAccountInfo()
        .then((r) => {
          this.platformAccountConfigs = r.data;
        });
      this.result = getUserInfo()
        .then(response => {
          let data = response.data;
          this.isLocalUser = response.data.source === 'LOCAL';
          let dataList = [];
          dataList[0] = data;
          this.form = data;
          this.getPlatformInfo(data);
          this.getWsAndPj();
        })
    },
    getWsAndPj() {
      getWsAndPj()
        .then(response => {
          let data = response.data;
          this.workspaceList = data.workspace;
          this.projectList = data.project
        });
    },
    getActiveIndex() {
      if (hasPermission('PERSONAL_INFORMATION:READ+EDIT')) {
        this.activeIndex = 'commons.personal_setting';
        return;
      } else if (hasPermission('PERSONAL_INFORMATION:READ+API_KEYS')) {
        this.activeIndex = 'commons.api_keys';
        return;
      } else if (hasPermission('PERSONAL_INFORMATION:READ+EDIT_PASSWORD')) {
        this.activeIndex = 'change_password';
        return;
      } else if (hasPermission('PERSONAL_INFORMATION:READ+THIRD_ACCOUNT')) {
        this.activeIndex = 'third_account';
        return;
      } else if (hasPermission('PERSONAL_INFORMATION:READ+UI_SETTING')) {
        this.activeIndex = 'commons.ui_setting';
        return;
      }
    },
  },
  created() {
    this.getActiveIndex();
    this.initTableData();
  }

}
</script>

<style scoped>
.setting-item {
  height: 40px;
  line-height: 40px;
}

.el-form-item-class {
  margin-left: 110px;
}
</style>
