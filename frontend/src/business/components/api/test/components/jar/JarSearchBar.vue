<template>
  <el-input class="jar-header-search"
            v-model="searchCondition"
            type="text"
            size="small"
            prefix-icon="el-icon-search"
            @keyup.enter.native="search"
            :placeholder="$t('project.search_by_name_jar')" clearable/>
</template>

<script>
    export default {
      name: "MsJarSearchBar",
      props: {
        condition: {
          type: Object,
          default() {
            return undefined
          },
        },
      },
      data() {
        return {
          searchCondition: this.condition
        };
      },
      watch: {
        inputCondition(value) {
          this.searchCondition = value;
        }
      },
      methods: {
        search() {
          this.$emit('refresh', this.searchCondition);
        }
      },
    }
</script>

<style scoped>
  .jar-header-search {
    margin-bottom: 10px;
    margin-top: 10px;
    min-width: 100px;
    max-width: 250px;
  }
</style>
