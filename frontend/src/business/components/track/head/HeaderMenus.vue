<template>

  <div id="menu-bar">
    <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
             :default-active='$route.path'>
      <el-menu-item :index="'/track/home'">
        {{ $t("i18n.home") }}
      </el-menu-item>

      <el-submenu v-if="isCurrentWorkspaceUser"
                  index="3" popper-class="submenu" v-permission="['test_manager']" >
        <template v-slot:title>{{$t('commons.project')}}</template>
        <track-recent-project v-if="beaseUrl == 'tack'"/>
        <el-divider/>
        <el-menu-item :index="'/track/project/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="'/track/project/create'">
          <el-button type="text">{{$t('project.create')}}</el-button>
        </el-menu-item>
      </el-submenu>

      <el-submenu v-if="isCurrentWorkspaceUser"
                  index="6" popper-class="submenu" v-permission="['test_manager', 'test_user']">
        <template v-slot:title>{{$t('test_track.test_case')}}</template>
        <recent-case-plan/>
        <el-divider/>
        <el-menu-item :index="'/track/case/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('test_track.case_list')}}</span>
        </el-menu-item>
        <!--<el-menu-item :index="'/' + beaseUrl + '/case/create'">-->
          <!--<el-button type="text">{{$t('test_track.create_case')}}</el-button>-->
        <!--</el-menu-item>-->
      </el-submenu>

      <el-submenu v-if="isCurrentWorkspaceUser"
                  index="7" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
        <template v-slot:title>{{$t('test_track.test_plan')}}</template>
        <el-divider/>
        <el-menu-item :index="'/track/plan/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="'/track/plan/create'">
          <el-button type="text">{{$t('test_track.create_plan')}}</el-button>
        </el-menu-item>
      </el-submenu>

    </el-menu>
  </div>

</template>

<script>

  import PerformanceRecentTestPlan from "../../performance/test/PerformanceRecentTestPlan";
  import ApiRecentTest from "../../api/test/ApiRecentTest";
  import PerformanceRecentProject from "../../performance/project/PerformanceRecentProject";
  import ApiRecentProject from "../../api/project/ApiRecentProject";
  import PerformanceRecentReport from "../../performance/report/PerformanceRecentReport";
  import ApiRecentReport from "../../api/report/ApiRecentReport";
  import {checkoutCurrentWorkspace} from "../../../../common/utils";
  import TrackRecentProject from "../../track/project/TrackRecentProject";
  import RecentCasePlan from "../../track/case/RecentCasePlan";

  export default {
    name: "MsMenus",
    components: {PerformanceRecentReport, PerformanceRecentTestPlan, ApiRecentTest, ApiRecentReport,
      PerformanceRecentProject, ApiRecentProject, TrackRecentProject, RecentCasePlan},
    data() {
      return {
        isCurrentWorkspaceUser: false,
      }
    },
    props: {
        beaseUrl: {
          type: String
        }
    },
    mounted() {
      this.isCurrentWorkspaceUser = checkoutCurrentWorkspace();
    }
  }

</script>

<style>

  .header-menu.el-menu--horizontal > li {
    height: 39px;
    line-height: 40px;
    color: dimgray;
  }

  .header-menu.el-menu--horizontal > li.el-submenu > * {
    height: 39px;
    line-height: 40px;
    color: dimgray;
  }

  .header-bottom {
    line-height: 40px;
    margin-left: 20%;
  }

</style>

<style scoped>
  .el-divider--horizontal {
    margin: 0;
  }
  #menu-bar {
    border-bottom: 1px solid #E6E6E6;
  }
</style>
