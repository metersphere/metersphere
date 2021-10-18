<template>
  <div class="dependencies-container">
    <el-main>
      <i class="el-icon-view" @click="openGraph"></i>
    </el-main>

    <test-case-relationship-list :title="'前置对象'" relationship-type="PRE" :case-id="caseId" ref="preRelationshipList"/>
    <test-case-relationship-list :title="'后置对象'" relationship-type="POST" :case-id="caseId" ref="postRelationshipList"/>

    <relationship-graph-drawer :graph-data="graphData" ref="relationshipGraph"/>

  </div>
</template>

<script>
import TestCaseRelationshipList from "@/business/components/track/case/components/TestCaseRelationshipList";
import RelationshipGraphDrawer from "@/business/components/xpack/graph/RelationshipGraphDrawer";
import {getRelationshipGraph} from "@/network/graph";
export default {
  name: "TestCaseDependencies",
  components: {RelationshipGraphDrawer, TestCaseRelationshipList},
  props: [
    'caseId'
  ],
  data() {
    return {
      graphData: {}
    }
  },
  methods: {
    open() {
      this.$refs.preRelationshipList.getTableData();
      this.$refs.postRelationshipList.getTableData();
    },
    openGraph() {
      getRelationshipGraph(this.caseId, 'TEST_CASE',  (data) => {
        this.graphData = data;
        this.$refs.relationshipGraph.open();
      });
    }
  }
}
</script>

<style scoped>
.dependencies-container .el-main:nth-child(3) {
  margin-top: 20px;
}
</style>
