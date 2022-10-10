<template>
  <div id="menu-bar">
    <el-row type="flex">
      <project-switch :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router :default-active="pathName">
          <el-menu-item :index="'/performance/home'" v-permission="['PROJECT_PERFORMANCE_HOME:READ']">
            {{ $t("i18n.home") }}
          </el-menu-item>
          <el-menu-item :index="'/performance/test/all'" v-permission="['PROJECT_PERFORMANCE_TEST:READ']">
            {{ $t('commons.test') }}
          </el-menu-item>
          <el-menu-item :index="'/performance/report/all'" v-permission="['PROJECT_PERFORMANCE_REPORT:READ']">
            {{ $t('commons.report') }}
          </el-menu-item>
        </el-menu>
      </el-col>
      <el-col :span="10">
        <ms-header-right-menus/>
      </el-col>
    </el-row>
  </div>
</template>

<script>

import MsRecentList from "metersphere-frontend/src/components/head/RecentList";
import MsCreateButton from "metersphere-frontend/src/components/head/CreateButton";
import MsShowAll from "metersphere-frontend/src/components/head/ShowAll";
import ProjectSwitch from "metersphere-frontend/src/components/head/ProjectSwitch";
import MsHeaderRightMenus from "metersphere-frontend/src/components/layout/HeaderRightMenus";
import {PROJECT_NAME} from "metersphere-frontend/src/utils/constants";

export default {
  name: "PerformanceHeaderMenus",
  components: {
    ProjectSwitch,
    MsCreateButton,
    MsShowAll,
    MsRecentList,
    MsHeaderRightMenus
  },
  data() {
    return {
      currentProject: sessionStorage.getItem(PROJECT_NAME),
      pathName: '',
    };
  },
  methods: {},
  watch: {
    '$route': {
      immediate: true,
      handler(to, from) {
        if (to.params && to.params.testId) {
          this.pathName = '/performance/test/all';
        } else if (to.params && to.params.reportId) {
          this.pathName = '/performance/report/all';
        } else {
          this.pathName = to.path;
        }
      }
    }
  },
};

</script>

<style scoped>
.el-divider--horizontal {
  margin: 0;
}

.el-menu.el-menu--horizontal {
  border-bottom: none;
}

#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}

.el-menu-item {
  padding: 0 10px;
}
</style>
