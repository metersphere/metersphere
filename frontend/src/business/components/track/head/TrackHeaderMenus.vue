<template>

  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <project-change :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
                 :default-active="pathName">
          <el-menu-item :index="'/track/home'">
            {{ $t("i18n.home") }}
          </el-menu-item>
          <el-menu-item :index="'/track/case/all'" v-permission="['PROJECT_TRACK_CASE:READ']">
            {{ $t("test_track.case.test_case") }}
          </el-menu-item>

          <el-menu-item :index="'/track/review/all'" v-permission="['PROJECT_TRACK_REVIEW:READ']"
                        popper-class="submenu">
            {{ $t('test_track.review.test_review') }}
          </el-menu-item>
          <el-menu-item :index="'/track/plan/all'" v-permission="['PROJECT_TRACK_PLAN:READ']"
                        popper-class="submenu">
            {{ $t('test_track.plan.test_plan') }}
          </el-menu-item>

          <el-menu-item :index="'/track/issue'" popper-class="submenu" v-permission="['PROJECT_TRACK_ISSUE:READ']">
            {{ $t('test_track.issue.issue_management') }}
          </el-menu-item>

          <el-menu-item :index="'/track/testPlan/reportList'" popper-class="submenu">
            {{ $t("commons.report") }}
          </el-menu-item>
        </el-menu>
      </el-col>
      <el-col :span="8"/>
    </el-row>
  </div>

</template>
<script>

import MsShowAll from "../../common/head/ShowAll";
import MsRecentList from "../../common/head/RecentList";
import MsCreateButton from "../../common/head/CreateButton";
import SearchList from "@/business/components/common/head/SearchList";
import ProjectChange from "@/business/components/common/head/ProjectSwitch";
import {getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "TrackHeaderMenus",
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

/*.project-change {*/
/*  height: 40px;*/
/*  line-height: 40px;*/
/*  color: inherit;*/
/*  margin-left: 20px;*/
/*}*/

</style>
