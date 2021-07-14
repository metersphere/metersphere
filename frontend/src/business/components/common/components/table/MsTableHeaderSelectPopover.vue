<template>
  <el-table-column v-if="isShow" width="1" :resizable="false" fixed="left" align="center">
    <el-popover slot="header" placement="right" trigger="click" style="margin-right: 0px;">
      <el-link
        @click.native.stop="click('selectAll')"
        ref="selectAllLink">
        <span :style="selectAllFontColor">
          {{ $t('api_test.batch_menus.select_all_data', [total]) }}
        </span>

      </el-link>

      <br/>
      <el-link
        @click.native.stop="click('selectPageAll')"
        ref="selectPageAllLink">
          <span :style="selectPageFontColor">
            {{ $t('api_test.batch_menus.select_show_data', [tableDataCountInPage]) }}
          </span>
      </el-link>

      <i class="el-icon-arrow-down table-select-icon" slot="reference"></i>
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
      created() {
        if(this.keyIndex === 0){
          this.keyIndex++;
          this.reload();
        }
      },
      data() {
        return {
          isShow: true,
          selectAllFontColor: {
            color: "gray",
          },
          selectPageFontColor: {
            color: "gray",
          },
          keyIndex:0,
        };
      },
      methods: {
        click(even) {
          if (even === 'selectPageAll') {
            this.selectAllFontColor.color = "gray";
            this.selectPageFontColor.color = document.body.style.getPropertyValue("--count_number");
          } else if (even === 'selectAll') {
            this.selectAllFontColor.color = document.body.style.getPropertyValue("--count_number");
            this.selectPageFontColor.color = "gray";

          } else {
            this.selectAllFontColor.color = "gray";
            this.selectPageFontColor.color = "gray";
          }
          //首次渲染之后，该组件不会重新渲染样式，使用keyIndex判断强制刷新一次，激活它的重新渲染功能
          if(this.keyIndex === 0){
            this.keyIndex++;
            this.reload();
          }
          this.$emit(even);
        },
        reload() {
            this.isShow = false;
            this.$nextTick(() => {
              this.isShow = true;
            });
        }
      }
    }
</script>

<style scoped>

.selected-link{
  color: #783887 !important;
}

</style>
