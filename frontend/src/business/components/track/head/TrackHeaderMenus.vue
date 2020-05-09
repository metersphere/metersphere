<template>

  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <el-col :span="8">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
                 :default-active='$route.path'>
          <el-menu-item :index="'/track/home'">
            {{ $t("i18n.home") }}
          </el-menu-item>

          <el-submenu v-if="isCurrentWorkspaceUser"
                      index="3" popper-class="submenu" v-permission="['test_manager']" >
            <template v-slot:title>{{$t('commons.project')}}</template>
            <ms-recent-list :options="projectRecent"/>
            <el-divider/>
            <ms-show-all :index="'/track/project/all'"/>
            <ms-create-button :index="'/track/project/create'" :title="$t('project.create')"/>
          </el-submenu>

          <el-submenu v-if="isCurrentWorkspaceUser"
                      index="6" popper-class="submenu" v-permission="['test_manager', 'test_user']">
            <template v-slot:title>{{$t('test_track.case.test_case')}}</template>
            <ms-recent-list :options="caseRecent"/>
            <el-divider/>
            <ms-show-all :index="'/track/case/all'"/>
            <el-menu-item :index="testCaseEditPath" class="blank_item"></el-menu-item>
          </el-submenu>

          <el-submenu v-if="isCurrentWorkspaceUser"
                      index="7" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
            <template v-slot:title>{{$t('test_track.plan.test_plan')}}</template>
            <ms-recent-list :options="planRecent"/>
            <el-divider/>
            <ms-show-all :index="'/track/plan/all'"/>
            <el-menu-item :index="testPlanViewPath" class="blank_item"></el-menu-item>
            <ms-create-button :index="'/track/plan/create'" :title="$t('test_track.plan.create_plan')"/>
          </el-submenu>
        </el-menu>
      </el-col>
      <el-col :span="16"/>
    </el-row>
  </div>

</template>
<script>

  import {checkoutCurrentWorkspace} from "../../../../common/js/utils";
  import MsShowAll from "../../common/head/ShowAll";
  import MsRecentList from "../../common/head/RecentList";
  import MsCreateButton from "../../common/head/CreateButton";

  export default {
    name: "TrackHeaderMenus",
    components: {MsShowAll, MsRecentList, MsCreateButton},
    data() {
      return {
        isCurrentWorkspaceUser: false,
        testPlanViewPath: '',
        isRouterAlive: true,
        testCaseEditPath: '',
        projectRecent: {
          title: this.$t('project.recent'),
          url: "/project/recent/5",
          index: function (item) {
            return '/track/case/' + item.id;
          },
          router: function (item) {
            return {name: 'testCase', params: {projectId: item.id, projectName: item.name}}
          }
        },
        caseRecent: {
          title: this.$t('test_track.recent_case'),
          url: "/test/case/recent/5",
          index: function (item) {
            return '/track/case/edit/' + item.id;
          },
          router: function (item) {}
        },
        planRecent: {
          title: this.$t('test_track.recent_plan'),
          url: "/test/plan/recent/5",
          index: function (item) {
            return '/track/plan/view/' + item.id;
          },
          router: function (item) {}
        }
      }
    },
    watch: {
      '$route'(to, from) {
        let path = to.path;
        if (path.indexOf("/track/plan/view") >= 0) {
          this.testPlanViewPath = '/track/plan/view/' + this.$route.params.planId;
          this.reload();
        }
        if (path.indexOf("/track/case/edit") >= 0) {
          this.testCaseEditPath = '/track/case/edit/' + this.$route.params.caseId;
          this.reload();
        }
      }
    },
    mounted() {
      this.isCurrentWorkspaceUser = checkoutCurrentWorkspace();
    },
    methods: {
      reload() {
        this.isRouterAlive = false;
        this.$nextTick(function () {
          this.isRouterAlive = true;
        });
      }
    }
  }

</script>

<style scoped>
  .el-divider--horizontal {
    margin: 0;
  }

  #menu-bar {
    border-bottom: 1px solid #E6E6E6;
    background-color: #FFF;
  }

  .blank_item {
    display: none;
  }

  .el-divider--horizontal {
    margin: 0;
  }
</style>
