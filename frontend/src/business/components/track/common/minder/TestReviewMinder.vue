<template>
  <ms-module-minder
    v-loading="result.loading"
    :tree-nodes="treeNodes"
    :data-map="dataMap"
    @save="save"
  />
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
import {getTestCaseDataMap} from "@/business/components/track/common/minder/minderUtils";
export default {
name: "TestReviewMinder",
  components: {MsModuleMinder},
  data() {
    return{
      testCase: [],
      dataMap: new Map(),
      result: {}
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
    reviewId: {
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
        this.result = this.$post('/test/review/case/list/all', {reviewId: this.reviewId}, response => {
          this.testCase = response.data;
          this.dataMap = getTestCaseDataMap(this.testCase);
        });
      }
    },
    save(data) {
      // let saveCases = [];
      // this.buildSaveCase(data.root, saveCases, undefined);
      // console.log(saveCases);
      // let param = {
      //   projectId: this.projectId,
      //   data: saveCases
      // }
      // this.result = this.$post('/test/case/minder/edit', param, () => {
      //   this.$success(this.$t('commons.save_success'));
      // });
    },
    buildSaveCase(root, saveCases, parent) {
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
    _buildSaveCase(node, saveCases, parent) {
      let data = node.data;
      let isChange = false;
      let testCase = {
        id: data.id,
        name: data.text,
        nodeId: parent ? parent.id : "",
        nodePath: parent ? parent.path : "",
        type: data.type ? data.type : 'functional',
        method: data.method ? data.method : 'manual',
        maintainer: data.maintainer,
        priority: 'P' + data.priority,
      };
      if (data.changed) isChange = true;
      let steps = [];
      let stepNum = 1;
      if (node.children) {
        node.children.forEach((childNode) => {
          let childData = childNode.data;
          if (childData.resource && childData.resource.indexOf('前置条件') > -1) {
            testCase.prerequisite = childData.text;
          } else if (childData.resource && childData.resource.indexOf('备注') > -1) {
            testCase.remark = childData.text;
          } else {
            // 测试步骤
            let step = {};
            step.num = stepNum++;
            step.desc = childData.text;
            if (childNode.children) {
              let result = "";
              childNode.children.forEach((child) => {
                result += child.data.text;
                if (child.data.changed) isChange = true;
              })
              step.result = result;
            }
            steps.push(step);
          }
          if (childData.changed) isChange = true;
        })
      }
      testCase.steps = JSON.stringify(steps);
      if (isChange) {
        saveCases.push(testCase);
      }
    },
  }
}
</script>

<style scoped>

</style>
