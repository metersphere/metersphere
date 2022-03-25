<template>
  <div v-loading="result.loading">
    <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule"
             ref="updateUserForm">
      <el-form-item label="ID" prop="id">
        <el-input v-model="form.id" autocomplete="off" :disabled="true"/>
      </el-form-item>
      <el-form-item :label="$t('commons.username')" prop="name">
        <el-input v-model="form.name" autocomplete="off"/>
      </el-form-item>
      <el-form-item :label="$t('commons.email')" prop="email">
        <el-input v-model="form.email" autocomplete="off" type="text" maxlength="60" show-word-limit/>
      </el-form-item>
      <el-form-item :label="$t('commons.phone')" prop="phone">
        <el-input v-model="form.phone" autocomplete="off"/>
      </el-form-item>
      <el-form-item>
        <el-button @click="cancel">{{$t('commons.cancel')}}</el-button>
        <el-button type="primary" @click="updateUser('updateUserForm')" @keydown.enter.native.prevent>{{$t('commons.confirm')}}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import {PROJECT_ID, TokenKey, WORKSPACE_ID} from "@/common/js/constants";
import MsDialogFooter from "../../common/components/MsDialogFooter";
import {
  fullScreenLoading, getCurrentProjectID,
  getCurrentUser,
  getCurrentWorkspaceId,
  removeGoBackListener, saveLocalStorage, stopFullScreenLoading
} from "@/common/js/utils";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import {EMAIL_REGEX, PHONE_REGEX} from "@/common/js/regex";
import JiraUserInfo from "@/business/components/settings/personal/JiraUserInfo";
import TapdUserInfo from "@/business/components/settings/personal/TapdUserInfo";
import ZentaoUserInfo from "@/business/components/settings/personal/ZentaoUserInfo";
import AzureDevopsUserInfo from "@/business/components/settings/personal/AzureDevopsUserInfo";

export default {
  name: "MsPersonFromSetting",
  components: {ZentaoUserInfo, TapdUserInfo, JiraUserInfo, AzureDevopsUserInfo, MsDialogFooter, MsTableOperatorButton},
  inject: [
    'reload',
    'reloadTopMenus'
  ],
  props:{
    form:{
      type:Object
    }
  },
  data() {
    return {
      result: {},
      isLocalUser: false,
      updatePath: '/user/update/current',
      ruleForm: {},
      rule: {
        name: [
          {required: true, message: this.$t('member.input_name'), trigger: 'blur'},
          {min: 2, max: 20, message: this.$t('commons.input_limit', [2, 20]), trigger: 'blur'},
          {
            required: true,
            pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
            message: this.$t('member.special_characters_are_not_supported'),
            trigger: 'blur'
          }
        ],
        phone: [
          {
            pattern: PHONE_REGEX,
            message: this.$t('member.mobile_number_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
        email: [
          {required: true, message: this.$t('member.input_email'), trigger: 'blur'},
          {
            required: true,
            pattern: EMAIL_REGEX,
            message: this.$t('member.email_format_is_incorrect'),
            trigger: 'blur'
          }
        ],
      },
    };
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
    cancel() {
      this.$emit("cancel");
    },
    updateUser(updateUserForm) {
      this.$refs[updateUserForm].validate(valid => {
        if (valid) {
          let param = {};
          this.$emit('getPlatformInfo', this.form);
          Object.assign(param, this.form);
          param.platformInfo = JSON.stringify(this.form.platformInfo);
          this.result = this.$post(this.updatePath, param, response => {
            this.$success(this.$t('commons.modify_success'));
            localStorage.setItem(TokenKey, JSON.stringify(response.data));
            this.reload();
          });
        } else {
          return false;
        }
      });
    },
    handleClose() {
      this.form = {};
      this.ruleForm = {};
      removeGoBackListener(this.handleClose);
    },
    getRedirectUrl(user) {
      if (!user.lastProjectId || !user.lastWorkspaceId) {
        // 没有项目级的权限直接回到 /
        // 只是某一个工作空间的用户组也转到 /
        return "/";
      }
      let redirectUrl = sessionStorage.getItem('redirectUrl');
      if (redirectUrl.startsWith("/")) {
        redirectUrl = redirectUrl.substring(1);
      }
      redirectUrl = redirectUrl.split("/")[0];
      return '/' + redirectUrl + '/';
    },
    changeWorkspace(workspace){

      let workspaceId = workspace.id;

      if (!workspaceId || getCurrentWorkspaceId() === workspaceId) {
        return false;
      }
      this.$emit('cancel', false);
      const loading = fullScreenLoading(this);
      this.$post("/user/switch/source/ws/" + workspaceId, {}, response => {
        saveLocalStorage(response);
        sessionStorage.setItem(WORKSPACE_ID, workspaceId);
        sessionStorage.setItem(PROJECT_ID, response.data.lastProjectId);
        this.$router.push(this.getRedirectUrl(response.data)).then(() => {
          this.reloadTopMenus(stopFullScreenLoading(loading));
          this.reload();
        }).catch(err => err);
      });
    },
    changeProject(project){
      let projectId = project.id;
      let currentProjectId = getCurrentProjectID();
      if (projectId === currentProjectId) {
        return;
      }

      const loading = fullScreenLoading(this);
      this.$post("/user/update/current", {id: this.currentUser().id, lastProjectId: projectId }, (response) => {
        saveLocalStorage(response);
        this.currentProjectId = projectId;

        this.$EventBus.$emit('projectChange');
        // 保存session里的projectId
        sessionStorage.setItem(PROJECT_ID, projectId);
        // 刷新路由
        this.reload();
        stopFullScreenLoading(loading, 1500);
        // this.changeProjectName(projectId);
      }, () => {
        stopFullScreenLoading(loading, 1500);
      });
    }
  }
};
</script>

<style scoped>
.ws-pj-class{
  cursor: pointer;
}
</style>
