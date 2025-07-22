<template>
  <div
    class="container"
    v-if="preheat"
    v-loading="preheat"
    :element-loading-text="$t('commons.login_info')"
    element-loading-spinner="el-icon-loading"></div>
  <div class="container" v-else>
    <el-row type="flex">
      <el-col :span="12">
        <div class="content">
          <div class="title">
            <div class="title-img">
              <img :src="'/display/file/loginLogo'" alt="" />
            </div>
            <div class="welcome">
              <span>{{ loginTitle || this.$t('commons.welcome') }}</span>
            </div>
          </div>
          <div class="form" v-if="!showQrCodeTab">
            <el-form :model="form" :rules="rules" ref="form">
              <el-form-item>
                <el-radio-group v-model="form.authenticate" @change="redirectAuth(form.authenticate)">
                  <el-radio label="LDAP" size="mini" v-if="openLdap">LDAP</el-radio>
                  <el-radio label="LOCAL" size="mini" v-if="openLdap">{{ $t('login.normal_Login') }}</el-radio>
                  <el-radio :label="auth.id" size="mini" v-for="auth in authSources" :key="auth.id"
                    >{{ auth.type }}
                    {{ auth.name }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item prop="username">
                <el-input
                  v-model="form.username"
                  :placeholder="$t('commons.login_username')"
                  autofocus
                  autocomplete="off" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input
                  v-model="form.password"
                  :placeholder="$t('commons.password')"
                  show-password
                  autocomplete="off"
                  maxlength="65"
                  show-word-limit />
              </el-form-item>
            </el-form>
          </div>
          <div v-if="showQrCodeTab">
            <tab-qr-code :tab-name="activeName ? activeName : orgOptions[0].value"></tab-qr-code>
          </div>
          <div class="btn" v-if="!showQrCodeTab">
            <el-button type="primary" class="submit" @click="submit('form')">
              {{ $t('commons.login') }}
            </el-button>
          </div>
          <el-divider v-xpack v-if="orgOptions.length > 0" class="login-divider"
            ><span style="color: #959598; font-size: 12px">更多登录方式</span></el-divider
          >
          <div v-xpack v-if="orgOptions.length > 0" class="loginType" @click="switchLoginType('QR_CODE')">
            <svg-icon v-if="!showQrCodeTab" icon-class="icon_scan_code" class-name="ms-icon" />
            <svg-icon v-if="showQrCodeTab" icon-class="icon_people" class-name="ms-icon" />
          </div>
          <div class="msg">
            {{ msg }}
          </div>
        </div>
      </el-col>

      <div class="divider" />

      <el-col :span="12">
        <div class="content">
          <img class="login-image" :src="'/display/file/loginImage'" alt="" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getCurrentUserId, publicKeyEncrypt } from '../../utils/token';
import { DEFAULT_LANGUAGE, PRIMARY_COLOR, UPLOAD_LIMIT } from '../../utils/constants';
import { hasLicense, hasPermissions, saveLicense } from '../../utils/permission';
import {
  checkLdapOpen,
  getAuthSource,
  getAuthSources,
  getDisplayInfo,
  getLanguage,
  getSystemTheme,
  saveBaseUrl,
} from '../../api/user';
import { useUserStore } from '@/store';
import { operationConfirm } from '../../utils';
import { getModuleList } from '../../api/module';
import { getLicense } from '../../api/license';
import { setLanguage } from '../../i18n';
import { getPlatformParamUrl } from '../../api/qrcode';
import tabQrCode from '../login/tabQrCode.vue';
import { getSystemBaseSetting } from '../../api/system';

const checkLicense = () => {
  return getLicense()
    .then((response) => {
      let data = response.data;
      if (!data || !data.status || !data.license || !data.license.count) {
        return;
      }
      saveLicense(data.status);
      if (data.status !== 'valid') {
        localStorage.setItem('setShowLicenseCountWarning', 'false');
      }
    })
    .catch((e) => {});
};

export default {
  name: 'Login',
  components: { tabQrCode },
  data() {
    return {
      loading: false,
      form: {
        username: '',
        password: '',
        authenticate: 'LOCAL',
      },
      preheat: true,
      rules: this.getDefaultRules(),
      msg: '',
      redirect: undefined,
      otherQuery: {},
      ready: false,
      openLdap: false,
      authSources: [],
      lastUser: sessionStorage.getItem('lastUser'),
      loginTitle: undefined,
      showQrCodeTab: false,
      activeName: '',
      orgOptions: [],
    };
  },
  watch: {
    $route: {
      handler: function (route) {
        const query = route.query;
        if (query) {
          this.redirect = query.redirect;
          this.otherQuery = this.getOtherQuery(query);
        }
      },
      immediate: true,
    },
  },
  beforeCreate() {
    const userStore = useUserStore();
    getSystemTheme().then((res) => {
      this.color = res.data ? res.data : PRIMARY_COLOR;
      document.body.style.setProperty('--primary_color', this.color);
    });

    // 保存当前站点url
    saveBaseUrl()
      .then(() => {})
      .catch(() => {});
    // ldap open
    checkLdapOpen('/ldap/open').then((response) => {
      this.openLdap = response.data;
    });
    getModuleList().then((response) => {
      let modules = {};
      response.data.forEach((m) => {
        modules[m.key] = m.status;
      });
      localStorage.setItem('modules', JSON.stringify(modules));
    });
    //
    checkLicense().then(() => {
      if (!hasLicense()) {
        return;
      }
      getAuthSources().then((response) => {
        this.authSources = response.data;
      });
      getDisplayInfo().then((response) => {
        if (response.data[3].paramValue) {
          this.loginTitle = response.data[3].paramValue;
        }

        if (response.data && response.data[6] && response.data[6].paramValue) {
          this.sysTitle = response.data[6].paramValue || 'MeterSphere';
          localStorage.setItem('default-sys-title', this.sysTitle);
        }

        let title = response.data[4].paramValue;
        if (title) {
          document.title = title;
        }
      });
    });

    userStore
      .getIsLogin()
      .then((res) => {
        this.preheat = true;
        this.getLanguage(res.data.language);
        this.setMaxUploadSize();
        window.location.href = '/';
      })
      .catch((data) => {
        this.preheat = false;
        // 保存公钥
        localStorage.setItem('publicKey', data.message);
        let lang = localStorage.getItem('language');
        if (lang) {
          setLanguage(lang);
          this.rules = this.getDefaultRules();
        }
        let url = localStorage.getItem('oidcLoginUrl');
        if (url) {
          window.location.href = url;
        }
      });
  },
  created: function () {
    document.addEventListener('keydown', this.watchEnter);
    if (hasLicense()) {
      this.initPlatformInfo();
    }
    this.activeName = localStorage.getItem('loginType') || 'WE_COM';
    let authenticate = localStorage.getItem('AuthenticateType');
    if (authenticate === 'LOCAL' || authenticate === 'LDAP') {
      this.form.authenticate = authenticate;
    }
  },

  destroyed() {
    document.removeEventListener('keydown', this.watchEnter);
  },
  methods: {
    watchEnter(e) {
      let keyCode = e.keyCode;
      if (keyCode === 13) {
        this.submit('form');
      }
    },
    submit(form) {
      localStorage.setItem('loginType', 'PASSWORD');
      this.$refs[form].validate((valid) => {
        if (valid) {
          this.doLogin();
        } else {
          return false;
        }
      });
    },
    getOtherQuery(query) {
      return Object.keys(query).reduce((acc, cur) => {
        if (cur !== 'redirect') {
          acc[cur] = query[cur];
        }
        return acc;
      }, {});
    },
    getDefaultRules() {
      // 设置完语言要重新赋值
      return {
        username: [{ required: true, message: this.$t('commons.input_login_username'), trigger: 'blur' }],
        password: [
          { required: true, message: this.$t('commons.input_password'), trigger: 'blur' },
          { min: 6, max: 65, message: this.$t('commons.input_limit', [6, 65]), trigger: 'blur' },
        ],
      };
    },
    checkRedirectUrl() {
      if (this.lastUser === getCurrentUserId()) {
        this.$router.push({ path: sessionStorage.getItem('redirectUrl') || '/' });
        return;
      }
      let redirectUrl = '/';
      if (
        hasPermissions(
          'PROJECT_USER:READ',
          'PROJECT_ENVIRONMENT:READ',
          'PROJECT_OPERATING_LOG:READ',
          'PROJECT_FILE:READ+JAR',
          'PROJECT_FILE:READ+FILE',
          'PROJECT_CUSTOM_CODE:READ',
          'PROJECT_MESSAGE:READ',
          'PROJECT_TEMPLATE:READ'
        )
      ) {
        redirectUrl = '/project/home';
      } else if (
        hasPermissions(
          'WORKSPACE_SERVICE:READ',
          'WORKSPACE_USER:READ',
          'WORKSPACE_PROJECT_MANAGER:READ',
          'WORKSPACE_PROJECT_ENVIRONMENT:READ',
          'WORKSPACE_OPERATING_LOG:READ'
        )
      ) {
        redirectUrl = '/setting/project/:type';
      } else if (
        hasPermissions(
          'SYSTEM_USER:READ',
          'SYSTEM_WORKSPACE:READ',
          'SYSTEM_GROUP:READ',
          'SYSTEM_TEST_POOL:READ',
          'SYSTEM_SETTING:READ',
          'SYSTEM_AUTH:READ',
          'SYSTEM_QUOTA:READ',
          'SYSTEM_OPERATING_LOG:READ'
        )
      ) {
        redirectUrl = '/setting';
      } else {
        redirectUrl = '/';
      }

      sessionStorage.setItem('redirectUrl', redirectUrl);
      sessionStorage.setItem('lastUser', getCurrentUserId());
      this.$router.push({ name: 'login_redirect', path: redirectUrl || '/', query: this.otherQuery });
    },
    doLogin() {
      const userStore = useUserStore();
      // 删除缓存
      sessionStorage.removeItem('changePassword');
      let publicKey = localStorage.getItem('publicKey');

      let form = {
        username: publicKeyEncrypt(this.form.username, publicKey),
        password: publicKeyEncrypt(this.form.password, publicKey),
        authenticate: this.form.authenticate,
      };

      userStore.userLogin(form).then((response) => {
        sessionStorage.setItem('loginSuccess', 'true');
        sessionStorage.setItem('changePassword', response.message);
        localStorage.setItem('AuthenticateType', this.form.authenticate);
        this.getLanguage(response.data.language);
        this.setMaxUploadSize();
        // 检查登录用户的权限
        this.checkRedirectUrl();
      });
    },
    getLanguage(language) {
      if (!language) {
        getLanguage().then((response) => {
          language = response.data;
          localStorage.setItem(DEFAULT_LANGUAGE, language);
        });
      }
    },
    setMaxUploadSize() {
      getSystemBaseSetting().then((res) => {
        let maxSize = res.data.maxSize;
        if (maxSize) {
          localStorage.setItem(UPLOAD_LIMIT, maxSize);
        }
      });
    },
    redirectAuth(authId) {
      if (authId === 'LDAP' || authId === 'LOCAL') {
        return;
      }
      getAuthSource(authId).then((res) => {
        if (!res || !res.data) {
          return;
        }
        if (res.data.status !== 'ENABLE') {
          this.$message.error(this.$t('login.auth_not_enable'));
          return;
        }
        let source = this.authSources.filter((auth) => auth.id === authId)[0];
        // 以前的cas登录
        if (source.type === 'CAS') {
          let config = JSON.parse(source.configuration);
          if (config.casServerUrl && !config.loginUrl) {
            return;
          }
        }
        operationConfirm(
          this,
          this.$t('commons.auth_redirect_tip'),
          () => {
            let config = JSON.parse(source.configuration);
            let redirectUrl = eval('`' + config.redirectUrl + '`');
            let url;
            if (source.type === 'CAS') {
              url = config.loginUrl + '?service=' + encodeURIComponent(redirectUrl);
            }
            if (source.type === 'OIDC') {
              url =
                config.authUrl +
                '?client_id=' +
                config.clientId +
                '&redirect_uri=' +
                redirectUrl +
                '&response_type=code&scope=openid+profile+email&state=' +
                authId;
              // 保存一个登录地址，禁用本地登录
              if (config.loginUrl) {
                localStorage.setItem('oidcLoginUrl', config.loginUrl);
              }
            }
            if (source.type === 'OAuth2') {
              url =
                config.authUrl +
                '?client_id=' +
                config.clientId +
                '&response_type=code' +
                '&redirect_uri=' +
                redirectUrl +
                '&state=' +
                authId;
              if (config.scope) {
                url += '&scope=' + config.scope;
              }
            }
            if (url) {
              window.location.href = url;
            }
          },
          () => {
            this.form.authenticate = 'LOCAL';
          }
        );
      });
    },
    async initPlatformInfo() {
      try {
        await getPlatformParamUrl().then((res) => {
          if (localStorage.getItem('loginType') && localStorage.getItem('loginType') !== 'PASSWORD') {
            this.showQrCodeTab = true;
            this.activeName = localStorage.getItem('loginType') || 'WE_COM';
          }
          this.orgOptions = res.data.map((e) => ({
            label: e.name,
            value: e.id,
          }));
        });
      } catch (error) {
        // eslint-disable-next-line no-console
        console.log(error);
      }
    },
    switchLoginType(type) {
      this.showQrCodeTab = this.showQrCodeTab === false;
    },
  },
};
</script>

<style scoped>
.container {
  width: 1200px;
  height: 730px;
  margin: 0 auto;
  background-color: #ffffff;
}

.content {
  margin-left: 10px;
}

.el-row--flex {
  height: 730px;
  margin-top: calc((100vh - 800px) / 2);
}

.el-col:nth-child(3) {
  align-items: center;
  display: flex;
}

.title img {
  width: 293px;
  max-height: 60px;
  margin-top: 165px;
}

.title-img {
  letter-spacing: 0;
  text-align: center;
}

.login-image {
  height: 365px;
  width: 567px;
  margin: auto;
  display: block;
}

.welcome {
  margin-top: 12px;
  margin-bottom: 75px;
  font-size: 14px;
  color: var(--primary_color);
  line-height: 14px;
  text-align: center;
}

.form,
.btn {
  padding: 0;
  width: 443px;
  margin: auto;
}

.btn > .submit {
  border-radius: 70px;
  border-color: var(--primary_color);
  background-color: var(--primary_color);
}

.btn > .submit:hover {
  border-color: var(--primary_color);
  background-color: var(--primary_color);
}

.btn > .submit:active {
  border-color: var(--primary_color);
  background-color: var(--primary_color);
}

.el-form-item:first-child {
  margin-top: 60px;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: var(--primary_color);
  background: var(--primary_color);
  border-color: var(--primary_color);
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: var(--primary_color);
}

:deep(.el-input__inner) {
  border-radius: 70px !important;
  background: #f6f3f8 !important;
  border-color: #f6f3f8 !important;
  /*谷歌浏览器默认填充的颜色无法替换，使用下列样式填充*/
  box-shadow: inset 0 0 0 1000px #f6f3f8 !important;
}

.el-input,
.el-button {
  width: 443px;
}

:deep(.el-input__inner:focus) {
  border: 1px solid var(--primary_color) !important;
}

.divider {
  border: 1px solid #f6f3f8;
  height: 480px;
  margin: 165px 0px;
}
.svg-icon.ms-icon {
  width: 18px;
  height: 18px;
  vertical-align: center;
  fill: var(--primary_color);
  border: 1px solid #ededf1;
  border-radius: 50%;
}
.login-divider {
  display: flex;
  margin: 26px auto;
  width: 480px;
}
.loginType {
  display: flex;
  align-items: center;
  align-content: center;
  flex-wrap: nowrap;
  flex-direction: row;
  justify-content: center;
}
</style>
