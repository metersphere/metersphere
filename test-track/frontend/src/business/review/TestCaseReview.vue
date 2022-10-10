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
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";

export default {
  name: "TestCaseReview",
  components: {
    MsMainContainer,
    MsContainer,
    TestCaseReviewList,
    TestCaseReviewEdit
  },
  data() {
    return {

    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  mounted() {
    if (this.$route.path.indexOf("/track/review/create") >= 0){
      this.openCaseReviewEditDialog();
      this.$router.push('/track/review/all');
    }
  },
  watch: {
    '$route'(to) {
      if (to.path.indexOf("/track/review/create") >= 0){
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
      this.$refs.caseReviewList.condition = {};
      this.$refs.caseReviewList.initTableData();
    }
  }
}
</script>

<style scoped>

</style>
