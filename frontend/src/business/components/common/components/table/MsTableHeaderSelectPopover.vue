<template>
  <el-table-column v-if="isShow" width="1" :resizable="false" align="center">
    <el-popover slot="header" placement="right" trigger="click" style="margin-right: 0px;">
      <el-link
        :class="{'selected-link': selectDataCounts === this.total}"
        @click.native.stop="click('selectAll')"
        ref="selectAllLink">
        {{$t('api_test.batch_menus.select_all_data',[total])}}
      </el-link>

      <br/>
        <el-link
          :class="{'selected-link': selectDataCounts === this.pageSize}"
          @click.native.stop="click('selectPageAll')"
          ref="selectPageAllLink">
          {{$t('api_test.batch_menus.select_show_data',[pageSize])}}
      </el-link>

      <i class="el-icon-arrow-down" slot="reference"></i>
    </el-popover>
  </el-table-column>
</template>

<script>
    export default {
      name: "MsTableSelectAll",
      props: ['total', 'pageSize', 'selectDataCounts'],
      data() {
        return {
          isShow: true
        };
      },
      watch: {
        selectDataCounts() {
          this.reload();
        }
      },
      methods: {
        click(even) {
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
