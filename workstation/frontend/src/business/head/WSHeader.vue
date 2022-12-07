<template>
  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
                 :default-active="pathName"
                >
          <el-menu-item :index="'/workstation/dashboard'">
            {{ $t("workstation.dash_board") }}
          </el-menu-item>
          <el-menu-item :index="'/workstation/upcoming'" >
            {{ $t("workstation.upcoming") }}
          </el-menu-item>

          <el-menu-item :index="'/workstation/focus'">
            {{ $t('workstation.focus') }}
          </el-menu-item>
          <el-menu-item :index="'/workstation/creation'" >
            {{ $t('workstation.creation') }}
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
import MsHeaderRightMenus from "metersphere-frontend/src/components/layout/HeaderRightMenus";
export default {
  components: {MsHeaderRightMenus},
  data(){
    return {
      testPlanViewPath: '',
      isRouterAlive: true,
      pathName: '',
      testCaseEditPath: '',
      testCaseReviewEditPath: '',
      testCaseProjectPath: '',
      isProjectActivation: true,
    }
  },
  watch: {
    '$route': {
      immediate: true,
      handler(to, from) {
        if (to.params && to.params.reviewId) {
          //this.pathName = '/track/review/all';
        } else if (to.params && to.params.planId) {
         // this.pathName = '/track/plan/all';
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
}
</script>
<style scoped>
#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}
.header-menu{
  margin-left: 190px;
}
.el-menu-item {
  padding: 0 10px;
}
</style>
