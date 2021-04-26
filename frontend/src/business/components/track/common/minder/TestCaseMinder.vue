<template>
  <ms-module-minder
    v-loading="result.loading"
    :tree-nodes="treeNodes"
    :data-map="dataMap"
    :tags="tags"
    :select-node="selectNode"
    :distinct-tags="tags"
    :tag-edit-check="tagEditCheck()"
    :priority-disable-check="priorityDisableCheck()"
    @save="save"
    ref="minder"
  />
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
import {
  appendChild,
  getTestCaseDataMap,
  parseCase, priorityDisableCheck, tagEditCheck, updateNode
} from "@/business/components/track/common/minder/minderUtils";
import {getNodePath} from "@/common/js/utils";
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
  computed: {
    selectNodeIds() {
      return this.$store.state.testCaseSelectNodeIds;
    },
    selectNode() {
      return this.$store.state.testCaseSelectNode;
    },
    moduleOptions() {
      return this.$store.state.testCaseModuleOptions;
    }
  },
  watch: {
    selectNode() {
      if (this.$refs.minder) {
        this.$refs.minder.handleNodeSelect(this.selectNode);
      }
      // this.getTestCases();
    }
  },
  mounted() {
    if (this.selectNode && this.selectNode.data) {
      if (this.$refs.minder) {
        let importJson = this.$refs.minder.getImportJsonBySelectNode(this.selectNode.data);
        this.$refs.minder.setJsonImport(importJson);
      }
    }
    this.$nextTick(() => {
      this.getTestCases();
    })
  },
  methods: {
    getTestCases() {
      if (this.projectId) {
        let param = {
          projectId: this.projectId,
          nodeIds: this.selectNodeIds
        }
        this.result = this.$post('/test/case/list/minder', param,response => {
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
        this.getTestCases();
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
        nodePath: getNodePath(parent ? parent.id : '', this.moduleOptions),
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
      if (testCase.nodeId !== 'root' && testCase.nodeId.length < 15) {
        let tip = this.$t('test_track.case.create_case') + "'" + testCase.name + "'" + this.$t('test_track.case.minder_create_tip');
        this.$error(tip)
        throw new Error(tip);
      }
    },
    tagEditCheck() {
      return tagEditCheck;
    },
    priorityDisableCheck() {
      return priorityDisableCheck;
    },
    addCase(data, type) {
      let nodeData = parseCase(data, new Map());
      let minder = window.minder;
      let jsonImport = minder.exportJson();
      if (type === 'edit') {
        updateNode(jsonImport.root, nodeData);
      } else {
        appendChild(data.nodeId, jsonImport.root, nodeData);
      }
      this.$refs.minder.setJsonImport(jsonImport);
    },
    refresh() {
      if (this.$refs.minder) {
        this.$refs.minder.reload();
      }
    }
  }
}
</script>

<style scoped>

</style>
