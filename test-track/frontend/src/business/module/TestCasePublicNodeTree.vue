<template>
  <ms-node-tree class="node-tree"
                v-loading="loading"
                local-suffix="test_case"
                default-label="未规划用例"
                @nodeSelectEvent="publicNodeChange"
                :tree-nodes="publicTreeNodes"
                ref="publicNodeTree"/>
</template>

<script>
import MsNodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import {getTestCasePublicNodes} from "@/api/testCase";

export default {
  name: "TestCasePublicNodeTree",
  components: {MsNodeTree},
  props: {
    caseCondition: Object
  },
  data() {
    return {
      publicTreeNodes: [],
      loading: false
    }
  },
  methods: {
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
