<template>
  <el-table-column v-if="isShow" width="1" :resizable="false" align="center">
    <el-popover slot="header" placement="right" trigger="click" style="margin-right: 0px;">
      <el-link
        :class="{'selected-link': selectDataCounts === total}"
        @click.native.stop="click('selectAll')"
        ref="selectAllLink">
        <span :style="selectAllFontColor">
          {{ $t('api_test.batch_menus.select_all_data', [total]) }}
        </span>

      </el-link>

      <br/>
      <el-link
        :class="{'selected-link': selectDataCounts === this.pageSize}"
        @click.native.stop="click('selectPageAll')"
        ref="selectPageAllLink">
          <span :style="selectPageFontColor">
            {{ $t('api_test.batch_menus.select_show_data', [pageSize]) }}
          </span>
      </el-link>

      <i class="el-icon-arrow-down" slot="reference"></i>
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
        selectDataCounts: {
          type: Number,
          default() {
            return 0;
          }
        },
      },
      data() {
        return {
          selectType: "",
          isShow: true,
          selectAllFontColor: {
            color: "gray",
          },
          selectPageFontColor: {
            color: "gray",
          },

        };
      },
      watch: {
        // selectDataCounts() {
        //   this.reload();
        // },
        // total() {
        //   this.reload();
        // }
      },
      methods: {
        click(even) {
          if (even === 'selectPageAll') {
            this.selectPageFontColor.color = document.body.style.getPropertyValue("--count_number");
            this.selectAllFontColor.color = "gray";

          } else if (even === 'selectAll') {
            this.selectAllFontColor.color = document.body.style.getPropertyValue("--count_number");
            this.selectPageFontColor.color = "gray";

          } else {
            this.selectAllSimpleStyle.color = "gray";
            this.selectPageFontColor.color = "gray";
          }
          this.$emit(even);
          // this.isShow = false;
          // this.$nextTick(() => {
          //   this.isShow = true;
          // });

        },
        // reload() {
        //   this.isShow = false;
        //   this.selectAllLinkType = "info";
        //   this.selectPageLinkType = "info";
        //   this.$nextTick(() => {
        //     this.isShow = true;
        //   });
        // }
      }
    }
</script>

<style scoped>

.selected-link{
  color: #783887 !important;
}

</style>
