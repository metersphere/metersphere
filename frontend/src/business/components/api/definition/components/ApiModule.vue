<template>
  <div v-loading="result.loading">
    <el-select class="protocol-select" size="small" v-model="protocol" @change="changeProtocol">
      <el-option
        v-for="item in options"
        :key="item.value"
        :name="item.name"
        :value="item.value"
        :disabled="item.disabled">
      </el-option>
    </el-select>
    <el-input class="filter-input" :placeholder="$t('test_track.module.search')" v-model="filterText"
              size="small">
      <template v-slot:append>
        <el-dropdown size="small" split-button type="primary" class="ms-api-button" @click="handleCommand('add-api')"
                     @command="handleCommand">
          <el-button icon="el-icon-folder-add" @click="addApi"></el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="add-api">{{$t('api_test.definition.request.title')}}</el-dropdown-item>
            <el-dropdown-item command="debug">{{$t('api_test.definition.request.fast_debug')}}</el-dropdown-item>
            <el-dropdown-item command="import">{{$t('api_test.api_import.label')}}</el-dropdown-item>
            <el-dropdown-item command="export">{{$t('report.export')}}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </template>
    </el-input>

    <el-tree :data="data"
             class="filter-tree node-tree"
             node-key="id"
             :default-expanded-keys="expandedNode"
             :expand-on-click-node="false"
             @node-expand="nodeExpand"
             @node-collapse="nodeCollapse"
             @node-click="selectModule"
             @node-drag-end="handleDragEnd"
             :filter-node-method="filterNode"
             :draggable="true"
             :allow-drop="allowDrop"
             :allow-drag="allowDrag" ref="tree">
        <span class="custom-tree-node father" slot-scope="{ node, data }">
          <!-- 如果是编辑状态 -->
          <template v-if="data.isEdit==1">
            <el-input ref="input"
                      @blur="() => submitEdit(node,data)"
                      v-model="newLabel"
                      class="ms-el-input" size="mini"></el-input>
          </template>
          <!-- 如果不是编辑状态 -->
           <i class="el-icon-delete" v-if="data.isEdit!=1 && data.id==='gc'"/>
           <i class="el-icon-folder" v-if="data.isEdit!=1 && data.id!='gc'"/>
          <span class="node-title" v-if="data.isEdit!=1" v-text="data.name"></span>
          <span class="node-operate child">
            <el-tooltip
              v-if="data.id!='root' && data.id!='gc'"
              class="item"
              effect="dark"
              :open-delay="200"
              :content="$t('test_track.module.rename')"
              placement="top">
              <i @click.stop="() => edit(node,data)" class="el-icon-edit"></i>
            </el-tooltip>

            <el-tooltip
              v-if="data.id!='gc'"
              class="item"
              effect="dark"
              :open-delay="200"
              :content="$t('test_track.module.add_submodule')"
              placement="top">
              <i @click.stop="() => append(node,data)" class="el-icon-circle-plus-outline"></i>
            </el-tooltip>

             <el-tooltip
               v-if="data.id!='root' && data.id!='gc'"
               class="item"
               effect="dark"
               :open-delay="200"
               :content="$t('commons.delete')"
               placement="top">
              <i @click.stop="() => remove(node, data)" class="el-icon-delete"></i>
            </el-tooltip>
          </span>
        </span>
    </el-tree>

    <ms-add-basis-api :current-protocol="protocol" ref="basisApi"></ms-add-basis-api>
    <api-import ref="apiImport" :project-id="projectId" @refresh="refresh"/>

  </div>

</template>

<script>
  import MsAddBasisApi from "./basis/AddBasisApi";
  import SelectMenu from "../../../track/common/SelectMenu";
  import {OPTIONS, DEFAULT_DATA} from "../model/JsonData";
  import ApiImport from "./import/ApiImport";
  import {getCurrentProjectID} from "@/common/js/utils";

  export default {
    name: 'MsApiModule',
    components: {
      MsAddBasisApi,
      SelectMenu,
      ApiImport
    },
    data() {
      return {
        result: {},
        options: OPTIONS,
        protocol: OPTIONS[0].value,
        httpVisible: false,
        expandedNode: [],
        filterText: "",
        nextFlag: true,
        projectId: "",
        data: DEFAULT_DATA,
        currentModule: {},
        newLabel: ""
      }
    },
    mounted() {
      this.projectId = getCurrentProjectID();
      this.changeProtocol();
    },
    watch: {
      filterText(val) {
        this.$refs.tree.filter(val);
      }
    },
    methods: {
      getApiModuleTree() {
        if (this.expandedNode.length === 0) {
          this.expandedNode.push("root");
        }
        this.nextFlag = true;
        this.result = this.$get("/api/module/list/" + this.projectId + "/" + this.protocol, response => {
          if (response.data != undefined && response.data != null) {
            this.data[1].children = response.data;
            let moduleOptions = [];
            this.data[1].children.forEach(node => {
              this.buildNodePath(node, {path: ''}, moduleOptions);
            });
            this.$emit('getApiModuleTree', moduleOptions);
          }
        });
      },
      handleCommand(e) {
        switch (e) {
          case "debug":
            this.$emit('debug');
            break;
          case "add-api":
            this.addApi();
            break;
          case "add-module":
            break;
          case "import":
            this.$refs.apiImport.open(this.currentModule);
            break;
          default:
            this.$emit('exportAPI');
            break;
        }
      },
      buildNodePath(node, option, moduleOptions) {
        //递归构建节点路径
        option.id = node.id;
        option.path = option.path + '/' + node.name;
        node.path = option.path;
        moduleOptions.push(option);
        if (node.children) {
          for (let i = 0; i < node.children.length; i++) {
            this.buildNodePath(node.children[i], {path: option.path}, moduleOptions);
          }
        }
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
      buildParam(draggingNode, dropNode, dropType) {
        let param = {};
        param.id = draggingNode.data.id;
        param.name = draggingNode.data.name;
        param.projectId = draggingNode.data.projectId;
        if (dropType === "inner") {
          param.parentId = dropNode.data.id;
          param.level = dropNode.data.level;
        } else {
          if (!dropNode.parent.id || dropNode.parent.id === 0) {
            param.parentId = 0;
            param.level = 1;
          } else {
            param.parentId = dropNode.parent.data.id;
            param.level = dropNode.parent.data.level;
          }
        }
        let nodeIds = [];
        this.getChildNodeId(draggingNode.data, nodeIds);
        if (dropNode.level == 1 && dropType != "inner") {
          param.nodeTree = draggingNode.data;
        } else {
          for (let i = 0; i < this.data.length; i++) {
            param.nodeTree = this.findTreeByNodeId(this.data[i], dropNode.data.id);
            if (param.nodeTree) {
              break;
            }
          }
        }
        param.nodeIds = nodeIds;
        return param;
      },
      getTreeNode(nodes, id, list) {
        if (!nodes) {
          return;
        }
        for (let i = 0; i < nodes.length; i++) {
          if (nodes[i].id === id) {
            i - 1 >= 0 ? list[0] = nodes[i - 1].id : list[0] = "";
            list[1] = nodes[i].id;
            i + 1 < nodes.length ? list[2] = nodes[i + 1].id : list[2] = "";
            return;
          }
          if (nodes[i].children) {
            this.getTreeNode(nodes[i].children, id, list);
          }
        }
      },
      handleDragEnd(draggingNode, dropNode, dropType, ev) {
        if (dropNode.data.id === "root" || dropType === "none" || dropType === undefined) {
          return;
        }
        let param = this.buildParam(draggingNode, dropNode, dropType);

        this.list = [];
        if (param.parentId === "root") {
          param.parentId = null;
        }
        this.getTreeNode(this.data, draggingNode.data.id, this.list);

        this.$post("/api/module/drag", param, () => {
          this.getApiModuleTree();
        }, (error) => {
          this.getApiModuleTree();
        });
      },

      allowDrop(draggingNode, dropNode, type) {
        if (dropNode.data.id === "root") {
          return false
        } else {
          return true
        }
      },
      allowDrag(draggingNode) {
        // 顶层默认分组不允许拖拽
        if (draggingNode.data.id === "root") {
          return false
        } else {
          return true
        }
      },
      append(node, data) {
        if (this.nextFlag === true) {
          const newChild = {
            id: "newId",
            isEdit: 0,
            name: "",
            children: []
          }
          if (!data.children) {
            this.$set(data, 'children', [])
          }
          this.nextFlag = false;
          data.children.push(newChild)
          this.edit(node, newChild);
        } else {
          this.$message.warning(this.$t('commons.please_save'));
        }
      },

      remove(node, data) {
        if (data.name === "") {
          this.nextFlag = true;
        }
        let delIds = [];
        this.getChildNodeId(data, delIds);
        delIds.push(data.id);
        this.$post("/api/module/delete", delIds, () => {
          this.$success(this.$t('commons.save_success'));
          // 移除节点
          const parent = node.parent
          const children = parent.data.children || parent.data
          const index = children.findIndex(d => d.id != undefined && data.id != undefined && d.id === data.id)
          children.splice(index, 1);
        });
      },

      edit(node, data) {
        this.$set(data, 'isEdit', 1)
        this.newLabel = data.name
        this.$nextTick(() => {
        })
      },

      submitEdit(node, data) {
        // 触发了保存按钮
        if (this.newLabel === "") {
          this.nextFlag = false;
          this.$message.warning(this.$t('commons.input_name'));
          return;
        }

        this.$set(data, 'name', this.newLabel)
        let flag = this.editApiModule(node, data);
        if (flag === false) {
          this.$set(data, 'isEdit', 1)
          return;
        }
        this.$set(data, 'isEdit', 0)
        this.newLabel = ""
        this.nextFlag = true;
      },

      cancelEdit(node, data) {
        this.newLabel = ""
        this.$set(data, 'isEdit', 0)
      },

      getChildNodeId(rootNode, nodeIds) {
        //递归获取所有子节点ID
        nodeIds.push(rootNode.id);
        this.nodePath += rootNode.name + "/";
        if (rootNode.children) {
          for (let i = 0; i < rootNode.children.length; i++) {
            this.getChildNodeId(rootNode.children[i], nodeIds);
          }
        }
      },
      // 保存或修改
      editApiModule(node, data) {
        if (!this.projectId) {
          this.$error("$t('api_test.select_project')");
          return;
        }
        let url = "";
        if (data.id === "newId") {
          url = '/api/module/add';
          data.level = 1;
          if (node.parent && node.parent.key != "root") {
            data.parentId = node.parent.key;
            data.level = node.parent.level;
          }
        } else {
          url = '/api/module/edit';
          let ids = [];
          this.getChildNodeId(data, ids);
          data.nodeIds = ids;
        }
        data.protocol = this.protocol;
        data.projectId = this.projectId;
        this.$post(url, data, () => {
          this.$success(this.$t('commons.save_success'));
          this.getApiModuleTree();
          this.nextFlag = true;
          return true;
        });
        return false;
      },

      selectModule(data) {
        if (data.id != "root") {
          if (data.path != undefined && !data.path.startsWith("/")) {
            data.path = "/" + data.path;
          }
          if (data.path != undefined && data.path.endsWith("/")) {
            data.path = data.path.substr(0, data.path.length - 1);
          }
          let nodeIds = [];
          this.getChildNodeId(data, nodeIds);
          data.ids = nodeIds;
          this.currentModule = data;
        }
        this.$emit('selectModule', data);
      },
      refresh(data) {
        this.$emit('refresh', data);
        this.getApiModuleTree();
      },
      saveAsEdit(data) {
        this.$emit('saveAsEdit', data);
      },
      filterNode(value, data) {
        if (!value) return true;
        return data.name.indexOf(value) !== -1;
      },
      addApi() {
        this.$refs.basisApi.open(this.currentModule, this.projectId);
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
      },
      changeProtocol() {
        this.getApiModuleTree();
        this.$emit('changeProtocol', this.protocol);
      }
    }
  }
</script>

<style scoped>
  .node-tree {
    margin-top: 15px;
    margin-bottom: 15px;
  }

  .ms-el-input {
    height: 25px;
    line-height: 25px;
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

  /deep/ .el-tree-node__content {
    height: 33px;
  }

  .ms-api-button {
    width: 30px;
  }

  .protocol-select {
    width: 95px;
    height: 30px;
  }

  .filter-input {
    width: 175px;
    padding-left: 3px;
  }

  .ms-api-button .el-button {
    padding: 10px;
  }

  .filter-input >>> .el-input-group__append {
    padding-right: 10px;
  }

  .protocol-select >>> .el-input--small {
    font-size: 10px;
    width: 90px;
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
