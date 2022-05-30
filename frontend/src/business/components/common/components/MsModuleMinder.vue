<template>
  <div>
    <div v-loading="loading" class="minder" :class="{'full-screen': isFullScreen}">
      <ms-full-screen-button :is-full-screen.sync="isFullScreen"/>
      <minder-editor
        v-if="isActive"
        class="minder-container"
        :import-json="importJson"
        :progress-enable="false"
        :tags="tags"
        :height="height"
        :move-enable="moveEnable"
        :tag-edit-check="tagEditCheck"
        :priority-disable-check="priorityDisableCheck"
        :distinct-tags="distinctTags"
        :default-mold="defaultMode"
        @afterMount="$emit('afterMount')"
        @moldChange="handleMoldChange"
        :disabled="disabled"
        @save="save"
      />
      <is-change-confirm
        :title="'请保存脑图'"
        :tip="'脑图未保存，确认保存脑图吗？'"
        @confirm="changeConfirm"
        ref="isChangeConfirm"/>
    </div>
  </div>

</template>

<script>

import MsFullScreenButton from "@/business/components/common/components/MsFullScreenButton";
import IsChangeConfirm from "@/business/components/common/components/IsChangeConfirm";
import {minderPageInfoMap} from "@/network/testCase";
export default {
  name: "MsModuleMinder",
  components: {IsChangeConfirm, MsFullScreenButton},
  props: {
    minderKey: String,
    treeNodes: {
      type: Array,
      default() {
        return []
      }
    },
    tags: {
      type: Array,
      default() {
        return []
      }
    },
    tagEnable: {
      type: Boolean,
      default() {
        return false;
      }
    },
    distinctTags: {
      type: Array,
      default() {
        return []
      }
    },
    selectNode: {
      type: Object,
    },
    tagDisableCheck: Function,
    tagEditCheck: Function,
    priorityDisableCheck: Function,
    disabled: Boolean,
    ignoreNum: Boolean,
    showModuleTag: Boolean,
    moduleDisable: {
      type: Boolean,
      default() {
        return true;
      }
    },
    moveEnable: {
      type: Boolean,
      default: true
    },
    getExtraNodeCount: {
      type: Function
    }
  },
  data() {
    return {
      importJson: {
        root: {
          data: {
            text: this.$t('test_track.review_view.all_case'),
            disable: true,
            id: "root",
            type: 'node',
            level: 0,
            resource: this.showModuleTag ? [this.$t('test_track.module.module')] : [],
            path: "",
            tagEnable: this.tagEnable
          },
          children: []
        },
        template: 'default'
      },
      isActive: true,
      isFullScreen: false,
      height: '',
      defaultMode: 3,
      loading: false,
      tmpNode: {}
    }
  },
  created() {
    this.height = document.body.clientHeight - 285;
  },
  destroyed() {
    minderPageInfoMap.clear();
  },
  mounted() {
    this.defaultMode = 3;
    if (this.minderKey) {
      let model = localStorage.getItem(this.minderKey + 'minderModel');
      if (model) {
        this.defaultMode = Number.parseInt(model);
      }
    }
    this.initData();
  },
  methods: {
    initData() {
      this.$nextTick(() => {
        if (this.selectNode && this.selectNode.data) {
          this.handleNodeSelect(this.selectNode);
        } else {
          this.parse(this.importJson.root, this.treeNodes);
        }
      });
    },
    getNoCaseModuleIds(ids, nodes) {
      if (nodes) {
        nodes.forEach(node => {
          if (node.caseNum < 1) {
            ids.push(node.id);
          }
          if (node.children) {
            this.getNoCaseModuleIds(ids, node.children);
          }
        });
      }
    },
    setExtraNodeCount(countMap, nodes) {
      nodes.forEach(node => {
        if (countMap[node.id]) {
          node.extraNodeCount = countMap[node.id];
        }
        if (node.children) {
          this.setExtraNodeCount(countMap, node.children);
        }
      });
    },
    handleMoldChange(index) {
      if (this.minderKey) {
        localStorage.setItem(this.minderKey + 'minderModel', index);
      }
      this.defaultMode = index;
    },
    save(data) {
      this.$emit('save', data)
    },
    parse(root, nodes) {
      this.loading = true;
      if (this.getExtraNodeCount) {
        // 如果有临时节点，筛选出用例数为空的模块，
        // 去查找下这些模块下的临时节点数量，来判断模块是不是要有展开图标
        let noCaseModuleIds = [];
        this.getNoCaseModuleIds(noCaseModuleIds, nodes);
        if (noCaseModuleIds.length < 1) {
          this._parse(root, nodes);
          this.loading = false;
          this.reload();
        } else {
          this.getExtraNodeCount(noCaseModuleIds, (data) => {
            this.setExtraNodeCount(data, nodes);
            this._parse(root, nodes);
            this.loading = false;
            this.reload();
          });
        }
      } else {
        this._parse(root, nodes);
        this.loading = false;
        this.reload();
      }
    },
    _parse(root, children) {
      root.children = [];
      if (!children) {
        children = [];
      }
      if (root.data.text === '未规划用例' && root.data.level === 1) {
        root.data.disable = true;
      }
      let caseNum = root.data.caseNum;
      let hasChildren = caseNum && caseNum > 0;
      if (this.getExtraNodeCount) {
        // 如果有临时节点的脑图，就判断下临时节点数量
        let extraNodeCount = root.data.extraNodeCount;
        hasChildren = hasChildren || (extraNodeCount && extraNodeCount > 0);
      }
      if (children.length < 1 && (this.ignoreNum || hasChildren)) {
        root.children.push({
          data: {
            text: '',
            type: 'tmp',
            expandState:"collapse"
          },
        });
      }

      if (children.length < 1) {
        return;
      }

      children.forEach((item) => {
        let node = {
          data: {
            text: item.name,
            id: item.id,
            disable: this.moduleDisable,
            type: 'node',
            level: item.level,
            resource: this.showModuleTag ? [this.$t('test_track.module.module')] : [],
            caseNum: item.caseNum,
            extraNodeCount: item.extraNodeCount,
            path: root.data.path + "/" + item.name,
            expandState:"collapse"
          },
        }
        if (this.tagEnable) {
          node.data.tagEnable = this.tagEnable;
        }
        root.children.push(node);
        this._parse(node, item.children);
      });
    },
    reload() {
      this.isActive = false;
      this.$nextTick(() => {
        this.isActive = true;
      });
    },
    setJsonImport(data) {
      this.importJson = data;
    },
    changeConfirm(isSave) {
      if (isSave) {
        this.save(window.minder.exportJson());
      } else {
        this.$store.commit('setIsTestCaseMinderChanged', false);
        this._handleNodeSelect(this.tmpNode);
      }
    },
    handleNodeSelect(node) {
      let isTestCaseMinderChanged = this.$store.state.isTestCaseMinderChanged;
      if (isTestCaseMinderChanged) {
        this.tmpNode = node;
        this.$refs.isChangeConfirm.open();
        return;
      }
      this._handleNodeSelect(node);
    },
    _handleNodeSelect(node) {
      if (node && node.data) {
        let nodeData = node.data;
        let importJson = this.getImportJsonBySelectNode(nodeData);
        this.setJsonImport(importJson);
        this.parse(importJson.root, nodeData.children);
      }
    },
    getImportJsonBySelectNode(nodeData) {
      let importJson =  {
        root: {
          data: {
            text: nodeData.name,
            id: nodeData.id,
            caseNum: nodeData.caseNum,
            disable: this.moduleDisable || nodeData.id === 'root',
            tagEnable: this.tagEnable,
            type: 'node',
            level: nodeData.level,
            resource: this.showModuleTag ? [this.$t('test_track.module.module')] : [],
          },
          children: []
        },
        template: "default"
      };
      return importJson;
    }
  }
}
</script>

<style scoped>
.minder-container >>> .save-btn {
  right: 30px;
  bottom: auto;
  top: 30px;
}

.minder {
  position: relative;
}

.fulls-screen-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1;
}

.full-screen {
  position: fixed;
  top: 0px;
  left: 0px;
  width: 100%;
  background: white;
  height: 100vh;
  z-index: 2;
}

.full-screen >>> .minder-container {
  height: calc(100vh - 109px) !important;
}

.full-screen .fulls-screen-btn {
  right: 30px;
}

/deep/ *[disabled] {
  opacity: 0.7 !important;
}
</style>
