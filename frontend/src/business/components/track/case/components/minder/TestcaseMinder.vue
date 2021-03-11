<template>
  <div class="minder">
    <minder-editor class="minder-container" :import-json="importJson"/>
  </div>
</template>

<script>

export default {
  name: "TestcaseMinder",
  components: {},
  data() {
    return {
      importJson: {
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
      }
    }
  },
  mounted() {
  }
}
</script>

<style scoped>
</style>
