<template>

  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <project-switch :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
                 :default-active="pathName">
          <el-menu-item v-for="(menu) in menus" :key="menu.path" :index="menu.path" v-permission="[menu.permission]">
            {{ menu.name }}
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

import ProjectSwitch from "metersphere-frontend/src/components/head/ProjectSwitch";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import MsHeaderRightMenus from "metersphere-frontend/src/components/layout/HeaderRightMenus";
import {PROJECT_NAME} from "metersphere-frontend/src/utils/constants";
import {hasPermission} from "@/business/utils/sdk-utils";

export default {
  name: "TrackHeaderMenus",
  components: {ProjectSwitch, MsHeaderRightMenus},
  data() {
    return {
      testPlanViewPath: '',
      isRouterAlive: true,
      testCaseEditPath: '',
      testCaseReviewEditPath: '',
      testCaseProjectPath: '',
      isProjectActivation: true,
      currentProject: sessionStorage.getItem(PROJECT_NAME),
      pathName: '',
      menus: [
        {
          path: '/track/home',
          name: this.$t('i18n.home'),
          permission: 'PROJECT_TRACK_HOME:READ'
        },
        {
          path: '/track/case/all?projectId=' + this.getProjectId(),
          name: this.$t('test_track.case.test_case'),
          permission: 'PROJECT_TRACK_CASE:READ'
        },
        {
          path: '/track/review/all',
          name: this.$t('test_track.review.test_review'),
          permission: 'PROJECT_TRACK_REVIEW:READ'
        },
        {
          path: '/track/plan/all',
          name: this.$t('test_track.plan.test_plan'),
          permission: 'PROJECT_TRACK_PLAN:READ'
        },
        {
          path: '/track/issue',
          name: this.$t('test_track.issue.issue_management'),
          permission: 'PROJECT_TRACK_ISSUE:READ'
        },
        {
          path: '/track/testPlan/reportList',
          name: this.$t('commons.report'),
          permission: 'PROJECT_TRACK_REPORT:READ'
        }
      ]
    };
  },
  watch: {
    '$route': {
      immediate: true,
      handler(to, from) {
        if (to.path.indexOf("/track/review") >= 0) {
          this.pathName = '/track/review/all';
        } else if (to.path.indexOf("/track/plan") >= 0) {
          this.pathName = '/track/plan/all';
        } else if (to.path.indexOf("/track/case") >= 0) {
          this.pathName = '/track/case/all?projectId=' + this.getProjectId();
        } else if(to.path.indexOf("/track/home") >= 0) {
          // 默认跳转到首页
          for (let menu of this.menus) {
            if (hasPermission(menu.permission)) {
              // 如果有首页的权限则不处理
              if(menu.path.indexOf("/track/home") >= 0) {
                break;
              } else {
                // 否则跳转到第一个有权限的页面
                this.pathName = menu.path;
                this.$router.push(this.pathName);
                break;
              }
            }
          }
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
    getProjectId() {
      return getCurrentProjectID();
    }
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

.deactivation :deep(.el-submenu__title) {
  border-bottom: white !important;
}

.el-menu-item {
  padding: 0 10px;
}
</style>
