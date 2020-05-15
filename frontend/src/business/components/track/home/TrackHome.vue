<template>
  <div class="container">
    <div class="main-content">
      <el-row>
        <el-col :span="15">
          <related-test-plan-list ref="relatedTestPlanList"/>
        </el-col>
        <el-col :span="9">
          <test-case-side-list :title="'最近测试'" ref="testCaseRecentList"/>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
    import RelatedTestPlanList from "./components/RelatedTestPlanList";
    import TestCaseSideList from "./components/TestCaseSideList";
    export default {
      name: "TrackHome",
      components: {TestCaseSideList, RelatedTestPlanList},
      watch: {
        '$route'(to,from) {
          if (to.path.indexOf('/track/home') > -1) {
            this.innitData();
          }
        }
      },
      methods: {
        innitData() {
          this.$refs.relatedTestPlanList.initTableData();
          this.$refs.testCaseRecentList.initTableData();
        }
      }
    }
</script>

<style scoped>

  .main-content >>> .el-table {
    cursor:pointer;
  }

</style>
