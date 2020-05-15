<template>
  <div class="container">
    <div class="main-content">
      <el-row>
        <el-col :span="12">
          <related-test-plan-list/>
        </el-col>
        <el-col :span="12">
          <el-row>
            <el-col>
              <recent-test-case-list/>
            </el-col>
            <el-col>
              <pending-test-case-list/>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
    import PendingTestCaseList from "./components/PendingTestCaseList";
    import RecentTestCaseList from "./components/RecentTestCaseList";
    import RelatedTestPlanList from "./components/RelatedTestPlanList";
    export default {
      name: "TrackHome",
      components: {RelatedTestPlanList, RecentTestCaseList, PendingTestCaseList}
    }
</script>

<style scoped>

</style>
