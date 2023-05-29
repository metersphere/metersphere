<template>
  <div>
    <div v-loading="loading" :class="[isFullScreen ? 'full-screen' : 'minder']">
      <ms-full-screen-button :is-full-screen.sync="isFullScreen" @toggleMinderFullScreen="toggleMinderFullScreen"/>
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
        :del-confirm="delConfirm"
        :font-enable="false"
        :arrange-enable="false"
        :style-enable="false"
        @afterMount="$emit('afterMount')"
        @moldChange="handleMoldChange"
        :disabled="disabled"
        @save="save"
      />
      <is-change-confirm
        @confirm="changeConfirm"
        ref="isChangeConfirm"/>
    </div>
  </div>

</template>

<script>
import Vue from "vue"
import vueMinderEditor from 'vue-minder-editor-plus'
import i18n from "@/i18n";
import MsFullScreenButton from "metersphere-frontend/src/components/MsFullScreenButton";
import IsChangeConfirm from "metersphere-frontend/src/components/IsChangeConfirm";
import {minderPageInfoMap} from "@/api/testCase";
import { useStore } from "@/store";

Vue.use(vueMinderEditor, {
  i18n: (key, value) => i18n.t(key, value)
});

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
    },
    delConfirm: {
      type: Function,
      default: null
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
    this.height = document.body.clientHeight - 325;
  },
  destroyed() {
    if (this.$EventBus) {
      this.$EventBus.$off("appFixedChange", this.setFullScreenLeft);
    }
    minderPageInfoMap.clear();
  },
  mounted() {
    if (this.$EventBus) {
      // 导出的报告不走这里
      this.$EventBus.$on("appFixedChange", this.setFullScreenLeft);
    }
    this.setFullScreenLeft();
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
    setFullScreenLeft() {
      const root = document.querySelector(':root');
      // 获取 :root 上 --screen-left 变量的值
      const left = getComputedStyle(root).getPropertyValue('--screen-left').trim();
      // 设置 :root 上 --screen-left 变量的值
      root.style.setProperty('--screen-left', left === '44px' ? '150px' : '44px');
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
          this.getExtraNodeCount(noCaseModuleIds)
            .then((r) => {
              this.setExtraNodeCount(r.data, nodes);
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
            expandState: "collapse"
          },
        });
      }

      if (children.length < 1) {
        return;
      }

      children.forEach((item) => {
        if (!item.id) {
          return;
        }
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
            expandState: "collapse"
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
        useStore().isTestCaseMinderChanged = false;
        this._handleNodeSelect(this.tmpNode);
      }
    },
    handleNodeSelect(node) {
      let isTestCaseMinderChanged = useStore().isTestCaseMinderChanged;
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
      let importJson = {
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
    },
    toggleMinderFullScreen(isFullScreen) {
      this.$emit("toggleMinderFullScreen", isFullScreen);
      this.$EventBus.$emit('toggleFullScreen', isFullScreen);
    }
  },
}
</script>

<style scoped>
:root {
  --screen-left: var(--asideWidth);
}

.minder-container :deep(.save-btn) {
  right: 24px;
  bottom: auto;
  top: 24px;
  width: 80px;
  height: 32px;
  border-radius: 4px;
}

.minder-container :deep(.save-btn span) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: -8px;
}

.minder {
  position: relative;
  top: 35px
}

.fulls-screen-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1;
}

.full-screen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px;
  height: 100vh;
  z-index: 1999;
  max-height: calc(100vh);
}

.full-screen :deep(.minder-container) {
  height: calc(100vh - 149px) !important;
}

.full-screen .fulls-screen-btn {
  right: 30px;
}

:deep(*[disabled]) {
  opacity: 0.7 !important;
}

:deep(.minder-container.km-editor.km-view.focus) {
  min-height: 422px;
  background: #F5F6F7!important;
}

:deep(.menu-container) {
  height: 60px;
}
</style>
