<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    :data="request"
    :draggable="draggable"
    :color="defColor"
    :background-color="defBackgroundColor"
    :title="request.elementType">

  </api-base-component>
</template>

<script>
  import MsCodeEdit from "../../../../common/components/MsCodeEdit";
  import MsInstructionsIcon from "../../../../common/components/MsInstructionsIcon";
  import MsDropdown from "../../../../common/components/MsDropdown";
  import ApiBaseComponent from "../common/ApiBaseComponent";
  import Jsr233ProcessorContent from "../common/Jsr233ProcessorContent";

  export default {
    name: "JmeterElementComponent",
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
      request: {
        type: Object,
      },
      defTitle: {type: String, default: "Jmeter组件"},
      defColor: {type: String, default: "#606260"},
      defBackgroundColor: {type: String, default: "#F4F4FF"},
      node: {},
    },
    methods: {
      remove() {
        this.$emit('remove', this.jsr223Processor, this.node);
      },
      copyRow() {
        this.$emit('copyRow', this.jsr223Processor, this.node);
      },
    }
  }
</script>

<style scoped>
  /deep/ .el-divider {
    margin-bottom: 10px;
  }
</style>
