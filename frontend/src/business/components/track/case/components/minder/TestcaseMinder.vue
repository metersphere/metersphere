<template>
  <ms-module-minder
    :tree-nodes="treeNodes"/>
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
export default {
name: "TestcaseMinder",
  components: {MsModuleMinder},
  props: {
    treeNodes: {
      type: Array,
      default() {
        return []
      }
    }
  },

}
</script>

<style scoped>

</style>
