<template>
  <ms-module-minder
    v-loading="result.loading"
    :tree-nodes="treeNodes"
    :tags="tags"
    minder-key="testCase"
    :select-node="selectNode"
    :distinct-tags="tags"
    :show-module-tag="true"
    :tag-edit-check="tagEditCheck()"
    @afterMount="handleAfterMount"
    :priority-disable-check="priorityDisableCheck()"
    :disabled="disabled"
    @save="save"
    ref="minder"
  />
</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
import {
  handleAfterSave,
  handleExpandToLevel, handleTestCaseAdd, handTestCaeEdit,
  listenBeforeExecCommand, listenDblclick,
  listenNodeSelected,
  loadSelectNodes,
  priorityDisableCheck,
  tagEditCheck,
} from "@/business/components/track/common/minder/minderUtils";
import {getNodePath, hasPermission} from "@/common/js/utils";
import {getTestCasesForMinder, getMinderExtraNode} from "@/network/testCase";
export default {
name: "TestCaseMinder",
  components: {MsModuleMinder},
  data() {
    return{
      testCase: [],
      dataMap: new Map(),
      tags: [this.$t('api_test.definition.request.case'), this.$t('test_track.case.prerequisite'), this.$t('commons.remark'), '模块'],
      result: {loading: false},
      needRefresh: false,
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
    projectId: String,
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
    },
    disabled() {
      return !hasPermission('PROJECT_TRACK_CASE:READ+EDIT');
    },
  },
  watch: {
    selectNode() {
      if (this.$refs.minder) {
        this.$refs.minder.handleNodeSelect(this.selectNode);
      }
    }
  },
  mounted() {
    this.setIsChange(false);
    if (this.selectNode && this.selectNode.data) {
      if (this.$refs.minder) {
        let importJson = this.$refs.minder.getImportJsonBySelectNode(this.selectNode.data);
        this.$refs.minder.setJsonImport(importJson);
      }
    }
  },
  methods: {
    handleAfterMount() {
      listenNodeSelected(() => {
        // 展开模块下的用例
        loadSelectNodes(this.getParam(),  getTestCasesForMinder, null, getMinderExtraNode);
      });

      listenDblclick(() => {
        let minder = window.minder;
        let selectNodes = minder.getSelectedNodes();
        let isNotDisableNode = false;
        // 如果鼠标双击了非模块的节点，表示已经编辑
        selectNodes.forEach(node => {
          if (!node.data.disable) {
            isNotDisableNode = true;
          }
        });
        if (isNotDisableNode) {
          this.setIsChange(true);
        }
      });

      listenBeforeExecCommand((even) => {
        if (even.commandName === 'expandtolevel') {
          let level = Number.parseInt(even.commandArgs);
          handleExpandToLevel(level, even.minder.getRoot(), this.getParam(), getTestCasesForMinder);
        }
        if (['priority', 'resource', 'removenode', 'appendchildnode', 'appendparentnode', 'appendsiblingnode'].indexOf(even.commandName) > 0) {
          // 这些情况则脑图有改变
          this.setIsChange(true);
        }
      });
    },
    getParam() {
      return {
        request: {
          projectId: this.projectId,
          orders: this.condition.orders
        },
        result: this.result,
        isDisable: false
      }
    },
    setIsChange(isChanged) {
      this.$store.commit('setIsTestCaseMinderChanged', isChanged);
    },
    save(data) {
      let saveCases = [];
      let deleteCases = []; // 包含测试用例和临时节点
      let saveExtraNode = {};
      this.buildSaveCase(data.root, saveCases, deleteCases, saveExtraNode);

      let param = {
        projectId: this.projectId,
        data: saveCases,
        ids: deleteCases.map(item => item.id)
      }

      let saveCase = new Promise((resolve) => {
        this.result = this.$post('/test/case/minder/edit', param, () => {
          resolve();
        });
      });


      let extraNodeParam = {
        groupId: this.projectId,
        type: "TEST_CASE",
        data: saveExtraNode,
        ids: deleteCases.map(item => item.id)
      }

      let saveExtraNodePromise = new Promise((resolve) => {
        this.result = this.$post('/minder/extra/node/batch/edit', extraNodeParam, () => {
          resolve();
        });
      });

      Promise.all([saveCase, saveExtraNodePromise])
        .then(() => {
          this.$success(this.$t('commons.save_success'));
          handleAfterSave(window.minder.getRoot(), this.getParam());
          this.setIsChange(false);
        });
    },
    buildSaveCase(root, saveCases, deleteCases, saveExtraNode, parent, preNode, nextNode) {
      let data = root.data;
      if (data.resource && data.resource.indexOf(this.$t('api_test.definition.request.case')) > -1) {
        this._buildSaveCase(root, saveCases, deleteCases, parent, preNode, nextNode);
      } else {
        let deleteChild = data.deleteChild;
        if (deleteChild && deleteChild.length > 0 && data.type === 'node') {
          deleteCases.push(...deleteChild);
        }

        if (data.type !== 'node' && data.type !== 'tmp'
        && parent && parent.type === 'node' && data.changed === true) {
          // 保存额外信息，只保存模块下的一级子节点
          let nodes = saveExtraNode[parent.id];
          if (!nodes) {
            nodes = [];
          }
          nodes.push(JSON.stringify(this.buildExtraNode(root)));
          saveExtraNode[parent.id] = nodes;
        }

        if (data.id === null) {
          let tip = '脑图编辑无法创建模块：' + data.text + '';
          this.$error(tip)
          throw new Error(tip);
        }
        if (root.children) {
          for (let i = 0; i < root.children.length; i++) {
            let childNode = root.children[i];
            let preNode = null;
            let nextNode = null;
            if (i != 0) {
              preNode = root.children[i - 1];
            }
            if (i + 1 < root.children.length) {
              nextNode = root.children[i + 1];
            }
            this.buildSaveCase(childNode, saveCases, deleteCases, saveExtraNode, root.data, preNode, nextNode);
          }
        }
      }
    },
    _buildSaveCase(node, saveCases, deleteCases, parent, preNode, nextNode) {
      let data = node.data;
      if (!data.text) {
        return;
      }

      if (data.isExtraNode) {
        // 如果是临时节点，打上了用例标签，则删除临时节点并新建用例节点
        let deleteData = {};
        Object.assign(deleteData, data);
        deleteCases.push(deleteData);
        data.id = "";
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
        prerequisite: "",
        remark: "",
        stepDescription: "",
        expectedResult: "",
        steps: "[]"
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

            if (data.stepModel === 'TEXT') {
              testCase.stepDescription = step.desc;
              testCase.expectedResult = step.result;
            }
          }
          if (childData.changed) isChange = true;
        })
      }
      testCase.steps = JSON.stringify(steps);

      if (isChange) {

        testCase.targetId = null; // 排序处理
        if (this.isCaseNode(preNode)) {
          let preId = preNode.data.id;
          if (preId && preId.length > 15) {
            testCase.targetId = preId;
            testCase.moveMode = 'AFTER';
          }
        } else if (this.isCaseNode(nextNode)) {
          let nextId = nextNode.data.id;
          if (nextId && nextId.length > 15) {
            testCase.targetId = nextId;
            testCase.moveMode = 'BEFORE';
          }
        }

        saveCases.push(testCase);
      }
      if (testCase.nodeId !== 'root' && testCase.nodeId.length < 15) {
        let tip = this.$t('test_track.case.create_case') + "'" + testCase.name + "'" + this.$t('test_track.case.minder_create_tip');
        this.$error(tip)
        throw new Error(tip);
      }
    },
    isCaseNode(node) {
      if (node && node.resource && node.resource.indexOf(this.$t('api_test.definition.request.case')) > -1) {
        return true;
      }
      return false;
    },
    buildExtraNode(node) {
      let data = node.data;
      let nodeData = {
        text: data.text,
        id: data.id,
        resource: data.resource,
      };
      if (node.children) {
        nodeData.children = [];
        node.children.forEach(item => {
          nodeData.children.push(this.buildExtraNode(item));
        });
      }
      return nodeData;
    },
    tagEditCheck() {
      return tagEditCheck;
    },
    priorityDisableCheck() {
      return priorityDisableCheck;
    },
    // 打开脑图之后，添加新增或修改tab页时，同步修改脑图
    addCase(data, type) {
      if (type === 'edit') {
        handTestCaeEdit(data);
      } else {
        handleTestCaseAdd(data.nodeId, data);
      }
      this.needRefresh = true;
    },
    refresh() {
      // 切换tab页，如果没有修改用例，不刷新脑图
      if (this.needRefresh) {
        let jsonImport = window.minder.exportJson();
        this.$refs.minder.setJsonImport(jsonImport);
        this.$nextTick(() => {
          if (this.$refs.minder) {
            this.$refs.minder.reload();
          }
        });
        this.needRefresh = false;
      }
    }
  }
}
</script>

<style scoped>

</style>
