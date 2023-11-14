<template>
  <ms-container :is-show-warning="isShowWarning && isSystemGroup">
    <ms-aside-container pageKey="SETTING" :enable-aside-hidden="enableAsideHidden" :width="enableAsideHidden ? '300px' : '0px'">
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
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import {getCurrentUser} from "metersphere-frontend/src/utils/token";
import {GROUP_SYSTEM} from "metersphere-frontend/src/utils/constants";
import MsHeaderRightMenus from "metersphere-frontend/src/components/layout/HeaderRightMenus";
import {hasPermissions} from "metersphere-frontend/src/utils/permission";
import {useUserStore} from "@/store";
const store = useUserStore();

export default {
  name: "MsSetting",
  components: {MsMainContainer, MsContainer, MsAsideContainer, MsSettingMenu, MsCurrentUser, MsHeaderRightMenus},
  computed: {
    isShowWarning() {
      return store.showLicenseCountWarning;
    },
    isSystemGroup() {
      let user = getCurrentUser();
      if (user && user.groups) {
        let group = user.groups.filter(gp => gp && gp.type === GROUP_SYSTEM);
        return group.length > 0;
      }
      return false;
    },
    enableAsideHidden() {
      let systemPermission = [
        'SYSTEM_USER:READ',
        'SYSTEM_ORGANIZATION:READ',
        'SYSTEM_GROUP:READ',
        'SYSTEM_WORKSPACE:READ',
        'SYSTEM_TEST_POOL:READ',
        'SYSTEM_SETTING:READ',
        'SYSTEM_QUOTA:READ',
        'SYSTEM_AUTH:READ'
      ];
      let workspacePermission = [
        'WORKSPACE_USER:READ',
        'WORKSPACE_SERVICE:READ',
        'WORKSPACE_PROJECT_MANAGER:READ',
        'WORKSPACE_PROJECT_ENVIRONMENT:READ',
        'WORKSPACE_OPERATING_LOG:READ'
      ];
      return hasPermissions(...systemPermission)
        || hasPermissions(...workspacePermission);
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
}

h1 {
  font-size: 20px;
  font-weight: 500;
}

.ms-system-header {
  width: 400px;
  position: fixed;
  right: 0;
  z-index: 2000;
  top: 6px;
}
</style>

