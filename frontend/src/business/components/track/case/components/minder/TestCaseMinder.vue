<template>
  <ms-module-minder
    :tree-nodes="treeNodes"/>
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
export default {
name: "TestCaseMinder",
  components: {MsModuleMinder},
  data() {
    return{
      testCase: [],
      dataMap: new Map()
    }
  },
  props: {
    treeNodes: {
      type: Array,
      default() {
        return []
      }
    },
    condition: Object,
    projectId: String
  },
  mounted() {
    // this.getTestCases();
  },
  methods: {
    getTestCases() {
      if (this.projectId) {
        this.result = this.$get('/test/case/list/detail/' + this.projectId,response => {
          this.testCase = response.data;
          console.log(this.testCase)
          this.parse();
        });
      }
    },
    parse() {
      this.testCase.forEach(item => {
        let mapItem = this.dataMap.get(item.moduleId);
        let nodeItem = {
          id: item.id,
          name: item.name,

        }
        if (mapItem) {
          mapItem.push(item);
        } else {
          mapItem = [];
          mapItem.push(item);
        }
        if (item.tags && item.tags.length > 0) {
          item.tags = JSON.parse(item.tags);
        }
      })
    }
  },
}
</script>

<style scoped>

</style>
