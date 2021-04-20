<template>
  <el-table-column v-if="isShow" width="1" :resizable="false" align="center">
    <el-popover slot="header" placement="right" trigger="click" style="margin-right: 0px;">
      <el-link
        :class="{'selected-link': selectDataCounts === total}"
        @click.native.stop="click('selectAll')"
        :type="selectAllLinkType"
        ref="selectAllLink">
        <span>
          {{ $t('api_test.batch_menus.select_all_data', [total]) }}
        </span>

      </el-link>

      <br/>
      <el-link
        :class="{'selected-link': selectDataCounts === this.pageSize}"
        @click.native.stop="click('selectPageAll')"
        :type="selectPageLinkType"
        ref="selectPageAllLink">
          <span>
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
          selectAllLinkType: "info",
          selectPageLinkType: "info",
        };
      },
      watch: {
        selectDataCounts() {
          this.reload();
        },
        total() {
          this.reload();
        }
      },
      methods: {
        click(even) {
          if (even === 'selectPageAll') {
            this.selectAllLinkType = "info";
            this.selectPageLinkType = "primary";
          } else if (even === 'selectAll') {
            this.selectAllLinkType = "primary";
            this.selectPageLinkType = "info";
          } else {
            this.selectAllLinkType = "info";
            this.selectPageLinkType = "info";
          }
          this.$emit(even);
          this.isShow = false;
          this.$nextTick(() => {
            this.isShow = true;
          });

        },
        reload() {
          this.isShow = false;
          this.selectAllLinkType = "info";
          this.selectPageLinkType = "info";
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
