<template>

  <div id="menu-bar" v-if="isRouterAlive">
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

          <el-menu-item :index="'/project/env'" v-permission="['PROJECT_ENVIRONMENT:READ']"
                        popper-class="submenu">
            {{ $t('project.env') }}
          </el-menu-item>
          <el-menu-item :index="'/project/file/manage'"
                        v-permission="['PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE']"
                        popper-class="submenu">
            {{ $t('project.file_manage') }}
          </el-menu-item>

          <el-menu-item :index="'/project/log'" popper-class="submenu" v-permission="['PROJECT_OPERATING_LOG:READ']">
            {{ $t('project.log') }}
          </el-menu-item>

          <el-menu-item :index="'/project/code/segment'" popper-class="submenu"
                        v-permission="['PROJECT_CUSTOM_CODE:READ']">
            {{ $t('project.code_segment.code_segment') }}
          </el-menu-item>

          <el-menu-item :index="'/project/version'" popper-class="submenu" disabled class="hidden-sm-and-down">
            {{ $t('project.version_manage') }}
          </el-menu-item>

          <el-menu-item :index="'/project/app/manage'" popper-class="submenu" disabled class="hidden-sm-and-down">
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
import SearchList from "@/business/components/common/head/SearchList";
import ProjectChange from "@/business/components/common/head/ProjectSwitch";
import {getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "ProjectHeaderMenus",
  components: {ProjectChange, SearchList, MsShowAll, MsRecentList, MsCreateButton},
  data() {
    return {
      testPlanViewPath: '',
      isRouterAlive: true,
      testCaseEditPath: '',
      testCaseReviewEditPath: '',
      testCaseProjectPath: '',
      isProjectActivation: true,
      currentProject: '',
      caseRecent: {
        title: this.$t('test_track.recent_case'),
        url: "/test/case/recent/5",
        index: function (item) {
          return '/track/case/edit/' + item.id;
        },
        router: function (item) {
        }
      },
      reviewRecent: {
        title: this.$t('test_track.recent_review'),
        url: "/test/case/review/recent/5",
        index: function (item) {
          return '/track/review/view/' + item.id;
        },
        router: function (item) {
        }
      },
      planRecent: {
        title: this.$t('test_track.recent_plan'),
        url: getCurrentProjectID() === '' ? "/test/plan/recent/5/" + undefined : "/test/plan/recent/5/" + getCurrentProjectID(),
        index: function (item) {
          return '/track/plan/view/' + item.id;
        },
        router: function (item) {
        }
      },
      pathName: '',
    };
  },
  watch: {
    '$route': {
      immediate: true,
      handler(to, from) {
        if (to.params && to.params.reviewId) {
          this.pathName = '/track/review/all';
        } else if (to.params && to.params.planId) {
          this.pathName = '/track/plan/all';
        } else {
          this.pathName = to.path;
        }
        this.init();
      }
    }
  },
  mounted() {
    this.init();
  },
  methods: {
    reload() {
      this.isRouterAlive = false;
      this.$nextTick(function () {
        this.isRouterAlive = true;
      });
    },
    init() {
      let path = this.$route.path;
      if (path.indexOf("/track/plan/view") >= 0) {
        this.testPlanViewPath = path;
        this.reload();
      }
      if (path.indexOf("/track/case/edit") >= 0) {
        this.testCaseEditPath = path;
        this.reload();
      }
      if (path.indexOf("/track/review/view") >= 0) {
        this.testCaseReviewEditPath = path;
        this.reload();
      }
    },

  },
  beforeDestroy() {
  }
};

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

.deactivation >>> .el-submenu__title {
  border-bottom: white !important;
}

</style>
