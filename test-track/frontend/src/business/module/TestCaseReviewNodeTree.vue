<template>
  <div>
    <slot name="header"></slot>
    <ms-node-tree
      v-loading="loading"
      :tree-nodes="treeNodes"
      :type="'edit'"
      :name-limit="100"
      :all-label="$t('commons.all_label.review')"
      :delete-permission="['PROJECT_TRACK_REVIEW:READ+DELETE']"
      :add-permission="['PROJECT_TRACK_REVIEW:READ+CREATE']"
      :update-permission="['PROJECT_TRACK_REVIEW:READ+EDIT']"
      default-label="未规划模块"
      local-suffix="ui_module"
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
        <ms-search-bar
            :show-operator="showOperator"
            :condition="condition"/>
      </template>
    </ms-node-tree>
  </div>
</template>

<script>
import MsNodeTree from "metersphere-frontend/src/components/new-ui/MsNodeTree";
import MsSearchBar from "metersphere-frontend/src/components/new-ui/MsSearchBar";
import {buildNodePath, buildTree} from "metersphere-frontend/src/model/NodeTree";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {
  getTestCaseReviewNodes,
  testCaseReviewNodeAdd,
  testCaseReviewNodeDelete,
  testCaseReviewNodeDrag,
  testCaseReviewNodeEdit,
  testCaseReviewNodePos
} from "@/api/test-case-review-node";
import {useStore} from "@/store";

export default {
  name: "TestCaseReviewNodeTree",
  components: {
    MsSearchBar,
    MsNodeTree,
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
        filterText: ""
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
    reviewCondition: Object,
    hideNodeOperator: {
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
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  methods: {
    filter() {
      this.$refs.nodeTree.filter(this.condition.filterText);
    },
    refresh() {
      this.$emit("refreshTable");
    },
    list() {
      if (this.projectId) {
        this.loading = true;
        getTestCaseReviewNodes(this.projectId, this.reviewCondition)
          .then(r => {
            this.loading = false;
            this.treeNodes = r.data;
            this.treeNodes.forEach(node => {
              buildTree(node, {path: ''});
            });
            this.setModuleOptions();
            if (this.$refs.nodeTree) {
              this.$refs.nodeTree.filter(this.condition.filterText);
            }
            if (this.currentNode && this.currentNode.data) {
              this.justSetCurrentKey();
            } else {
              this.$refs.nodeTree.setCurrentKey('root');
            }
          });
      }
    },
    setModuleOptions() {
      let moduleOptions = [];
      this.treeNodes.forEach(node => {
        buildNodePath(node, {path: ''}, moduleOptions);
      });
      useStore().testCaseReviewModuleOptions = moduleOptions;
    },
    nodeChange(node, nodeIds, pNodes) {
      this.currentNode = node;
      // 父组件刷新列表数据, 同时刷新Tree
      this.$emit("nodeSelectEvent", node, node.data.id === 'root' ? [] : nodeIds, pNodes);
    },
    justSetCurrentKey() {
      if (this.$refs.nodeTree) {
        this.$refs.nodeTree.justSetCurrentKey(this.currentNode.data.id)
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
      testCaseReviewNodeEdit(param)
        .then(() => {
          this.list();
          this.$emit("refreshTable");
        });
    },
    add(param) {
      if (param.length > 10) {
        this.$error(this.$t('commons.delete_success'), false);
      }
      param.projectId = this.projectId;
      testCaseReviewNodeAdd(param)
        .then(() => {
          this.$success(this.$t("test_track.module.success_create"), false);
          this.list();
          this.$emit("refreshTable");
        }).catch(() => {
          this.list();
          this.$emit("refreshTable");
      });
    },
    remove(nodeIds) {
      testCaseReviewNodeDelete(nodeIds)
        .then(() => {
          this.list();
          this.$emit("refreshTable");
          this.$success(this.$t('commons.delete_success'), false);
        });
    },
    drag(param, list) {
      testCaseReviewNodeDrag(param)
        .then(() => {
          testCaseReviewNodePos(list)
          this.list();
          this.$emit("refreshTable");
        }).catch(() => {
          this.list();
          this.$emit("refreshTable");
        });
    }
  }
};
</script>
