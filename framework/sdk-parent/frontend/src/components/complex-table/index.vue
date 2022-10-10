<template>
  <div class="complex-table">
    <div class="complex-table__header" v-if="$slots.header || header">
      <slot name="header">{{ header }}</slot>
    </div>

    <div class="complex-table__toolbar" v-if="$slots.toolbar || searchConfig">
      <div>
        <slot name="toolbar">

        </slot>
      </div>
      <fu-search-bar v-bind="searchConfig" @exec="search">
        <template #complex>
          <slot name="complex"></slot>
        </template>
        <slot name="buttons"></slot>
        <fu-table-column-select :columns="columns"/>
      </fu-search-bar>
    </div>

    <div class="complex-table__body">
      <fu-table v-on="$listeners" v-bind="$attrs" :columns="columns" :local-key="localKey">
        <slot></slot>
      </fu-table>
    </div>

    <div class="complex-table__pagination" v-if="$slots.pagination || paginationConfig">
      <slot name="pagination">
        <fu-table-pagination :current-page.sync="paginationConfig.currentPage"
                             :page-size.sync="paginationConfig.pageSize"
                             v-bind="paginationConfig"
                             @change="search"/>
      </slot>
    </div>
  </div>
</template>

<script>

export default {
  name: "ComplexTable",
  props: {
    columns: {
      type: Array,
      default: () => []
    },
    localKey: String, // 如果需要记住选择的列，则这里添加一个唯一的Key
    header: String,
    searchConfig: Object,
    paginationConfig: Object,
  },
  data() {
    return {
      condition: {}
    }
  },
  methods: {
    search(condition, e) {
      if (condition) {
        this.condition = condition
      }
      this.$emit("search", this.condition, e)
    }
  },
}
</script>

<style lang="scss">
@import "~../../styles/common/mixins.scss";
@import "~../../styles/common/variables.scss";

.complex-table {
  .complex-table__header {
    @include flex-row(flex-start, center);
    line-height: 60px;
    font-size: 18px;
  }

  .complex-table__toolbar {
    @include flex-row(space-between, center);

    .fu-search-bar {
      width: auto;
    }
  }

  .complex-table__pagination {
    margin-top: 20px;
    @include flex-row(flex-end);
  }
}

</style>
