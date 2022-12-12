<template>
  <!-- 表格头部搜索栏 -->
  <div class="side-content-box">
    <div class="search-wrap">
      <div class="search-lable-row">
        <div class="label" v-if="label">{{ label }} (</div>
        <div class="total-count" v-if="label">{{ totalCount }})</div>
      </div>
      <div class="search-opt-row">
        <div class="simple-search-row">
          <!-- 简单搜索框 -->
          <slot name="simpleSearch"></slot>
        </div>

        <div class="version-select-row" v-xpack>
          <!-- 版本切换组件 -->
          <slot name="versionSelect"></slot>
        </div>

        <div class="adv-search-row">
          <!-- 高级搜索框  -->
          <slot name="advSearch"></slot>
        </div>
      </div>
    </div>
    <div class="table-data-wrap">
      <div class="table-data-row">
        <slot name="tableRow"></slot>
      </div>

      <div class="pagenation-wrap">
        <slot name="pagination"></slot>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: "CaseSideDialogContent",
  props: {
    label: {
      type: String,
      default: "",
    },
    totalCount: {
      type: Number,
      default: 0,
    },
  },
};
</script>
<style scoped lang="scss">
@import "@/business/style/index.scss";
.side-content-box {
  height: px2rem(72);
  padding-left: px2rem(24);
  padding-right: px2rem(24);
  .search-wrap {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
    .search-lable-row {
      display: flex;
      height: 26px;
      font-weight: 500;
      font-size: 18px;
      line-height: 26px;
    }
    .search-opt-row {
      display: flex;
      height: 32px;
      align-items: center;
      justify-content: flex-end;
      .simple-search-row {
        width: 200px;
        margin-right: 12px;
      }
      .version-select-row {
        width: 140px;
        margin-right: 12px;
      }
      .adv-search-row {
        width: 32px;
        height: 32px;
        :deep(button.el-button.el-button--default.el-button--mini) {
          box-sizing: border-box;
          width: 32px;
          height: 32px;
          background: #ffffff;
          border: 1px solid #bbbfc4;
          border-radius: 4px;
          flex: none;
          order: 5;
          align-self: center;
          flex-grow: 0;
        }
      }
    }
  }
  .table-data-wrap {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    max-height: px2rem(690);
    .table-data-row {
      /* height: px2rem(620); */
      :deep(.test-content) {
        /* height: px2rem(620) !important; */
        /* min-height: px2rem(621) !important; */
      }
      :deep(.el-table__body-wrapper) {
        /* height: px2rem(470) !important; */
      }
    }
    .pagenation-wrap {
      height: px2rem(68);
      border-top: 1px solid rgba(31, 35, 41, 0.15);
      :deep(.home-pagination) {
        margin-top: 6px;
      }
    }
  }
}
</style>
