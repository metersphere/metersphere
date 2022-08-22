<template>
  <el-container v-if="auth">
    <el-header :height="headerHeight" class="ms-header-w">
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
    </el-header>

    <el-container>
      <el-aside
        :class="isCollapse ? 'ms-aside': 'ms-aside-collapse-open'"
        class="ms-left-aside"
        :style="isFixed ? 'opacity:100%; position: relative;z-index: 666;': 'opacity: 95%;position: fixed'"
        @mouseenter.native="collapseOpen"
        @mouseleave.native="collapseClose">
        <ms-aside-header :sideTheme="sideTheme" :isCollapse="isCollapse"/>
        <ms-aside-menus :sideTheme="sideTheme" :color="color" :isCollapse="isCollapse"/>
        <div class="ms-header-fixed" v-show="!isCollapse">
          <svg-icon iconClass="pushpin" class-name="ms-menu-pin" v-if="isFixed" @click.native="fixedChange(false)"/>
          <svg-icon iconClass="unpin" class-name="ms-menu-pin" v-else @click.native="fixedChange(true)"/>
        </div>
      </el-aside>
      <el-main class="container">
        <div :class="isFixed ? 'ms-left-fixed': 'ms-aside-left'"/>
        <div :class="isFixed ? 'ms-right-fixed': 'ms-main-view ms-aside-right'">
          <ms-view v-if="isShow"/>
        </div>
      </el-main>
      <theme/>
    </el-container>
  </el-container>
</template>

<script>
import MsAsideMenus from "./components/layout/AsideMenus";
import MsAsideHeader from "./components/layout/AsideHeader";
import MsAsideFooter from "./components/layout/AsideFooter";
import MsView from "./components/common/router/View";
import {
  hasLicense,
  saveLocalStorage,
  setAsideColor,
  setColor,
  setCustomizeColor,
  setDefaultTheme,
  setLightColor
} from "@/common/js/utils";
import {registerRequestHeaders} from "@/common/js/ajax";
import {ORIGIN_COLOR} from "@/common/js/constants";

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
      isCollapse: true,
      headerHeight: "0px",
      isFixed: false,
      sideTheme: "",
    };
  },
  computed: {
    changePassword() {
      return JSON.parse(sessionStorage.getItem("changePassword"));
    },
  },
  created() {
    if (this.licenseHeader != null || this.changePassword) {
      this.headerHeight = "30px";
    }
    this.initSessionTimer();
    if (!hasLicense()) {
      setDefaultTheme();
      setCustomizeColor();
      this.color = ORIGIN_COLOR;
    } else {
      this.$get('/system/theme', res => {
        this.color = res.data ? res.data : ORIGIN_COLOR;
        setColor(this.color, this.color, this.color, this.color, this.color);
        this.$store.commit('setTheme', res.data);
      });
      this.getDisplayInfo();
    }

    window.addEventListener("beforeunload", () => {
      localStorage.setItem("store", JSON.stringify(this.$store.state));
    });
    this.isFixed = localStorage.getItem('app-fixed') === 'true' || false;
    this.isCollapse = !this.isFixed;
    this.$store.commit('setAppFixed', this.isFixed);
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

        if (window.location.href.endsWith('/#/login')) {
          window.location.replace("/#/setting/personsetting");
        }

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
    getDisplayInfo() {
      this.result = this.$get("/display/info", response => {
        let theme = "";
        if (response.data && response.data[5] && response.data[5].paramValue) {
          theme = response.data[5].paramValue;
        }
        if (response.data && response.data[7] && response.data[7].paramValue) {
          this.sideTheme = response.data[7].paramValue;
        }
        this.setAsideTheme(theme);
      });
    },
    setAsideTheme(theme) {
      switch (this.sideTheme) {
        case "theme-light":
          setLightColor();
          break;
        case "theme-default":
          setAsideColor();
          break;
        default:
          setCustomizeColor(theme);
          break;
      }
    },
    fixedChange(isFixed) {
      this.isFixed = isFixed;
      if (this.isFixed) {
        this.isCollapse = false;
      }
      localStorage.removeItem('app-fixed');
      localStorage.setItem('app-fixed', this.isFixed);
      this.$store.commit('setAppFixed', this.isFixed);
    },
    collapseOpen() {
      this.isCollapse = false;
    },
    collapseClose() {
      if (!this.isFixed) {
        this.isCollapse = true;
      }
    },
    initSessionTimer() {
      let timeout = 1800;
      this.initTimer(timeout);
    },
    initTimer(timeout) {
      setInterval(() => {
        this.$get("/isLogin")
          .then(response => {
            if (!response.data.success) {
              this.$refs.headerUser.logout();
            }
          })
          .catch(() => {
            window.location.href = "/login";
          });
      }, timeout * 1000);
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
    MsView,
    MsAsideMenus,
    MsAsideHeader,
    MsAsideFooter,
    "LicenseMessage": header.default,
    "Theme": theme.default
  }
};
</script>


<style scoped>
.ms-aside {
  z-index: 666;
  width: var(--asideWidth) !important;
  background-color: var(--aside_color);
  color: var(--font_color);
  opacity: 100%;
  height: calc(100vh);
}

.ms-aside-collapse-open {
  width: var(--asideOpenWidth) !important;
  background-color: var(--aside_color);
  color: var(--font_color);
  opacity: 95%;
  z-index: 9999;
  border-right: 1px #DCDFE6 solid;
  border-radius: 2px;
}

.change-password-tip {
  height: 30px;
  background: #e6a23c;
  text-align: center;
  line-height: 30px;
  color: white;
}

.ms-left-aside {
  position: fixed;
  left: 0;
  height: calc(100vh);
  background-color: var(--aside_color);
  padding-left: 0px;
  overflow: hidden;
}

.ms-main-view {
  margin-left: var(--asideWidth);
}

.container {
  padding: 0px !important;
  height: calc(100vh);
}

.ms-aside-left {
  float: left;
  width: var(--asideWidth);
  height: calc(100vh);
  background-color: var(--aside_color);
}

.ms-left-fixed {
  width: 0px;
  border-right: 0px;
}

.ms-aside-right {
  flex: 1;
  height: calc(100vh);
}

.ms-right-fixed {
  flex: 0;
  margin-left: 0px;
}

.ms-header-w {
  width: 100%;
  padding: 0px;
}

.ms-header-fixed {
  margin-left: var(--asideOpenMargin);
  position: absolute;
  bottom: 20px;
}

.checkBox-input >>> .el-checkbox__inner {
  border-color: #fff;
}

.checkBox-input >>> .el-checkbox__inner::after {
  top: 4px;
  left: 4px;
  width: 3px;
  height: 3px;
  border-radius: 100%;
  background-color: #fff !important;
  content: "";
  position: absolute;
  border-color: #fff !important;
}

.ms-menu-pin {
  color: var(--font_color);
  fill: currentColor;
  font-size: 20px;
}

.ms-menu-pin:hover {
  cursor: pointer;
}
</style>
