<template>
  <div v-loading="result.loading">

    <ms-node-tree
      v-loading="result.loading"
      :tree-nodes="data"
      :type="'edit'"
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @nodeSelectEvent="nodeChange"
      ref="nodeTree">

      <template v-slot:header>
       <api-module-header
         :condition="condition"
         :current-module="currentModule"
         @exportAPI="exportAPI"
         @saveAsEdit="saveAsEdit"
         @refresh="refresh"
         @debug="debug"/>
      </template>

    </ms-node-tree>

  </div>

</template>

<script>
  import MsAddBasisApi from "../basis/AddBasisApi";
  import SelectMenu from "../../../../track/common/SelectMenu";
  import {OPTIONS} from "../../model/JsonData";
  import ApiImport from "../import/ApiImport";
  import {getCurrentProjectID} from "@/common/js/utils";
  import MsNodeTree from "../../../../track/common/NodeTree";
  import ApiModuleHeader from "./ApiModuleHeader";
  import {buildNodePath} from "../../model/NodeTree";

  export default {
    name: 'MsApiModule',
    components: {
      ApiModuleHeader,
      MsNodeTree,
      MsAddBasisApi,
      SelectMenu,
      ApiImport
    },
    data() {
      return {
        result: {},
        condition: {
          protocol: OPTIONS[0].value,
          filterText: "",
          trashEnable: false
        },

        httpVisible: false,
        expandedNode: [],
        nextFlag: true,
        projectId: "",
        data: [],
        currentModule: {},
        newLabel: ""
      }
    },
    mounted() {
      this.projectId = getCurrentProjectID();
      this.$emit('protocolChange', this.condition.protocol);
      this.list();
    },
    watch: {
      'condition.filterText'(val) {
        this.$refs.nodeTree.filter(val);
      },
      'condition.protocol'() {
        this.$emit('protocolChange', this.condition.protocol);
        this.list();
      },
      'condition.trashEnable'() {
        this.$emit('enableTrash', this.condition.trashEnable);
      },
    },
    methods: {
      list() {
        if (this.projectId) {
          this.result = this.$get("/api/module/list/" + this.projectId + "/" + this.condition.protocol, response => {
            if (response.data != undefined && response.data != null) {
              this.data = response.data;
              let moduleOptions = [];
              this.data.forEach(node => {
                buildNodePath(node, {path: ''}, moduleOptions);
              });
              this.$emit('setModuleOptions', moduleOptions);
            }
          });
        }
      },
      edit(param) {
        param.projectId = this.projectId;
        param.protocol = this.condition.protocol;
        this.$post("/api/module/edit", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.list();
          this.refresh();
        }, (error) => {
          this.list();
        });
      },
      add(param) {
        param.projectId = this.projectId;
        param.protocol = this.condition.protocol;
        this.$post("/api/module/add", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.list();
        }, (error) => {
          this.list();
        });
      },
      remove(nodeIds) {
        this.$post("/api/module/delete", nodeIds, () => {
          this.list();
          this.refresh();
        }, (error) => {
          this.list();
        });
      },
      drag(param, list) {
        this.$post("/api/module/drag", param, () => {
          // this.$post("/api/module/pos", list); //todo 排序
          this.list();
        }, (error) => {
          this.list();
        });
      },
      nodeChange(node, nodeIds, pNodes) {
        this.currentModule = node;
        this.condition.trashEnable = false;
        if (node.data.id === 'root') {
          this.$emit("nodeSelectEvent", node, [], pNodes);
        } else {
          this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
        }
      },
      exportAPI() {
        this.$emit('exportAPI');
      },
      debug() {
        this.$emit('debug');
      },
      saveAsEdit(data) {
        this.$emit('saveAsEdit', data);
      },
      refresh() {
        this.$emit("refreshTable");
      },
    }
  }
</script>

<style scoped>

</style>
