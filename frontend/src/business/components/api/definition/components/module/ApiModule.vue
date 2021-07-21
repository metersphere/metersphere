<template>
  <div v-loading="result.loading">

    <slot name="header"></slot>

    <ms-node-tree
      :is-display="getIsRelevance"
      v-loading="result.loading"
      :tree-nodes="data"
      :type="isReadOnly ? 'view' : 'edit'"
      :allLabel="$t('commons.all_module_title')"
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @refresh="list"
      :delete-permission="['PROJECT_API_DEFINITION:READ+DELETE_API']"
      :add-permission="['PROJECT_API_DEFINITION:READ+CREATE_API']"
      :update-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']"
      @nodeSelectEvent="nodeChange"
      ref="nodeTree">

      <template v-slot:header>
        <api-module-header
          :show-operator="showOperator"
          :condition="condition"
          :current-module="currentModule"
          :is-read-only="isReadOnly"
          :moduleOptions="data"
          @exportAPI="exportAPI"
          @saveAsEdit="saveAsEdit"
          @refreshTable="$emit('refreshTable')"
          @schedule="$emit('schedule')"
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
  import MsNodeTree from "../../../../track/common/NodeTree";
  import ApiModuleHeader from "./ApiModuleHeader";
  import {buildTree} from "../../model/NodeTree";
  import {getCurrentProjectID} from "@/common/js/utils";

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
        openType: 'relevance',
        result: {},
        condition: {
          protocol: OPTIONS[0].value,
          filterText: "",
          trashEnable: false
        },
        data: [],
        currentModule: {},
      }
    },
    props: {
      isReadOnly: {
        type: Boolean,
        default() {
          return false
        }
      },
      showOperator: Boolean,
      planId: String,
      relevanceProjectId: String,
      reviewId: String,
      pageSource:String,
    },
    computed: {
      isPlanModel() {
        return this.planId ? true : false;
      },
      isRelevanceModel() {
        return this.relevanceProjectId ? true : false;
      },
      isReviewModel() {
        return this.reviewId ? true : false;
      },
      projectId() {
        return getCurrentProjectID();
      },
      getIsRelevance(){
        if(this.pageSource !== 'definition'){
          return this.openType;
        }else {
          return "definition";
        }
      }
    },
    mounted() {
      this.initProtocol();
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
      planId() {
        this.list();
      },
      relevanceProjectId() {
        this.list();
      },
      reviewId() {
        this.list();
      }
    },
    methods: {
      initProtocol() {
        this.$get('/api/module/getUserDefaultApiType/', response => {
          this.condition.protocol = response.data;
          this.$emit('protocolChange', this.condition.protocol);
          this.list();
        });
      },
      list(projectId) {
        let url = undefined;
        if (this.isPlanModel) {
          url = '/api/module/list/plan/' + this.planId + '/' + this.condition.protocol;
        } else if (this.isRelevanceModel) {
          url = "/api/module/list/" + this.relevanceProjectId + "/" + this.condition.protocol;
        } else {
          url = "/api/module/list/" + (projectId ? projectId : this.projectId) + "/" + this.condition.protocol;
          if (!this.projectId) {
            return;
          }
        }
        this.result = this.$get(url, response => {
          if (response.data != undefined && response.data != null) {
            this.data = response.data;
            this.data.forEach(node => {
              buildTree(node, {path: ''});
            });
            this.$emit('setModuleOptions', this.data);
            this.$emit('setNodeTree', this.data);
            if (this.$refs.nodeTree) {
              this.$refs.nodeTree.filter(this.condition.filterText);
            }
          }
        });
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
          this.$post("/api/module/pos", list, () => {
            this.list();
          });
        }, (error) => {
          this.list();
        });
      },
      nodeChange(node, nodeIds, pNodes) {
        this.currentModule = node.data;
        this.condition.trashEnable = false;
        if (node.data.id === 'root') {
          this.$emit("nodeSelectEvent", node, [], pNodes);
        } else {
          this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
        }
      },
      //创建根目录的模块---供父类使用
      createRootModel() {
        let dataArr = this.$refs.nodeTree.extendTreeNodes;
        if (dataArr.length > 0) {
          this.$refs.nodeTree.append({}, dataArr[0]);
        }
      },
      exportAPI(type) {
        this.$emit('exportAPI', type, this.data);
      },
      debug() {
        this.$emit('debug');
      },
      saveAsEdit(data) {
        this.$emit('saveAsEdit', data);
      },
      refresh() {
        this.list();
        this.$emit('refreshTable');
      },
    }
  }
</script>

<style scoped>

</style>
