<template>
  <ms-container>

    <ms-main-container>
      <test-case-review-list
        @openCaseReviewEditDialog="openCaseReviewEditDialog"
        @caseReviewEdit="openCaseReviewEditDialog"
        ref="caseReviewList"/>
    </ms-main-container>

    <test-case-review-edit ref="caseReviewEditDialog" @refresh="refreshCaseReviewList"/>

  </ms-container>
</template>

<script>
import TestCaseReviewList from "./components/TestCaseReviewList";
import TestCaseReviewEdit from "./components/TestCaseReviewEdit";
import MsMainContainer from "../../common/components/MsMainContainer";
import MsContainer from "../../common/components/MsContainer";
import {getCurrentProjectID} from "@/common/js/utils";
import {TEST_REVIEW} from "@/business/components/common/components/search/search-components";

export default {
  name: "TestCaseReview",
  components: {
    MsMainContainer,
    MsContainer,
    TestCaseReviewList,
    TestCaseReviewEdit
  },
  data() {
    return {}
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  mounted() {
    if (this.$route.path.indexOf("/track/review/create") >= 0) {
      this.openCaseReviewEditDialog();
      this.$router.push('/track/review/all');
    }
  },
  watch: {
    '$route'(to) {
      if (to.path.indexOf("/track/review/create") >= 0) {
        if (!this.projectId) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        this.openCaseReviewEditDialog();
        this.$router.push('/track/review/all');
      }
    }
  },
  methods: {
    openCaseReviewEditDialog(data) {
      this.$refs.caseReviewEditDialog.openCaseReviewEditDialog(data);
    },
    refreshCaseReviewList() {
      this.$refs.caseReviewList.condition = {TEST_REVIEW};
      this.$refs.caseReviewList.initTableData();
    }
  }
}
</script>

<style scoped>

</style>
