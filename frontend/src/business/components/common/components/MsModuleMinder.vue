<template>
  <div class="minder" :class="{'full-screen': isFullScreen}">
    <ms-full-screen-button :is-full-screen.sync="isFullScreen"/>
    <minder-editor
      v-if="isActive"
      class="minder-container"
      :import-json="importJson"
      :progress-enable="false"
      :tags="tags"
      :height="height"
      :tag-edit-check="tagEditCheck"
      :priority-disable-check="priorityDisableCheck"
      :distinct-tags="distinctTags"
      :default-mold="defaultMode"
      @afterMount="$emit('afterMount')"
      @moldChange="handleMoldChange"
      @save="save"
    />
  </div>
</template>

<script>

import MsFullScreenButton from "@/business/components/common/components/MsFullScreenButton";
import {listenNodeSelected} from "@/business/components/track/common/minder/minderUtils";
export default {
  name: "MsModuleMinder",
  components: {MsFullScreenButton},
  props: {
    minderKey: String,
    treeNodes: {
      type: Array,
      default() {
        return []
      }
    },
    dataMap: {
      type: Map,
      default() {
        return new Map();
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
    priorityDisableCheck: Function
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
      defaultMode: 3
    }
  },
  created() {
    this.height = document.body.clientHeight - 340;
  },
  mounted() {
    this.defaultMode = 3;
    if (this.minderKey) {
      let model = localStorage.getItem(this.minderKey + 'minderModel');
      if (model) {
        this.defaultMode = Number.parseInt(model);
      }
    }
  },
  watch: {
    dataMap() {
      this.$nextTick(() => {
        if (this.selectNode && this.selectNode.data) {
          this.handleNodeSelect(this.selectNode);
        } else {
          this.parse(this.importJson.root, this.treeNodes);
        }
        this.reload();
      })
    }
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
      if (root.data.id ===  'root') {
        // nodeId 为空的用例
        let rootChildData = this.dataMap.get("");
        if (rootChildData) {
          rootChildData.forEach((dataNode) => {
            root.children.push(dataNode);
          })
        }
      }
      // 添加数据节点
      let dataNodes = this.dataMap.get(root.data.id);
      if (dataNodes) {
        dataNodes.forEach((dataNode) => {
          root.children.push(dataNode);
        })
      }

      if (children == null || children.length < 1) {
        return;
      }

      children.forEach((item) => {
        let node = {
          data: {
            text: item.name,
            id: item.id,
            disable: true,
            type: 'node',
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
      })
      this.$nextTick(() => {
        listenNodeSelected();
      })
    },
    setJsonImport(data) {
      this.importJson = data;
    },
    handleNodeSelect(node) {
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
            disable: true,
            tagEnable: this.tagEnable,
            type: 'node'
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
</style>
