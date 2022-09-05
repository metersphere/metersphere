<template>
  <el-dialog :title="this.$t('test_track.case.select_catalog')"
             :visible.sync="dialogVisible"
             :before-close="close"
             :destroy-on-close="true" width="600px"
             v-loading="loading" append-to-body class="batch-move">
    <div>
      <div v-xpack style="margin-bottom: 5px">
        <i class="el-icon-info"/>
        <span>{{ $t('project.project_file.validation.can_not_move_repository_file') }}</span>
      </div>

      <el-input :placeholder="$t('test_track.module.search')" v-model="filterText" size="small"/>
      <el-tree
        class="filter-tree node-tree"
        :data="treeNodes"
        node-key="id"
        :filter-node-method="filterNode"
        :expand-on-click-node="false"
        highlight-current
        style="overflow: auto"
        @node-click="nodeClick"
        ref="tree">
        <template v-slot:default="{node}">
          <span>
            <span class="node-icon">
              <i class="el-icon-folder"/>
            </span>
            <span class="node-title" v-if="node.label === 'DEF_MODULE'">
              {{ $t('commons.module_title') }}
            </span>
            <span class="node-title" v-else>{{ node.label }}</span>
          </span>
        </template>
      </el-tree>
    </div>
    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="close"
        @confirm="save"/>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "../../../../common/components/MsDialogFooter";
import {buildTree} from "@/business/components/api/definition/model/NodeTree";
import {getCurrentProjectID} from "../../../../../../common/js/utils";

export default {
  name: "MsFileBatchMove",
  components: {
    MsDialogFooter
  },
  data() {
    return {
      treeNodes: [],
      fileIds: [],
      dialogVisible: false,
      currentKey: "",
      filterText: "",
      loading: false,
    }
  },
  props: {
    publicEnable: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    }
  },
  methods: {
    open(treeNodes, fileIds) {
      this.dialogVisible = true;
      this.fileIds = fileIds;
      this.treeNodes = treeNodes;
    },
    init() {
      this.dialogVisible = true;
      this.loading = true;
      let url = "/file/module/list/" + getCurrentProjectID();
      this.result = this.$get(url, response => {
        if (response.data != undefined && response.data != null) {
          this.treeNodes = response.data;
          this.treeNodes.forEach(node => {
            node.name = node.name === 'DEF_MODULE' ? this.$t('commons.module_title') : node.name
            buildTree(node, {path: ''});
          });
          this.loading = false;
        }
      });
    },
    save() {
      if (!this.currentKey || this.currentKey === '') {
        this.$warning(this.$t('test_track.case.input_module'));
        return;
      }
      if (this.fileIds && this.fileIds.length > 0) {
        let request = {moduleId: this.currentKey, metadataIds: this.fileIds};
        this.$post('/file/metadata/move', request, () => {
          this.dialogVisible = false;
          this.refresh();
        });
      } else {
        this.$emit("setModuleId", this.currentKey);
        this.dialogVisible = false;
      }
    },
    refresh() {
      this.$emit('refreshModule');
    },
    close() {
      this.filterText = "";
      this.dialogVisible = false;
    },
    filterNode(value, data) {
      if (!value) {
        return true;
      }
      return data.label.indexOf(value) !== -1;
    },
    nodeClick() {
      this.currentKey = this.$refs.tree.getCurrentKey();
    }
  }
}
</script>

<style scoped>
.node-title {
  width: 0;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1 1 auto;
  padding: 0 5px;
  overflow: hidden;
}

.batch-move {
  min-height: 500px;
}

</style>
