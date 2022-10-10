<template>
  <span>
    <ms-table-operator-button v-for="(btn, index) in buttons" :key="index"
                              v-permission="btn.permissions"
                              :disabled="isDisable(btn)"
                              :class="btn.class"
                              :tip="btn.tip" :icon="btn.icon" :type="btn.type" :isDivButton="btn.isDivButton"
                              @exec="click(btn)" @click.stop="clickStop(btn)"/>
  </span>
</template>

<script>
import MsTableOperatorButton from "./MsTableOperatorButton";

export default {
  name: "MsTableOperators",
  components: {MsTableOperatorButton},
  props: {
    row: Object,
    buttons: Array,
    index: Number
  },
  methods: {
    click(btn) {
      if (btn.exec instanceof Function) {
        btn.exec(this.row, this.index);
      }
    },
    clickStop(btn) {
      if (btn.stop instanceof Function) {
        btn.stop(this.row, this.index);
      }
    },
    isDisable(btn) {
      if (btn.isDisable) {
        if (btn.isDisable instanceof Function) {
          return btn.isDisable(this.row);
        } else {
          return btn.isDisable;
        }
      }
      return false;
    }
  }
};
</script>

<style scoped>

</style>
