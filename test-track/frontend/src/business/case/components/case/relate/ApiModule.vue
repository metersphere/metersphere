<template>
    <div v-loading="loading">
  
      <slot name="header"></slot>
  
      <ms-node-tree
        v-if="refreshDataOver"
        v-loading="loading"
        :tree-nodes="data"
        :type="isReadOnly ? 'view' : 'edit'"
        :allLabel="$t('api_test.definition.api_all')"
        :default-label="$t('api_test.definition.unplanned_api')"
        :hide-opretor="isTrashData"
        local-suffix="api_definition"
        @add="add"
        @edit="edit"
        @drag="drag"
        @remove="remove"
        @refresh="list"
        @filter="filter"
        :show-case-num="showCaseNum"
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
            :options="options"
            :total="total"
            :is-trash-data="isTrashData"
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
  import {OPTIONS} from "metersphere-frontend/src/model/JsonData";
  import MsNodeTree from "metersphere-frontend/src/components/new-ui/MsNodeTree";
  import ApiModuleHeader from "metersphere-frontend/src/components/environment/snippet/ext/module/ApiModuleHeader";
  import {buildTree} from "metersphere-frontend/src//model/NodeTree";
  import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
  import {getModuleByUrl, getUserDefaultApiType} from "metersphere-frontend/src/api/environment";
  
  export default {
    name: 'MsApiModule',
    components: {
      ApiModuleHeader,
      MsNodeTree,
    },
    data() {
      return {
        result: {},
        loading: false,
        refreshDataOver: true,
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
          return false;
        }
      },
      defaultProtocol: String,
      showCaseNum: {
        type: Boolean,
        default() {
          return true;
        }
      },
      isTrashData: {
        type: Boolean,
        default() {
          return false;
        }
      },
      showOperator: Boolean,
      planId: String,
      currentVersion: String,
      relevanceProjectId: String,
      reviewId: String,
      pageSource: String,
      selectDefaultProtocol: Boolean,
      total: Number,
      isRelevance: Boolean,
      options: {
        type: Array,
        default() {
          return OPTIONS;
        }
      }
    },
    computed: {
      isPlanModel() {
        return !!this.planId;
      },
      isRelevanceModel() {
        return !!this.relevanceProjectId;
      },
      isReviewModel() {
        return !!this.reviewId;
      },
      projectId() {
        return getCurrentProjectID();
      }
    },
    mounted() {
      this.initProtocol();
    },
  
    watch: {
      'condition.filterText'() {
        this.filter();
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
      },
      isTrashData() {
        this.condition.trashEnable = this.isTrashData;
        this.list();
      },
      defaultProtocol() {
        if (this.condition.protocol !== this.defaultProtocol) {
          this.condition.protocol = this.defaultProtocol;
        }
      }
    },
    methods: {
      initProtocol() {
        //不是跳转来的页面不查询上次请求类型
        let isRedirectPage = this.isRedirect();
        if (this.$route.params.type) {
          this.condition.protocol = this.$route.params.type;
          this.$emit('protocolChange', this.condition.protocol);
          this.list();
        } else if (!this.isRelevance && !isRedirectPage && this.selectDefaultProtocol) {
          //展示页面是非引用页面才会查询上一次接口类型
          getUserDefaultApiType().then(response => {
            this.condition.protocol = response.data;
            this.$emit('protocolChange', this.condition.protocol);
            this.list();
          })
        } else {
          this.$emit('protocolChange', this.condition.protocol);
          this.list();
        }
      },
      isRedirect() {
        let isRedirectPage = false;
        let routeParamObj = this.$route.params.paramObj;
        if (routeParamObj) {
          if (routeParamObj.dataSelectRange) {
            let item = JSON.parse(JSON.stringify(routeParamObj.dataSelectRange)).param;
            if (item !== undefined) {
              let type = item.taskGroup.toString();
              if (type === "SWAGGER_IMPORT") {
                isRedirectPage = true;
              }
            }
          }
        } else {
          if (this.$route.params.dataSelectRange) {
            let item = JSON.parse(JSON.stringify(this.$route.params.dataSelectRange)).param;
            if (item !== undefined) {
              let type = item.taskGroup.toString();
              if (type === "SWAGGER_IMPORT") {
                isRedirectPage = true;
              }
            }
          }
        }
        return isRedirectPage;
      },
      filter() {
        this.$refs.nodeTree.filter(this.condition.filterText);
      },
      list(projectId) {
        let url = undefined;
        if (this.isPlanModel) {
          url = '/api/module/list/plan/' + this.planId + '/' + this.condition.protocol;
        } else if (this.isRelevanceModel) {
          url = "/api/module/list/" + this.relevanceProjectId + "/" + this.condition.protocol +
            (this.currentVersion ? '/' + this.currentVersion : '');
        } else if (this.isTrashData) {
          url = "/api/module/trash/list/" + (projectId ? projectId : this.projectId) + "/" + this.condition.protocol +
            (this.currentVersion ? '/' + this.currentVersion : '');
        } else {
          url = "/api/module/list/" + (projectId ? projectId : this.projectId) + "/" + this.condition.protocol +
            (this.currentVersion ? '/' + this.currentVersion : '');
          if (!this.projectId) {
            return;
          }
        }
        this.loading = getModuleByUrl(url).then(response => {
          if (response.data) {
            this.data = response.data;
            this.data.forEach(node => {
              node.name = node.name === '未规划接口' ? this.$t('api_test.definition.unplanned_api') : node.name
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
  
      },
      add(param) {
  
      },
      remove(nodeIds) {
  
      },
      drag(param, list) {
  
      },
      nodeChange(node, nodeIds, pNodes) {
        this.currentModule = node.data;
        if (node.data.id === 'root') {
          this.$emit("nodeSelectEvent", node, [], pNodes);
        } else {
          this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
        }
        this.nohupReloadTree(node.data.id);
      },
      nohupReloadTree(selectNodeId) {
        let url = undefined;
        if (this.isPlanModel) {
          url = '/api/module/list/plan/' + this.planId + '/' + this.condition.protocol;
        } else if (this.isRelevanceModel) {
          url = "/api/module/list/" + this.relevanceProjectId + "/" + this.condition.protocol +
            (this.currentVersion ? '/' + this.currentVersion : '');
        } else if (this.isTrashData) {
          if (!this.projectId) {
            return;
          }
          url = "/api/module/trash/list/" + this.projectId + "/" + this.condition.protocol +
            (this.currentVersion ? '/' + this.currentVersion : '');
        } else {
          if (!this.projectId) {
            return;
          }
          url = "/api/module/list/" + this.projectId + "/" + this.condition.protocol +
            (this.currentVersion ? '/' + this.currentVersion : '');
        }
        getModuleByUrl(url).then(response => {
          if (response.data) {
            let treeData = response.data;
            treeData.forEach(node => {
              node.name = node.name === '未规划接口' ? this.$t('api_test.definition.unplanned_api') : node.name
              buildTree(node, {path: ''});
            });
            this.data = treeData;
  
            this.$nextTick(() => {
              if (this.$refs.nodeTree) {
                this.$refs.nodeTree.filter(this.condition.filterText);
                if (selectNodeId) {
                  this.$refs.nodeTree.justSetCurrentKey(selectNodeId);
                }
              }
            })
          }
        })
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
      removeModuleId(nodeIds) {
        if (localStorage.getItem('tcp') || localStorage.getItem('http') || localStorage.getItem('sql') || localStorage.getItem('dubbo')) {
          if (this.condition.protocol === 'TCP') {
            if (localStorage.getItem('tcp') === nodeIds[0]) {
              localStorage.setItem('tcp', undefined);
            }
          } else if (this.condition.protocol === 'HTTP') {
            if (localStorage.getItem('http') === nodeIds[0]) {
              localStorage.setItem('http', undefined);
            }
          } else if (this.condition.protocol === 'SQL') {
            if (localStorage.getItem('sql') === nodeIds[0]) {
              localStorage.setItem('sql', undefined);
            }
          } else if (this.condition.protocol === 'DUBBO') {
            if (localStorage.getItem('dubbo') === nodeIds[0]) {
              localStorage.setItem('dubbo', undefined);
            }
          }
        }
      }
    }
  }
  </script>
  
  <style scoped>
  
  </style>
  