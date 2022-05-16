<template>
  <div style="float: right;">
    <ms-table-search-bar
      v-if="showBaseSearch"
      :condition.sync="condition"
      :style="{width: baseSearchWidth + 'px'}"
      :tip="baseSearchTip"
      @change="search"/>
    <ms-table-adv-search-bar
      :show-link="showAdvSearchLink"
      :condition.sync="condition"
      @search="search"
      class="ms-adv-search"
      ref="advSearch"/>
  </div>
</template>

<script>
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";

export default {
  name: "MsSearch",
  components: {
    MsTableSearchBar,
    MsTableAdvSearchBar
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
  computed: {
    showAdvSearchLink() {
      return this.condition.components !== undefined && this.condition.components.length > 0;
    },
  },
  methods: {
    search() {
      this.$emit("search");
    },
    resetAdvSearch() {
      if (this.$refs.advSearch) {
        this.$refs.advSearch.reset();
      }
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
