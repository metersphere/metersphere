<template>
  <ms-container :is-show-warning="isShowWarning && isSystemGroup">

    <ms-aside-container>
      <ms-current-user/>
      <el-divider/>
      <h1>{{ $t('commons.setting') }}</h1>
      <ms-setting-menu/>
    </ms-aside-container>

    <ms-main-container>
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

export default {
  name: "MsSetting",
  components: {MsMainContainer, MsContainer, MsAsideContainer, MsSettingMenu, MsCurrentUser},
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
  height: calc(100vh - 40px) !important;
  padding: 20px;
}

.ms-main-container {
  height: calc(100vh - 40px) !important;
}

h1 {
  font-size: 20px;
  font-weight: 500;
}

</style>

