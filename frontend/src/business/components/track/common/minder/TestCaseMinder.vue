<template>
  <ms-module-minder
    v-loading="result.loading"
    :tree-nodes="treeNodes"
    :data-map="dataMap"
    :tags="tags"
    :distinct-tags="tags"
    @save="save"
  />
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
import {getTestCaseDataMap} from "@/business/components/track/common/minder/minderUtils";
export default {
name: "TestCaseMinder",
  components: {MsModuleMinder},
  data() {
    return{
      testCase: [],
      dataMap: new Map(),
      tags: [this.$t('api_test.definition.request.case'), this.$t('test_track.case.prerequisite'), this.$t('commons.remark')],
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
    condition: Object,
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
        this.result = this.$get('/test/case/list/detail/' + this.projectId,response => {
          this.testCase = response.data;
          this.dataMap = getTestCaseDataMap(this.testCase);
        });
      }
    },
    save(data) {
      let saveCases = [];
      let deleteCases = [];
      this.buildSaveCase(data.root, saveCases, deleteCases, undefined);
      let param = {
        projectId: this.projectId,
        data: saveCases,
        ids: deleteCases.map(item => item.id)
      }
      this.result = this.$post('/test/case/minder/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
      });
    },
    buildSaveCase(root, saveCases, deleteCases, parent) {
      let data = root.data;
      if (data.resource && data.resource.indexOf(this.$t('api_test.definition.request.case')) > -1) {
        this._buildSaveCase(root, saveCases, parent);
      } else {
        let deleteChild = data.deleteChild;
        if (deleteChild && deleteChild.length > 0) {
          deleteCases.push(...deleteChild);
        }
        if (root.children) {
          root.children.forEach((childNode) => {
            this.buildSaveCase(childNode, saveCases, deleteCases, root.data);
          })
        }
      }
    },
    _buildSaveCase(node, saveCases, parent) {
      let data = node.data;
      if (!data.text) {
        return;
      }
      let isChange = false;
      let testCase = {
        id: data.id,
        name: data.text,
        nodeId: parent ? parent.id : "",
        nodePath: parent ? parent.path : "",
        type: data.type ? data.type : 'functional',
        method: data.method ? data.method: 'manual',
        maintainer: data.maintainer,
        priority: 'P' + (data.priority ? data.priority - 1 : 0),
      };
      if (data.changed) isChange = true;
      let steps = [];
      let stepNum = 1;
      if (node.children) {
        node.children.forEach((childNode) => {
          let childData = childNode.data;
          if (childData.resource && childData.resource.indexOf(this.$t('test_track.case.prerequisite')) > -1) {
            testCase.prerequisite = childData.text;
          } else if (childData.resource && childData.resource.indexOf(this.$t('commons.remark')) > -1) {
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
      if (testCase.nodeId.length < 15) {
        let tip = this.$t('test_track.case.create_case') + "'" + testCase.name + "'" + this.$t('test_track.case.minder_create_tip');
        this.$error(tip)
        throw new Error(tip);
      }
    },

  }
}
</script>

<style scoped>

</style>
