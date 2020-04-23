<template>
  <span>
    <ms-table-operator-button icon="el-icon-edit"
                              @click="editClick" @click.stop="editClickStop"/>
    <ms-table-operator-button icon="el-icon-delete" type="danger"
                              @click="deletClick" @click.stop="deleteClickStop"/>
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
        deletClick() {
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
