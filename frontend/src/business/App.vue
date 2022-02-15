<template>
  <el-col v-if="auth">
    <el-row v-if="licenseHeader != null">
      <el-col>
        <component :is="licenseHeader"></component>
      </el-col>
    </el-row>
    <el-row v-if="changePassword">
      <el-col>
        <div class="change-password-tip">
          {{ $t('commons.change_password_tips') }}
        </div>
      </el-col>
    </el-row>
    <el-row id="header-top" type="flex" justify="space-between" align="middle" v-if="isMenuShow">
      <el-col :span="12">
        <img :src="'/display/file/logo'" class="logo" alt="">
        <ms-top-menus :color="color"/>
      </el-col>

      <el-col :span="12" class="align-right">
        <!-- float right -->
        <ms-user ref="headerUser"/>
        <ms-language-switch :color="color"/>
        <ms-header-ws :color="color"/>
        <ms-task-center :color="color"/>
        <ms-notification :color="color"/>
      </el-col>
    </el-row>

    <ms-view v-if="isShow"/>

    <theme/>
  </el-col>
</template>

<script>
import MsTopMenus from "./components/common/head/HeaderTopMenus";
import MsView from "./components/common/router/View";
import MsUser from "./components/common/head/HeaderUser";
import MsHeaderWs from "./components/common/head/HeaderWs";
import MsLanguageSwitch from "./components/common/head/LanguageSwitch";
import {hasLicense, saveLocalStorage, setColor, setDefaultTheme} from "@/common/js/utils";
import {registerRequestHeaders} from "@/common/js/ajax";
import {ORIGIN_COLOR} from "@/common/js/constants";
import MsTaskCenter from "@/business/components/task/TaskCenter";
import MsNotification from "@/business/components/notice/Notification";

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const header = requireComponent.keys().length > 0 ? requireComponent("./license/LicenseMessage.vue") : {};
const display = requireComponent.keys().length > 0 ? requireComponent("./display/Display.vue") : {};
const theme = requireComponent.keys().length > 0 ? requireComponent("./display/Theme.vue") : {};
let timer = null;

export default {
  name: 'app',
  props: {},
  data() {
    return {
      licenseHeader: null,
      auth: false,
      header: {},
      logoId: '_blank',
      color: '',
      sessionTimer: null,
      isShow: true,
      isMenuShow: true,
    };
  },
  computed: {
    changePassword() {
      return JSON.parse(sessionStorage.getItem("changePassword"));
    }
  },
  created() {
    this.initSessionTimer();
    if (!hasLicense()) {
      setDefaultTheme();
      this.color = ORIGIN_COLOR;
    } else {
      //
      this.$get('/system/theme', res => {
        this.color = res.data ? res.data : ORIGIN_COLOR;
        setColor(this.color, this.color, this.color, this.color, this.color);
        this.$store.commit('setTheme', res.data);
      });
    }
    // OIDC redirect 之后不跳转
    if (window.location.href.endsWith('#/refresh')) {
      window.location.replace("/#/setting/personsetting");
      setTimeout(() => {
        window.location.reload();
      }, 200);
    }
    window.addEventListener("beforeunload", () => {
      localStorage.setItem("store", JSON.stringify(this.$store.state));
    });
  },
  beforeCreate() {
    this.$get("/isLogin").then(response => {
      if (response.data.success) {
        this.$setLang(response.data.data.language);
        saveLocalStorage(response.data);
        registerRequestHeaders();
        this.auth = true;
        // 是否显示校验信息
        if (header.default !== undefined) {
          this.licenseHeader = "LicenseMessage";
        }

        if (display.default !== undefined) {
          display.default.showHome(this);
        }

        //
        if (localStorage.getItem("store")) {
          this.$store.replaceState(Object.assign({}, this.$store.state, JSON.parse(localStorage.getItem("store"))));
          this.$get("/project/listAll", response => {
            let projectIds = response.data;
            if (projectIds && projectIds.length <= 0) {
              this.$store.commit('setProjectId', undefined);
            }
          });
        }
      } else {
        window.location.href = "/login";
      }
    }).catch(() => {
      window.location.href = "/login";
    });
  },
  // 提供可注入子组件属性
  provide() {
    return {
      reload: this.reload,
      reloadTopMenus: this.reloadTopMenus,
    };
  },
  methods: {
    initSessionTimer() {
      this.$get('/system/timeout')
        .then(response => {
          let timeout = response.data.data;
          this.initTimer(timeout);
          window.addEventListener('click', () => {
            this.currentTime(timeout);
          });
        })
        .catch(() => {
        });
    },
    initTimer(timeout) {
      setTimeout(() => {
        this.$get("/isLogin")
          .then(response => {
            if (!response.data.success) {
              this.$refs.headerUser.logout();
            }
          })
          .catch(() => {
            window.location.href = "/login";
          });
      }, 1000 * (timeout + 10));
    },
    currentTime(timeout) { // 超时退出
      if (timer) {
        window.clearTimeout(timer);
        timer = null;
      }
      timer = window.setTimeout(() => {
        this.$refs.headerUser.logout();
      }, 1000 * timeout);
    },
    reload() {
      // 先隐藏
      this.isShow = false;
      this.$nextTick(() => {
        this.isShow = true;
      });
    },
    reloadTopMenus(callback) {
      this.$get("/isLogin").then(response => {
        if (response.data.success) {
          this.$setLang(response.data.data.language);
          saveLocalStorage(response.data);
          // 先隐藏
          this.isMenuShow = false;
          this.isShow = false;
          this.$nextTick(() => {
            this.isShow = true;
            this.isMenuShow = true;
            if (callback) {
              callback();
            }
          });
        } else {
          window.location.href = "/login";
        }
      }).catch(() => {
        if (callback) {
          callback();
        }
        window.location.href = "/login";
      });
    }
  },
  components: {
    MsNotification,
    MsTaskCenter,
    MsLanguageSwitch,
    MsUser,
    MsView,
    MsTopMenus,
    MsHeaderWs,
    "LicenseMessage": header.default,
    "Theme": theme.default
  }
};
</script>


<style scoped>
#header-top {
  width: 100%;
  padding: 0 10px;
  background-color: var(--color);
  color: rgb(245, 245, 245);
  font-size: 14px;
  height: 40px;
}

.logo {
  width: 156px;
  margin-bottom: 0;
  border: 0;
  margin-right: 20px;
  display: inline-block;
  line-height: 37px;
  background-size: 156px 30px;
  box-sizing: border-box;
  height: 37px;
  background-repeat: no-repeat;
  background-position: 50% center;
}

.menus > * {
  color: inherit;
  padding: 0;
  max-width: 180px;
  white-space: pre;
  cursor: pointer;
  line-height: 40px;
}

.header-top-menus {
  display: inline-block;
  border: 0;
  position: absolute;
}

.menus > a {
  padding-right: 15px;
  text-decoration: none;
}

.align-right {
  float: right;
}

.license-head {
  height: 30px;
  background: #BA331B;
  text-align: center;
  line-height: 30px;
  color: white;
}

.change-password-tip {
  height: 30px;
  background: #e6a23c;
  text-align: center;
  line-height: 30px;
  color: white;
}
</style>
