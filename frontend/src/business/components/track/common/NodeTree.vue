<template>

  <div v-loading="result.loading">
    <el-input :placeholder="$t('test_track.module.search')" v-model="filterText"
              size="small">
      <template v-if="type == 'edit'" v-slot:append>
        <el-button icon="el-icon-folder-add" @click="openEditNodeDialog('add')"></el-button>
      </template>
    </el-input>

    <el-tree
      class="filter-tree node-tree"
      :data="treeNodes"
      node-key="id"
      @node-drag-end="handleDragEnd"
      :filter-node-method="filterNode"
      :expand-on-click-node="false"
      highlight-current
      draggable
      ref="tree">

      <template v-slot:default="{node,data}">

        <span class="custom-tree-node father" @click="handleNodeSelect(node)">

          <span>{{node.label}}</span>

          <el-dropdown  v-if="type == 'edit'" class="node-dropdown child">
              <span class="el-dropdown-link">
                <i class="el-icon-folder-add"></i>
              </span>
              <el-dropdown-menu v-slot:default>
                <el-dropdown-item>
                  <div @click="openEditNodeDialog('edit', data)">{{$t('test_track.module.rename')}}</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div @click="openEditNodeDialog('add', data)">{{$t('test_track.module.add_submodule')}}</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div @click="remove(node, data)">{{$t('commons.delete')}}</div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>

        </span>

      </template>
    </el-tree>

    <node-edit ref="nodeEdit" @refresh="refreshNode"/>

  </div>

</template>

<script>

  import NodeEdit from './NodeEdit';

  export default {
    name: "NodeTree",
    components: {NodeEdit},
    data() {
      return {
        result: {},
        filterText: '',
        defaultProps: {
          children: 'children',
          label: 'label'
        }
      };
    },
    props: {
      type: {
        type: String,
        default: 'view'
      },
      treeNodes: {
        type: Array
      },
      selectNode: {
        type: Object
      }
    },
    watch: {
      filterText(val) {
        this.$refs.tree.filter(val);
      }
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
      remove(node, data) {
        this.$alert(this.$t('test_track.module.delete_confirm') + data.label + "，" +
          this.$t('test_track.module.delete_all_resource') + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              let nodeIds = [];
              this.getChildNodeId(node, nodeIds);
              this.$post("/case/node/delete", nodeIds, () => {
                const parent = node.parent;
                const children = parent.data.children || parent.data;
                const index = children.findIndex(d => d.id === data.id);
                children.splice(index, 1);
                this.$success(this.$t('commons.delete_success'));
                this.$emit("refresh");
              });
            }
          }
        });
      },
      handleNodeSelect(node) {
        let nodeIds = [];
        let nodeNames = [];
        this.getChildNodeId(node, nodeIds);
        this.getParentNodeName(node, nodeNames);
        this.$emit("nodeSelectEvent", nodeIds, nodeNames);
        this.$emit("update:selectNode", node);
      },
      getChildNodeId(rootNode, nodeIds) {
        //递归获取所有子节点ID
        nodeIds.push(rootNode.data.id);
        for (let i = 0; i < rootNode.childNodes.length; i++) {
          this.getChildNodeId(rootNode.childNodes[i], nodeIds);
        }
      },
      getParentNodeName(rootNode, nodeNames) {
        if (rootNode.parent && rootNode.parent.id != 0) {
          this.getParentNodeName(rootNode.parent, nodeNames)
        }
        if (rootNode.data.name && rootNode.data.name != '') {
          nodeNames.push(rootNode.data.name);
        }
      },
      filterNode(value, data) {
        if (!value) return true;
        return data.label.indexOf(value) !== -1;
      },
      openEditNodeDialog(type, data) {
        this.$refs.nodeEdit.open(type, data);
      },
      refreshNode() {
        this.$emit('refresh');
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

</style>
