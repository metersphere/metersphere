<template>
  <div v-loading="result.loading">
    <el-input :placeholder="$t('test_track.module.search')" v-model="filterText" size="small">
      <template v-if="type == 'edit'" v-slot:append>
        <el-button :disabled="disabled" icon="el-icon-folder-add" @click="openEditNodeDialog('add')"></el-button>
      </template>
    </el-input>

    <el-tree
      class="filter-tree node-tree"
      :data="treeNodes"
      :default-expanded-keys="expandedNode"
      node-key="id"
      @node-drag-end="handleDragEnd"
      @node-expand="nodeExpand"
      @node-collapse="nodeCollapse"
      :filter-node-method="filterNode"
      :expand-on-click-node="false"
      highlight-current
      :draggable="draggable"
      ref="tree">
      <template v-slot:default="{node,data}">
        <span class="custom-tree-node father" @click="handleNodeSelect(node)">
          <span class="node-icon">
            <i class="el-icon-folder"></i>
          </span>

          <span class="node-title">{{node.label}}</span>

          <span v-if="type == 'edit' && !disabled" class="node-operate child">
            <el-tooltip
              class="item"
              effect="dark"
              :open-delay="200"
              :content="$t('test_track.module.rename')"
              placement="top">
              <i @click.stop="openEditNodeDialog('edit', data)" class="el-icon-edit"></i>
            </el-tooltip>
            <el-tooltip
              class="item"
              effect="dark"
              :open-delay="200"
              :content="$t('test_track.module.add_submodule')"
              placement="top">
              <i @click.stop="openEditNodeDialog('add', data)" class="el-icon-circle-plus-outline"></i>
            </el-tooltip>
            <el-tooltip class="item" effect="dark"
              :open-delay="200" :content="$t('commons.delete')" placement="top">
              <i @click.stop="remove(node, data)" class="el-icon-delete"></i>
            </el-tooltip>
          </span>
        </span>
      </template>
    </el-tree>
    <node-edit ref="nodeEdit" :current-project="currentProject" :tree-nodes="treeNodes" @refresh="refreshNode" />
  </div>
</template>

<script>
import NodeEdit from "./NodeEdit";
import {ROLE_TEST_MANAGER, ROLE_TEST_USER} from "../../../../common/js/constants";
import {checkoutTestManagerOrTestUser, hasRoles} from "../../../../common/js/utils";

export default {
  name: "NodeTree",
  components: { NodeEdit },
  data() {
    return {
      result: {},
      expandedNode: [],
      filterText: "",
      defaultProps: {
        children: "children",
        label: "label"
      },
      disabled: false,
      list: []
    };
  },
  props: {
    type: {
      type: String,
      default: "view"
    },
    treeNodes: {
      type: Array
    },
    selectNode: {
      type: Object
    },
    draggable: {
      type: Boolean,
      default: true
    },
    currentProject: {
      type: Object
    }
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    }
  },
  mounted() {
    if (!checkoutTestManagerOrTestUser()) {
      this.disabled = true;
    }
  },
  methods: {
    handleDragEnd(draggingNode, dropNode, dropType, ev) {
      if (dropType === "none" || dropType === undefined) {
        return;
      }
      let param = this.buildParam(draggingNode, dropNode, dropType);

      this.list = [];
      this.getNodeTree(this.treeNodes,draggingNode.data.id, this.list);
      this.$post("/case/node/drag", param, () => {
        draggingNode.data.level = param.level;
        this.$post("/case/node/pos", this.list);
        this.refreshTable();
      }, (error) => {
        this.refreshNode();
      });
    },
    buildParam(draggingNode, dropNode, dropType) {
      let param = {};
      param.id = draggingNode.data.id;
      param.name = draggingNode.data.name;
      param.projectId = draggingNode.data.projectId;
      if (dropType === "inner") {
        param.parentId = dropNode.data.id;
        param.level = dropNode.data.level + 1;
      } else {
        if (!dropNode.parent.id || dropNode.parent.id === 0) {
          param.parentId = 0;
          param.level = 1;
        } else {
          param.parentId = dropNode.parent.data.id;
          param.level = dropNode.parent.data.level + 1;
        }
      }
      let nodeIds = [];
      this.getChildNodeId(draggingNode.data, nodeIds);
      if (dropNode.level == 1 && dropType != "inner") {
        param.nodeTree = draggingNode.data;
      } else {
        for (let i = 0; i < this.treeNodes.length; i++) {
          param.nodeTree = this.findTreeByNodeId(this.treeNodes[i], dropNode.data.id);
          if (param.nodeTree) {
            break;
          }
        }
      }

      param.nodeIds = nodeIds;
      return param;
    },
    getNodeTree(nodes, id, list) {
      if (!nodes) {
        return;
      }
      for (let i = 0; i < nodes.length; i++) {
        if (nodes[i].id === id) {
          i - 1 >= 0 ? list[0] = nodes[i-1].id : list[0] = "";
          list[1] = nodes[i].id;
          i + 1 < nodes.length ? list[2] = nodes[i+1].id : list[2] = "";
          return;
        }
        if (nodes[i].children) {
          this.getNodeTree(nodes[i].children, id, list);
        }
      }
    },
    refreshTable() {
      this.$emit('refreshTable');
    },
    findTreeByNodeId(rootNode, nodeId) {
      if (rootNode.id == nodeId) {
        return rootNode;
      }
      if (rootNode.children) {
        for (let i = 0; i < rootNode.children.length; i++) {
          if (this.findTreeByNodeId(rootNode.children[i], nodeId)) {
            return rootNode;
          }
        }
      }
    },
    remove(node, data) {
      this.$alert(
        this.$t("test_track.module.delete_confirm") +
          data.label +
          "，" +
          this.$t("test_track.module.delete_all_resource") +
          "？",
        "",
        {
          confirmButtonText: this.$t("commons.confirm"),
          callback: action => {
            if (action === "confirm") {
              let nodeIds = [];
              this.getChildNodeId(node.data, nodeIds);
              this.$post("/case/node/delete", nodeIds, () => {
                const parent = node.parent;
                const children = parent.data.children || parent.data;
                const index = children.findIndex(d => d.id === data.id);
                children.splice(index, 1);
                this.$success(this.$t("commons.delete_success"));
                this.$emit("refresh");
              });
            }
          }
        }
      );
    },
    handleNodeSelect(node) {
      let nodeIds = [];
      let pNodes = [];
      this.getChildNodeId(node.data, nodeIds);
      this.getParentNodes(node, pNodes);
      this.$emit("nodeSelectEvent", nodeIds, pNodes);
      this.$emit("update:selectNode", node);
    },
    getChildNodeId(rootNode, nodeIds) {
      //递归获取所有子节点ID
      nodeIds.push(rootNode.id);
      if (rootNode.children) {
        for (let i = 0; i < rootNode.children.length; i++) {
          this.getChildNodeId(rootNode.children[i], nodeIds);
        }
      }
    },
    getParentNodes(rootNode, pNodes) {
      if (rootNode.parent && rootNode.parent.id != 0) {
        this.getParentNodes(rootNode.parent, pNodes);
      }
      if (rootNode.data.name && rootNode.data.name != "") {
        pNodes.push(rootNode.data);
      }
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    openEditNodeDialog(type, data) {
      let nodeIds = [];
      if (type == 'edit') {
        this.getChildNodeId(data, nodeIds);
      }
      this.$refs.nodeEdit.open(type, data, nodeIds);
    },
    refreshNode() {
      this.$emit("refresh");
    },
    nodeExpand(data) {
      if (data.id) {
        this.expandedNode.push(data.id);
      }
    },
    nodeCollapse(data) {
      if (data.id) {
        this.expandedNode.splice(this.expandedNode.indexOf(data.id), 1);
      }
    }
  }
};
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: #409eff;
}

.el-icon-arrow-down {
  font-size: 12px;
}

.custom-tree-node {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  width: 100%;
}

.node-tree {
  margin-top: 15px;
}

.father .child {
  display: none;
}

.father:hover .child {
  display: block;
}

.node-title {
  width: 0px;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1 1 auto;
  padding: 0px 5px;
  overflow: hidden;
}

.node-operate > i {
  color: #409eff;
  margin: 0px 5px;
}
</style>
