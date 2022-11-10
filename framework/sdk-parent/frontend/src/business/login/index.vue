<template>
  <div class="container">
    <el-row type="flex">
      <el-col :span="12">
        <div class="content">
          <div class="title">
            <div class="title-img">
              <img :src="'/display/file/loginLogo'" alt="">
            </div>
            <div class="welcome">
              <span>{{ loginTitle }}</span>
            </div>
          </div>
          <div class="form">
            <el-form :model="form" :rules="rules" ref="form">
              <el-form-item>
                <el-radio-group v-model="form.authenticate" @change="redirectAuth(form.authenticate)">
                  <el-radio label="LDAP" size="mini" v-if="openLdap">LDAP</el-radio>
                  <el-radio label="LOCAL" size="mini" v-if="openLdap">{{ $t('login.normal_Login') }}</el-radio>
                  <el-radio :label="auth.id" size="mini" v-for="auth in authSources" :key="auth.id">{{ auth.type }}
                    {{ auth.name }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item prop="username">
                <el-input v-model="form.username" :placeholder="$t('commons.login_username')" autofocus
                          autocomplete="off"/>
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="form.password" :placeholder="$t('commons.password')" show-password
                          autocomplete="off"
                          maxlength="30" show-word-limit/>
              </el-form-item>
            </el-form>
          </div>
          <div class="btn">
            <el-button type="primary" class="submit" @click="submit('form')">
              {{ $t('commons.login') }}
            </el-button>
          </div>
          <div class="msg">
            {{ msg }}
          </div>
        </div>
      </el-col>

      <div class="divider"/>

      <el-col :span="12">
        <div class="content">
          <img class="login-image" :src="'/display/file/loginImage'" alt="">
        </div>
      </el-col>

    </el-row>

  </div>
</template>

<script>
import {getCurrentUserId, publicKeyEncrypt} from "../../utils/token";
import {CURRENT_LANGUAGE, DEFAULT_LANGUAGE, PRIMARY_COLOR} from "../../utils/constants";
import {hasLicense, hasPermissions, saveLicense} from "../../utils/permission";
import {checkLdapOpen, getAuthSources, getDisplayInfo, getLanguage, getSystemTheme, saveBaseUrl} from "../../api/user";
import {useUserStore} from "@/store"
import {checkMicroMode, operationConfirm} from "../../utils";
import {getModuleList} from "../../api/module";
import {getLicense} from "../../api/license";
import {setLanguage} from "../../i18n";

const checkLicense = () => {
  return getLicense()
    .then(response => {
      let data = response.data;
      if (!data || !data.status || !data.license || !data.license.count) {
        return;
      }
      saveLicense(data.status)
      if (data.status !== 'valid') {
        localStorage.setItem('setShowLicenseCountWarning', "false");
      }
    })
    .catch(e => {
    })
}

export default {
  name: "Login",
  data() {
    return {
      loading: false,
      form: {
        username: '',
        password: '',
        authenticate: 'LOCAL'
      },
      rules: this.getDefaultRules(),
      msg: '',
      redirect: undefined,
      otherQuery: {},
      ready: false,
      openLdap: false,
      authSources: [],
      lastUser: sessionStorage.getItem('lastUser'),
      loginTitle: this.$t('commons.welcome')
    }
  },
  watch: {
    $route: {
      handler: function (route) {
        const query = route.query
        if (query) {
          this.redirect = query.redirect
          this.otherQuery = this.getOtherQuery(query)
        }
      },
      immediate: true
    }
  },
  beforeCreate() {
    const userStore = useUserStore()
    getSystemTheme()
      .then(res => {
        this.color = res.data ? res.data : PRIMARY_COLOR;
        document.body.style.setProperty('--primary_color', this.color);
      });

    // 保存当前站点url
    saveBaseUrl()
      .then(() => {
      })
      .catch(() => {
      });
    // ldap open
    // 网关统一登录，本地不再提供这个登录方式
    if (checkMicroMode()) {
      checkLdapOpen("/ldap/open")
        .then(response => {
          this.openLdap = response.data;
        });
    }
    //
    checkLicense()
      .then(() => {
        if (!hasLicense()) {
          return;
        }
        // 网关统一sso登录，本地不再提供这个登录方式
        if (checkMicroMode()) {
          getAuthSources()
            .then(response => {
              this.authSources = response.data;
            });
        }
        getDisplayInfo()
          .then(response => {
            if (response.data[3].paramValue) {
              this.loginTitle = response.data[3].paramValue;
            }

            let title = response.data[4].paramValue;
            if (title) {
              document.title = title;
            }
          })
        getModuleList().then(response => {
          let modules = {};
          response.data.forEach(m => {
            modules[m.key] = m.status;
          });
          localStorage.setItem('modules', JSON.stringify(modules));
        });
      })


    userStore.getIsLogin()
      .then(res => {
        this.getLanguage(res.data.language);
        window.location.href = "/";
      })
      .catch(data => {
        // 保存公钥
        localStorage.setItem("publicKey", data.message);
        let lang = localStorage.getItem("language");
        if (lang) {
          setLanguage(lang);
          this.rules = this.getDefaultRules();
        }
      });
  },
  created: function () {
    document.addEventListener("keydown", this.watchEnter);
    let authenticate = localStorage.getItem('AuthenticateType');
    if (checkMicroMode()) {
      if (authenticate === 'LOCAL' || authenticate === 'LDAP') {
        this.form.authenticate = authenticate;
      }
    }
  },

  destroyed() {
    document.removeEventListener("keydown", this.watchEnter);
  },
  methods: {
    watchEnter(e) {
      let keyCode = e.keyCode;
      if (keyCode === 13) {
        this.submit('form');
      }
    },
    submit(form) {
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
          acc[cur] = query[cur]
        }
        return acc
      }, {})
    },
    getDefaultRules() { // 设置完语言要重新赋值
      return {
        username: [
          {required: true, message: this.$t('commons.input_login_username'), trigger: 'blur'},
        ],
        password: [
          {required: true, message: this.$t('commons.input_password'), trigger: 'blur'},
          {min: 6, max: 30, message: this.$t('commons.input_limit', [6, 30]), trigger: 'blur'}
        ]
      };
    },
    checkRedirectUrl() {
      if (this.lastUser === getCurrentUserId()) {
        this.$router.push({path: sessionStorage.getItem('redirectUrl') || '/'});
        return;
      }
      let redirectUrl = '/';
      if (hasPermissions('PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_OPERATING_LOG:READ', 'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE', 'PROJECT_CUSTOM_CODE:READ', 'PROJECT_MESSAGE:READ', 'PROJECT_TEMPLATE:READ')) {
        redirectUrl = '/project/home';
      } else if (hasPermissions('WORKSPACE_SERVICE:READ', 'WORKSPACE_USER:READ', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_OPERATING_LOG:READ')) {
        redirectUrl = '/setting/project/:type';
      } else if (hasPermissions('SYSTEM_USER:READ', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_GROUP:READ', 'SYSTEM_TEST_POOL:READ', 'SYSTEM_SETTING:READ', 'SYSTEM_AUTH:READ', 'SYSTEM_QUOTA:READ', 'SYSTEM_OPERATING_LOG:READ')) {
        redirectUrl = '/setting';
      } else {
        redirectUrl = '/';
      }

      sessionStorage.setItem('redirectUrl', redirectUrl);
      sessionStorage.setItem('lastUser', getCurrentUserId());
      this.$router.push({name: "login_redirect", path: redirectUrl || '/', query: this.otherQuery});
    },
    doLogin() {
      const userStore = useUserStore()
      // 删除缓存
      sessionStorage.removeItem('changePassword');
      let publicKey = localStorage.getItem("publicKey");

      let form = {
        username: publicKeyEncrypt(this.form.username, publicKey),
        password: publicKeyEncrypt(this.form.password, publicKey),
        authenticate: this.form.authenticate
      };

      userStore.userLogin(form)
        .then(response => {
          sessionStorage.setItem('loginSuccess', 'true');
          sessionStorage.setItem('changePassword', response.message);
          localStorage.setItem('AuthenticateType', this.form.authenticate);
          this.getLanguage(response.data.language);
          // 检查登录用户的权限
          this.checkRedirectUrl();
        });
    },
    getLanguage(language) {
      if (!language) {
        getLanguage()
          .then(response => {
            language = response.data;
            localStorage.setItem(DEFAULT_LANGUAGE, language);
          });
      }
    },
    redirectAuth(authId) {
      if (authId === 'LDAP' || authId === 'LOCAL') {
        return;
      }
      let source = this.authSources.filter(auth => auth.id === authId)[0];
      // 以前的cas登录
      if (source.type === 'CAS') {
        let config = JSON.parse(source.configuration);
        if (config.casServerUrl && !config.loginUrl) {
          return;
        }
      }
      operationConfirm(this, this.$t('commons.auth_redirect_tip'), () => {
        let config = JSON.parse(source.configuration);
        let redirectUrl = eval('`' + config.redirectUrl + '`');
        let url;
        if (source.type === 'CAS') {
          url = config.loginUrl + "?service=" + encodeURIComponent(redirectUrl);
        }
        if (source.type === 'OIDC') {
          url = config.authUrl + "?client_id=" + config.clientId + "&redirect_uri=" + redirectUrl +
            "&response_type=code&scope=openid+profile+email&state=" + authId;
        }
        if (source.type === 'OAUTH2') {
          url = config.authUrl
            + "?client_id=" + config.clientId
            + "&scope=" + config.scope
            + "&response_type=code"
            + "&redirect_uri=" + redirectUrl
            + "&state=" + authId;
        }
        if (url) {
          window.location.href = url;
        }
      }, () => {
        this.form.authenticate = 'LOCAL';
      });
    },
  }
}
</script>

<style scoped>
.container {
  width: 1200px;
  height: 730px;
  margin: 0 auto;
  background-color: #FFFFFF;
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

.form, .btn {
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

.el-input, .el-button {
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

</style>

