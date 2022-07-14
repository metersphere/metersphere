<template>
  <ms-container :is-show-warning="isShowWarning && isSystemGroup">
    <ms-aside-container>
      <ms-setting-menu/>
    </ms-aside-container>
    <ms-main-container>
      <div class="ms-system-header">
        <ms-header-right-menus/>
      </div>
      <keep-alive>
        <router-view/>
      </keep-alive>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsCurrentUser from "./CurrentUser";
import MsSettingMenu from "./SettingMenu";
import MsAsideContainer from "../common/components/MsAsideContainer";
import MsContainer from "../common/components/MsContainer";
import MsMainContainer from "../common/components/MsMainContainer";
import {getCurrentUser} from "@/common/js/utils";
import {GROUP_SYSTEM} from "@/common/js/constants";
import MsHeaderRightMenus from "@/business/components/layout/HeaderRightMenus";

export default {
  name: "MsSetting",
  components: {MsMainContainer, MsContainer, MsAsideContainer, MsSettingMenu, MsCurrentUser, MsHeaderRightMenus},
  computed: {
    isShowWarning() {
      return this.$store.state.showLicenseCountWarning;
    },
    isSystemGroup() {
      let user = getCurrentUser();
      if (user && user.groups) {
        let group = user.groups.filter(gp => gp && gp.type === GROUP_SYSTEM);
        return group.length > 0;
      }
      return false;
    }
  }
};
</script>

<style scoped>
.ms-aside-container {
  height: calc(100vh) !important;
  padding: 0px;
}

.ms-main-container {
  height: calc(100vh) !important;
  padding: 0px;
}

h1 {
  font-size: 20px;
  font-weight: 500;
}

.ms-system-header {
  width: 400px;
  position: fixed;
  right: 0px;
  z-index: 2000;
  top: 6px;
}
</style>

