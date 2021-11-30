<template>
  <div id="menu-bar">
    <el-row type="flex">
      <project-change :project-name="currentProject"/>
      <el-col :span="24">
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
                        v-permission="['PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE']"
                        popper-class="submenu">
            {{ $t('project.file_manage') }}
          </el-menu-item>
          <el-menu-item :index="'/project/code/segment'" popper-class="submenu"
                        v-permission="['PROJECT_CUSTOM_CODE:READ']">
            {{ $t('project.code_segment.code_segment') }}
          </el-menu-item>
          <el-menu-item :index="'/project/log'" popper-class="submenu" v-permission="['PROJECT_OPERATING_LOG:READ']">
            {{ $t('project.log') }}
          </el-menu-item>
          <el-menu-item v-xpack :index="'/project/version'">
            {{ $t('project.version_manage') }}
          </el-menu-item>
          <el-menu-item :index="'/project/app'" popper-class="submenu"
                        v-permission="['PROJECT_APP_MANAGER:READ+EDIT']">
            {{ $t('project.app_manage') }}
          </el-menu-item>
        </el-menu>
      </el-col>
    </el-row>
  </div>

</template>
<script>

import MsShowAll from "@/business/components/common/head/ShowAll";
import MsRecentList from "@/business/components/common/head/RecentList";
import MsCreateButton from "@/business/components/common/head/CreateButton";
import ProjectChange from "@/business/components/common/head/ProjectSwitch";
import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId} from "@/common/js/utils";

export default {
  name: "ProjectHeaderMenus",
  components: {ProjectChange, MsShowAll, MsRecentList, MsCreateButton},
  data() {
    return {
      currentProject: '',
      pathName: '',
      isProjectAdmin: true
    };
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
    clickPlanMenu() {
      this.$info(this.$t('commons.function_planning'));
      return false;
    },
  }
};

</script>

<style scoped>
#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}
</style>
