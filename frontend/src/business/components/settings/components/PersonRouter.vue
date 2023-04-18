<template>
  <div>
    <el-tabs v-model="activeIndex" >
      <el-tab-pane  v-if="hasPermission('PERSONAL_INFORMATION:READ+EDIT')" name="commons.personal_setting" :label="$t('commons.personal_setting')" class="setting-item" ></el-tab-pane>
      <el-tab-pane  v-if="hasPermission('PERSONAL_INFORMATION:READ+API_KEYS')" name="commons.api_keys" :label="$t('commons.api_keys')" class="setting-item" ></el-tab-pane>
      <el-tab-pane  v-if="hasPermission('PERSONAL_INFORMATION:READ+EDIT_PASSWORD')" name="change_password" :label="$t('member.edit_password')" class="setting-item" ></el-tab-pane>
      <el-tab-pane  v-if="hasPermission('PERSONAL_INFORMATION:READ+THIRD_ACCOUNT')&&(hasJira||hasTapd||hasZentao||hasAzure)&&hasPermission('WORKSPACE_SERVICE:READ')" name="third_account" :label="$t('commons.third_account')" class="setting-item" ></el-tab-pane>
      <el-tab-pane  v-if="hasPermission('PERSONAL_INFORMATION:READ+UI_SETTING') && isXpack" name="commons.ui_setting" :label="$t('commons.ui_setting')" class="setting-item" ></el-tab-pane>
    </el-tabs>
    <ms-main-container>
      <ms-person-from-setting v-if="activeIndex==='commons.personal_setting'" :form = form @getPlatformInfo = "getPlatformInfo" @cancel = "cancel"   />
      <ms-api-keys v-if="activeIndex==='commons.api_keys'" />
      <password-info v-if="activeIndex==='change_password'" :rule-form = "ruleForm" @cancel="cancel" ></password-info>
      <ui-setting v-if="activeIndex==='commons.ui_setting'" @cancel="cancel"></ui-setting>
      <el-form v-if="activeIndex==='third_account'" >
        <jira-user-info @auth="handleAuth" v-if="hasJira" :data="currentPlatformInfo" />
        <tapd-user-info @auth="handleAuth" v-if="hasTapd" :data="currentPlatformInfo" />
        <zentao-user-info @auth="handleAuth" v-if="hasZentao" :data="currentPlatformInfo" />
        <azure-devops-user-info @auth="handleAuth" v-if="hasAzure" :data="currentPlatformInfo" />
        <el-form-item >
          <el-button @click="cancel">{{$t('commons.cancel')}}</el-button>
          <el-button type="primary" @click="updateUser('updateUserForm')" @keydown.enter.native.prevent>{{$t('commons.confirm')}}</el-button>
        </el-form-item>
      </el-form>
    </ms-main-container>

  </div>

</template>

<script>
  import MsPersonFromSetting from "@/business/components/settings/personal/PersonFromSetting";
  import MsApiKeys from "@/business/components/settings/personal/ApiKeys";
  import MsMainContainer from "@/business/components/common/components/MsMainContainer";
  import PasswordInfo from "@/business/components/settings/personal/PasswordInfo";
  import UiSetting from "@/business/components/settings/personal/UiSetting";
  import {getCurrentUser, getCurrentWorkspaceId, hasPermission, hasLicense} from "@/common/js/utils";
  import ZentaoUserInfo from "@/business/components/settings/personal/ZentaoUserInfo";
  import TapdUserInfo from "@/business/components/settings/personal/TapdUserInfo";
  import JiraUserInfo from "@/business/components/settings/personal/JiraUserInfo";
  import AzureDevopsUserInfo from "@/business/components/settings/personal/AzureDevopsUserInfo";
  import {getIntegrationService} from "@/network/workspace";
  import { TokenKey } from "@/common/js/constants";

  export default {
    name: "MsPersonRouter",
    components: {MsMainContainer,MsPersonFromSetting,MsApiKeys,PasswordInfo,ZentaoUserInfo, TapdUserInfo, JiraUserInfo, AzureDevopsUserInfo, UiSetting},
    inject: [
      'reload',
    ],
    data(){
      return{
        activeIndex: '',
        ruleForm:{},
        hasJira: false,
        hasTapd: false,
        hasZentao: false,
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
        result:{},
        workspaceList:[],
        projectList:[]
      }
    },
    mounted() {
      this.$EventBus.$on('siwtchActive', (item) => {
        if(item){
          this.activeIndex = item
        }
      })
      this.isXpack = hasLicense();
    },
    beforeDestroy(){
      this.$EventBus.$off("siwtchActive")
    },
    methods:{
      hasPermission,
      currentUser: () => {
        return getCurrentUser();
      },
      handleAuth(type) {
        let param = {...this.currentPlatformInfo};
        if(type==='Jira'){
          if(!param.jiraAccount){
            this.$error(this.$t('organization.integration.input_api_account'));
            return
          }else if(!param.jiraPassword){
            this.$error(this.$t('organization.integration.input_api_password'));
            return
          }

        }else if(type==='Zentao'){
          if(!param.zentaoUserName){
            this.$error(this.$t('organization.integration.input_api_account'));
            return
          }else if(!param.zentaoPassword){
            this.$error(this.$t('organization.integration.input_api_password'));
            return
          }
        }else if(type==='AzureDevops'){
          if(!param.azureDevopsPat){
            this.$error(this.$t('organization.integration.input_azure_pat'));
            return
          }
        }
        param.workspaceId = getCurrentWorkspaceId();
        param.platform = type;
        this.$parent.result = this.$post("issues/user/auth", param, () => {
          this.$success(this.$t('organization.integration.verified'));
        });
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
        this.result = getIntegrationService((data) => {
          let platforms = data.map(d => d.platform);
          if (platforms.indexOf("Tapd") !== -1) {
            this.hasTapd = true;
          }
          if (platforms.indexOf("Jira") !== -1) {
            this.hasJira = true;
          }
          if (platforms.indexOf("Zentao") !== -1) {
            this.hasZentao = true;
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
        this.result = this.$post(this.updatePath, param, response => {
          this.$success(this.$t('commons.modify_success'));
          localStorage.setItem(TokenKey, JSON.stringify(response.data));
          this.$emit('closeDialog', false);
          this.initTableData()
          this.reload();
        });
      },
      initTableData() {
        this.result = this.$get("/user/info", response => {
          let data = response.data;
          this.isLocalUser = response.data.source === 'LOCAL';
          let dataList = [];
          dataList[0] = data;
          this.form = data;
          this.getPlatformInfo(data);
          this.getWsAndPj();
        });
      },
      getWsAndPj(){
        this.$get("/user/get/ws_pj/" + encodeURIComponent(this.currentUser().id), response => {
          let data = response.data;
          this.workspaceList = data.workspace;
          this.projectList = data.project
        });
      },
      getActiveIndex(){
        if(hasPermission('PERSONAL_INFORMATION:READ+EDIT')){
          this.activeIndex = 'commons.personal_setting';
          return;
        }else if(hasPermission('PERSONAL_INFORMATION:READ+API_KEYS')){
          this.activeIndex = 'commons.api_keys';
          return;
        }else if(hasPermission('PERSONAL_INFORMATION:READ+EDIT_PASSWORD')){
          this.activeIndex = 'change_password';
          return;
        }else if(hasPermission('PERSONAL_INFORMATION:READ+THIRD_ACCOUNT')){
          this.activeIndex = 'third_account';
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

