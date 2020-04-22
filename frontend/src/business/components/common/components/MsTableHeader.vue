<template>

  <div>
    <el-row class="title" type="flex" justify="space-between" align="middle">
      <slot name="title">
         {{title}}
      </slot>
    </el-row>
    <el-row type="flex" justify="space-between" align="middle">
      <span class="operate-button">
        <ms-table-button v-if="showCreate" icon="el-icon-circle-plus-outline" :content="createTip" @click="create"/>
        <slot name="button"></slot>
      </span>
      <span>
        <ms-table-search-bar :condition.sync="condition" @change="search"/>
      </span>
    </el-row>
  </div>

</template>

<script>
  import MsTableSearchBar from './MsTableSearchBar';
  import MsTableButton from './MsTableButton';

    export default {
      name: "MsTableHeader",
      components: {MsTableSearchBar, MsTableButton},
      props: {
        title: {
          type: String,
          default() {
            return this.$t('commons.name');
          }
        },
        showCreate: {
          type: Boolean,
          default: true
        },
        condition: {
          type: Object
        },
        createTip: {
          type: String,
          default() {
            return this.$t('commons.create');
          }
        }
      },
      methods: {
        search() {
          this.$emit('update:condition', this.condition);
          this.$emit('search');
        },
        create() {
          this.$emit('create');
        }
      }
    }
</script>

<style scoped>

  .title {
    height: 40px;
    font-weight: bold;
  }

</style>
