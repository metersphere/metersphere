<template>
  <ms-node-tree class="node-tree"
                v-loading="loading"
                local-suffix="test_case"
                default-label="未规划用例"
                @nodeSelectEvent="publicNodeChange"
                :tree-nodes="trashTreeNodes"
                ref="trashNodeTree"/>
</template>

<script>
import MsNodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import {getTestCaseTrashNodes} from "@/api/testCase";

export default {
  name: "TestCaseTrashNodeTree",
  components: {MsNodeTree},
  props: {
    caseCondition: Object
  },
  data() {
    return {
      trashTreeNodes: [],
      loading: false
    }
  },
  methods: {
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
