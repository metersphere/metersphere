<template>
  <ms-module-minder
    v-loading="result.loading"
    :tree-nodes="treeNodes"
    :data-map="dataMap"
    :tags="tags"
    @save="save"
  />
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
import {getTestCaseDataMap} from "@/business/components/track/common/minder/minderUtils";
export default {
name: "TestPlanMinder",
  components: {MsModuleMinder},
  data() {
    return{
      dataMap: new Map(),
      result: {},
      tags: ['通过', '失败', '阻塞', '跳过'],
    }
  },
  props: {
    treeNodes: {
      type: Array,
      default() {
        return []
      }
    },
    selectNodeIds: {
      type: Array
    },
    planId: {
      type: String
    },
    projectId: String
  },
  mounted() {
    this.$nextTick(() => {
      this.getTestCases();
    })
  },
  methods: {
    getTestCases() {
      if (this.projectId) {
        this.result = this.$get('/test/plan/case/list/minder/' + this.planId, response => {
          this.dataMap = getTestCaseDataMap(response.data, true, (data, item) => {
            if (item.stats === 'Pass') {
              data.resource.push("通过");
            } else if (item.reviewStatus === 'Failure') {
              data.resource.push("失败");
            } else if (item.reviewStatus === 'Blocking') {
              data.resource.push("阻塞");
            } else if (item.reviewStatus === 'Skip') {
              data.resource.push("跳过");
            } else {
              data.resource.push("未开始");
            }
          });
        });
      }
    },
    save(data) {
      let saveCases = [];
      this.buildSaveCase(data.root, saveCases);
      this.result = this.$post('/test/plan/case/minder/edit', saveCases, () => {
        this.$success(this.$t('commons.save_success'));
      });
    },
    buildSaveCase(root, saveCases) {
      let data = root.data;
      if (data.resource && data.resource.indexOf("用例") > -1) {
        this._buildSaveCase(root, saveCases, parent);
      } else {
        if (root.children) {
          root.children.forEach((childNode) => {
            this.buildSaveCase(childNode, saveCases, root.data);
          })
        }
      }
    },
    _buildSaveCase(node, saveCases) {
      let data = node.data;
      if (!data.changed) {
        return;
      }
      let testCase = {
        id: data.id,
      };
      if (data.resource.length > 1) {
        if (data.resource.indexOf('失败') > -1) {
          testCase.status = 'Failure';
        } else if (data.resource.indexOf('通过') > -1) {
          testCase.status = 'Pass';
        } else if (data.resource.indexOf('阻塞') > -1) {
          testCase.status = 'Blocking';
        } else if (data.resource.indexOf('跳过') > -1) {
          testCase.status = 'Skip';
        }
      }
      saveCases.push(testCase);
    }
  }
}
</script>

<style scoped>

</style>
