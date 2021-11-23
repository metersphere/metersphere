<template>
  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <project-change :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router :default-active='$route.path'>
          <el-menu-item :index="'/report/projectStatistics'">
            {{ $t("commons.report_statistics.menu.project_statistics") }}
          </el-menu-item>
          <el-menu-item :index="'/report/projectReport'" v-xpack v-permission="['PROJECT_API_DEFINITION:READ']">
            {{ $t("commons.report_statistics.menu.project_report") }}
          </el-menu-item>

        </el-menu>
      </el-col>
    </el-row>
  </div>

</template>

<script>

import ProjectChange from "@/business/components/common/head/ProjectSwitch";
import {hasLicense, hasPermission} from "@/common/js/utils";

export default {
  name: "ReportStatisticsHeaderMenus",
  components: {ProjectChange},
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
</style>
