<template>
  <ms-node-tree class="node-tree"
                v-loading="result.loading"
                local-suffix="test_case"
                default-label="未规划用例"
                :current-node-key="currentNodeKey"
                @nodeSelectEvent="publicNodeChange"
                :tree-nodes="trashTreeNodes"
                ref="trashNodeTree"/>
</template>

<script>
import MsNodeTree from "@/business/components/track/common/NodeTree";
import {getTestCaseTrashNodes} from "@/network/testCase";

export default {
  name: "TestCaseTrashNodeTree",
  components: {MsNodeTree},
  props: {
    caseCondition: Object
  },
  data() {
    return {
      currentNodeKey: null,
      trashTreeNodes: [],
      result: {}
    }
  },
  methods: {
    publicNodeChange(node, nodeIds, pNodes) {
      this.$emit("nodeSelectEvent", node, node.data.id === 'root' ? [] : nodeIds, pNodes);
      this.nohupReloadTree(node.data.id);
    },
    nohupReloadTree(selectNodeId) {
      getTestCaseTrashNodes(this.caseCondition, data => {
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
            this.currentNodeKey = selectNodeId;
          }
        })
      });
    },
    list() {
      this.currentNodeKey = null;
      this.result = getTestCaseTrashNodes(this.caseCondition, data => {
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
