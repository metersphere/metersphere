<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    @active="active"
    :data="request"
    :draggable="draggable"
    :color="color"
    :is-max="isMax"
    :show-btn="showBtn"
    :background-color="backgroundColor"
    :title="title" v-loading="loading">

    <legend style="width: 100%">
      <jdbc-processor-content
        :showScript="false"
        :request="request"/>
    </legend>

  </api-base-component>
</template>

<script>
import MsCodeEdit from "../../../../common/components/MsCodeEdit";
import MsInstructionsIcon from "../../../../common/components/MsInstructionsIcon";
import MsDropdown from "../../../../common/components/MsDropdown";
import ApiBaseComponent from "../common/ApiBaseComponent";
import JdbcProcessorContent from "@/business/components/api/automation/scenario/common/JDBCProcessorContent";

export default {
  name: "MsJdbcProcessor",
  components: {
    JdbcProcessorContent,
    ApiBaseComponent, MsDropdown, MsInstructionsIcon, MsCodeEdit
  },
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
    request:Object,
    title: String,
    color: String,
    backgroundColor: String,
    node: {},
  },
  created() {
  },
  data() {
    return {loading: false};
  },
  methods: {
    remove() {
      this.$emit('remove', this.request, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.request, this.node);
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    active() {
      this.request.active = !this.request.active;
      this.reload();
    },
  }
};
</script>

<style scoped>
/deep/ .el-divider {
  margin-bottom: 10px;
}
</style>
