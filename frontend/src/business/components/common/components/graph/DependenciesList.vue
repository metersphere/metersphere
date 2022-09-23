<template>
  <div class="dependencies-container">

    <div class="right-list">
      <relationship-list
        class="pre-list"
        :read-only="readOnly" :title="resourceType === 'TEST_CASE' ? $t('commons.relationship.pre_case') : $t('commons.relationship.pre_api')"
        relationship-type="PRE" :resource-id="resourceId"
        :version-enable="versionEnable"
        @setCount="setPreCount"
        :resource-type="resourceType" ref="preRelationshipList"/>
      <relationship-list
        class="post-list"
        :read-only="readOnly"
        :version-enable="versionEnable"
        :title="resourceType === 'TEST_CASE' ? $t('commons.relationship.post_case') : $t('commons.relationship.post_api')"
        relationship-type="POST" :resource-id="resourceId"
        @setCount="setPostCount"
        :resource-type="resourceType" ref="postRelationshipList"/>
    </div>

    <div class="left-icon">
      <el-tooltip v-xpack class="item" effect="dark" :content="$t('commons.relationship.graph')" placement="left">
        <font-awesome-icon class="graph-icon" :icon="['fas', 'sitemap']" size="lg" @click="openGraph"/>
      </el-tooltip>
    </div>

    <relationship-graph-drawer v-xpack v-permission :graph-data="graphData" @closeRelationGraph="closeRelationGraph" ref="relationshipGraph"/>

  </div>
</template>

<script>
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const relationshipGraphDrawer = requireComponent.keys().length > 0 ? requireComponent("./graph/RelationshipGraphDrawer.vue") : {};

import {getRelationshipGraph} from "@/network/graph";
import RelationshipList from "@/business/components/common/components/graph/RelationshipList";
export default {
  name: "DependenciesList",
  components: {RelationshipList,
    "relationshipGraphDrawer": relationshipGraphDrawer.default,
  },
  props: [
    'resourceId',
    'resourceType',
    'readOnly',
    'versionEnable',
  ],
  data() {
    return {
      graphData: {},
      preCount: 0,
      postCount: 0,
    }
  },
  methods: {
    open() {
      this.$refs.preRelationshipList.getTableData();
      this.$refs.postRelationshipList.getTableData();
    },
    openGraph() {
      if (!this.resourceId) {
        this.$warning(this.$t('api_test.automation.save_case_info'));
        return;
      }
      getRelationshipGraph(this.resourceId, this.resourceType,  (data) => {
        this.graphData = data;
        this.$refs.relationshipGraph.open();
        this.$emit("openDependGraphDrawer", true);
      });
    },
    closeRelationGraph() {
      this.$emit("openDependGraphDrawer", false);
    },
    setPreCount(count) {
      this.preCount = count;
      this.$emit('setCount', this.preCount + this.postCount);
    },
    setPostCount(count) {
      this.postCount = count;
      this.$emit('setCount', this.preCount + this.postCount);
    },
  }
}
</script>

<style scoped>
.post-list {
  margin-top: 20px;
}

.graph-icon {
  float: right;
  margin-right: 8px;
  color: var(--primary_color);
}

.right-list {
  width: 96%;
  display: inline-block;
}

.left-icon {
  width: 4%;
  display: inline-block;
  position: absolute;
  top: 25px;
}
</style>
