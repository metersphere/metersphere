<template>
  <div>
    <el-row v-for="row in customFieldsRows" :key="row.id">
      <el-col
        :span="isRichTextRow(row) ? 21 : 7"
        v-for="(item, index) in row"
        :key="index"
      >
        <el-form-item
          v-if="item"
          :label-width="formLabelWidth"
          :label="item.system ? $t(systemNameMap[item.name]) : item.name"
          :prop="item.name"
        >
          <custom-filed-component
            v-if="!loading"
            :disabled="true"
            :data="item"
            :form="{}"
            prop="defaultValue"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import CustomFiledComponent from "metersphere-frontend/src/components/new-ui/MsCustomFiledComponent";

export default {
  name: "CustomFieldFormItems",
  components: {
    CustomFiledComponent
  },
  props: {
    fields: {
      type: Array,
      default() {
        return []
      }
    },
    formLabelWidth: [String, Number],
    systemNameMap: Object,
    loading: Boolean
  },
  computed: {
    customFieldsRows() {
      // 自定义字段每三个一行显示，富文本框独占一行
      let displayRows = [];
      // 将 [{}, {}, {type: richText}, {}] 转化成 [[{}, {}], [{type: richText}], [{}]]
      this.fields.forEach((item) => {
        if (item.type === 'richText') {
          displayRows.push([item]);
        } else if(displayRows[displayRows.length - 1]) {
          if (displayRows[displayRows.length - 1][0].type === 'richText') {
            displayRows.push([item]);
          } else {
            displayRows[displayRows.length - 1].push(item);
          }
        } else {
          displayRows.push([item]);
        }
      });
      return displayRows;
    }
  },
  methods: {
    isRichTextRow(row) {
      if (row && row.length === 1 && row[0].type === 'richText') {
        return true;
      }
      return false;
    }
  }
}
</script>

<style scoped>

</style>
