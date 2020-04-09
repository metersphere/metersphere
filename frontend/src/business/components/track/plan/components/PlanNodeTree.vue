<template>

  <div>
    <el-input :placeholder="$t('test_track.search_module')" v-model="filterText" size="small"></el-input>

    <el-tree
      v-loading="result.loading"
      class="filter-tree node-tree"
      :data="treeNodes"
      node-key="id"
      @node-drag-end="handleDragEnd"
      :filter-node-method="filterNode"
      :expand-on-click-node="false"
      highlight-current
      draggable
      ref="tree">
      <template v-slot:default="{node}">
        <span class="custom-tree-node" @click="selectNode(node)">
          {{node.label}}
        </span>
      </template>
    </el-tree>

  </div>

</template>

<script>
    export default {
      name: "PlanNodeTree",
      data() {
        return {
          result: {},
          filterText: '',
          defaultProps: {
            children: 'children',
            label: 'label'
          },
          dialogTableVisible: false,
          defaultKeys: [],
          treeNodes: []
        };
      },
      props: {
        planId: {
          type: String
        },
        showAll: {
          type: Boolean
        }
      },
      created() {
        this.initTree();
      },
      watch: {
        filterText(val) {
          this.$refs.tree.filter(val);
        },
        planId() {
          this.initTree();
        },
        '$route'(to, from) {
          if (to.path.indexOf("/track/plan/view/") >= 0){
            this.initTree();
          }
        }
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
      methods: {
        initTree() {
          if (this.showAll) {
            this.getAllNodeTreeByPlanId();
          } else {
            this.getNodeTreeByPlanId();
          }
        },
        handleDragEnd(draggingNode, dropNode, dropType, ev) {
          let param = {};
          param.id = draggingNode.data.id;
          if(dropType === 'inner'){
            param.pId = dropNode.data.id;
            param.level = dropNode.data.level + 1;
          } else {
            if(dropNode.parent.id === 0){
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
          for (let i = 0; i < rootNode.childNodes.length; i++){
            this.getChildNodeId(rootNode.childNodes[i], nodeIds);
          }
          return nodeIds;
        },
        filterNode(value, data) {
          if (!value) return true;
          return data.label.indexOf(value) !== -1;
        },
        getNodeTreeByPlanId() {
          if(this.planId){
            this.result = this.$get("/case/node/list/plan/" + this.planId, response => {
              this.treeNodes = response.data;
            });
          }
        },
        getAllNodeTreeByPlanId() {
          if (this.planId) {
            this.result = this.$get("/case/node/list/all/plan/" + this.planId, response => {
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
