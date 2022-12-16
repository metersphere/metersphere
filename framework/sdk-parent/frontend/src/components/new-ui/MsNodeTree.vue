<template>
  <div>

    <slot name="header">
      <el-input :placeholder="$t('test_track.module.search')" v-model="filterText" size="small" :clearable="true"/>
    </slot>

    <ms-left-2-right-container v-if="scroll">
      <el-tree
        class="filter-tree node-tree"
        :data="extendTreeNodes"
        :default-expanded-keys="expandedNode"
        :default-expand-all="defaultExpandAll"
        node-key="id"
        @node-drag-end="handleDragEnd"
        @node-expand="nodeExpand"
        @node-collapse="nodeCollapse"
        :filter-node-method="filterNode"
        :expand-on-click-node="false"
        highlight-current
        :draggable="!disabled&&!hideOpretor"
        ref="tree">

        <template v-slot:default="{node,data}">
          <span class="custom-tree-node father" @click="handleNodeSelect(node)">
            <span v-if="!disabled" class="node-operate drag child">
              <svg-icon v-if="data.id !== 'root' && !hideOpretor" icon-class="icon_drag_outlined"/>
            </span>

            <span v-if="data.isEdit" @click.stop>
              <el-input @blur.stop="save(node, data)" @keyup.enter.native.stop="$event.target.blur()" v-model="data.name"
                        class="name-input" size="mini" ref="nameInput" :draggable="true"/>
            </span>

            <span v-if="!data.isEdit" class="node-icon">
              <svg-icon :icon-class="node.isCurrent ? 'icon_folder_selected' : 'icon_folder'"/>
            </span>

            <el-tooltip class="item" effect="dark" :content="data.name" placement="top-start" :open-delay="1000">
              <span v-if="!data.isEdit" class="node-title" v-text="isDefault(data) ? showBySubStr(getLocalDefaultName()) : showBySubStr(data.name)" :case-num="getCaseNum(data)"/>
            </el-tooltip>

            <span v-if="!disabled" class="node-operate child">
              <el-tooltip
                class="item"
                effect="dark"
                :open-delay="200"
                v-permission="addPermission"
                v-if="data.id && !isDefault(data) && !hideOpretor"
                :content="$t('test_track.module.add_submodule')"
                placement="top">
                <el-button class="node-operate-btn" @click.stop="append(node, data)" icon="el-icon-plus"/>
              </el-tooltip>

              <el-button v-if="!data.id" class="node-operate-btn" @click="remove(node, data)" icon="el-icon-delete"/>

              <el-dropdown placement="bottom-start" v-if="data.id && data.id !== 'root' && data.name !== defaultLabel && !hideOpretor">
                <el-button class="node-operate-btn" icon="el-icon-more" />
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item :disabled="!updatePermission">
                    <span @click.stop="edit(node, data)" class="more-operate-btn">
                      <svg-icon icon-class="icon_global_rename" style="margin-right: 9px; margin-top: 1px; width: 1.1em; height: 1.1em"/>
                      {{$t('test_track.module.rename')}}
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!deletePermission" :divided="true">
                    <span @click.stop="remove(node, data)" class="more-operate-btn" style="color: #F54A45;">
                      <svg-icon icon-class="icon_delete-trash_outlined_red" style="margin-right: 9px; margin-top: 1px; width: 1.1em; height: 1.1em"/>
                      {{$t('commons.delete')}}
                    </span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>

              <el-dropdown placement="bottom-start" v-if="data.id && data.name === defaultLabel && data.level !== 1 && !hideOpretor">
                <el-button class="node-operate-btn" icon="el-icon-more" />
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item :disabled="!updatePermission">
                    <span @click.stop="edit(node, data)" class="more-operate-btn">
                      <svg-icon icon-class="icon_global_rename" style="margin-right: 9px; margin-top: 1px; width: 1.1em; height: 1.1em"/>
                      {{$t('test_track.module.rename')}}
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item :disabled="!deletePermission" :divided="true">
                    <span @click.stop="remove(node, data)" class="more-operate-btn" style="color: #F54A45;">
                      <svg-icon icon-class="icon_delete-trash_outlined_red" style="margin-right: 9px; margin-top: 1px; width: 1.1em; height: 1.1em"/>
                      {{$t('commons.delete')}}
                    </span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>
          </span>
        </template>
      </el-tree>
    </ms-left-2-right-container>

    <el-tree
      v-else
      class="filter-tree node-tree"
      :data="extendTreeNodes"
      :default-expanded-keys="expandedNode"
      :default-expand-all="defaultExpandAll"
      node-key="id"
      @node-drag-end="handleDragEnd"
      @node-expand="nodeExpand"
      @node-collapse="nodeCollapse"
      :filter-node-method="filterNode"
      :expand-on-click-node="false"
      highlight-current
      :draggable="!disabled&&!hideOpretor"
      ref="tree">

      <template v-slot:default="{node,data}">
        <span class="custom-tree-node father" @click="handleNodeSelect(node)">
          <span v-if="!disabled" class="node-operate drag child">
            <svg-icon v-if="data.id !== 'root' && !hideOpretor" icon-class="icon_drag_outlined"/>
          </span>

          <span v-if="data.isEdit" @click.stop>
            <el-input @blur.stop="save(node, data)" @keyup.enter.native.stop="$event.target.blur()" v-model="data.name"
                      class="name-input" size="mini" ref="nameInput" :draggable="true"/>
          </span>

          <span v-if="!data.isEdit" class="node-icon">
            <svg-icon :icon-class="node.isCurrent ? 'icon_folder_selected' : 'icon_folder'"/>
          </span>

          <el-tooltip class="item" effect="dark" :content="data.name" placement="top-start" :open-delay="1000">
            <span v-if="!data.isEdit" class="node-title" v-text="isDefault(data) ? showBySubStr(getLocalDefaultName()) : showBySubStr(data.name)" :case-num="getCaseNum(data)"/>
          </el-tooltip>


          <span v-if="!disabled" class="node-operate child">
            <el-tooltip
              class="item"
              effect="dark"
              :open-delay="200"
              v-permission="addPermission"
              v-if="data.id && !isDefault(data) && !hideOpretor"
              :content="$t('test_track.module.add_submodule')"
              placement="top">
              <el-button class="node-operate-btn" @click.stop="append(node, data)" icon="el-icon-plus"/>
            </el-tooltip>

            <el-button v-if="!data.id" class="node-operate-btn" @click="remove(node, data)" icon="el-icon-delete"/>

            <el-dropdown placement="bottom-start" v-if="data.id && data.id !== 'root' && data.name !== defaultLabel && !hideOpretor">
              <el-button class="node-operate-btn" icon="el-icon-more" />
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :disabled="!updatePermission">
                  <span @click.stop="edit(node, data)" class="more-operate-btn">
                    <svg-icon icon-class="icon_global_rename" style="margin-right: 9px; margin-top: 1px; width: 1.1em; height: 1.1em"/>
                    {{$t('test_track.module.rename')}}
                  </span>
                </el-dropdown-item>
                <el-dropdown-item :disabled="!deletePermission" :divided="true">
                  <span @click.stop="remove(node, data)" class="more-operate-btn" style="color: #F54A45;">
                    <svg-icon icon-class="icon_delete-trash_outlined_red" style="margin-right: 9px; margin-top: 1px; width: 1.1em; height: 1.1em"/>
                    {{$t('commons.delete')}}
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>

            <el-dropdown placement="bottom-start" v-if="data.id && data.name === defaultLabel && data.level !== 1 && !hideOpretor">
              <el-button class="node-operate-btn" icon="el-icon-more" />
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :disabled="!updatePermission">
                  <span @click.stop="edit(node, data)" class="more-operate-btn">
                    <svg-icon icon-class="icon_global_rename" style="margin-right: 9px; margin-top: 1px; width: 1.1em; height: 1.1em"/>
                    {{$t('test_track.module.rename')}}
                  </span>
                </el-dropdown-item>
                <el-dropdown-item :disabled="!deletePermission" :divided="true">
                  <span @click.stop="remove(node, data)" class="more-operate-btn" style="color: #F54A45;">
                    <svg-icon icon-class="icon_delete-trash_outlined_red" style="margin-right: 9px; margin-top: 1px; width: 1.1em; height: 1.1em"/>
                    {{$t('commons.delete')}}
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </span>
        </span>
      </template>
    </el-tree>

    <slot name="bottom"/>
  </div>
</template>

<script>
import MsLeft2RightContainer from "../MsLeft2RightContainer";

export default {
  name: "MsNodeTree",
  components: {MsLeft2RightContainer},
  data() {
    return {
      result: {},
      filterText: "",
      expandedNode: [],
      reloaded: false,
      defaultProps: {
        children: "children",
        label: "label"
      },
      extendTreeNodes: [],
    };
  },
  props: {
    //是否允许拖拽隐藏侧边栏
    scroll: {
      type: Boolean,
      default: false
    },
    existSlotContainer: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      default: "view"
    },
    treeNodes: {
      type: Array
    },
    allLabel: {
      type: String,
      default() {
        return this.$t("commons.all_label.case");
      }
    },
    defaultLabel: {
      type: String,
      default() {
        return '未规划用例';
      }
    },
    nameLimit: {
      type: Number,
      default() {
        return 50;
      }
    },
    defaultExpandAll: {
      type: Boolean,
      default() {
        return false;
      }
    },
    showRemoveTip: {
      type: Boolean,
      default() {
        return true;
      }
    },
    showCaseNum: {
      type: Boolean,
      default() {
        return true;
      }
    },
    updatePermission: Array,
    addPermission: Array,
    deletePermission: Array,
    localSuffix: String,
    hideOpretor: Boolean,
  },
  watch: {
    treeNodes() {
      this.init();
    },
    filterText(val) {
      this.filter(val);
    }
  },
  computed: {
    disabled() {
      return this.type !== 'edit';
    }
  },
  methods: {
    init() {
      let num = 0;
      this.treeNodes.forEach(t => {
        num += t.caseNum;
      });
      this.extendTreeNodes = [];
      this.extendTreeNodes.unshift({
        "id": "root",
        "name": this.allLabel,
        "level": 0,
        "children": this.treeNodes,
        "caseNum": num > 0 ? num : ""
      });
      if (this.expandedNode.length === 0) {
        this.expandedNode.push("root");
      }
    },
    handleNodeSelect(node) {
      let nodeIds = [];
      let pNodes = [];
      this.getChildNodeId(node.data, nodeIds);
      this.getParentNodes(node, pNodes);
      this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
    },
    filterNode(value, data) {
      if (!value) return true;
      if (data.label) {
        return data.label.toLowerCase().indexOf(value.toLowerCase()) !== -1;
      }
      return false;
    },
    filter(val) {
      this.$nextTick(() => {
        if (this.$refs.tree) {
          this.$refs.tree.filter(val);
        }
      });
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
      // this.reloaded = false;
      this.$nextTick(() => {
        let node = this.$refs.tree.getNode(data);
        if (node) {
          node.expanded = false;
        }

        if (data.children && data.children.length > 0) {
          this.changeTreeNodeStatus(data);
        }
      });
    },
    // 改变节点的状态
    changeTreeNodeStatus(parentData) {
      for (let i = 0; i < parentData.children.length; i++) {
        let data = parentData.children[i];
        if (data.id) {
          this.expandedNode.splice(this.expandedNode.indexOf(data.id), 1);
        }
        let node = this.$refs.tree.getNode(data);
        if (node) {
          node.expanded = false;
        }

        // 遍历子节点
        if (data.children && data.children.length > 0) {
          this.changeTreeNodeStatus(data)
        }
      }
    },
    edit(node, data, isAppend) {
      this.$set(data, 'isEdit', true);
      this.$nextTick(() => {
        this.$refs.nameInput.focus();

        // 不知为何，执行this.$set(data, 'isEdit', true);进入编辑状态之后过滤会失效，重新执行下过滤
        if (!isAppend) {
          this.$nextTick(() => {
            this.filter(this.filterText);
          });
          this.$nextTick(() => {
            this.$emit('filter');
          });
        }
      });
    },
    increase(id) {
      this.traverse(id, node => {
        if (node.caseNum) {
          node.caseNum++;
        }
      }, true);
      if (this.extendTreeNodes[0].id === 'root') {
        this.extendTreeNodes[0].caseNum++;
      }
    },
    decrease(id) {
      this.traverse(id, node => {
        if (node.caseNum) {
          node.caseNum--;
        }
      }, true);
      if (this.extendTreeNodes[0].id === 'root') {
        this.extendTreeNodes[0].caseNum--;
      }
    },
    traverse(id, callback, isParentCallback) {
      for (let i = 0; i < this.treeNodes.length; i++) {
        let rootNode = this.treeNodes[i];
        this._traverse(rootNode, id, callback, isParentCallback);
      }
    },
    _traverse(rootNode, id, callback, isParentCallback) {
      if (rootNode.id === id) {
        if (callback) {
          callback(rootNode);
        }
        return true;
      }
      if (!rootNode.children) {
        return false;
      }
      for (let i = 0; i < rootNode.children.length; i++) {
        let children = rootNode.children[i];
        let result = this._traverse(children, id, callback, isParentCallback);
        if (result === true) {
          if (isParentCallback) {
            callback(rootNode);
          }
          return result;
        }
      }
    },
    append(node, data) {
      const newChild = {
        id: undefined,
        isEdit: false,
        name: "",
        children: []
      };
      if (!data.children) {
        this.$set(data, 'children', [])
      }
      data.children.push(newChild);
      this.edit(node, newChild, true);
      node.expanded = true;
      this.$nextTick(() => {
        this.$refs.nameInput.focus();
      });
    },
    save(node, data) {
      if (data.name.trim() === '') {
        this.$warning(this.$t('test_track.case.input_name'));
        this.$refs['nameInput'].focus();
        return;
      }
      if (data.name.trim().length > this.nameLimit) {
        this.$warning(this.$t('test_track.length_less_than') + this.nameLimit);
        this.$refs['nameInput'].focus();
        return;
      }
      if (data.name.indexOf("\\") > -1) {
        this.$warning(this.$t('commons.node_name_tip'));
        this.$refs['nameInput'].focus();
        return;
      }
      let param = {};
      this.buildSaveParam(param, node.parent.data, data);
      if (param.type === 'edit') {
        this.$emit('edit', param);
      } else {
        this.expandedNode.push(param.parentId);
        this.$emit('add', param);
      }
      if (!data.level) {
        data.level = param.level;
      }
      this.$set(data, 'isEdit', false);
    },
    remove(node, data) {
      if (data.label === undefined) {
        this.$refs.tree.remove(node);
        return;
      }
      let title = this.$t('commons.confirm_delete') + ': ' + data.label + "?";
      // let tip = '确定删除节点 ' + data.label + ' 及其子节点下所有资源' + '？';
      // let info =  this.$t("test_track.module.delete_confirm") + data.label + "，" + this.$t("test_track.module.delete_all_resource") + "？";
      if (this.showRemoveTip) {
        this.$confirm(this.$t('test_track.module.delete_tip'), title, {
            cancelButtonText: this.$t("commons.cancel"),
            confirmButtonText: this.$t("commons.confirm"),
            customClass: 'custom-confirm-delete',
            callback: action => {
              if (action === "confirm") {
                let nodeIds = [];
                this.getChildNodeId(node.data, nodeIds);
                this.$emit('remove', nodeIds, data);
              }
            }
          }
        );
      } else {
        let nodeIds = [];
        this.getChildNodeId(node.data, nodeIds);
        this.$emit('remove', nodeIds, data);
      }
    },
    handleDragEnd(draggingNode, dropNode, dropType, ev) {
      if (dropType === "none" || dropType === undefined) {
        return;
      }
      if (dropNode.data.id === 'root' && dropType === 'before' || draggingNode.data.name === this.defaultLabel) {
        this.$emit('refresh');
        return false;
      }
      let param = this.buildParam(draggingNode, dropNode, dropType);
      let list = [];
      this.getNodeTree(this.treeNodes, draggingNode.data.id, list);
      if (param.parentId === 'root') {
        param.parentId = undefined;
      }
      this.$emit('drag', param, list);
    },
    buildSaveParam(param, parentData, data) {
      if (data.id) {
        param.nodeIds = [];
        param.type = 'edit';
        param.id = data.id;
        param.level = data.level;
        param.parentId = data.parentId;
        this.getChildNodeId(data, param.nodeIds);
      } else {
        param.level = 1;
        param.type = 'add';
        if (parentData.id != 'root') {
          // 非根节点
          param.parentId = parentData.id;
          param.level = parentData.level + 1;
        }
      }
      param.name = data.name.trim();
      param.label = data.name;
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
      if (dropNode.data.level === 1 && dropType !== "inner") {
        // nodeTree 为需要修改的子节点
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
          i - 1 >= 0 ? list[0] = nodes[i - 1].id : list[0] = "";
          list[1] = nodes[i].id;
          i + 1 < nodes.length ? list[2] = nodes[i + 1].id : list[2] = "";
          return;
        }
        if (nodes[i].children) {
          this.getNodeTree(nodes[i].children, id, list);
        }
      }
    },
    findTreeByNodeId(rootNode, nodeId) {
      if (rootNode.id === nodeId) {
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
      if (rootNode.parent && rootNode.parent.id !== 0) {
        this.getParentNodes(rootNode.parent, pNodes);
      }
      if (rootNode.data.name && rootNode.data.name !== "") {
        pNodes.push(rootNode.data);
      }
    },
    setCurrentKey(currentNode) {
      if (currentNode && currentNode.data) {
        this.$nextTick(() => {
          this.handleNodeSelect(currentNode);
          this.$refs.tree.setCurrentKey(currentNode.data.id);
        })
      }
    },
    justSetCurrentKey(id) {
      if (id) {
        this.$nextTick(() => {
          this.$refs.tree.setCurrentKey(id);
        })
      }
    },
    isDefault(data) {
      return data.name === this.defaultLabel && data.level === 1;
    },
    getLocalDefaultName() {
      if (this.localSuffix) {
        return this.$t('commons.default_module.' + this.localSuffix);
      } else {
        return this.$t('commons.module_title');
      }
    },
    showBySubStr(text) {
      if (text.toString().length > 7) {
        return text.toString().substring(0, 7) + "...";
      } else {
        return text.toString();
      }
    },
    getCaseNum(data) {
      if (this.showCaseNum && data.caseNum) {
        return " (" + data.caseNum + ")";
      } else {
        return " (0)";
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

/*.father:hover .child {*/
/*  display: block;*/
/*}*/

:deep(.el-tree-node__content:hover .child) {
  display: block;
}

:deep(.node-icon .svg-icon) {
  width: 1.2em;
  height: 1.2em;
  margin-top: 4px;
}

.node-title {
  width: 0px;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1 1 auto;
  padding: 0px 9px;
  overflow: hidden;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  display: flex;
  align-items: center;
  color: #1F2329;
}

.node-title:after {
  color: #8F959E;
  content: attr(case-num);
  margin-left: 10px;
}

.count-title {
  width: auto;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0px 5px;
  overflow: hidden;
}

.node-operate > i {
  color: #409eff;
  margin: 0px 5px;
}

.name-input {
  height: 25px;
  line-height: 25px;
}

.name-input :deep( .el-input__inner ) {
  height: 25px;
  line-height: 25px;
}

:deep(.el-tree-node__expand-icon.el-icon-caret-right:before) {
  color: #646A73;
  font-size: 15px;
}

:deep(.is-leaf.el-tree-node__expand-icon.el-icon-caret-right:before) {
  color: transparent;
}

:deep(.el-tree-node__content) {
  width: auto;
  height: 40px;
  border-radius: 4px;
}

:deep(.el-tree-node__content:hover){
  background-color: rgba(31, 35, 41, 0.1);
  border-radius: 4px;
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background-color: rgba(120, 56, 135, 0.1);
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content  .el-tooltip.node-title.item) {
  color: #783887;
  font-weight: 500;
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content  .el-tooltip.node-title.item:after) {
  color: #783887;
  font-weight: 500;
}

.tree-node-drag-hover {
  text-align: center;
  height: 28px;
}

.ms-icon-more {
  transform: rotate(90deg);
  width: 9px;
  color: #cccccc;
}

.ms-icon-more:first-child {
  margin-right: -5px;
}

.drag {
  position: relative;
  left: -38px;
  top: 2px;
  width: 0;
}

.drag .svg-icon {
  width: 1.2em;
  height: 1.2em;
}

.node-operate-btn {
  width: 24px;
  height: 24px;
  padding: 0;
  border: none;
  background: rgba(31, 35, 41, 0.1);
  border-radius: 4px;
  margin-left: 5px;
}

.node-operate-btn i {
  color: #646A73;
}

.node-operate-btn:hover, .node-operate-btn:focus {
  color: #646A73;
}

.more-operate-btn {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  display: flex;
  align-items: center;
  letter-spacing: -0.1px;
  color: #1F2329;
  width: 80px;
  height: 32px;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: rgba(31, 35, 41, 0.1);
}
</style>
