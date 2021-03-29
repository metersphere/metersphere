<template>
  <div>
    <slot name="header"></slot>
    <ms-node-tree
      v-loading="result.loading"
      :tree-nodes="treeNodes"
      :type="'edit'"
      :name-limit="100"
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @nodeSelectEvent="nodeChange"
      @refresh="list"
      ref="nodeTree">
      <template v-slot:header>
        <ms-search-bar
          :condition="condition"
          :commands="operators"/>
      </template>
    </ms-node-tree>
    <test-case-import @refreshAll="refreshAll" ref="testCaseImport"></test-case-import>
    <test-case-create
      :tree-nodes="treeNodes"
      @saveAsEdit="saveAsEdit"
      @createCase="createCase"
      @refresh="refresh"
      ref="testCaseCreate"
    ></test-case-create>
  </div>

</template>

<script>
import NodeEdit from "./NodeEdit";
import MsNodeTree from "./NodeTree";
import TestCaseCreate from "@/business/components/track/case/components/TestCaseCreate";
import TestCaseImport from "@/business/components/track/case/components/TestCaseImport";
import MsSearchBar from "@/business/components/common/components/search/MsSearchBar";

export default {
  name: "TestCaseNodeTree",
  components: {MsSearchBar, TestCaseImport, TestCaseCreate, MsNodeTree, NodeEdit},
  data() {
    return {
      defaultProps: {
        children: "children",
        label: "label"
      },
      result: {},
      treeNodes: [],
      condition: {
        filterText: "",
        trashEnable: false
      },
      operators: [
        {
          label: this.$t('test_track.case.create'),
          callback: this.addTestCase
        },
        {
          label: this.$t('api_test.api_import.label'),
          callback: this.handleImport
        },
        {
          label: this.$t('api_test.export_config'),
          callback: () => {this.$emit('exportTestCase')}
        }
      ]
    };
  },
  props: {
    type: {
      type: String,
      default: "view"
    },
  },
  watch: {
    treeNodes() {
      this.$emit('setTreeNodes', this.treeNodes);
    },
    'condition.filterText'(val) {
      this.$refs.nodeTree.filter(val);
    },
  },
  mounted() {
    this.list();
  },
  computed: {
    projectId() {
      return this.$store.state.projectId
    },
  },
  methods: {
    addTestCase(){
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
     this.$refs.testCaseCreate.open(this.currentModule)
    },
    saveAsEdit(data) {
      this.$emit('saveAsEdit', data);
    },
    createCase(data) {
      this.$emit('createCase', data);
    },
    refresh() {
      this.$emit("refreshTable");
    },
    refreshAll() {
      this.$emit('refreshAll');
    },
    list() {
      if (this.projectId) {
        this.result = this.$get("/case/node/list/" + this.projectId, response => {
          this.treeNodes = response.data;
          if (this.$refs.nodeTree) {
            this.$refs.nodeTree.filter();
          }
        });
      }
    },
    edit(param) {
      param.projectId = this.projectId;
      this.$post("/case/node/edit", param, () => {
        this.$success(this.$t('commons.save_success'));
        this.list();
        this.$emit("refreshTable");
      }, (error) => {
        this.list();
      });
    },
    add(param) {
      param.projectId = this.projectId;
      this.$post("/case/node/add", param, () => {
        this.$success(this.$t('commons.save_success'));
        this.list();
      }, (error) => {
        this.list();
      });
    },
    handleImport() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$refs.testCaseImport.open();
    },
    remove(nodeIds) {
      this.$post("/case/node/delete", nodeIds, () => {
        this.list();
        this.$emit("refreshTable")
      }, (error) => {
        this.list();
      });
    },
    drag(param, list) {
      this.$post("/case/node/drag", param, () => {
        this.$post("/case/node/pos", list);
        this.list();
      }, (error) => {
        this.list();
      });
    },
    nodeChange(node, nodeIds, pNodes) {
      this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
      this.currentModule = node.data;
      if (node.data.id === 'root') {
        this.$emit("nodeSelectEvent", node, [], pNodes);
      } else {
        this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
      }
    },
  }
};
</script>

<style scoped>

</style>
