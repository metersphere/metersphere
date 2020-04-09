<template>

  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <el-col :span="8">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router :default-active='$route.path'>
          <el-menu-item :index="'/performance/home'">
            {{ $t("i18n.home") }}
          </el-menu-item>

          <el-submenu v-if="isCurrentWorkspaceUser"
                      index="3" popper-class="submenu" v-permission="['test_manager']">
            <template v-slot:title>{{$t('commons.project')}}</template>
            <ms-recent-list :options="projectRecent"/>
            <el-divider/>
            <ms-show-all :index="'/performance/project/all'"/>
            <ms-create-button :index="'/performance/project/create'" :title="$t('project.create')"/>
          </el-submenu>

          <el-submenu v-if="isCurrentWorkspaceUser"
                      index="4" popper-class="submenu" v-permission="['test_manager', 'test_user']">
            <template v-slot:title>{{$t('commons.test')}}</template>
            <ms-recent-list :options="testRecent"/>
            <el-divider/>
            <ms-show-all :index="'/performance/test/all'"/>
            <ms-create-button :index="'/performance/test/create'" :title="$t('load_test.create')"/>
            <el-menu-item :index="testCaseProjectPath" class="blank_item"></el-menu-item>
            <el-menu-item :index="testEditPath" class="blank_item"></el-menu-item>
          </el-submenu>

          <el-submenu v-if="isCurrentWorkspaceUser"
                      index="5" popper-class="submenu" v-permission="['test_manager', 'test_user', 'test_viewer']">
            <template v-slot:title>{{$t('commons.report')}}</template>
            <ms-recent-list :options="reportRecent"/>
            <el-divider/>
            <ms-show-all :index="'/performance/report/all'"/>
          </el-submenu>
        </el-menu>
      </el-col>
      <el-col :span="8">
        <el-row type="flex" justify="center">
          <ms-create-test :show="isCurrentWorkspaceUser" :to="'/performance/test/create'"/>
        </el-row>
      </el-col>
      <el-col :span="8"/>
    </el-row>
  </div>

</template>

<script>

  import {checkoutCurrentWorkspace} from "../../../../common/js/utils";
  import MsCreateTest from "../../common/head/CreateTest";
  import MsRecentList from "../../common/head/RecentList";
  import MsCreateButton from "../../common/head/CreateButton";
  import MsShowAll from "../../common/head/ShowAll";

  export default {
    name: "PerformanceHeaderMenus",
    components: {
      MsCreateButton,
      MsShowAll,
      MsRecentList,
      MsCreateTest
    },
    data() {
      return {
        isCurrentWorkspaceUser: false,
        testCaseProjectPath: '',
        testEditPath: '',
        reportViewPath: '',
        isRouterAlive: true,
        projectRecent: {
          title: this.$t('project.recent'),
          url: "/project/recent/5",
          index(item) {
            return '/performance/test/' + item.id;
          },
          router(item) {
            return {name: 'perPlan', params: {projectId: item.id, projectName: item.name}}
          }
        },
        testRecent: {
          title: this.$t('load_test.recent'),
          url: "/performance/recent/5",
          index(item) {
            return '/performance/test/edit/' + item.id;
          },
          router(item) {
          }
        },
        reportRecent: {
          title: this.$t('report.recent'),
          url: "/performance/report/recent/5",
          index(item) {
            return '/performance/report/view/' + item.id;
          },
          router(item) {
          }
        }
      }
    },
    mounted() {
      this.isCurrentWorkspaceUser = checkoutCurrentWorkspace();
    },
    watch: {
      '$route'(to, from) {
        let path = to.path;
        //激活菜单栏
        if (path.indexOf("/performance/test/") >= 0) {
          this.testCaseProjectPath = '/performance/test/' + this.$route.params.projectId;
          this.reload();
        }
        if (path.indexOf("/performance/test/edit/") >= 0) {
          this.testEditPath = '/performance/test/edit/' + this.$route.params.testId;
          this.reload();
        }
        if (path.indexOf("/performance/report/view/") >= 0) {
          this.reportViewPath = '/performance/report/view/' + this.$route.params.reportId;
          this.reload();
        }
      }
    },
    methods: {
      reload() {
        this.isRouterAlive = false;
        this.$nextTick(function () {
          this.isRouterAlive = true;
        })
      }
    }
  }

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

  .blank_item {
    display: none;
  }
</style>
