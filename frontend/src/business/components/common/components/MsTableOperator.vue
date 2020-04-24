<template>
  <span>
    <ms-table-operator-button icon="el-icon-edit"
                              :exec="editClick" @click.stop="editClickStop"/>
    <ms-table-operator-button icon="el-icon-delete" type="danger"
                              :exec="deleteClick" @click.stop="deleteClickStop"/>
  </span>

</template>

<script>
    import MsTableOperatorButton from "./MsTableOperatorButton";
    export default {
      name: "MsTableOperator",
      components: {MsTableOperatorButton},
      methods: {
        editClick() {
          this.$emit('editClick');
        },
        editClickStop() {
          this.$emit('editClickStop');
        },
        deleteClick() {
          this.$emit('deleteClick');
        },
        deleteClickStop() {
          this.$emit('deleteClickStop');
        }
      }
    }
</script>

<style scoped>

</style>
