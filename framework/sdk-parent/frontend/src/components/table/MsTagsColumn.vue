<template>
  <ms-table-column
    prop="tags"
    sortable
    :field="field"
    :fields-width="fieldsWidth"
    :show-overflow-tooltip="false"
    :label="$t('api_test.automation.tag')"
    min-width="200px">
    <template v-slot:default="scope">
      <span v-if="scope.row.tags && scope.row.tags.length === 1">
        <ms-tag
          v-for="(name, index) in scope.row.tags"
          :key="index"
          type="success"
          effect="plain"
          :content="name"
          style="margin-left: 0; margin-right: 2px"
        />
      </span>
      <el-tooltip class="item" effect="dark" placement="top" :enterable="false" v-else>
        <div v-html="scope.row.tags ? scope.row.tags.join('，'): ''" slot="content"></div>
        <div class="oneLine">
          <ms-tag
            v-for="(name, index) in scope.row.tags"
            :key="index"
            type="success"
            effect="plain"
            :content="name"
            style="margin-left: 0; margin-right: 2px"
          />
          <span/>
        </div>
      </el-tooltip>
    </template>
  </ms-table-column>
</template>

<script>
import MsTableColumn from "./MsTableColumn";
import MsTag from "../MsTag";
export default {
  name: "MsTagsColumn",
  components: {MsTag, MsTableColumn},
  props: {
    field: Object,
    fieldsWidth: Object,
  }
}
</script>

<style scoped>
.oneLine {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
