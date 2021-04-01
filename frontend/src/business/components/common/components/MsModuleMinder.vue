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
      :distinct-tags="distinctTags"
      @save="save"
    />
  </div>
</template>

<script>

import MsFullScreenButton from "@/business/components/common/components/MsFullScreenButton";
export default {
  name: "MsModuleMinder",
  components: {MsFullScreenButton},
  props: {
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
    distinctTags: {
      type: Array,
      default() {
        return []
      }
    },
    selectNode: {
      type: Object,
    }
  },
  data() {
    return {
      importJson: {
        root: {
          data: {
            text: this.$t('test_track.review_view.all_case'),
            disable: true,
            id: 'root',
          },
          children: []
        },
        template: 'default'
      },
      isActive: true,
      isFullScreen: false,
      height: ''
    }
  },
  created() {
    this.height = document.body.clientHeight - 340;
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
            expandState:"collapse"
          },
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
