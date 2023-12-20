<template>
  <el-dialog
    :title="this.$t('test_track.case.dump_module')"
    :visible.sync="dialogVisible"
    :before-close="close"
    :destroy-on-close="true"
    width="600px"
    v-loading="loading"
    append-to-body
    class="batch-move">
    <div>
      <el-input :placeholder="$t('test_track.module.search')" v-model="filterText" size="small" />
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
        <template v-slot:default="{ node }">
          <span>
            <span class="node-icon">
              <i class="el-icon-folder" />
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
      <ms-dialog-footer @cancel="close" @confirm="save" />
    </template>
  </el-dialog>
</template>

<script>
import {getFileModules} from 'metersphere-frontend/src/api/file-metadata';
import MsDialogFooter from 'metersphere-frontend/src/components/MsDialogFooter';
import {buildTree} from 'metersphere-frontend/src/model/NodeTree';
import {getCurrentProjectID} from 'metersphere-frontend/src/utils/token';

export default {
  name: 'MsFileBatchMove',
  components: {
    MsDialogFooter,
  },
  data() {
    return {
      treeNodes: [],
      fileIds: [],
      dialogVisible: false,
      currentKey: '',
      filterText: '',
      loading: false,
    };
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
    },
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
      this.result = getFileModules(getCurrentProjectID()).then((response) => {
        if (response.data != undefined && response.data != null) {
          this.treeNodes = response.data.filter(node => node['moduleType'] !== 'repository');
          this.treeNodes.forEach((node) => {
            node.name = node.name === 'DEF_MODULE' ? this.$t('commons.module_title') : node.name;
            buildTree(node, { path: '' });
          });
          this.loading = false;
        }
      });
    },
    save() {
      this.$emit('setModuleId', this.currentKey);
      this.dialogVisible = false;
    },
    refresh() {
      this.$emit('refreshModule');
    },
    close() {
      this.filterText = '';
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
    },
  },
};
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
