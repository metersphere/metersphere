<template>
  <div>
    <span :class="labelClassName" :title="nodeName" >{{nodeName}}</span>
    <el-tag size="mini" v-if="nodeOperationInfo!== undefined && nodeOperationInfo.runningTask>0" style="color:#E5594B;background-color: #FFFFFF;border-color: #E5594B;margin-left: 5px;margin-right: 5px">
      {{$t("commons.running")}}</el-tag>
    <el-tag size="mini" v-else-if="nodeOperationInfo!== undefined" style="color:#89DB7E;background-color: #FFFFFF;border-color: #89DB7E;margin-left: 5px;margin-right: 5px">
      {{$t("commons.idle")}}</el-tag>
    <span v-if="nodeOperationInfo!== undefined && nodeOperationInfo.runningTask>0" style="color:#A9A9A9">
      {{" CPU:"+nodeOperationInfo.cpuUsage}}</span>
    <span v-if="nodeOperationInfo!== undefined " style="color:#A9A9A9;">
      {{" CPU:"+nodeOperationInfo.cpuUsage}}</span>
  </div>
</template>

<script>
export default {
  name: "NodeOperationLabel",
  props: {
    nodeName: String,
    nodeOperationInfo:Object,
    labelClassName: {
      type: String,
      default: "node-label"
    },
  },
  data() {
    return {
      loaded: true,
      defaultInitOptions: this.initOptions
    };
  },
  computed: {
  },
  mounted() {
  },
  methods: {
  }
};
</script>

<style scoped>
.node-label{
  display: inline-flex;
  width:240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
