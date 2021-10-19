<template>
  <div class="dependencies-container">
    <el-main v-xpack>
      <i class="el-icon-view" @click="openGraph"></i>
    </el-main>

    <relationship-list :title="'前置对象'" relationship-type="PRE" :resource-id="resourceId" :resource-type="resourceType" ref="preRelationshipList"/>
    <relationship-list :title="'后置对象'" relationship-type="POST" :resource-id="resourceId" :resource-type="resourceType" ref="postRelationshipList"/>

    <relationship-graph-drawer :graph-data="graphData" ref="relationshipGraph"/>

  </div>
</template>

<script>
import RelationshipGraphDrawer from "@/business/components/xpack/graph/RelationshipGraphDrawer";
import {getRelationshipGraph} from "@/network/graph";
import RelationshipList from "@/business/components/common/components/graph/RelationshipList";
export default {
  name: "DependenciesList",
  components: {RelationshipList, RelationshipGraphDrawer},
  props: [
    'resourceId',
    'resourceType'
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
      getRelationshipGraph(this.resourceId, this.resourceType,  (data) => {
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
