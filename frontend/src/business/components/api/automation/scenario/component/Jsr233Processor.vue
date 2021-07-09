<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    @active="active"
    :data="jsr223Processor"
    :draggable="draggable"
    :color="color"
    :is-max="isMax"
    :show-btn="showBtn"
    :background-color="backgroundColor"
    :title="title" v-loading="loading">

    <legend style="width: 100%">
      <jsr233-processor-content
        :jsr223-processor="jsr223Processor"
        :is-pre-processor="isPreProcessor"
        :node="node"
        :is-read-only="this.jsr223Processor.disabled"/>
    </legend>

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
    isMax: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
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
  data() {
    return {loading: false}
  },
  methods: {
    remove() {
      this.$emit('remove', this.jsr223Processor, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.jsr223Processor, this.node);
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    active() {
      this.jsr223Processor.active = !this.jsr223Processor.active;
      this.reload();
    },
  }
  }
</script>

<style scoped>
  /deep/ .el-divider {
    margin-bottom: 10px;
  }
</style>
