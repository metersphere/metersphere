<template>
  <div>
    <slot name="header"></slot>
    <ms-node-tree
      v-loading="result.loading"
      :tree-nodes="data"
      :allLabel="$t('project.all_file')"
      :type="isReadOnly ? 'view' : 'edit'"
      :delete-permission="['PROJECT_API_SCENARIO:READ+DELETE']"
      :add-permission="['PROJECT_API_SCENARIO:READ+CREATE']"
      :update-permission="['PROJECT_API_SCENARIO:READ+EDIT']"
      :default-label="$t('commons.module_title')"
      :show-case-num="showCaseNum"
      :operation_type_add="treeOperationType"
      :operation_type_edit="treeOperationType"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @refresh="list"
      @filter="filter"
      @nodeSelectEvent="nodeChange"
      @addOperation="fileTreeModuleAdd"
      @editOperation="fileTreeModuleEdit"
      ref="nodeTree">

      <template v-slot:header>
        <ms-search-bar
          :show-operator="showOperator"
          :condition="condition"/>
        <ms-my-file :condition="condition" :exe="myFile" :total='total' v-if="loading"/>
      </template>
    </ms-node-tree>
    <file-module-dialog @refresh="list" ref="fileModuleDialog"/>
  </div>
</template>

<script>
import MsNodeTree from "../../../../track/common/NodeTree";
import {buildTree} from "@/business/components/api/definition/model/NodeTree";
import MsMyFile from "./MyFile";
import MsSearchBar from "@/business/components/common/components/search/MsSearchBar";
import {getCurrentProjectID, getCurrentUserId, hasLicense} from "@/common/js/utils";
import FileModuleDialog from "@/business/components/project/menu/file/dialog/FileModuleDialog";

export default {
  name: 'MsFileModule',
  components: {
    MsSearchBar,
    MsMyFile,
    MsNodeTree,
    FileModuleDialog,
  },
  props: {
    isReadOnly: {
      type: Boolean,
      default() {
        return false;
      }
    },
    showOperator: Boolean,
    relevanceProjectId: String,
    planId: String,
    pageSource: String,
    showCaseNum: {
      type: Boolean,
      default() {
        return true;
      }
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    treeOperationType() {
      let returnStr = hasLicense() ? 'external' : 'simple'
      return returnStr;
    }
  },
  data() {
    return {
      result: {},
      total: 0,
      loading: true,
      condition: {
        filterText: "",
        trashEnable: false
      },
      data: [],
      currentModule: undefined,
    }
  },
  mounted() {
    this.myFiles();
    this.list();
  },
  watch: {
    'condition.filterText'() {
      this.filter();
    },
    relevanceProjectId() {
      this.myFiles();
      this.list();
    }
  },
  methods: {
    fileTreeModuleAdd(param) {
      this.$refs.fileModuleDialog.open('create', param);
    },
    fileTreeModuleEdit(data) {
      this.$refs.fileModuleDialog.open('edit', data);
    },
    reload() {
      this.loading = false
      this.$nextTick(() => {
        this.loading = true
      });
    },
    filter() {
      this.$refs.nodeTree.filter(this.condition.filterText);
    },
    myFiles() {
      let url = "/file/metadata/count/" + getCurrentProjectID() + "/" + getCurrentUserId();
      this.result = this.$get(url, response => {
        this.total = response.data;
      });
    },
    list(projectId) {
      let url = "/file/module/list/" + (projectId ? projectId : this.projectId);
      this.result = this.$get(url, response => {
        if (response.data != undefined && response.data != null) {
          this.data = response.data;
          this.data.forEach(node => {
            node.name = node.name === 'DEF_MODULE' ? this.$t('commons.module_title') : node.name
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
      this.$post("/file/module/edit", param, () => {
        this.$success(this.$t('commons.save_success'));
        this.list();
        this.refresh();
      }, (error) => {
        this.list();
      });
    },
    remove(nodeIds) {
      this.$post("/file/module/delete", nodeIds, () => {
        this.list();
        this.refresh();
      }, (error) => {
        this.list();
      });
    },
    drag(param, list) {
      this.$post("/file/module/drag", param, () => {
        this.$post("/file/module/pos", list, () => {
          this.list();
        });
      }, (error) => {
        this.list();
      });
    },
    nodeChange(node, nodeIds, pNodes) {
      this.currentModule = node.data;
      if (!this.condition.filters) {
        this.condition.filters = {createUser: []};
      } else {
        this.condition.filters.createUser = [];
      }
      this.reload();
      if (node.data.id === 'root') {
        this.$emit("nodeSelectEvent", node, [], pNodes);
      } else {
        this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
      }
    },
    refresh() {
      this.myFiles();
      this.list(getCurrentProjectID());
    },
    myFile() {
      this.$nextTick(() => {
        let nodes = document.getElementsByClassName("el-tree-node");
        for (let i = 0, len = nodes.length; i < len; i++) {
          nodes[i].classList.remove("is-current");
        }
      });
      if (!this.condition.filters) {
        this.condition.filters = {createUser: [getCurrentUserId()]};
      } else {
        this.condition.filters.createUser = [getCurrentUserId()];
      }
      this.condition.filters.moduleIds = [];
      this.$emit("myFile");
      this.reload();
    }
  }
}
</script>

<style scoped>
.custom-tree-node {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  width: 100%;
}

.node-operate > i {
  color: #409eff;
  margin: 0 5px;
}

/deep/ .el-tree-node__content {
  height: 33px;
}
</style>
