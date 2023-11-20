<template>
  <ms-node-tree class="public-node-tree"
                v-loading="loading"
                local-suffix="test_case"
                default-label="未规划用例"
                @nodeSelectEvent="publicNodeChange"
                @filter="filter"
                :tree-nodes="publicTreeNodes"
                ref="publicNodeTree">
    <template v-slot:header>
      <ms-search-bar
        :show-operator="showOperator"
        :condition="condition"/>
    </template>
  </ms-node-tree>
</template>

<script>
import MsSearchBar from "metersphere-frontend/src/components/new-ui/MsSearchBar";
import MsNodeTree from "metersphere-frontend/src/components/new-ui/MsNodeTree";
import {getTestCasePublicNodes} from "@/api/testCase";
import {useStore} from "@/store";

const store = useStore();
export default {
  name: "TestCasePublicNodeTree",
  components: {MsNodeTree, MsSearchBar},
  props: {
    caseCondition: Object,
    showOperator: Boolean,
  },
  data() {
    return {
      publicTreeNodes: [],
      loading: false,
      condition: {
        filterText: ""
      }
    }
  },
  watch: {
    'condition.filterText'() {
      this.filter();
    },
  },
  mounted() {
    store.testCasePublicSelectNode = {};
    store.testCasePublicSelectNodeIds = [];
  },
  methods: {
    filter() {
      this.$refs.publicNodeTree.filter(this.condition.filterText);
    },
    publicNodeChange(node, nodeIds, pNodes) {
      store.testCasePublicSelectNodeIds = nodeIds;
      store.testCasePublicSelectNode = node;
      this.$emit("nodeSelectEvent", node, node.data.id === 'root' ? [] : nodeIds, pNodes);
    },
    list() {
      this.loading = true;
      getTestCasePublicNodes(this.caseCondition)
        .then(r => {
          this.loading = false;
          this.publicTreeNodes = r.data;
          if (this.$refs.publicNodeTree) {
            this.publicTreeNodes.forEach(firstLevel => {
              this.$refs.publicNodeTree.nodeExpand(firstLevel);
              if (this.$refs.publicNodeTree) {
                this.$refs.publicNodeTree.justSetCurrentKey(store.testCasePublicSelectNode.data.id);
              }
            })
          }
        });
    },
  }
}
</script>

<style scoped>
.public-node-tree :deep(.node-tree) {
  height: calc(100vh - 205px);
}
</style>
