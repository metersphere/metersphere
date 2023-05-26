<template>
  <div>
    <ms-test-plan-header-bar>
      <template v-slot:info>
        <select-menu
          :data="testReviews"
          :current-data="currentReview"
          :title="$t('test_track.review_view.review')"
          @dataChange="changeReview"/>
      </template>
      <template v-slot:menu>
        <el-menu v-if="isMenuShow" :active-text-color="color"
                 class="el-menu-demo header-menu" mode="horizontal" @select="handleSelect"
                 :default-active="activeIndex">
          <el-menu-item index="functional">{{ $t('test_track.functional_test_case') }}</el-menu-item>
        </el-menu>
      </template>
    </ms-test-plan-header-bar>
    <test-review-function v-if="activeIndex === 'functional'" :redirectCharType="redirectCharType"
                          :clickType="clickType" :review-id="reviewId" :version-enable="versionEnable"
                          ref="testReviewFunction"/>
  </div>

</template>

<script>


import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsAsideContainer from "metersphere-frontend/src/components/MsAsideContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import NodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import SelectMenu from "../../common/SelectMenu";
import TestReviewRelevance from "./components/TestReviewRelevance";
import MsTestPlanHeaderBar from "./components/head/TestPlanHeaderBar";
import TestReviewFunction from "@/business/review/view/components/TestReviewFunction";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {hasLicense} from "metersphere-frontend/src/utils/permission";
import {PROJECT_ID} from "metersphere-frontend/src/utils/constants";
import {getTestReviewTestCase, testReviewListAll} from "@/api/test-review";
import {testCaseNodeListReview} from "@/api/test-case-node";
import {versionEnableByProjectId} from "@/api/project";

export default {
  name: "TestCaseReviewView",
  components: {
    TestReviewFunction,
    MsTestPlanHeaderBar,
    MsMainContainer,
    MsAsideContainer,
    MsContainer,
    NodeTree,
    TestReviewRelevance,
    SelectMenu
  },
  data() {
    return {
      result: {},
      testReviews: [],
      currentReview: {},
      selectNodeIds: [],
      selectParentNodes: [],
      treeNodes: [],
      currentPlan: {},
      activeIndex: "functional",
      isMenuShow: true,
      //报表跳转过来的参数-通过哪个图表跳转的
      redirectCharType: '',
      //报表跳转过来的参数-通过哪种数据跳转的
      clickType: '',
      projectId: null,
      versionEnable: false,
    }
  },
  computed: {
    reviewId: function () {
      return this.$route.params.reviewId;
    },
    color: function () {
      return `var(--primary_color)`
    }
  },
  created() {
    let projectId = this.$route.query.projectId;
    if (projectId) {
      sessionStorage.setItem(PROJECT_ID, projectId);
    }
    this.$EventBus.$on('projectChange', this.handleProjectChange);
  },
  destroyed() {
    this.$EventBus.$off('projectChange', this.handleProjectChange);
  },
  mounted() {
    this.initData();
    this.openTestCaseEdit(this.$route.path);
    this.checkVersionEnable();
  },
  beforeRouteLeave(to, from, next) {
    if (!this.$refs.testReviewFunction) {
      next();
    } else if (this.$refs.testReviewFunction.handleBeforeRouteLeave(to)) {
      next();
    }
  },
  watch: {
    '$route'(to, from) {
      this.openTestCaseEdit(to.path);
    },
    reviewId() {
      this.initData();
    }
  },
  activated() {
    this.genRedirectParam();
  },
  methods: {
    handleProjectChange() {
      if (this.$route.path.indexOf("track/review") > -1) {
        this.$nextTick(() => {
          this.$router.push('/track/review/all');
        });
      }
    },
    handleSelect(key) {
      this.activeIndex = key;
    },
    genRedirectParam() {
      this.redirectCharType = this.$route.params.charType;
      this.clickType = this.$route.params.clickType;
      if (this.redirectCharType != "") {
        if (this.redirectCharType == 'scenario') {
          this.activeIndex = 'api';
        } else if (this.redirectCharType != null && this.redirectCharType != '') {
          this.activeIndex = this.redirectCharType;
        }
      } else {
        this.activeIndex = "functional";
      }
    },
    initData() {
      this.projectId = getCurrentProjectID();
      this.getTestReviews();
      this.getNodeTreeByReviewId();
    },
    openTestReviewRelevanceDialog() {
      this.$refs.testReviewRelevance.openTestReviewRelevanceDialog();
    },
    getTestReviews() {
      testReviewListAll()
        .then((response) => {
          this.testReviews = response.data;
          this.testReviews.forEach(review => {
            if (this.reviewId && review.id === this.reviewId) {
              this.currentReview = review;
            }
          });
        })
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
      this.selectParentNodes = pNodes;
    },
    changeReview(review) {
      this.currentReview = review;
      this.$router.push('/track/review/view/' + review.id);
    },
    getNodeTreeByReviewId() {
      if (this.reviewId) {
        testCaseNodeListReview(this.reviewId)
          .then((response) => {
            this.treeNodes = response.data;
          })
      }
    },
    openTestCaseEdit(path) {
      if (path.indexOf("/review/view/edit") >= 0) {
        let caseId = this.$route.params.caseId;
        getTestReviewTestCase(caseId)
          .then((response) => {
            let testCase = response.data;
            if (testCase) {
              this.$refs.testPlanTestCaseList.handleEdit(testCase);
              this.$router.push('/track/review/view/' + testCase.reviewId);
            }
          })
      }
    },
    reloadMenu() {
      this.isMenuShow = false;
      this.$nextTick(() => {
        this.isMenuShow = true;
      });
    },
    checkVersionEnable() {
      if (!this.projectId) {
        return;
      }
      if (hasLicense()) {
        versionEnableByProjectId(this.projectId)
          .then((response) => {
            this.versionEnable = response.data;
          })
      }
    },
  }
}
</script>

<style scoped>

:deep(.ms-main-container) {
  height: calc(100vh - 93px);
}

:deep(.ms-aside-container ){
  height: calc(100vh - 93px) !important;
  margin-top: 1px;
}

.header-menu.el-menu--horizontal > li {
  height: 49px;
  line-height: 50px;
  color: dimgray;
}
</style>
