<template>
  <div>
    <div class="minder" :class="{'full-screen': isFullScreen}">
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
      tmpNode: {}
    }
  },
  created() {
    this.height = document.body.clientHeight - 285;
  },
  mounted() {
    this.defaultMode = 3;
    if (this.minderKey) {
      let model = localStorage.getItem(this.minderKey + 'minderModel');
      if (model) {
        this.defaultMode = Number.parseInt(model);
      }
    }
    this.$nextTick(() => {
      if (this.selectNode && this.selectNode.data) {
        this.handleNodeSelect(this.selectNode);
      } else {
        this.parse(this.importJson.root, this.treeNodes);
      }
      this.reload();
    });
  },
  methods: {
    handleMoldChange(index) {
      if (this.minderKey) {
        localStorage.setItem(this.minderKey + 'minderModel', index);
      }
      this.defaultMode = index;
    },
    save(data) {
      this.$emit('save', data)
    },
    parse(root, children) {
      root.children = [];
      if (!children) {
        children = [];
      }
      let caseNum = root.data.caseNum;
      if (root.data.text === '未规划用例' && root.data.level === 1) {
        root.data.disable = true;
      }
      if (children.length < 1 && (this.ignoreNum || caseNum && caseNum > 0)) {
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
            path: root.data.path + "/" + item.name,
            expandState:"collapse"
          },
        }
        if (this.tagEnable) {
          node.data.tagEnable = this.tagEnable;
        }
        root.children.push(node);
        this.parse(node, item.children);
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
        this.parse(importJson.root, nodeData.children);
        this.setJsonImport(importJson);
        this.reload();
      }
    },
    getImportJsonBySelectNode(nodeData) {
      let importJson =  {
        root: {
          data: {
            text: nodeData.name,
            id: nodeData.id,
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
