<template>
  <ms-node-tree class="node-tree"
                v-loading="loading"
                local-suffix="test_case"
                default-label="未规划用例"
                @nodeSelectEvent="publicNodeChange"
                @filter="filter"
                :tree-nodes="trashTreeNodes"
                ref="trashNodeTree">
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
import {getTestCaseTrashNodes} from "@/api/testCase";

export default {
  name: "TestCaseTrashNodeTree",
  components: {MsNodeTree, MsSearchBar},
  props: {
    caseCondition: Object,
    showOperator: Boolean,
  },
  data() {
    return {
      trashTreeNodes: [],
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
      this.$refs.trashNodeTree.filter(this.condition.filterText);
    },
    publicNodeChange(node, nodeIds, pNodes) {
      this.$emit("nodeSelectEvent", node, node.data.id === 'root' ? [] : nodeIds, pNodes);
      this.nohupReloadTree(node.data.id);
    },
    nohupReloadTree(selectNodeId) {
      this.loading = true;
      getTestCaseTrashNodes(this.caseCondition)
        .then(r => {
          this.loading = false;
          let data = r.data;
          if (data && data.length > 0) {
            this.trashTreeNodes = data[0].children;
          } else {
            this.trashTreeNodes = [];
          }
          this.$nextTick(() => {
            if (this.$refs.trashNodeTree) {
              this.trashTreeNodes.forEach(firstLevel => {
                this.$refs.trashNodeTree.nodeExpand(firstLevel);
              })
            }
            if (selectNodeId) {
              this.$refs.trashNodeTree.justSetCurrentKey(selectNodeId);
            }
          });
        });
    },
    list() {
      this.loading = true;
      getTestCaseTrashNodes(this.caseCondition)
        .then((r) => {
          this.loading = false;
          let data = r.data;
          if (data && data.length > 0) {
            this.trashTreeNodes = data[0].children;
          } else {
            this.trashTreeNodes = [];
          }
          if (this.$refs.trashNodeTree) {
            this.trashTreeNodes.forEach(firstLevel => {
              this.$refs.trashNodeTree.nodeExpand(firstLevel);
            })
          }
        });
    },
  }
}
</script>

<style scoped>

</style>
