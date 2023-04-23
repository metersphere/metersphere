<template>
  <div>
    <slot name="header"></slot>
    <ms-node-tree
      v-loading="loading"
      :tree-nodes="treeNodes"
      :type="'edit'"
      :name-limit="100"
      :delete-permission="['PROJECT_TRACK_CASE:READ+DELETE']"
      :add-permission="['PROJECT_TRACK_CASE:READ+CREATE']"
      :update-permission="['PROJECT_TRACK_CASE:READ+EDIT']"
      default-label="未规划用例"
      local-suffix="test_case"
      :hide-node-operator="hideNodeOperator"
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @nodeSelectEvent="nodeChange"
      @refresh="list"
      @filter="filter"
      ref="nodeTree">
      <template v-slot:header>
        <module-public-button
          v-if="showPublicBtn"
          :condition="condition"
          :public-total="publicTotal"
          :exe="enablePublic"/>
      </template>
    </ms-node-tree>
    <module-trash-button
      v-if="showTrashBtn"
      :condition="condition"
      :total="total"
      :exe="enableTrash"/>
    <is-change-confirm
      :tip="$t('test_track.case.minder_import_save_confirm_tip')"
      @confirm="changeConfirm"
      ref="isChangeConfirm"/>

  </div>

</template>

<script>
import MsNodeTree from "metersphere-frontend/src/components/new-ui/MsNodeTree";
import TestCaseCreate from "@/business/case/components/TestCaseCreate";
import TestCaseImport from "@/business/case/components/import/TestCaseImport";
import MsSearchBar from "metersphere-frontend/src/components/new-ui/MsSearchBar";
import {buildTree, buildNodePath} from "metersphere-frontend/src/model/NodeTree";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import ModuleTrashButton from "metersphere-frontend/src/components/ModuleTrashButton";
import {getTestCaseNodesByCaseFilter, getTestCaseNodesCountMap} from "@/api/testCase";
import IsChangeConfirm from "metersphere-frontend/src/components/IsChangeConfirm";
import ModulePublicButton from "metersphere-frontend/src/components/module/ModulePublicButton";
import {useStore} from "@/store";
import {
  testCaseNodeAdd,
  testCaseNodeDelete,
  testCaseNodeDrag,
  testCaseNodeEdit,
  testCaseNodePos
} from "@/api/test-case-node";
import {hasPermission} from "@/business/utils/sdk-utils";

export default {
  name: "TestCaseNodeTree",
  components: {
    IsChangeConfirm,
    MsSearchBar,
    TestCaseImport,
    TestCaseCreate,
    MsNodeTree,
    ModuleTrashButton,
    ModulePublicButton
  },
  data() {
    return {
      defaultProps: {
        children: "children",
        label: "label"
      },
      loading: false,
      treeNodes: [],
      condition: {
        filterText: "",
        trashEnable: false,
        publicEnable: false
      },
      currentNode: {}
    };
  },
  props: {
    type: {
      type: String,
      default: "view"
    },
    showOperator: Boolean,
    total: Number,
    publicTotal: Number,
    caseCondition: Object,
    showTrashBtn: {
      type: Boolean,
      default: true
    },
    showPublicBtn: {
      type: Boolean,
      default: true
    },
    hideNodeOperator: {
      type: Boolean,
      default: false
    },
    // 脑图模式
    isMinderMode: {
      type: Boolean,
      default: false
    },
  },
  watch: {
    treeNodes() {
      this.$emit('setTreeNodes', this.treeNodes);
    },
    'condition.filterText'() {
      this.filter();
    },
  },
  mounted() {
    let store = useStore();
    store.testCaseSelectNode = {};
    store.testCaseSelectNodeIds = [];
    this.list();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    routeModuleId() {
      return this.$route.query.moduleId;
    }
  },
  methods: {
    filter() {
      this.$refs.nodeTree.filter(this.condition.filterText);
    },
    saveAsEdit(data) {
      this.$emit('saveAsEdit', data);
    },
    createCase(data) {
      this.$emit('createCase', data);
    },
    refresh() {
      this.$emit("refreshTable");
    },
    refreshAll() {
      this.$emit('refreshAll');
    },
    importRefresh() {
      this.$emit('importRefresh');
    },
    enableTrash() {
      this.condition.trashEnable = true;
      // 隐藏公共用例库背景色
      this.condition.publicEnable = false;
      this.$emit('enableTrash', this.condition.trashEnable);
      this.$emit('toPublic', 'trash');
    },
    enablePublic() {
      this.condition.publicEnable = true;
      this.condition.trashEnable = false;
      this.$emit('enablePublic', this.condition.publicEnable);
      this.$emit('toPublic', 'public');
    },
    list() {
      if (this.projectId) {
        this.caseCondition.casePublic = false;
        this.loading = true;
        getTestCaseNodesByCaseFilter(this.projectId, this.caseCondition)
          .then(r => {
            this.loading = false;
            this.treeNodes = r.data;
            this.treeNodes.forEach(node => {
              node.name = node.name === '未规划用例' ? this.$t('api_test.unplanned_case') : node.name
              buildTree(node, {path: ''});
            });
            this.setModuleOptions();
            if (this.$refs.nodeTree) {
              this.$refs.nodeTree.filter(this.condition.filterText);
            }
            let isCurrentNodeEqualsRouteModule = this.currentNode && this.currentNode.data && this.currentNode.data.id === this.routeModuleId;
            if (this.routeModuleId && !isCurrentNodeEqualsRouteModule) {
              if (this.$refs.nodeTree) {
                this.$nextTick(() => {
                  if (this.$refs.nodeTree.getNode(this.routeModuleId)) {
                    // 如果项目有此模块，则设置 moduleId
                    this.$refs.nodeTree.setCurrentKeyById(this.routeModuleId);
                  }
                });
              }
            } else {
              if (this.isMinderMode) {
                this.forceSetCurrentKey();
              } else {
                this.setCurrentKey();
              }
            }
          });
      }
    },
    async waitList() {
      if (this.projectId) {
        this.caseCondition.casePublic = false;
        this.loading = true;
        await getTestCaseNodesByCaseFilter(this.projectId, this.caseCondition)
          .then(r => {
            this.loading = false;
            this.treeNodes = r.data;
            this.treeNodes.forEach(node => {
              node.name = node.name === '未规划用例' ? this.$t('api_test.unplanned_case') : node.name
              buildTree(node, {path: ''});
            });
            this.setModuleOptions();
            if (this.$refs.nodeTree) {
              this.$refs.nodeTree.filter(this.condition.filterText);
            }
            this.setCurrentKey();
          });
      }
    },
    setCurrentKey() {
      if (this.$refs.nodeTree) {
        this.$refs.nodeTree.setCurrentKey(this.currentNode);
      }
    },
    // 重新获取 currentNode ，因为脑图更新完之后可能存在 currentNode 过时的情况
    forceSetCurrentKey() {
      if (this.$refs.nodeTree && this.currentNode && this.currentNode.data) {
        this.$refs.nodeTree.setCurrentKeyById(this.currentNode.data.id);
      }
    },
    increase(id) {
      this.$refs.nodeTree.increase(id);
    },
    decrease(id) {
      this.$refs.nodeTree.decrease(id);
    },
    edit(param) {
      param.projectId = this.projectId;
      testCaseNodeEdit(param)
        .then(() => {
          this.list();
          this.$emit("refreshTable");
        });
    },
    add(param) {
      param.projectId = this.projectId;
      testCaseNodeAdd(param)
        .then(() => {
          this.$success(this.$t("test_track.module.success_create"), false);
          this.list();
        }).catch(() => {
        this.list();
      });
    },
    remove(nodeIds) {
      testCaseNodeDelete(nodeIds)
        .then(() => {
          this.list();
          this.$emit("refreshTable")
          this.$success(this.$t('commons.delete_success'), false);
        });
    },
    drag(param, list) {
      testCaseNodeDrag(param)
        .then(() => {
          testCaseNodePos(list)
          this.list();
        }).catch((err) => {
          this.list();
        });
    },
    setModuleOptions() {
      let moduleOptions = [];
      this.treeNodes.forEach(node => {
        buildNodePath(node, {path: ''}, moduleOptions);
      });
      useStore().testCaseModuleOptions = moduleOptions;
    },
    nodeChange(node, nodeIds, pNodes) {
      let store = useStore();
      store.testCaseSelectNode = node;
      store.testCaseSelectNodeIds = nodeIds;
      this.condition.trashEnable = false;
      this.condition.publicEnable = false;
      this.currentModule = node.data;
      this.currentNode = node;

      this.$emit("nodeSelectEvent", node, node.data.id === 'root' ? [] : nodeIds, pNodes);
      // 刷新模块用例数
      this.updateNodeCount();
    },
    updateNodeCount() {
      getTestCaseNodesCountMap(this.projectId, this.caseCondition)
        .then((r) => {
          if (this.$refs.nodeTree) {
            this.$refs.nodeTree.updateNodeCount(r.data);
          }
        });
    },
    nohupReloadTree(selectNodeId) {
      if (this.projectId) {
        this.caseCondition.casePublic = false;
        getTestCaseNodesByCaseFilter(this.projectId, this.caseCondition)
          .then(r => {
            this.treeNodes = r.data;
            this.treeNodes.forEach(node => {
              node.name = node.name === '未规划用例' ? this.$t('api_test.unplanned_case') : node.name
              buildTree(node, {path: ''});
            });
            this.$nextTick(() => {
              if (this.$refs.nodeTree) {
                this.$refs.nodeTree.filter(this.condition.filterText);
                if (selectNodeId) {
                  this.$refs.nodeTree.justSetCurrentKey(selectNodeId);
                }
              }
            })
          });
      }
    },
    openMinderConfirm() {
      let isTestCaseMinderChanged = useStore().isTestCaseMinderChanged;
      if (!hasPermission('PROJECT_TRACK_CASE:READ+EDIT')) {
        return false;
      }
      if (isTestCaseMinderChanged) {
        this.$refs.isChangeConfirm.open();
      }
      return isTestCaseMinderChanged;
    },
    changeConfirm(isSave) {
      this.$emit('importChangeConfirm', isSave);
    }
  }
};
</script>

<style scoped>

</style>
