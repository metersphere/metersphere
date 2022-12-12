<template>
  <ms-node-tree class="node-tree"
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
  methods: {
    filter() {
      this.$refs.publicNodeTree.filter(this.condition.filterText);
    },
    publicNodeChange(node, nodeIds, pNodes) {
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
            })
          }
        });
    },
  }
}
</script>

<style scoped>

</style>
