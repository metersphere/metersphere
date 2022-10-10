<template>
  <api-base-component
    @copy="copyRow"
    @remove="remove"
    :data="timer"
    :draggable="draggable"
    :show-collapse="false"
    :is-max="isMax"
    :inner-step="innerStep"
    color="#67C23A"
    background-color="#F2F9EE"
    :title="$t('api_test.automation.wait_controller')">

    <template v-slot:headerLeft>
      <el-input-number class="time-input" size="mini" v-model="timer.delay" :min="0" :step="1000" ref="nameInput"
                       :disabled="timer.disabled"/>
      ms
    </template>

  </api-base-component>
</template>

<script>
import ApiBaseComponent from "../common/ApiBaseComponent";

export default {
  name: "MsConstantTimer",
  components: {ApiBaseComponent},
  props: {
    timer: {},
    innerStep: {
      type: Boolean,
      default: false,
    },
    node: {},
    isMax: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    draggable: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {}
  },
  created() {
    this.$nextTick(() => {
      this.$refs.nameInput.focus();
    });
  },
  methods: {
    remove() {
      this.$emit('remove', this.timer, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.timer, this.node);
    },
  }
}
</script>

<style scoped>
.time-input {
  width: 30%;
}
</style>
