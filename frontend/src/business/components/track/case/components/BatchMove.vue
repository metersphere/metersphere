<template>
  <div v-loading="result.loading">
    <el-dialog :title="this.$t('test_track.case.select_catalog')"
               :visible.sync="dialogVisible"
               :before-close="close"
               :destroy-on-close="true"
               width="20%"
    >
      <div>
        <el-input :placeholder="$t('test_track.module.search')" v-model="filterText" size="small"/>
        <el-tree
          class="filter-tree node-tree"
          :data="treeNodes"
          node-key="id"
          :filter-node-method="filterNode"
          :expand-on-click-node="false"
          highlight-current
          @node-click="nodeClick"
          ref="tree"
        >
          <template v-slot:default="{node}">
          <span>
            <span class="node-icon">
              <i class="el-icon-folder"/>
            </span>
            <span class="node-title">{{node.label}}</span>
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
  </div>
</template>

<script>
  import MsDialogFooter from "../../../common/components/MsDialogFooter";
  import {TrackEvent,LIST_CHANGE} from "@/business/components/common/head/ListEvent";

  export default {
    name: "BatchMove",
    components: {
      MsDialogFooter
    },
    data() {
      return {
        treeNodes: [],
        selectIds: [],
        selectNode: {},
        dialogVisible: false,
        currentKey: "",
        moduleOptions: [],
        filterText: "",
        result: {},
      }
    },
    watch: {
      filterText(val) {
        this.$refs.tree.filter(val);
      }
    },
    methods: {
      open(treeNodes, selectIds, moduleOptions) {
        this.dialogVisible = true;
        this.treeNodes = treeNodes;
        this.selectIds = selectIds;
        this.moduleOptions = moduleOptions;
      },
      save() {
        if (!this.currentKey) {
          this.$warning(this.$t('test_track.case.input_module'));
          return;
        }
        let param = {};
        param.nodeId = this.currentKey;
        this.moduleOptions.forEach(item => {
          if (item.id === this.currentKey) {
            param.nodePath = item.path;
          }
        });
        param.ids = this.selectIds;
        this.result = this.$post('/test/case/batch/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.close();
          // 发送广播，刷新 head 上的最新列表
          TrackEvent.$emit(LIST_CHANGE);
          this.$emit('refresh');
        });
      },
      refresh() {
        this.$emit("refresh");
      },
      close() {
        this.filterText = "";
        this.dialogVisible = false;
        this.selectNode = {};
      },
      filterNode(value, data) {
        if (!value) return true;
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

</style>
