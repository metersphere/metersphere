<template>
  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <project-change :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router :default-active='$route.path'>
          <el-menu-item
            :index="'/report/projectStatistics'"
            v-permission="['PROJECT_REPORT_ANALYSIS:READ', 'PROJECT_REPORT_ANALYSIS:READ+EXPORT',
            'PROJECT_REPORT_ANALYSIS:READ+UPDATE', 'PROJECT_REPORT_ANALYSIS:READ+CREATE']">
            {{ $t("commons.report_statistics.menu.project_statistics") }}
          </el-menu-item>
          <el-menu-item
            :index="'/report/projectReport'" v-xpack
            v-permission="['PROJECT_ENTERPRISE_REPORT:READ+EXPORT', 'PROJECT_ENTERPRISE_REPORT:READ+CREATE',
            'PROJECT_ENTERPRISE_REPORT:READ+DELETE', 'PROJECT_ENTERPRISE_REPORT:READ+COPY',
            'PROJECT_ENTERPRISE_REPORT:READ+SCHEDULE', 'PROJECT_ENTERPRISE_REPORT:READ+EDIT']">
            {{ $t("commons.report_statistics.menu.project_report") }}
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

import ProjectChange from "@/business/components/common/head/ProjectSwitch";
import {hasLicense, hasPermission} from "@/common/js/utils";
import MsHeaderRightMenus from "@/business/components/layout/HeaderRightMenus";

export default {
  name: "ReportStatisticsHeaderMenus",
  components: {ProjectChange,MsHeaderRightMenus},
  data() {
    return {
      licenseCheck: false,
      isProjectActivation: true,
      isRouterAlive: true,
      apiTestProjectPath: '',
      currentProject: ''
    };
  },
  methods: {
    hasPermission,
    hasLicense,
    reload() {
      this.isRouterAlive = false;
      this.$nextTick(function () {
        this.isRouterAlive = true;
      });
    },
  },
  mounted() {
  },
  created() {
    if(this.hasLicense()){
      this.licenseCheck = true;
    }
  }
};

</script>

<style scoped>
#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}

.menu-divider {
  margin: 0;
}

.blank_item {
  display: none;
}

.deactivation >>> .el-submenu__title {
  border-bottom: white !important;
}

.el-menu-item {
  padding: 0 10px;
}
</style>
