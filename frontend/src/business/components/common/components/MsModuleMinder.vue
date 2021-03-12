<template>
  <div class="minder">
    <minder-editor v-if="isActive"
      class="minder-container"
      :import-json="importJson"
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
    data: {
      type: Array,
      default() {
        return []
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
            disable: true
          },
          children: []
        },
        "template":"default"
      },
      isActive: true
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.parse(this.importJson.root, this.treeNodes);
      this.reload();
    })
  },
  methods: {
    save(data) {
      console.log(data);
      // console.log(this.treeNodes);
    },
    parse(root, children) {
      if (children == null || children.length < 1) {
        return;
      }
      root.children = [];
      children.forEach((item) => {
        let node = {
          data: {
            text: item.name,
            id: item.id,
            disable: true,
            // resource: ['#']
          },
        }
        root.children.push(node);
        this.parse(node, item.children);
      })
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
</style>
