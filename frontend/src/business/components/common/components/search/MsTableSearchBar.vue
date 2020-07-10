<template>

    <el-input
      class="search"
      type="text"
      size="small"
      :placeholder="$t('commons.search_by_name')"
      prefix-icon="el-icon-search"
      @change="search"
      maxlength="60"
      v-model="condition.name" clearable/>

</template>

<script>
    export default {
      name: "MsTableSearchBar",
      props: {
        condition: {
          type: Object
        }
      },
      methods: {
        search() {
          this.$emit('update:condition', this.condition);
          this.$emit('change');
        }
      }
    }
</script>

<style scoped>

</style>
