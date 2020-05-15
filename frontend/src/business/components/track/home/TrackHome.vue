<template>
  <div class="container">
    <div class="main-content">
      <el-row>
        <el-col :span="15">
          <related-test-plan-list/>
        </el-col>
        <el-col :span="9">
          <el-row>
            <el-col>
              <test-case-side-list :title="'最近测试'" :type="'recent'"/>
            </el-col>
            <el-col>
              <test-case-side-list :title="'待完成'" :type="'pending'"/>
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
      components: {TestCaseSideList, RelatedTestPlanList}
    }
</script>

<style scoped>

  .main-content >>> .el-table {
    cursor:pointer;
  }

</style>
