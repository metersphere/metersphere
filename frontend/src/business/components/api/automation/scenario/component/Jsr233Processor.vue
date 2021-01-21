<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    :data="jsr223Processor"
    :draggable="draggable"
    :color="color"
    :background-color="backgroundColor"
    :title="title">

    <jsr233-processor-content
      :jsr223-processor="jsr223Processor"
      :is-pre-processor="isPreProcessor"
      :node="node"
      :is-read-only="isReadOnly"/>

  </api-base-component>
</template>

<script>
  import MsCodeEdit from "../../../../common/components/MsCodeEdit";
  import MsInstructionsIcon from "../../../../common/components/MsInstructionsIcon";
  import MsDropdown from "../../../../common/components/MsDropdown";
  import ApiBaseComponent from "../common/ApiBaseComponent";
  import Jsr233ProcessorContent from "../common/Jsr233ProcessorContent";

  export default {
    name: "MsJsr233Processor",
    components: {Jsr233ProcessorContent, ApiBaseComponent, MsDropdown, MsInstructionsIcon, MsCodeEdit},
    props: {
      draggable: {
        type: Boolean,
        default: false,
      },
      isReadOnly: {
        type: Boolean,
        default:
          false
      },
      jsr223Processor: {
        type: Object,
      },
      isPreProcessor: {
        type: Boolean,
        default:
          false
      },
      title: String,
      color: String,
      backgroundColor: String,
      node: {},
    },
    methods: {
      remove() {
        this.$emit('remove', this.jsr223ProcessorData, this.node);
      },
      copyRow() {
        this.$emit('copyRow', this.jsr223ProcessorData, this.node);
      },
    }
  }
</script>

<style scoped>
  /deep/ .el-divider {
    margin-bottom: 10px;
  }
</style>
