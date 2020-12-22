<template>
  <input-tag v-model="data" placeholder="输入回车添加"></input-tag>
</template>

<script>
  import InputTag from 'vue-input-tag'

  export default {
    name: "MsInputTag",
    components: {InputTag},
    props: {currentScenario: {}},
    created() {
      if (!this.currentScenario.tags) {
        this.currentScenario.tags = [];
      }
      this.data = this.currentScenario.tags;
    },
    data() {
      return {
        data: [],
      }
    },
    watch: {
      data() {
        this.currentScenario.tags = this.data;
      }
    },
    methods: {}
  }
</script>

<style scoped>

  /deep/ .vue-input-tag-wrapper {
    border-radius: 2px;
    border: 1px solid #a5d24a;
    color: #909399;
    display: inline-block;
  }

  /deep/ .input-tag {
    display: inline-block;
    font-size: 12px;
    min-width: auto;
    border-width: 1px;
    border-style: solid;
    border-radius: 4px;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    white-space: nowrap;
    background-color: #fff;
    border-color: #909399;
    color: #909399;
    width: auto;

    height: 23px;
    padding: 0 5px;
    line-height: 19px;
  }

  /deep/ .remove {
    color: #909399;
  }

  /deep/ .el-form-item__content {
    line-height: 100%;
  }
</style>
