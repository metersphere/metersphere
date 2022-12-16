<template>
  <div style="float: right;">
    <ms-table-search-bar
      v-if="showBaseSearch"
      :condition.sync="condition"
      :style="{width: baseSearchWidth + 'px'}"
      :tip="baseSearchTip"
      @change="search"/>
  </div>
</template>

<script>
import MsTableSearchBar from "../MsTableSearchBar";

export default {
  name: "MsSearch",
  components: {
    MsTableSearchBar
  },
  props: {
    condition: {
      type: Object,
      default() {
        return {};
      }
    },
    baseSearchTip: {
      type: String,
      default() {
        return this.$t('commons.search_by_name_or_id');
      }
    },
    showBaseSearch: {
      type: Boolean,
      default() {
        return true;
      }
    },
    baseSearchWidth: {
      type: Number,
      default() {
        return 240;
      }
    }
  },
  methods: {
    search() {
      this.$emit("search");
    }
  }
}
</script>

<style scoped>
.ms-adv-search {
  margin-left: 5px;
  margin-right: 5px;
}
</style>
