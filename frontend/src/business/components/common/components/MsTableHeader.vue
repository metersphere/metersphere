<template>

  <div class="ms-table-header">
    <el-row v-if="title" class="table-title" type="flex" justify="space-between" align="middle">
      <slot name="title">
        {{title}}
      </slot>
    </el-row>
    <el-row type="flex" justify="space-between" align="middle">
      <span class="operate-button">
        <ms-table-button v-permission="createPermission" v-if="showCreate" icon="el-icon-circle-plus-outline"
                         :content="createTip" @click="create"/>
        <ms-table-button icon="el-icon-download" v-if="showImport"
                         :content="importTip" @click="importData"/>
        <ms-table-button v-if="showRun" icon="el-icon-video-play"
                         type="primary"
                         :content="runTip" @click="runTest"/>
          <ms-table-button v-if="showRun" icon="el-icon-circle-plus-outline"
                           content="转场景测试" @click="historicalDataUpgrade"/>

        <slot name="button"></slot>
      </span>
      <span>
        <slot name="searchBarBefore"></slot>
        <ms-table-search-bar :condition.sync="condition" @change="search" class="search-bar" :tip="tip" v-if="haveSearch"/>
        <ms-table-adv-search-bar :condition.sync="condition" @search="search" v-if="isCombine"/>
      </span>
    </el-row>
  </div>

</template>

<script>
  import MsTableSearchBar from './MsTableSearchBar';
  import MsTableButton from './MsTableButton';
  import MsTableAdvSearchBar from "./search/MsTableAdvSearchBar";

  export default {
    name: "MsTableHeader",
    components: {MsTableAdvSearchBar, MsTableSearchBar, MsTableButton},
    props: {
      title: {
        type: String,
        default() {
          return null;
        }
      },
      showCreate: {
        type: Boolean,
        default: true
      },
      showImport: {
        type: Boolean,
        default: false
      },
      showRun: {
        type: Boolean,
        default: false
      },
      condition: {
        type: Object
      },
      createTip: {
        type: String,
        default() {
          return this.$t('commons.create');
        }
      },
      importTip: {
        type: String,
        default() {
          return this.$t('commons.import');
        }
      },
      createPermission: {
        type: Array,
        default() {
          return []
        }
      },
      runTip: {
        type: String,

      },

      isTesterPermission: {
        type: Boolean,
        default: false
      },
      tip: {
        String,
        default() {
          return this.$t('commons.search_by_name');
        }
      },
      haveSearch: {
        Boolean,
        default() {
          return true;
        }
      }
    },
    methods: {
      search(value) {
        this.$emit('update:condition', this.condition);
        this.$emit('search', value);
      },
      create() {
        this.$emit('create');
      },
      importData() {
        this.$emit('import');
      },
      runTest() {
        this.$emit('runTest')
      },
      historicalDataUpgrade() {
        this.$emit('historicalDataUpgrade');
      }
    },
    computed: {
      isCombine() {
        return this.condition.components !== undefined && this.condition.components.length > 0;
      }
    }
  }
</script>

<style>

  .table-title {
    height: 40px;
    font-weight: bold;
    font-size: 18px;
  }

</style>

<style scoped>

  .operate-button {
    margin-bottom: -5px;
  }

  .search-bar {
    width: 240px
  }

</style>
