<template>
  <div id="menu-bar">
    <el-row type="flex">
      <project-switch :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
                 :default-active="pathName">
          <el-menu-item :index="'/project/home'">
            {{ $t('project.info') }}
          </el-menu-item>
          <el-menu-item :index="'/project/member'" v-permission="['PROJECT_USER:READ']">
            {{ $t('project.member') }}
          </el-menu-item>
          <el-menu-item :index="'/project/usergroup'" v-permission="['PROJECT_GROUP:READ']">
            {{ $t('project.group_permission') }}
          </el-menu-item>
          <el-menu-item :index="'/project/env'" v-permission="['PROJECT_ENVIRONMENT:READ']"
                        popper-class="submenu">
            {{ $t('project.env') }}
          </el-menu-item>
          <el-menu-item :index="'/project/file/manage'"
                        v-permission="['PROJECT_FILE:READ', 'PROJECT_FILE:READ+JAR']"
                        popper-class="submenu">
            {{ $t('project.file_manage') }}
          </el-menu-item>
          <el-menu-item
            :index="'/project/errorreportlibrary'"
            v-permission="['PROJECT_ERROR_REPORT_LIBRARY:READ']"
            v-xpack
            v-if="showMenu">
            {{ $t("error_report_library.name") }}
          </el-menu-item>
          <el-menu-item
            index="/project/template"
            v-permission="['PROJECT_TEMPLATE:READ']"
            v-if="showMenu">
            {{ $t('workspace.template_manage') }}
          </el-menu-item>
          <el-menu-item
            :index="'/project/messagesettings'"
            v-permission="['PROJECT_MESSAGE:READ']"
            v-if="showMenu">
            {{ $t("organization.message_settings") }}
          </el-menu-item>
          <el-menu-item
            :index="'/project/log'"
            popper-class="submenu"
            v-permission="['PROJECT_OPERATING_LOG:READ']"
            v-if="showMenu">
            {{ $t('project.log') }}
          </el-menu-item>
          <el-menu-item
            v-xpack
            :index="'/project/version'"
            v-permission="['PROJECT_VERSION:READ']"
            v-if="showMenu">
            {{ $t('project.version_manage') }}
          </el-menu-item>
          <el-menu-item
            :index="'/project/app'"
            popper-class="submenu"
            v-permission="['PROJECT_APP_MANAGER:READ+EDIT']"
            v-if="showMenu">
            {{ $t('project.app_manage') }}
          </el-menu-item>
          <el-menu-item
            :index="'/project/code/segment'"
            popper-class="submenu"
            v-permission="['PROJECT_CUSTOM_CODE:READ']"
            v-if="showMenu">
            {{ $t('project.code_segment.code_segment') }}
          </el-menu-item>
          <el-submenu index="2" v-if="!showMenu">
            <template slot="title">{{ $t('commons.report_statistics.report_filter.more_options') }}</template>
            <el-menu-item :index="'/project/errorreportlibrary'" v-permission="['PROJECT_ERROR_REPORT_LIBRARY:READ']"
                          v-xpack>
              {{ $t("error_report_library.name") }}
            </el-menu-item>
            <el-menu-item index="/project/template" v-permission="['PROJECT_TEMPLATE:READ']">
              <template slot="title">{{ $t('workspace.template_manage') }}</template>
            </el-menu-item>
            <el-menu-item :index="'/project/messagesettings'" v-permission="['PROJECT_MESSAGE:READ']">
              {{ $t("organization.message_settings") }}
            </el-menu-item>
            <el-menu-item :index="'/project/log'" popper-class="submenu" v-permission="['PROJECT_OPERATING_LOG:READ']">
              {{ $t('project.log') }}
            </el-menu-item>
            <el-menu-item v-xpack :index="'/project/version'" v-permission="['PROJECT_VERSION:READ']">
              {{ $t('project.version_manage') }}
            </el-menu-item>
            <el-menu-item :index="'/project/app'" popper-class="submenu"
                          v-permission="['PROJECT_APP_MANAGER:READ+EDIT']">
              {{ $t('project.app_manage') }}
            </el-menu-item>
            <el-menu-item :index="'/project/code/segment'" popper-class="submenu"
                          v-permission="['PROJECT_CUSTOM_CODE:READ']">
              {{ $t('project.code_segment.code_segment') }}
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </el-col>
      <el-col :span="10">
        <ms-header-right-menus/>
      </el-col>
    </el-row>
  </div>

</template>
<script>

import MsShowAll from "metersphere-frontend/src/components/head/ShowAll";
import MsRecentList from "metersphere-frontend/src/components/head/RecentList";
import MsCreateButton from "metersphere-frontend/src/components/head/CreateButton";
import ProjectSwitch from "metersphere-frontend/src/components/head/ProjectSwitch";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import MsHeaderRightMenus from "metersphere-frontend/src/components/layout/HeaderRightMenus";
import {PROJECT_NAME} from "metersphere-frontend/src/utils/constants";
import {hasPermission} from "metersphere-frontend/src/utils/permission";

export default {
  name: "ProjectHeaderMenus",
  components: {ProjectSwitch, MsShowAll, MsRecentList, MsCreateButton, MsHeaderRightMenus},
  data() {
    return {
      currentProject: sessionStorage.getItem(PROJECT_NAME),
      pathName: '',
      showMenu: true
    };
  },
  mounted() {
    let menuCount = 0;
    let permissions = [
      'PROJECT_APP_MANAGER:READ+EDIT',
      'PROJECT_MESSAGE:READ',
      'PROJECT_OPERATING_LOG:READ',
      'PROJECT_CUSTOM_CODE:READ',
      'PROJECT_TEMPLATE:READ',
      'PROJECT_VERSION:READ',
      'PROJECT_ERROR_REPORT_LIBRARY:READ',
    ];
    for (let permission of permissions) {
      // 更多选项中菜单数量小于3个时，不显示更多选项
      if (menuCount >= 3) {
        this.showMenu = false;
        break;
      }
      if (hasPermission(permission)) {
        menuCount++;
      }
    }
  },
  watch: {
    '$route': {
      immediate: true,
      handler(to) {
        this.pathName = to.path;
      }
    }
  },
  methods: {
    hasLicense,
  }
};

</script>

<style scoped>
#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}

.el-menu-item {
  padding: 0 10px;
}
</style>
