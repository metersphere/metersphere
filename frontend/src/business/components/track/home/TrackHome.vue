<template>
  <div class="container">
    <div class="main-content">
      <el-row>
        <el-col :span="15">
          <related-test-plan-list ref="relatedTestPlanList"/>
        </el-col>
        <el-col :span="9">
          <el-row>
            <el-col>
              <test-case-side-list :title="'最近测试'" :type="'recent'" ref="testCaseRecentList"/>
            </el-col>
            <el-col>
              <test-case-side-list :title="'待完成'" :type="'pending'" ref="testCasePendingList"/>
            </el-col>
          </el-row>
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
          this.$refs.testCasePendingList.initTableData();
        }
      }
    }
</script>

<style scoped>

  .main-content >>> .el-table {
    cursor:pointer;
  }

</style>
