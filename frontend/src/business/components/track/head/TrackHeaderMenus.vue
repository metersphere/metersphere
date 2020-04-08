<template>

  <div id="menu-bar" v-if="isRouterAlive">
    <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
             :default-active='$route.path'>
      <el-menu-item :index="'/track/home'">
        {{ $t("i18n.home") }}
      </el-menu-item>

      <el-submenu v-if="isCurrentWorkspaceUser"
                  index="3" popper-class="submenu" v-permission="['test_manager']" >
        <template v-slot:title>{{$t('commons.project')}}</template>
        <track-recent-project/>
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
        <recent-test-case/>
        <el-divider/>
        <el-menu-item :index="'/track/case/all'">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('test_track.case_list')}}</span>
        </el-menu-item>
        <el-menu-item :index="testCaseEditPath" class="blank_item"></el-menu-item>
        <!--<el-menu-item :index="'/' + beaseUrl + '/case/create'">-->
          <!--<el-button type="text">{{$t('test_track.create_case')}}</el-button>-->
        <!--</el-menu-item>-->
      </el-submenu>

      <el-submenu v-if="isCurrentWorkspaceUser"
                  index="7" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
        <template v-slot:title>{{$t('test_track.test_plan')}}</template>
        <recent-test-plan/>
        <el-divider/>
        <el-menu-item index="/track/plan/all">
          <font-awesome-icon :icon="['fa', 'list-ul']"/>
          <span style="padding-left: 5px;">{{$t('commons.show_all')}}</span>
        </el-menu-item>
        <el-menu-item :index="testPlanViewPath" class="blank_item"></el-menu-item>
        <el-menu-item index="/track/plan/create">
          <el-button type="text">{{$t('test_track.create_plan')}}</el-button>
        </el-menu-item>
      </el-submenu>

    </el-menu>
  </div>

</template>
<script>

  import {checkoutCurrentWorkspace} from "../../../../common/utils";
  import TrackRecentProject from "../../track/project/TrackRecentProject";
  import RecentTestCase from "../case/components/RecentTestCase";
  import RecentTestPlan from "../plan/components/RecentTestPlan";

  export default {
    name: "TrackHeaderMenus",
    components: {RecentTestCase, TrackRecentProject, RecentTestPlan},
    data() {
      return {
        isCurrentWorkspaceUser: false,
        testPlanViewPath: '',
        isRouterAlive: true,
        testCaseEditPath: ''
      }
    },
    watch: {
      '$route'(to, from) {
        let path = to.path;
        if (path.indexOf("/track/plan/view") >= 0){
          this.testPlanViewPath = '/track/plan/view/' + this.$route.params.planId;
          this.reload();
        }
        if (path.indexOf("/track/case/edit") >= 0){
          this.testCaseEditPath = '/track/case/edit/' + this.$route.params.caseId;
          this.reload();
        }
      }
    },
    mounted() {
      this.isCurrentWorkspaceUser = checkoutCurrentWorkspace();
    },
    methods: {
      reload () {
        this.isRouterAlive = false;
        this.$nextTick(function () {
          this.isRouterAlive = true;
        })
      }
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

  .blank_item {
    display: none;
  }
</style>
