<template>
  <div class="script-content">
    <ms-code-edit mode="java" :read-only="isReadOnly" :data.sync="beanShellProcessor.script" theme="eclipse" :modes="['java']" ref="codeEdit"/>
  </div>
</template>

<script>
    import MsCodeEdit from "../../../../common/components/MsCodeEdit";
    export default {
      name: "MsBeanShellProcessor",
      components: {MsCodeEdit},
      data() {
        return {
        }
      },
      props: {
        type: {
          type: String,
        },
        isReadOnly: {
          type: Boolean,
          default: false
        },
        beanShellProcessor: {
          type: Object,
          default() {
            return {}
          }
        }
      },
    }
</script>

<style scoped>

  .ace_editor {
    border-radius: 5px;
  }

  .script-content {
    padding: 15px 0;
    height: 300px;
  }

</style>
