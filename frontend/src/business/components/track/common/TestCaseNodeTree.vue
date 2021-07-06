<template>
  <div>
    <slot name="header"></slot>
    <ms-node-tree
      v-loading="result.loading"
      :tree-nodes="treeNodes"
      :type="'edit'"
      :name-limit="100"
      :delete-permission="['PROJECT_TRACK_CASE:READ+DELETE']"
      :add-permission="['PROJECT_TRACK_CASE:READ+CREATE']"
      :update-permission="['PROJECT_TRACK_CASE:READ+EDIT']"
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @nodeSelectEvent="nodeChange"
      @refresh="list"
      ref="nodeTree">
      <template v-slot:header>
        <ms-search-bar
          :show-operator="showOperator"
          :condition="condition"
          :commands="operators"/>
          <module-trash-button :condition="condition" :exe="enableTrash"/>
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
import {buildTree} from "../../api/definition/model/NodeTree";
import {buildNodePath} from "@/business/components/api/definition/model/NodeTree";
import {getCurrentProjectID} from "@/common/js/utils";
import ModuleTrashButton from "@/business/components/api/definition/components/module/ModuleTrashButton";

export default {
  name: "TestCaseNodeTree",
  components: {MsSearchBar, TestCaseImport, TestCaseCreate, MsNodeTree, NodeEdit,ModuleTrashButton},
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
          callback: this.addTestCase,
          permissions: ['PROJECT_TRACK_CASE:READ+CREATE']
        },
        {
          label: this.$t('api_test.api_import.label'),
          callback: this.handleImport,
          permissions: ['PROJECT_TRACK_CASE:READ+IMPORT']
        },
        {
          label: this.$t('api_test.export_config'),
          callback: () => {
            this.$emit('exportTestCase');
          },
          permissions: ['PROJECT_TRACK_CASE:READ+EXPORT']
        }
      ]
    };
  },
  props: {
    type: {
      type: String,
      default: "view"
    },
    showOperator: Boolean,
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
      return getCurrentProjectID();
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
    enableTrash(){
      this.condition.trashEnable = true;
      this.$emit('enableTrash', this.condition.trashEnable);
    },
    list() {
      if (this.projectId) {
        this.result = this.$get("/case/node/list/" + this.projectId, response => {
          this.treeNodes = response.data;
          this.treeNodes.forEach(node => {
            buildTree(node, {path: ''});
          });
          this.setModuleOptions();
          if (this.$refs.nodeTree) {
            this.$refs.nodeTree.filter();
          }
        });
      }
    },
    increase(id) {
      this.$refs.nodeTree.increase(id);
    },
    decrease(id) {
      this.$refs.nodeTree.decrease(id);
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
    setModuleOptions() {
      let moduleOptions = [];
      this.treeNodes.forEach(node => {
        buildNodePath(node, {path: ''}, moduleOptions);
      });
      this.$store.commit('setTestCaseModuleOptions', moduleOptions);
    },
    nodeChange(node, nodeIds, pNodes) {

      this.$store.commit('setTestCaseSelectNode', node);
      this.$store.commit('setTestCaseSelectNodeIds', nodeIds);

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
