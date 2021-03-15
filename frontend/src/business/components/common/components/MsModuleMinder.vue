<template>
  <div class="minder">
    <minder-editor
      v-if="isActive"
      class="minder-container"
      :import-json="importJson"
      :height="700"
      :progress-enable="false"
      @save="save"
    />
  </div>
</template>

<script>

export default {
  name: "MsModuleMinder",
  components: {},
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
    }
  },
  data() {
    return {
      importJsonTest: {
        "root": {
          "data": {
            "text": "test111"
          },
          "children": [
            { "data": { "text": "新闻"}},
            { "data": { "text": "网页"} },
            { "data": { "text": "贴吧"} },
            { "data": { "text": "知道"} },
            { "data": { "text": "音乐" } },
            { "data": { "text": "图片"} },
            { "data": { "text": "视频"} },
            { "data": { "text": "地图" } },
            { "data": { "text": "百科","expandState":"collapse"}}
          ]
        },
        "template":"default"
      },
      importJson: {
        root: {
          data: {
            text: "全部用例",
            disable: true,
            id: "root",
            path: ""
          },
          children: []
        },
        "template":"default"
      },
      isActive: true
    }
  },
  mounted() {
  },
  watch: {
    dataMap() {
      this.$nextTick(() => {
        this.parse(this.importJson.root, this.treeNodes);
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
            path: root.data.path + "/" + item.name,
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
</style>
