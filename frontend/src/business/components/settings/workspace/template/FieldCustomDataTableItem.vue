<template>
  <ms-table-column
    v-if="isShow"
    :label="$t('custom_field.api_field_name')"
    class="custom-with"
    prop="customData">
    <template v-slot="scope">
      <el-input :placeholder="$t('commons.input_content')" v-model="scope.row.customData">
      </el-input>
    </template>
  </ms-table-column>
</template>

<script>
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
export default {
  name: "FieldCustomDataTableItem",
  components: {MsTableColumn},
  props: [
    'scene'
  ],
  computed: {
    isShow() {
      if (this.scene === 'ISSUE') {
        return true;
      }
      return false;
    }
  }
};
</script>

<style scoped>

</style>
