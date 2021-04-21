<template>
  <span>
    <ms-table-operator-button v-for="(btn, index) in buttons" :key="index" :isTesterPermission="isTesterPermission(btn)"
                              :disabled="isDisable(btn)"
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
          return btn.isDisable(this.row);
        }
        return false;
      }
    },
    computed: {
      isTesterPermission() {
        return function (btn) {
          return btn.isTesterPermission !== false;
        }
      },
    }
  }
</script>

<style scoped>

</style>
