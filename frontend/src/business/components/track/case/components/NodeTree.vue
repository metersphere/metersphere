<template>

  <div>
    <el-input :placeholder="$t('test_track.search_module')" v-model="filterText"
              size="small">
      <template v-slot:append>
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
        <span class="custom-tree-node father" @click="selectNode(node)">
          <span>{{node.label}}</span>
          <el-dropdown class="node-dropdown child">
              <span class="el-dropdown-link">
                <i class="el-icon-folder-add"></i>
              </span>
            <el-dropdown-menu v-slot:default>
              <el-dropdown-item>
                <div @click="openEditNodeDialog('edit', data)">{{$t('test_track.rename')}}</div>
              </el-dropdown-item>
              <el-dropdown-item>
                <div @click="openEditNodeDialog('add', data)">{{$t('test_track.add_submodule')}}</div>
              </el-dropdown-item>
              <el-dropdown-item>
                <div @click="remove(node, data)">{{$t('commons.delete')}}</div>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </span>
      </template>
    </el-tree>

    <el-dialog :title="$t('test_track.add_module')" :visible.sync="dialogFormVisible" width="500px">

      <el-row type="flex" justify="center">
        <el-col :span="18">
          <el-form :model="form">
            <el-form-item :label="$t('test_track.module_name')" :label-width="formLabelWidth">
              <el-input v-model="form.name" autocomplete="off"></el-input>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false">{{$t('test_track.cancel')}}</el-button>
          <el-button type="primary" @click="editNode">{{$t('test_track.confirm')}}</el-button>
        </div>
      </template>
    </el-dialog>

  </div>

</template>

<script>

  import {CURRENT_PROJECT} from '../../../../../common/constants';

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
      currentProject: {
        type: Object
      }
    },
    watch: {
      filterText(val) {
        this.$refs.tree.filter(val);
      },
      currentProject() {
        this.getNodeTree();
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
      remove(node, data) {
        this.$alert(this.$t('test_track.delete_module_confirm') + data.label + "，" +
          this.$t('test_track.delete_module_resource') + "？", '', {
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
                this.$emit("refresh");
              });
            }
          }
        });
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
      editNode() {
        this.saveNode(this.editType, this.editData);
        this.dialogFormVisible = false;
      },
      openEditNodeDialog(type, data) {
        this.editType = type;
        this.editData = data;
        this.dialogFormVisible = true;
      },
      getNodeTree() {
        if (this.currentProject) {
          let projectId = this.currentProject.id;
          this.$get("/case/node/list/" + projectId, response => {
            this.treeNodes = response.data;
          });
        }
      },
      saveNode(type, pNode) {
        let param = {};
        let url = '';

        if (type === 'add') {
          url = '/case/node/add';
          param.level = 1;
          if (pNode) {
            //非根节点
            param.pId = pNode.id;
            param.level = pNode.level + 1;
          }
        } else if (type === 'edit') {
          url = '/case/node/edit';
          param.id = this.editData.id
        }

        param.name = this.form.name;
        param.label = this.form.name;

        if (localStorage.getItem(CURRENT_PROJECT)) {
          param.projectId = JSON.parse(localStorage.getItem(CURRENT_PROJECT)).id;
        }

        this.$post(url, param, response => {
          if (type === 'edit') {
            this.editData.label = param.label;
          }
          if (type === 'add') {
            param.id = response.data;
            if (pNode) {
              this.$refs.tree.append(param, pNode);
            } else {
              this.treeNodes.push(param);
            }
          }
        });
        this.form.name = '';
      },
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
