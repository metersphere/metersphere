<template>
  <div>
    <ms-module-minder
      v-loading="result.loading"
      :tree-nodes="treeNodes"
      :tags="tags"
      minder-key="testCase"
      :select-node="selectNode"
      :distinct-tags="tags"
      :module-disable="false"
      :show-module-tag="true"
      :move-enable="moveEnable"
      :tag-edit-check="tagEditCheck()"
      @afterMount="handleAfterMount"
      :priority-disable-check="priorityDisableCheck()"
      :disabled="disabled"
      @save="save"
      ref="minder"
    />

    <IssueRelateList
      :case-id="getCurCaseId()"
      @refresh="refreshRelateIssue"
      ref="issueRelate"/>

    <test-plan-issue-edit
      :is-minder="true"
      :plan-id="null"
      :case-id="getCurCaseId()"
      @refresh="refreshIssue"
      ref="issueEdit"/>

  </div>

</template>

<script>
import MsModuleMinder from "@/business/components/common/components/MsModuleMinder";
import {
  getChildNodeId,
  handleAfterSave,
  handleExpandToLevel,
  handleMinderIssueDelete,
  handleTestCaseAdd,
  handTestCaeEdit,
  isCaseNodeData,
  isModuleNode,
  isModuleNodeData,
  listenBeforeExecCommand,
  listenDblclick,
  listenNodeSelected,
  loadSelectNodes,
  priorityDisableCheck,
  tagEditCheck,
} from "@/business/components/track/common/minder/minderUtils";
import {getNodePath, getUUID, hasPermission} from "@/common/js/utils";
import {getTestCasesForMinder, getMinderExtraNode} from "@/network/testCase";
import {addIssueHotBox, getSelectedNodeData, handleIssueAdd, handleIssueBatch} from "./minderUtils";
import IssueRelateList from "@/business/components/track/case/components/IssueRelateList";
import TestPlanIssueEdit from "@/business/components/track/case/components/TestPlanIssueEdit";

const {getIssuesListById} = require("@/network/Issue");
const {getCurrentWorkspaceId} = require("@/common/js/utils");
export default {
name: "TestCaseMinder",
  components: {TestPlanIssueEdit, IssueRelateList, MsModuleMinder},
  data() {
    return{
      testCase: [],
      dataMap: new Map(),
      tags: [this.$t('api_test.definition.request.case'), this.$t('test_track.case.prerequisite'), this.$t('commons.remark'), this.$t('test_track.module.module')],
      result: {loading: false},
      needRefresh: false,
      saveCases: [],
      saveModules: [],
      saveModuleNodeMap: new Map(),
      deleteNodes: [], // 包含测试模块、用例和临时节点
      saveExtraNode: {}
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
    testCaseDefaultValue() {
      return this.$store.state.testCaseDefaultValue;
    },
    disabled() {
      return !hasPermission('PROJECT_TRACK_CASE:READ+EDIT');
    },
    moveEnable() {
      // 如果不是默认的排序条件不能调换位置
      return !this.condition.orders || this.condition.orders.length < 1;
    },
    workspaceId(){
      return getCurrentWorkspaceId();
    }

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
        // 点击模块，加载模块下的用例
        loadSelectNodes(this.getParam(),  getTestCasesForMinder, null, getMinderExtraNode);
      });

      listenDblclick(() => {
        let minder = window.minder;
        let selectNodes = minder.getSelectedNodes();
        let isNotDisableNode = false;
        selectNodes.forEach(node => {
          // 如果鼠标双击了非模块的节点，表示已经编辑
          if (!node.data.disable) {
            isNotDisableNode = true;
          }
          if (node.data.type === 'issue') {
            getIssuesListById(node.data.id, this.projectId,this.workspaceId,(data) => {
              data.customFields = JSON.parse(data.customFields);
              this.$refs.issueEdit.open(data);
            });
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

        if (handleMinderIssueDelete(even.commandName))  return; // 删除缺陷不算有编辑脑图信息

        if (['priority', 'resource', 'removenode', 'appendchildnode', 'appendparentnode', 'appendsiblingnode'].indexOf(even.commandName) > -1) {
          // 这些情况则脑图有改变
          this.setIsChange(true);
        }
      });

      addIssueHotBox(this);
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
    save() {
      this.saveCases = [];
      this.saveModules = [];
      this.deleteNodes = []; // 包含测试模块、用例和临时节点
      this.saveExtraNode = {};
      this.saveModuleNodeMap = new Map();
      this.buildSaveParam(window.minder.getRoot());

      this.saveModules.forEach(module => {
        let nodeIds = [];
        getChildNodeId(this.saveModuleNodeMap.get(module.id), nodeIds);
        module.nodeIds = nodeIds;
      });

      let param = {
        projectId: this.projectId,
        data: this.saveCases,
        ids: this.deleteNodes.map(item => item.id),
        testCaseNodes: this.saveModules,
        extraNodeRequest:  {
          groupId: this.projectId,
          type: "TEST_CASE",
          data: this.saveExtraNode,
        }
      }

      this.result = this.$post('/test/case/minder/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        handleAfterSave(window.minder.getRoot());
        this.$emit('refresh');
        this.setIsChange(false);
      });

    },
    buildSaveParam(root, parent, preNode, nextNode) {
      let data = root.data;
      if (isCaseNodeData(data)) {
        this.buildSaveCase(root, parent, preNode, nextNode);
      } else {
        let deleteChild = data.deleteChild;
        if (deleteChild && deleteChild.length > 0 && isModuleNodeData(data)) {
          this.deleteNodes.push(...deleteChild);
        }

        if (data.type !== 'tmp' && data.changed) {
          if (isModuleNodeData(data)) {
            if (data.contextChanged) {
              this.buildSaveModules(root, data, parent);
              root.children && root.children.forEach(i => {
                if (isModuleNode(i)) {
                  i.data.changed = true;
                  i.data.contextChanged = true; // 如果当前节点有变化，下面的模块节点也需要level也可能需要变化
                }
              });
            }
          } else {
            // 保存临时节点
            this.buildExtraNode(data, parent, root);
          }
        }

        if (root.children) {
          for (let i = 0; i < root.children.length; i++) {
            let childNode = root.children[i];
            let preNodeTmp = null;
            let nextNodeTmp = null;
            if (i != 0) {
              preNodeTmp = root.children[i - 1];
            }
            if (i + 1 < root.children.length) {
              nextNodeTmp = root.children[i + 1];
            }
            this.buildSaveParam(childNode, root.data, preNodeTmp, nextNodeTmp);
          }
        }
      }
    },
    buildSaveModules(node, data, parent) {
      if (!data.text) {
        return;
      }
      let pId = parent ? (parent.newId ? parent.newId : parent.id) : null;

      if (parent && !isModuleNodeData(parent)) {
        this.throwError(this.$t('test_track.case.minder_not_module_tip', [data.text]));
      }

      let module = {
        id: data.id,
        name: data.text,
        level: parent ? parent.level + 1 : data.level,
        parentId: pId
      };
      data.level = module.level;

      if (data.isExtraNode) {
        // 如果是临时节点，打上了模块标签，则删除临时节点并新建模块
        this.pushDeleteNode(data);
        module.id = null;
      }

      if (data.type === 'case') {
        // 如果是用例节点，打上了模块标签，则用例节点并新建模块
        this.pushDeleteNode(data);
        module.id = null;
      }

      if (module.id && module.id.length > 20) {
        module.isEdit = true; // 编辑
      } else {
        module.isEdit = false; // 新增
        module.id = getUUID();
        data.newId = module.id;
        this.moduleOptions.push({id: data.newId, path: getNodePath(pId, this.moduleOptions) + '/' + module.name});
      }

      this.saveModuleNodeMap.set(module.id, node);

      if (module.level > 8) {
        this.throwError(this.$t('commons.module_deep_limit'));
      }
      this.saveModules.push(module);
    },
    buildExtraNode(data, parent, root) {
      if (data.type !== 'node' && data.type !== 'tmp' && parent && isModuleNodeData(parent) && data.changed === true) {
        // 保存额外信息，只保存模块下的一级子节点
        let nodes = this.saveExtraNode[parent.id];
        if (!nodes) {
          nodes = [];
        }
        nodes.push(JSON.stringify(this._buildExtraNode(root)));
        this.saveExtraNode[parent.id] = nodes;
      }
    },
    buildSaveCase(node, parent, preNode, nextNode) {
      let data = node.data;
      if (!data.text) {
        return;
      }

      if (parent.id === 'root') {
        this.throwError(this.$t('test_track.case.minder_all_module_tip'));
      }

      if (parent.isExtraNode) {
        this.throwError(this.$t('test_track.case.minder_tem_node_tip', [parent.text]));
      }

      if (data.type === 'node') {
        this.throwError(this.$t('test_track.case.minder_is_module_tip', [data.text]));
      }

      let isChange = false;

      let nodeId = parent ? (parent.newId ? parent.newId : parent.id) : "";
      let priorityDefaultValue;
      if (data.priority ) {
        priorityDefaultValue = 'P' + (data.priority - 1);
      } else {
        priorityDefaultValue = this.testCaseDefaultValue['用例等级'] ? this.testCaseDefaultValue['用例等级'] : 'P' + 0;
      }

      let testCase = {
        id: data.id,
        name: data.text,
        nodeId: nodeId,
        nodePath: getNodePath(nodeId, this.moduleOptions),
        type: data.type ? data.type : 'functional',
        method: data.method ? data.method : 'manual',
        maintainer: this.testCaseDefaultValue['责任人'] ? this.testCaseDefaultValue['责任人'] : data.maintainer,
        priority: priorityDefaultValue,
        prerequisite: "",
        remark: "",
        stepDescription: "",
        expectedResult: "",
        status: this.testCaseDefaultValue['用例状态'],
        steps: [{
          num: 1,
          desc: '',
          result: ''
        }],
        tags: "[]",
        stepModel: "STEP"
      };
      if (data.changed) isChange = true;
      let steps = [];
      let stepNum = 1;
      if (node.children) {
        node.children.forEach((childNode) => {
          let childData = childNode.data;
          if (childData.type === 'issue') return;
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

      if (data.isExtraNode) {
        // 如果是临时节点，打上了用例标签，则删除临时节点并新建用例节点
        this.pushDeleteNode(data);
        testCase.id = null;
      }

      if (isChange) {

        testCase.targetId = null; // 排序处理

        if (this.moveEnable) {
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
        }

        if (testCase.id && testCase.id.length > 20) {
          testCase.isEdit = true; // 编辑
        } else {
          testCase.isEdit = false; // 新增
          testCase.id = getUUID();
          data.newId = testCase.id;
        }
        this.saveCases.push(testCase);
      }
      if (testCase.nodeId !== 'root' && testCase.nodeId.length < 15) {
        this.throwError(this.$t('test_track.case.create_case') + "'" + testCase.name + "'" + this.$t('test_track.case.minder_create_tip'));
      }
    },
    pushDeleteNode(data) {
      // 如果是临时节点，打上了用例标签，则删除临时节点并新建用例节点
      let deleteData = {};
      Object.assign(deleteData, data);
      this.deleteNodes.push(deleteData);
      data.id = "";
    },
    isCaseNode(node) {
      if (node && node.data.resource && node.data.resource.indexOf(this.$t('api_test.definition.request.case')) > -1) {
        return true;
      }
      return false;
    },
    _buildExtraNode(node) {
      let data = node.data;
      let nodeData = {
        text: data.text,
        id: data.id,
        resource: data.resource,
      };
      if (nodeData.id && nodeData.id.length > 20) {
        nodeData.isEdit = true; // 编辑
      } else {
        nodeData.isEdit = false; // 新增
        nodeData.id = getUUID();
        data.newId = nodeData.id;
      }
      if (node.children) {
        nodeData.children = [];
        node.children.forEach(item => {
          nodeData.children.push(this._buildExtraNode(item));
        });
      }
      data.isExtraNode = true;
      return nodeData;
    },
    throwError(tip) {
      this.$error(tip)
      throw new Error(tip);
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
    },
    getCurCaseId() {
      return getSelectedNodeData().id;
    },
    refreshIssue(issue) {
      handleIssueAdd(issue);
    },
    refreshRelateIssue(issues) {
      handleIssueBatch(issues);
    }
  }
}
</script>

<style scoped>

</style>
