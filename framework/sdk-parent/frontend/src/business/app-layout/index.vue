<template>
  <el-container>
    <el-header :height="headerHeight" class="ms-header-w">
      <el-row>
        <el-col>
          <mx-license-message/>
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
        :class="[isCollapse ? 'ms-aside': 'ms-aside-collapse-open', isFullScreen ? 'is-fullscreen' : '']"
        class="ms-left-aside shepherd-menu"
        :style="isFixed ? 'opacity:100%; position: relative;z-index: 666;': 'opacity: 95%;position: fixed'"
        @mouseenter.native="collapseOpen"
        @mouseleave.native="collapseClose">
        <ms-aside-header :sideTheme="sideTheme" :isCollapse="isCollapse" :title="sysTitle"/>
        <ms-aside-menus :sideTheme="sideTheme" :color="color" :isCollapse="isCollapse"/>
        <div class="ms-header-fixed" v-show="!isCollapse">
          <svg-icon iconClass="pushpin" class-name="ms-menu-pin" v-if="isFixed" @click.native="fixedChange(false)"/>
          <svg-icon iconClass="unpin" class-name="ms-menu-pin" v-else @click.native="fixedChange(true)"/>
        </div>
      </el-aside>
      <el-main class="container">
        <div :class="isFixed ? 'ms-left-fixed': 'ms-aside-left'"/>
        <div :class="isFixed ? 'ms-right-fixed': 'ms-main-view ms-aside-right'">
          <router-view v-if="isShow"/>
        </div>
      </el-main>
      <mx-theme/>
    </el-container>
  </el-container>
</template>

<script>

import MsAsideFooter from "../../components/layout/AsideFooter";
import MsAsideHeader from "../../components/layout/AsideHeader";
import MsAsideMenus from "../../components/layout/AsideMenus";
import MsView from "../../components/layout/View";
import MxLicenseMessage from "../../components/MxLicenseMessage";
import MxTheme from "../../components/MxTheme";
import {hasLicense} from "../../utils/permission";
import {setAsideColor, setColor, setCustomizeColor, setDefaultTheme, setLightColor} from "../../utils";
import {ORIGIN_COLOR} from "../../utils/constants";
import {getDisplayInfo, getSystemTheme, isLogin} from "../../api/user";
import {useUserStore} from "@/store";
import {getModuleList} from "../../api/module";


export default {
  name: "AppLayout",
  components: {MsView, MsAsideFooter, MsAsideHeader, MsAsideMenus, MxLicenseMessage, MxTheme},
  data() {
    return {
      licenseHeader: null,
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
      sysTitle: undefined,
      isFullScreen: false,
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
    getModuleList()
      .then(response => {
        let modules = {};
        response.data.forEach(m => {
          modules[m.key] = m.status;
        });
        localStorage.setItem('modules', JSON.stringify(modules));
      });
    if (!hasLicense()) {
      setDefaultTheme();
      setCustomizeColor();
      this.color = ORIGIN_COLOR;
    } else {
      getSystemTheme()
        .then(res => {
          this.color = res.data ? res.data : ORIGIN_COLOR;
          setColor(this.color, this.color, this.color, this.color, this.color);
          // this.$store.commit('setTheme', res.data);
        });
      this.getDisplayInfo();
    }

    this.isFixed = localStorage.getItem('app-fixed') === 'true' || false;
    this.isCollapse = this.isFixed === true ? false : true;

    this.$EventBus.$on("toggleFullScreen", (param) => {
      this.isFullScreen = param
    });
  },
  destroyed() {
    this.$EventBus.$off("toggleFullScreen");
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
      this.result = getDisplayInfo()
        .then(response => {
          let theme = "";
          if (response.data && response.data[5] && response.data[5].paramValue) {
            theme = response.data[5].paramValue;
          }
          if (response.data && response.data[7] && response.data[7].paramValue) {
            this.sideTheme = response.data[7].paramValue;
          }

          if (response.data && response.data[6] && response.data[6].paramValue) {
            this.sysTitle = response.data[6].paramValue;
          }

          let title = response.data[4].paramValue;
          if (title) {
            localStorage.setItem("default-document-title", title);
            if (this.$route.fullPath.indexOf("/track/case/edit") === -1) {
              // 如果不是用例编辑, 新增, 复制页面则需修改title为系统title
              document.title = title;
            }
          } else {
            localStorage.setItem("default-document-title", "MeterSphere");
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
      this.$EventBus.$emit('appFixedChange', this.isFixed);
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
      const userStore = useUserStore()
      setInterval(() => {
        isLogin()
          .then(() => {
          })
          .catch(() => {
            userStore.userLogout();
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
    reloadTopMenus() {
      const userStore = useUserStore()
      return userStore.getIsLogin()
        .then(response => {
          this.$setLang(response.data.language);
          // 先隐藏
          this.isMenuShow = false;
          this.isShow = false;
          this.$nextTick(() => {
            this.isShow = true;
            this.isMenuShow = true;
          });
        })
    }
  },
}
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
  padding: 0!important;
  height: calc(100vh);
  background-color: #F5F6F7;
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
  background-color: #F5F6F7;
}

.ms-right-fixed {
  flex: 0;
  margin-left: 0px;
  height: calc(100vh);
  background-color: #F5F6F7;
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

.checkBox-input :deep(.el-checkbox__inner) {
  border-color: #fff;
}

.checkBox-input :deep(.el-checkbox__inner::after) {
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

.is-fullscreen {
  display: none;
}
</style>
