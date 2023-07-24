<template>
  <el-card class="scenario-results">
    <div v-if="errorReport > 0">
      <el-tooltip :content="$t('api_test.automation.open_expansion')" placement="top" effect="light">
        <i class="el-icon-circle-plus-outline ms-open-btn ms-open-btn-left" v-prevent-re-click @click="openExpansion" />
      </el-tooltip>
      <el-tooltip :content="$t('api_test.automation.close_expansion')" placement="top" effect="light">
        <i class="el-icon-remove-outline ms-open-btn" size="mini" @click="closeExpansion" />
      </el-tooltip>
    </div>
    <div style="height: calc(100vh - 400px)">
      <vue-easy-tree
        :data="treeData"
        node-key="resourceId"
        :sizeDependencies="['expanded']"
        height="calc(100vh - 400px)"
        :minItemSize="47"
        :buffer="500"
        :expand-on-click-node="false"
        :default-expand-all="defaultExpand"
        :filter-node-method="filterNode"
        highlight-current
        class="ms-tree ms-report-tree"
        isDynamic
        ref="resultsTree">
        <template slot-scope="{ node, data }">
          <ms-scenario-result
            :key="data.resourceId"
            :node="data"
            :expanded.sync="node.expanded"
            :console="console"
            v-on:requestResult="requestResult"
            :is-share="isShare"
            :share-id="shareId" />
        </template>
      </vue-easy-tree>
    </div>
  </el-card>
</template>
<script>
import MsScenarioResult from './ScenarioResult';

export default {
  name: 'MsInfiniteScrollScenarioResults',
  components: { MsScenarioResult },
  props: {
    scenarios: Array,
    treeData: Array,
    console: String,
    errorReport: Number,
    report: Object,
    defaultExpand: {
      default: false,
      type: Boolean,
    },
    isShare: Boolean,
    shareId: String,
  },
  data() {
    return {};
  },
  created() {
    if (this.$refs.resultsTree && this.$refs.resultsTree.root) {
      this.$refs.resultsTree.root.expanded = true;
    }
  },
  computed: {
    isUi() {
      return this.report && this.report.reportType && this.report.reportType.startsWith('UI');
    },
  },
  methods: {
    filterNode(value, data) {
      if (!data.value && (!data.children || data.children.length === 0)) {
        return false;
      }
      if (!value) return true;
      if (data.value) {
        if (value === 'FAKE_ERROR') {
          if (data.errorCode && data.errorCode !== '' && data.value.status === 'FAKE_ERROR') {
            return true;
          }
        } else if (value === 'PENDING') {
          if (data.value.status === 'PENDING') {
            return true;
          }
        } else {
          return data.totalStatus !== 'FAKE_ERROR' && data.value.error > 0;
        }
      }
      return false;
    },
    filter(val) {
      this.$nextTick(() => {
        this.$refs.resultsTree.filter(val);
      });
    },
    requestResult(requestResult) {
      this.$emit('requestResult', requestResult);
    },
    nodeClick(data, node) {
      console.log('nodeClick', e);
      node.expanded = !node.expanded;
    },
    // 改变节点的状态
    changeTreeNodeStatus(node, expandCount) {
      node.expanded = this.expandAll;
      for (let i = 0; i < node.childNodes.length; i++) {
        // 改变节点的自身expanded状态
        node.childNodes[i].expanded = this.expandAll;
        // 遍历子节点
        if (node.childNodes[i].childNodes.length > 0) {
          this.changeTreeNodeStatus(node.childNodes[i]);
        }
      }
    },
    closeExpansion() {
      this.expandAll = false;
      this.changeTreeNodeStatus(this.$refs.resultsTree.store.root, 0);
    },
    openExpansion() {
      this.expandAll = true;
      // 改变每个节点的状态
      this.changeTreeNodeStatus(this.$refs.resultsTree.store.root, 0);
    },
  },
};
</script>

<style scoped>
.scenario-results {
  height: 100%;
}

.ms-report-tree :deep(.el-tree-node__content) {
  height: 100%;
  vertical-align: center;
}

:deep(.el-drawer__body) {
  overflow: auto;
}

:deep(.el-step__icon.is-text) {
  border: 1px solid;
}

:deep(.el-drawer__header) {
  margin-bottom: 0px;
}

:deep(.el-link) {
  font-weight: normal;
}

:deep(.el-checkbox) {
  color: #303133;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', Arial, sans-serif;
  font-size: 13px;
  font-weight: normal;
}

:deep(.el-checkbox__label) {
  padding-left: 5px;
}

.ms-sc-variable-header :deep(.el-dialog__body) {
  padding: 0px 20px;
}

.ms-open-btn {
  margin: 5px 5px 0px;
  color: #783887;
  font-size: 20px;
}

.ms-open-btn:hover {
  background-color: #f2f9ee;
  cursor: pointer;
  color: #67c23a;
}

.ms-open-btn-left {
  margin-left: 30px;
}
</style>
