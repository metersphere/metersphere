<template>
  <el-table-column v-if="isShow" width="1" :resizable="false" fixed="left" align="center">
    <el-popover slot="header" placement="bottom" trigger="hover" style="margin-right: 0px;" popper-class="select-popover">
      <el-button type="text" @click.native.stop="click('selectPageAll')">
          <span>
            {{ $t('api_test.batch_menus.select_current_page') }}
          </span>
      </el-button>
      <el-button type="text" @click.native.stop="click('selectAll')">
        <span>
          {{ $t('api_test.batch_menus.select_all_page') }}
        </span>
      </el-button>
      <i class="el-icon-arrow-down table-select-icon" slot="reference" style="cursor: pointer"></i>
    </el-popover>
  </el-table-column>
</template>

<script>
export default {
  name: "MsTableHeaderSelectPopover",
  // props: ['total', 'pageSize', 'selectDataCounts'],
  props: {
    total: {
      type: Number,
      default() {
        return 10;
      }
    },
    pageSize: {
      type: Number,
      default() {
        return 10;
      }
    },
    selectType: {
      type: Boolean,
      default() {
        return false;
      }
    },
    tableDataCountInPage: {
      type: Number,
      default() {
        return 10;
      }
    },
    selectDataCounts: {
      type: Number,
      default() {
        return 0;
      }
    },
  },
  watch: {
    total() {
      this.reload();
    },
    tableDataCountInPage() {
      this.reload();
    }
  },
  created() {
    if (this.keyIndex === 0) {
      this.keyIndex++;
      this.reload();
    }
  },
  data() {
    return {
      isShow: true,
      selectAllFontColor: {
        color: "#1F2329",
      },
      selectPageFontColor: {
        color: "#1F2329",
      },
      keyIndex: 0,
    };
  },
  methods: {
    click(even) {
      this.$emit(even);
    },
    reload() {
      if (!this.selectType) {
        this.selectAllFontColor.color = "#1F2329";
        this.selectPageFontColor.color = "#1F2329";
      }
      this.isShow = false;
      this.$nextTick(() => {
        this.isShow = true;
      });
    }
  }
}
</script>

<style scoped>
button.el-button.el-button--text {
  width: 100%!important;
  height: 32px!important;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  display: flex;
  align-items: center;
  color: #1F2329;
  margin: 0;
  border-radius: 0;
  border: 0;
  padding-left: 10px;
}

button.el-button.el-button--text:hover {
  background: rgba(31, 35, 41, 0.1);
}

button.el-button.el-button--text:focus {
  color: var(--count_number);
}
</style>

<style>
.select-popover {
  min-width: 0;
  margin-left: 82px;
  margin-top: 0;
  width: 116px!important;
  height: 62px;
  padding: 8px 0;
  cursor: pointer;
}

.select-popover[x-placement^=bottom] .popper__arrow::after{
  border: none;
}

.select-popover[x-placement^=bottom]{
  margin-top: 4px;
  margin-left: 114px;
}
</style>
