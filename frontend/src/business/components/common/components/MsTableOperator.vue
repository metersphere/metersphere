<template>
  <span>
    <slot name="front"></slot>
    <ms-table-operator-button :tip="$t('commons.edit')" icon="el-icon-edit" @exec="editClick" @click.stop="editClickStop"/>
    <slot name="middle"></slot>
    <ms-table-operator-button :tip="$t('commons.delete')" icon="el-icon-delete" type="danger" @exec="deleteClick" @click.stop="deleteClickStop"/>
    <slot name="behind"></slot>
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
