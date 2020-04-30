<template>
  <el-button class="template-component">
    <i class="el-icon-s-unfold"/>
    <span>{{ component.name }}</span>
    <el-tag v-if="component.type == 'system'" size="mini" type="success">系统</el-tag>
    <el-tag v-if="component.type == 'custom'" size="mini">自定义</el-tag>
  </el-button>
</template>

<script>
    export default {
      name: "TemplateComponentBar",
      props: {
        component: {
          type: Object,
          default() {
            return {}
          }
        }
      }
    }
</script>

<style scoped>

  .template-component i {
    float: left;
    height: 20px;
    line-height: 20px;
  }

  .template-component .el-tag {
    float: right;
    height: 20px;
    line-height: 20px;
  }

  .template-component span {
    display: inline-block;
    height: 20px;
    line-height: 20px;
  }

  .template-component {
    display: block;
    margin-left: 10px;
    width: 90%;
    margin-bottom: 5px;
  }

  .template-component {
    font-size: 16px;
  }

</style>
