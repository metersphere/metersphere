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
                          :clickType="clickType" :review-id="reviewId"></test-review-function>
  </div>

</template>

<script>


import MsMainContainer from "../../../common/components/MsMainContainer";
import MsAsideContainer from "../../../common/components/MsAsideContainer";
import MsContainer from "../../../common/components/MsContainer";
import NodeTree from "../../common/NodeTree";
import TestReviewTestCaseList from "./components/TestReviewTestCaseList";
import SelectMenu from "../../common/SelectMenu";
import TestReviewRelevance from "./components/TestReviewRelevance";
import MsTestPlanHeaderBar from "@/business/components/track/plan/view/comonents/head/TestPlanHeaderBar";
import TestReviewFunction from "@/business/components/track/review/view/components/TestReviewFunction";
import TestReviewApi from "@/business/components/track/review/view/components/TestReviewApi";
import TestReviewLoad from "@/business/components/track/review/view/components/TestReviewLoad";

export default {
  name: "TestCaseReviewView",
  components: {
    TestReviewLoad,
    TestReviewApi,
    TestReviewFunction,
    MsTestPlanHeaderBar,
    MsMainContainer,
    MsAsideContainer,
    MsContainer,
    NodeTree,
    TestReviewTestCaseList,
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
    this.$EventBus.$on('projectChange', () => {
      if (this.$route.name === 'testCaseReviewView') {
        this.$router.push('/track/review/all');
      }
    });
  },
  mounted() {
    this.initData();
    this.openTestCaseEdit(this.$route.path);
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
      this.getTestReviews();
      this.getNodeTreeByReviewId();
    },
    openTestReviewRelevanceDialog() {
      this.$refs.testReviewRelevance.openTestReviewRelevanceDialog();
    },
    getTestReviews() {
      this.result = this.$post('/test/case/review/list/all', {}, response => {
        this.testReviews = response.data;
        this.testReviews.forEach(review => {
          if (this.reviewId && review.id === this.reviewId) {
            this.currentReview = review;
          }
        });
      });
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
        this.result = this.$get("/case/node/list/review/" + this.reviewId, response => {
          this.treeNodes = response.data;
        });
      }
    },
    openTestCaseEdit(path) {
      if (path.indexOf("/review/view/edit") >= 0) {
        let caseId = this.$route.params.caseId;
        this.$get('/test/review/case/get/' + caseId, response => {
          let testCase = response.data;
          if (testCase) {
            this.$refs.testPlanTestCaseList.handleEdit(testCase);
            this.$router.push('/track/review/view/' + testCase.reviewId);
          }
        });
      }
    },
    reloadMenu() {
      this.isMenuShow = false;
      this.$nextTick(() => {
        this.isMenuShow = true;
      });
    }
  }
}
</script>

<style scoped>

/deep/ .ms-main-container {
  height: calc(100vh - 80px - 53px);
}

/deep/ .ms-aside-container {
  height: calc(100vh - 80px - 53px);
  margin-top: 1px;
}

.header-menu.el-menu--horizontal > li {
  height: 49px;
  line-height: 50px;
  color: dimgray;
}
</style>
