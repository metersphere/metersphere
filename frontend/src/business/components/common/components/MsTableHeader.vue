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
        <ms-table-button icon="el-icon-download" v-if="showImport" v-permission="uploadPermission"
                         :content="importTip" @click="importData"/>
        <ms-table-button v-if="showRun" icon="el-icon-video-play"
                         type="primary"
                         :content="runTip" @click="runTest"/>
        <ms-table-button v-if="showRun" icon="el-icon-circle-plus-outline"
                         content="转场景测试" @click="historicalDataUpgrade"/>

        <slot name="button"></slot>
        <version-select v-xpack :project-id="projectId" @changeVersion="changeVersion" v-if="isShowVersion"/>
      </span>
      <span>
        <slot name="searchBarBefore"></slot>
        <ms-search
            :base-search-tip="tip"
            :condition.sync="condition"
            :show-base-search="haveSearch"
            @search="search">
        </ms-search>
      </span>
    </el-row>
  </div>

</template>

<script>
  import MsTableSearchBar from './MsTableSearchBar';
  import MsTableButton from './MsTableButton';
  import MsTableAdvSearchBar from "./search/MsTableAdvSearchBar";
  import {getCurrentProjectID} from "@/common/js/utils";
  import MsSearch from "@/business/components/common/components/search/MsSearch";

  const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
  const VersionSelect = requireComponent.keys().length > 0 ? requireComponent("./version/VersionSelect.vue") : {};

  export default {
    name: "MsTableHeader",
    components: {MsTableAdvSearchBar, MsTableSearchBar, MsTableButton,'VersionSelect': VersionSelect.default, MsSearch},
    data() {
      return {
        version:this.currentVersion
      };
    },
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
      uploadPermission: {
        type: Array,
        default() {
          return []
        }
      },
      runTip: {
        type: String,

      },
      currentVersion:{
        type: String,
      },
      isShowVersion:{
        type: Boolean,
        default: false
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
      },
      versionOptions:{
        type: Array,
        default() {
          return []
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
      },
      changeVersion(type){
        this.$emit('changeVersion',type);
      },
      resetSearchData() {
        if (this.$refs.searchBar) {
          this.$refs.searchBar.reset();
        }
      }
    },
    computed: {
      isCombine() {
        return this.condition.components !== undefined && this.condition.components.length > 0;
      },
      projectId() {
        return getCurrentProjectID();
      },

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
  .version-select {
    padding-left: 10px;
  }
</style>
