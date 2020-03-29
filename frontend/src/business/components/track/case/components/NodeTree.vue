<template>

  <div>

    <el-input placeholder="搜索模块" v-model="filterText"
              size="small">
      <el-button slot="append" icon="el-icon-folder-add" @click="openEditNodeDialog('add')"></el-button>
    </el-input>

    <el-tree
        class="filter-tree node-tree"
        :data="treeNodes"
        node-key="id"
        @node-drag-start="handleDragStart"
        @node-drag-enter="handleDragEnter"
        @node-drag-leave="handleDragLeave"
        @node-drag-over="handleDragOver"
        @node-drag-end="handleDragEnd"
        @node-drop="handleDrop"
        :filter-node-method="filterNode"
        :expand-on-click-node="false"
        draggable
        :allow-drop="allowDrop"
        :allow-drag="allowDrag"
        ref="tree">

      <span class="custom-tree-node father" slot-scope="{ node, data }" @click="selectNode(node)">
          <span>{{node.label}}</span>
          <el-dropdown class="node-dropdown child">
            <span class="el-dropdown-link">
              <i class="el-icon-folder-add"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
            <el-dropdown-item>
              <div @click="openEditNodeDialog('edit', data)">重命名</div>
            </el-dropdown-item>
            <el-dropdown-item >
              <div @click="openEditNodeDialog('add', data)">添加子模块</div>
            </el-dropdown-item>
            <el-dropdown-item>
              <div @click="remove(node, data)">删除</div>
            </el-dropdown-item>
          </el-dropdown-menu>
          </el-dropdown>
        </span>
    </el-tree>

    <el-dialog title="添加模块" :visible.sync="dialogFormVisible" width="500px">

        <el-row type="flex" justify="center">
          <el-col :span="18">
            <el-form :model="form">
              <el-form-item label="模块名称" :label-width="formLabelWidth">
                <el-input v-model="form.name" autocomplete="off"></el-input>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="editNode">确 定</el-button>
      </div>
    </el-dialog>

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
        projectId: null
      },
      watch: {
        filterText(val) {
          this.$refs.tree.filter(val);
        },
        projectId(val){
          this.getNodeTree(val);
        }
      },
      created() {
        this.getNodeTree(this.projectId);
      },
      methods: {
        handleDragStart(node, ev) {
          console.log('drag start', node);
        },
        handleDragEnter(draggingNode, dropNode, ev) {
          console.log('tree drag enter: ', dropNode.label);
        },
        handleDragLeave(draggingNode, dropNode, ev) {
          console.log('tree drag leave: ', dropNode.label);
        },
        handleDragOver(draggingNode, dropNode, ev) {
          console.log('tree drag over: ', dropNode.label);
        },
        handleDragEnd(draggingNode, dropNode, dropType, ev) {
          console.log('tree drag end: ', dropNode && dropNode.label, dropType);
        },
        handleDrop(draggingNode, dropNode, dropType, ev) {
          console.log('tree drop: ', dropNode.label, dropType);
        },
        allowDrop(draggingNode, dropNode, type) {
          if (dropNode.data.label === '二级 3-1') {
            return type !== 'inner';
          } else {
            return true;
          }
        },
        allowDrag(draggingNode) {
          return draggingNode.data.label.indexOf('三级 3-2-2') === -1;
        },
        remove(node, data) {
          this.$post("/case/node/delete/" + data.id, null, () => {
            const parent = node.parent;
            const children = parent.data.children || parent.data;
            const index = children.findIndex(d => d.id === data.id);
            children.splice(index, 1);
          });
        },
        selectNode(node) {
          let nodeIds = [];
          this.getChildNodeId(node, nodeIds);
          this.$emit("nodeSelectEvent", nodeIds);
        },
        getChildNodeId(rootNode, nodeIds) {
          //递归获取所有子节点ID
          nodeIds.push(rootNode.data.id)
          for (let i = 0; i < rootNode.childNodes.length; i++){
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
        getNodeTree(projectId) {
          if(projectId){
            this.$get("/case/node/list/" + projectId, response => {
              this.treeNodes = response.data;
            });
          }
        },
        saveNode(type, pNode) {
          let param = {};
          let url = '';

          if(type === 'add'){
            url = '/case/node/add';
            param.level = 1;
            if(pNode){
              //非根节点
              param.pId = pNode.id;
              param.level = pNode.level + 1;
            }
          } else if(type === 'edit'){
            url = '/case/node/edit';
            param.id = this.editData.id
          }

          param.name = this.form.name;
          param.label = this.form.name;
          param.projectId = this.projectId;

          this.$post(url, param, response => {
            if(type === 'edit'){
              this.editData.label = param.label;
            } if(type === 'add') {
              param.id = response.data;
              if(pNode){
                if(pNode.children){
                  pNode.children.push(param);
                } else {
                  pNode.children = [param];
                }
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
    width: 100px;
  }

  .node-tree {
    margin-top: 15px;
  }

  .father .child{
    display:none;
  }

  .father:hover .child{
    display:block;
  }

</style>
