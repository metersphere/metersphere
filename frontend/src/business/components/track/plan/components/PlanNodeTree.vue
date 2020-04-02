<template>

  <div>
    <el-input :placeholder="$t('test_track.search_module')" v-model="filterText" size="small"></el-input>

    <el-tree
      class="filter-tree node-tree"
      :data="treeNodes"
      node-key="id"
      @node-drag-end="handleDragEnd"
      :filter-node-method="filterNode"
      :expand-on-click-node="false"
      draggable
      ref="tree">
      <template @click="selectNode(node)" v-slot:default="{node}">
        {{node.label}}
      </template>
    </el-tree>
  </div>


</template>

<script>
  export default {
    name: "NodeTree",
    data() {
      return {
        filterText: '',
        defaultProps: {
          children: 'children',
          label: 'label'
        },
        form: {
          name: '',
        },
        formLabelWidth: '80px',
        dialogTableVisible: false,
        dialogFormVisible: false,
        editType: '',
        editData: {},
        treeNodes: [],
        defaultKeys: []
      };
    },
    props: {
      planId: {
        type: String
      }
    },
    watch: {
      filterText(val) {
        this.$refs.tree.filter(val);
      }
    },
    created() {
      this.getNodeTree();
    },
    methods: {
      handleDragEnd(draggingNode, dropNode, dropType, ev) {
        let param = {};
        param.id = draggingNode.data.id;
        if (dropType === 'inner') {
          param.pId = dropNode.data.id;
          param.level = dropNode.data.level + 1;
        } else {
          if (dropNode.parent.id === 0) {
            param.pId = 0;
            param.level = 1;
          } else {
            param.pId = dropNode.parent.data.id;
            param.level = dropNode.parent.data.level + 1;
          }
        }
        this.$post('/case/node/edit', param);
      },
      selectNode(node) {
        let nodeIds = [];
        this.getChildNodeId(node, nodeIds);
        this.$emit("nodeSelectEvent", nodeIds);
      },
      getChildNodeId(rootNode, nodeIds) {
        //递归获取所有子节点ID
        nodeIds.push(rootNode.data.id);
        for (let i = 0; i < rootNode.childNodes.length; i++) {
          this.getChildNodeId(rootNode.childNodes[i], nodeIds);
        }
        return nodeIds;
      },
      filterNode(value, data) {
        if (!value) return true;
        return data.label.indexOf(value) !== -1;
      },
      getNodeTree() {
        if (this.planId) {
          this.$get("/case/node/list/test/" + this.planId, response => {
            this.treeNodes = response.data;
          });
        }
      }
    }
  }
</script>

<style scoped>

  .el-dropdown-link {
    cursor: pointer;
    color: #409EFF;
  }

  .el-icon-arrow-down {
    font-size: 12px;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
    width: 100px;
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

</style>
