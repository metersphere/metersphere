<template>
  <div class="dependencies-container">
    <el-main v-xpack>
      <i class="el-icon-view" @click="openGraph"></i>
    </el-main>

    <relationship-list :read-only="readOnly" :title="$t('commons.relationship.pre')" relationship-type="PRE" :resource-id="resourceId" :resource-type="resourceType" ref="preRelationshipList"/>
    <relationship-list :read-only="readOnly" :title="$t('commons.relationship.post')" relationship-type="POST" :resource-id="resourceId" :resource-type="resourceType" ref="postRelationshipList"/>

    <relationship-graph-drawer v-permission :graph-data="graphData" ref="relationshipGraph"/>

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
    'resourceType',
    'readOnly'
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
.dependencies-container .el-main {
  margin-top: 20px;
}
</style>
