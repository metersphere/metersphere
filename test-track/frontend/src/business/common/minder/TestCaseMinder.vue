<template>
  <div style="height: 100%">
    <div class="case-main-layout-left" style="display: inline-block">
      <ms-table-count-bar
        :count-content="$t('case.all_case_content') + '(' + caseNum + ')'"
      ></ms-table-count-bar>
    </div>

    <div class="case-main-layout-right" style="float: right; display: flex">
      <version-select
        v-xpack
        :default-version="defaultVersion"
        :project-id="projectId"
        @changeVersion="changeVersion"
      />
    </div>

    <div style="height: 100%" v-loading="result.loading">
      <ms-module-minder
        minder-key="TEST_CASE"
        :tree-nodes="treeNodes"
        :tags="tags"
        :select-node="selectNode"
        :distinct-tags="tags"
        :module-disable="false"
        :show-module-tag="true"
        :move-confirm="moveConfirm"
        :tag-edit-check="tagEditCheck()"
        :priority-disable-check="priorityDisableCheck()"
        :disabled="disabled"
        :get-extra-node-count="getMinderTreeExtraNodeCount()"
        :del-confirm="handleDeleteConfirm"
        @afterMount="handleAfterMount"
        @toggleMinderFullScreen="toggleMinderFullScreen"
        @save="(data, callback) => save(callback)"
        ref="minder"
      />
    </div>

    <IssueRelateList
      :case-id="getCurCaseId()"
      @refresh="refreshRelateIssue"
      ref="issueRelate"
    />

    <test-plan-issue-edit
      :is-minder="true"
      :plan-id="null"
      :case-id="getCurCaseId()"
      @refresh="refreshIssue"
      ref="issueEdit"
    />

    <is-change-confirm @confirm="changeConfirm" ref="isChangeConfirm" />
  </div>
</template>

<script>
import {
  getChildNodeId,
  handleAfterSave,
  handleExpandToLevel,
  handleMinderIssueDelete,
  handlePasteAfter,
  handlePasteTip,
  handleSaveError,
  handleTestCaseAdd,
  handTestCaeEdit,
  isCaseNodeData,
  isModuleNode,
  isModuleNodeData,
  listenBeforeExecCommand,
  listenDblclick,
  listenNodeSelected,
  loadNode,
  loadSelectNodes,
  priorityDisableCheck,
  tagEditCheck,
} from "@/business/common/minder/minderUtils";
import { getNodePath, getUUID } from "metersphere-frontend/src/utils";
import { hasPermission } from "metersphere-frontend/src/utils/permission";
import {
  getTestCasesForMinder,
  getMinderExtraNode,
  getMinderTreeExtraNodeCount,
  testCaseMinderEdit,
} from "@/api/testCase";
import {
  addIssueHotBox,
  getSelectedNodeData,
  handleIssueAdd,
  handleIssueBatch,
} from "./minderUtils";
import IssueRelateList from "@/business/case/components/IssueRelateList";
import TestPlanIssueEdit from "@/business/case/components/TestPlanIssueEdit";
import { setPriorityView } from "vue-minder-editor-plus/src/script/tool/utils";
import IsChangeConfirm from "metersphere-frontend/src/components/IsChangeConfirm";
import MsModuleMinder from "@/business/common/minder/MsModuleMinder";
import MsTableCountBar from "metersphere-frontend/src/components/table/MsTableCountBar";
import MxVersionSelect from "metersphere-frontend/src/components/version/MxVersionSelect";

import { useStore } from "@/store";
import { mapState } from "pinia";
import { getCurrentWorkspaceId } from "@/business/utils/sdk-utils";
import { getIssuesForMinder } from "@/api/issue";

export default {
  name: "TestCaseMinder",
  components: {
    MsModuleMinder,
    IsChangeConfirm,
    TestPlanIssueEdit,
    IssueRelateList,
    MsTableCountBar,
    VersionSelect: MxVersionSelect,
  },
  data() {
    return {
      testCase: [],
      dataMap: new Map(),
      tags: [
        this.$t("api_test.definition.request.case"),
        this.$t("test_track.case.prerequisite"),
        this.$t("commons.remark"),
        this.$t("test_track.module.module"),
      ],
      result: { loading: false },
      needRefresh: false,
      noRefreshMinder: false,
      noRefreshMinderForSelectNode: false,
      saveCases: [],
      saveModules: [],
      saveModuleNodeMap: new Map(),
      deleteNodes: [], // 包含测试模块、用例和临时节点
      saveExtraNode: {},
      extraNodeChanged: [], // 记录转成用例或模块的临时节点
      currentVersion: null,
      caseNum: 0,
    };
  },
  props: {
    treeNodes: {
      type: Array,
      default() {
        return [];
      },
    },
    condition: Object,
    projectId: String,
    activeName: String,
    defaultVersion: String,
  },
  computed: {
    ...mapState(useStore, {
      selectNodeIds: "testCaseSelectNodeIds",
      selectNode: "testCaseSelectNode",
      moduleOptions: "testCaseModuleOptions",
      testCaseDefaultValue: "testCaseDefaultValue",
    }),
    disabled() {
      return !hasPermission("PROJECT_TRACK_CASE_MINDER:OPERATE");
    },
    moveEnable() {
      // 如果不是默认的排序条件不能调换位置
      return !this.condition.orders || this.condition.orders.length < 1;
    },
    workspaceId() {
      return getCurrentWorkspaceId();
    },
  },
  watch: {
    // 刷新模块树触发
    treeNodes(newVal, oldVal) {
      if (newVal !== oldVal && this.activeName === "default") {
        // 模块刷新并且当前 tab 是用例列表时，提示是否刷新脑图
        this.handleNodeUpdateForMinder();
      }
    },
    // 点击模块触发
    selectNode() {
      if (this.noRefreshMinderForSelectNode) {
        // 如果是保存触发的刷新模块，则不刷新脑图
        this.noRefreshMinderForSelectNode = false;
        return;
      }
      if (this.$refs.minder) {
        this.caseNum = this.selectNode.data.caseNum;
        this.$refs.minder.handleNodeSelect(this.selectNode);
      }
    },
    currentVersion() {
      this.$refs.minder.initData();
    },
  },
  mounted() {
    this.currentVersion = this.defaultVersion || null;
    this.setIsChange(false);
    let moduleNum = 0;
    this.treeNodes.forEach((node) => {
      moduleNum += node.caseNum;
    });
    this.caseNum = moduleNum;
    if (this.selectNode && this.selectNode.data) {
      if (this.$refs.minder) {
        let importJson = this.$refs.minder.getImportJsonBySelectNode(
          this.selectNode.data
        );
        this.$refs.minder.setJsonImport(importJson);
      }
    }
  },
  methods: {
    changeVersion(currentVersion) {
      this.currentVersion = currentVersion || null;
      this.$emit("versionChange", currentVersion);
    },
    handleNodeUpdateForMinder() {
      if (this.noRefreshMinder) {
        // 如果是保存触发的刷新模块，则不刷新脑图
        this.noRefreshMinder = false;
        return;
      }

      if (this.selectNode && this.selectNode.data) {
        // 列表刷新会修改 selectNode，触发 selectNode watch
        // 这里就不重复刷新了
        return;
      }

      // 如果脑图没有修改直接刷新，有修改提示
      if (!useStore().isTestCaseMinderChanged) {
        if (this.$refs.minder) {
          this.$refs.minder.initData();
        }
      } else {
        this.$refs.isChangeConfirm.open();
      }
    },
    changeConfirm(isSave) {
      if (isSave) {
        this.save(() => {
          this.initData();
        });
      } else {
        this.initData();
      }
    },
    initData() {
      if (this.$refs.minder) {
        this.$refs.minder.initData();
      }
    },
    getMinderTreeExtraNodeCount() {
      return getMinderTreeExtraNodeCount;
    },
    handleAfterMount() {
      listenNodeSelected(() => {
        // 点击模块，加载模块下的用例
        loadSelectNodes(
          this.getParam(),
          getTestCasesForMinder,
          null,
          getMinderExtraNode
        );
      });

      listenDblclick(() => {
        let minder = window.minder;
        let selectNodes = minder.getSelectedNodes();
        let isNotDisableNode = false;
        selectNodes.forEach((node) => {
          // 如果鼠标双击了非模块的节点，表示已经编辑
          if (!node.data.disable) {
            isNotDisableNode = true;
          }
          if (node.data.type === "issue") {
            getIssuesForMinder(
              node.data.id,
              this.projectId,
              this.workspaceId
            ).then((r) => {
              let data = r.data;
              this.$refs.issueEdit.open(data);
            });
          }
        });
        if (isNotDisableNode) {
          this.setIsChange(true);
        }
      });

      listenBeforeExecCommand((even) => {
        if (even.commandName === "expandtolevel") {
          let level = Number.parseInt(even.commandArgs);
          handleExpandToLevel(
            level,
            even.minder.getRoot(),
            this.getParam(),
            getTestCasesForMinder,
            null,
            getMinderExtraNode
          );
        }

        if (handleMinderIssueDelete(even.commandName)) return; // 删除缺陷不算有编辑脑图信息

        if (
          [
            "priority",
            "resource",
            "removenode",
            "appendchildnode",
            "appendparentnode",
            "appendsiblingnode",
            "paste",
          ].indexOf(even.commandName) > -1
        ) {
          // 这些情况则脑图有改变
          this.setIsChange(true);
        }

        if ("paste" === even.commandName) {
          handlePasteTip(window.minder.getSelectedNode());
          handlePasteAfter(window.minder.getSelectedNode());
        }

        if ("resource" === even.commandName) {
          // 设置完标签后，优先级显示有问题，重新设置下
          setTimeout(() => setPriorityView(true, "P"), 100);
        }

        if ("movetoparent" === even.commandName) {
          // 拖入的节点尚未加载时，加载节点
          loadNode(
            even.commandArgs[1],
            this.getParam(),
            getTestCasesForMinder,
            null,
            getMinderExtraNode
          );
        }
      });

      addIssueHotBox(this);
    },
    toggleMinderFullScreen(isFullScreen) {
      this.$emit("toggleMinderFullScreen", isFullScreen);
    },
    getParam() {
      return {
        request: {
          projectId: this.projectId,
          versionId: this.currentVersion,
          orders: this.condition.orders,
        },
        result: this.result,
        isDisable: false,
      };
    },
    moveConfirm() {
      let selectNodes = minder.getSelectedNodes();
      for (let node of selectNodes) {
        // 模块暂不支持调整顺序
        if (isModuleNode(node)) {
          this.$warning(this.$t("case.minder_module_move_confirm_tip"));
          return false;
        }
        // 列表排序后用例不能排序
        if (!this.moveEnable && isCaseNodeData(node.data)) {
          this.$warning(this.$t("case.minder_move_confirm_tip"));
          return false;
        }
      }
      return true;
    },
    handleDeleteConfirm() {
      let selectNodes = minder.getSelectedNodes();
      let moduleName = "";
      selectNodes.forEach((node) => {
        if (isModuleNode(node)) {
          moduleName += node.data.text + " ";
        }
      });
      if (moduleName.length > 0) {
        let title =
          this.$t("commons.confirm_delete") +
          ": " +
          this.$t("project.project_file.file_module_type.module") +
          moduleName +
          "?";
        this.$confirm(this.$t("test_track.module.delete_tip"), title, {
          cancelButtonText: this.$t("commons.cancel"),
          confirmButtonText: this.$t("commons.confirm"),
          customClass: "custom-confirm-delete",
          callback: (action) => {
            if (action === "confirm") {
              minder.forceRemoveNode();
            }
          },
        });
      } else {
        minder.forceRemoveNode();
      }
    },
    setIsChange(isChanged) {
      if (this.disabled) {
        return;
      }
      useStore().$patch({
        isTestCaseMinderChanged: isChanged,
      });
    },
    save(callback) {
      this.saveCases = [];
      this.saveModules = [];
      this.deleteNodes = []; // 包含测试模块、用例和临时节点
      this.saveExtraNode = {};
      this.saveModuleNodeMap = new Map();
      this.buildSaveParam(window.minder.getRoot());

      this.saveModules.forEach((module) => {
        let nodeIds = [];
        getChildNodeId(this.saveModuleNodeMap.get(module.id), nodeIds);
        module.nodeIds = nodeIds;
      });

      // 去掉为 null 的数据
      this.deleteNodes = this.deleteNodes.filter((i) => i);
      let param = {
        projectId: this.projectId,
        data: this.saveCases,
        ids: this.deleteNodes.map((item) => item.id),
        testCaseNodes: this.saveModules,
        extraNodeRequest: {
          groupId: this.projectId,
          type: "TEST_CASE",
          data: this.saveExtraNode,
        },
      };

      // 过滤为空的id
      param.ids = param.ids.filter((id) => id);

      this.result.loading = true;
      testCaseMinderEdit(param)
        .then(() => {
          this.result.loading = false;
          this.$success(this.$t("commons.save_success"), false);
          handleAfterSave(window.minder.getRoot());
          this.extraNodeChanged.forEach((item) => {
            item.isExtraNode = false;
          });
          this.extraNodeChanged = [];

          this.$emit("refresh");

          if (!this.noRefreshMinder) {
            // 保存会刷新模块，刷新完模块，脑图也会自动刷新
            // 如果是保存触发的刷新模块，则不刷新脑图
            this.noRefreshMinder = true;
          }

          if (!this.noRefreshMinderForSelectNode) {
            if (this.selectNode && this.selectNode.data) {
              // 如果有选中的模块， 则不刷新 watch -> selectNode
              this.noRefreshMinderForSelectNode = true;
            }
          }

          this.setIsChange(false);
          if (callback && callback instanceof Function) {
            callback();
          }
        })
        .catch(() => {
          this.result.loading = false;
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

        if (data.type !== "tmp" && data.changed) {
          if (isModuleNodeData(data)) {
            if (data.contextChanged && data.id !== "root") {
              this.buildSaveModules(root, data, parent);
              root.children &&
                root.children.forEach((i) => {
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

      if (
        !parent &&
        this.selectNode &&
        this.selectNode.data &&
        this.selectNode.data.id === data.id
      ) {
        // 当前头节点没有 parent, 参数中没有 parentId，校验重名会不准确
        pId = this.selectNode.data.parentId;
      }

      if (parent && !isModuleNodeData(parent)) {
        this.throwError(
          this.$t("test_track.case.minder_not_module_tip", [data.text])
        );
      }

      let module = {
        id: data.id,
        name: data.text,
        level: parent ? parent.level + 1 : data.level,
        parentId: pId,
      };
      data.level = module.level;

      if (data.isExtraNode) {
        // 如果是临时节点，打上了模块标签，则删除临时节点并新建模块
        this.pushDeleteNode(data);
        module.id = null;
        this.extraNodeChanged.push(data);
        if (node.children) {
          // 原本是临时节点，改成模块后，该节点的子节点需要生成新的临时节点
          node.children.forEach((child) => {
            if (child.data.isExtraNode) {
              child.data.changed = true;
              child.data.id = null;
            }
          });
        }
      }

      if (data.type === "case") {
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
        this.moduleOptions.push({
          id: data.newId,
          path: getNodePath(pId, this.moduleOptions) + "/" + module.name,
        });
      }

      this.saveModuleNodeMap.set(module.id, node);

      if (module.name.trim().length > 100) {
        this.throwError(
          this.$t("test_track.module.name") +
            this.$t("test_track.length_less_than") +
            100
        );
      }
      this.saveModules.push(module);
    },
    buildExtraNode(data, parent, root) {
      if (
        data.type !== "node" &&
        data.type !== "tmp" &&
        parent &&
        isModuleNodeData(parent) &&
        data.changed === true
      ) {
        // 保存额外信息，只保存模块下的一级子节点
        let pId = parent.newId ? parent.newId : parent.id;
        let nodes = this.saveExtraNode[pId];
        if (!nodes) {
          nodes = [];
        }
        nodes.push(JSON.stringify(this._buildExtraNode(root)));
        this.saveExtraNode[pId] = nodes;
      }
    },
    validate(parent, data) {
      if (parent.id === "root") {
        this.throwError(this.$t("test_track.case.minder_all_module_tip"));
      }

      if (parent.isExtraNode && !isModuleNodeData(parent)) {
        this.throwError(
          this.$t("test_track.case.minder_tem_node_tip", [parent.text])
        );
      }

      if (data.type === "node") {
        this.throwError(
          this.$t("test_track.case.minder_is_module_tip", [data.text])
        );
      }
    },
    buildSaveCase(node, parent, preNode, nextNode) {
      let data = node.data;
      if (!data.text) {
        return;
      }

      this.validate(parent, data);

      let isChange = false;

      let nodeId = parent ? (parent.newId ? parent.newId : parent.id) : "";
      let priorityDefaultValue;
      if (data.priority) {
        priorityDefaultValue = "P" + (data.priority - 1);
      } else {
        priorityDefaultValue = this.testCaseDefaultValue["用例等级"]
          ? this.testCaseDefaultValue["用例等级"]
          : "P" + 0;
      }

      let testCase = {
        id: data.id,
        name: data.text,
        nodeId: nodeId,
        nodePath: getNodePath(nodeId, this.moduleOptions),
        type: data.type ? data.type : "functional",
        method: data.method ? data.method : "manual",
        maintainer: this.testCaseDefaultValue["责任人"]
          ? this.testCaseDefaultValue["责任人"]
          : data.maintainer,
        priority: priorityDefaultValue,
        prerequisite: "",
        remark: "",
        stepDescription: "",
        expectedResult: "",
        status: this.testCaseDefaultValue["用例状态"]
          ? this.testCaseDefaultValue["用例状态"]
          : "Prepare",
        steps: [
          {
            num: 1,
            desc: "",
            result: "",
          },
        ],
        tags: "[]",
        stepModel: "STEP",
      };
      if (data.changed) isChange = true;
      let steps = [];
      let stepNum = 1;
      if (node.children) {
        let prerequisiteNodes = node.children.filter(
          (childNode) =>
            childNode.data.resource &&
            childNode.data.resource.indexOf(
              this.$t("test_track.case.prerequisite")
            ) > -1
        );
        if (prerequisiteNodes.length > 1) {
          this.throwError(
            "[" +
              testCase.name +
              "]" +
              this.$t("test_track.case.exists_multiple_prerequisite_node")
          );
        }
        let remarkNodes = node.children.filter(
          (childNode) =>
            childNode.data.resource &&
            childNode.data.resource.indexOf(this.$t("commons.remark")) > -1
        );
        if (remarkNodes.length > 1) {
          this.throwError(
            "[" +
              testCase.name +
              "]" +
              this.$t("test_track.case.exists_multiple_remark_node")
          );
        }
        node.children.forEach((childNode) => {
          let childData = childNode.data;
          if (childData.type === "issue") return;
          if (
            childData.resource &&
            childData.resource.indexOf(
              this.$t("test_track.case.prerequisite")
            ) > -1
          ) {
            testCase.prerequisite = childData.text;
            if (childNode.children && childNode.children.length > 0) {
              this.throwError(
                "[" + testCase.name + "]前置条件下不能添加子节点！"
              );
            }
          } else if (
            childData.resource &&
            childData.resource.indexOf(this.$t("commons.remark")) > -1
          ) {
            testCase.remark = childData.text;
            if (childNode.children && childNode.children.length > 0) {
              this.throwError("[" + testCase.name + "]备注下不能添加子节点！");
            }
          } else {
            // 测试步骤
            let step = {};
            step.num = stepNum++;
            step.desc = childData.text;
            if (childNode.children) {
              let result = "";
              childNode.children.forEach((child) => {
                result += child.data.text;
                if (child.data.changed) {
                  isChange = true;
                }
              });
              step.result = result;
            }
            steps.push(step);

            if (data.stepModel === "TEXT") {
              testCase.stepDescription = step.desc;
              testCase.expectedResult = step.result;
            }
          }
          if (childData.changed) {
            isChange = true;
          }

          childNode.children.forEach((child) => {
            if (child.children && child.children.length > 0) {
              this.throwError(
                "[" + testCase.name + "]用例下子节点不能超过两层！"
              );
            }
          });
        });
      }
      testCase.steps = JSON.stringify(steps);

      if (data.isExtraNode) {
        // 如果是临时节点，打上了用例标签，则删除临时节点并新建用例节点
        this.pushDeleteNode(data);
        testCase.id = null;
        this.extraNodeChanged.push(data);
      }

      if (isChange) {
        testCase.targetId = null; // 排序处理

        if (this.moveEnable) {
          if (this.isCaseNode(preNode)) {
            let preId = preNode.data.id;
            if (preId && preId.length > 15) {
              testCase.targetId = preId;
              testCase.moveMode = "AFTER";
            } else {
              testCase.moveMode = "APPEND";
            }
          } else if (this.isCaseNode(nextNode) && !nextNode.data.isExtraNode) {
            let nextId = nextNode.data.id;
            if (nextId && nextId.length > 15) {
              testCase.targetId = nextId;
              testCase.moveMode = "BEFORE";
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

        if (testCase.name.length > 255) {
          this.throwError(
            this.$t(
              "api_test.home_page.failed_case_list.table_coloum.case_name"
            ) +
              this.$t("test_track.length_less_than") +
              255
          );
        }
        this.saveCases.push(testCase);
      }
      if (testCase.nodeId !== "root" && testCase.nodeId.length < 15) {
        this.throwError(
          this.$t("test_track.case.create_case") +
            "'" +
            testCase.name +
            "'" +
            this.$t("test_track.case.minder_create_tip")
        );
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
      if (
        node &&
        node.data.resource &&
        node.data.resource.indexOf(
          this.$t("api_test.definition.request.case")
        ) > -1
      ) {
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
      data.originId = data.id;
      if (nodeData.id && nodeData.id.length > 20) {
        nodeData.isEdit = true; // 编辑
      } else {
        nodeData.isEdit = false; // 新增
        nodeData.id = getUUID();
        data.newId = nodeData.id;
      }
      if (node.children) {
        nodeData.children = [];
        node.children.forEach((item) => {
          if (
            !isCaseNodeData(item.data) &&
            !isModuleNodeData(item.data) &&
            item.data.type !== "tmp"
          ) {
            // 子节点是临时节点才解析
            nodeData.children.push(this._buildExtraNode(item));
          }
        });
      }
      data.isExtraNode = true;
      return nodeData;
    },
    throwError(tip) {
      this.$error(tip);
      handleSaveError(window.minder.getRoot());
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
      if (type === "edit") {
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
    },
  },
};
</script>

<style scoped>
:deep(.minder) {
  max-height: calc(100vh - 218px);
  position: relative;
  top: 12px;
}
</style>
