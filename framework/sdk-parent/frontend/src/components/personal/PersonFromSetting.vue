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
        <el-button @click="cancel">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="updateUser('updateUserForm')" @keydown.enter.native.prevent>{{ $t('commons.confirm') }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import MsDialogFooter from "../MsDialogFooter";
import {getUrlParams, removeGoBackListener} from "../../utils";
import MsTableOperatorButton from "../MsTableOperatorButton";
import {EMAIL_REGEX, PHONE_REGEX} from "../../utils/regex";
import {useUserStore} from "@/store";
import {updateInfo} from "../../api/user";

const userStore = useUserStore();

export default {
  name: "MsPersonFromSetting",
  components: {MsDialogFooter, MsTableOperatorButton},
  inject: [
    'reload',
    'reloadTopMenus'
  ],
  props: {
    form: {
      type: Object
    }
  },
  data() {
    return {
      result: {},
      isLocalUser: false,
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
  created() {
    let urlParams = getUrlParams(window.location.href);
    let oidcLoginUrl = urlParams['oidcLoginUrl']
    if (oidcLoginUrl) {
      localStorage.setItem('oidcLoginUrl', oidcLoginUrl);
    }
  },
  methods: {
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
          this.result = updateInfo(param)
            .then(response => {
              this.$success(this.$t('commons.modify_success'));
              userStore.$patch(response.data);
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
  }
};
</script>

<style scoped>
.ws-pj-class {
  cursor: pointer;
}
</style>
