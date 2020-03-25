<template>

  <div>


    <el-input placeholder="搜索模块" v-model="filterText"
              size="small">
      <el-button slot="append" icon="el-icon-folder-add" @click="editNode('add')"></el-button>
    </el-input>

    <el-tree
      class="filter-tree node-tree"
      :data="trees"
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

      <span class="custom-tree-node father" slot-scope="{ node, data }" @click="selectNode">
              <span>{{node.label}}</span>
              <el-dropdown class="node-dropdown child">
                <span class="el-dropdown-link">
                  <i class="el-icon-folder-add"></i>
                </span>
                <el-dropdown-menu slot="dropdown">
                <el-dropdown-item>
                  <div @click="editNode('edit', data)">重命名</div>
                </el-dropdown-item>
                <el-dropdown-item >
                  <div @click="editNode('add', data)">添加子模块</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div @click="test">删除</div>
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
        <el-button type="primary" @click="saveNode">确 定</el-button>
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
          trees: [{
            id: 1,
            label: '一级 1',
            children: [{
              id: 4,
              label: '二级 1-1',
              children: [{
                id: 9,
                label: '三级 1-1-1'
              }, {
                id: 10,
                label: '三级 1-1-2'
              }]
            }]
          }, {
            id: 2,
            label: '一级 2',
            children: [{
              id: 5,
              label: '二级 2-1'
            }, {
              id: 6,
              label: '二级 2-2'
            }]
          }, {
            id: 3,
            label: '一级 3',
            children: [{
              id: 7,
              label: '二级 3-1'
            }, {
              id: 8,
              label: '二级 3-2',
              children: [{
                id: 11,
                label: '三级 3-2-1'
              }, {
                id: 12,
                label: '三级 3-2-2'
              }, {
                id: 13,
                label: '三级 3-2-3'
              }]
            }]
          }],
          defaultProps: {
            children: 'children',
            label: 'label'
          },
          form: {
            name: '',
            region: '',
            date1: '',
            date2: '',
            delivery: false,
            type: [],
            resource: '',
            desc: ''
          },
          formLabelWidth: '80px',
          dialogTableVisible: false,
          dialogFormVisible: false,
          editType: '',
          editData: {}
        };
      },
      watch: {
        filterText(val) {
          this.$refs.tree.filter(val);
        }
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

        append(data) {
          let id = 0;
          const newChild = { id: id++, label: 'testtest', children: [] };
          if (!data.children) {
            this.$set(data, 'children', []);
          }
          data.children.push(newChild);
        },

        remove(node, data) {
          const parent = node.parent;
          const children = parent.data.children || parent.data;
          const index = children.findIndex(d => d.id === data.id);
          children.splice(index, 1);
        },
        selectNode() {
          console.log("selet node-----");
        },
        filterNode(value, data) {
          if (!value) return true;
          return data.label.indexOf(value) !== -1;
        },
        saveNode() {

          let type = this.editType;
          let node = this.editData;
          if( type === 'add' ){
            if(node === undefined){
              console.log("add root node");
            } else {
              console.log("add node");
            }
          } else if(type === 'edit'){
            console.log("rename");
          }

          this.form.name = '';

          this.dialogFormVisible = false;
        },
        editNode(type, data) {

          this.editType = type;
          this.editData = data;
          this.dialogFormVisible = true;
        },
        test() {

          console.log("----");
          alert("ehllo");
          // this.dialogFormVisible = true;
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


  .node-dropdown {
    /*align-items: right;*/
    /*margin-left: 50px;*/
    /*color: darkgrey;*/
    /*opacity:0.1;*/
    /*filter:alpha(opacity=1); !* 针对 IE8 以及更早的版本 *!*/
  }


  .father .child{
    display:none;
  }

  .father:hover .child{
    display:block;
  }

</style>
