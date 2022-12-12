<template>
  <div class="header-popover-layout">
    <el-popover
      placement="bottom-end"
      width="240"
      trigger="click" :popper-class="'header-custom-popover'" @show="reset" @after-leave="setHeader">
      <el-scrollbar>
        <div class="content">
          <span class="table-header-custom-tips">{{ $t('commons.header_custom_select_tips') }}</span>
          <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange">{{$t('group.check_all')}}</el-checkbox>
          <el-checkbox-group v-model="checkedFieldsLabel" @change="handleCheckedChange">
            <el-checkbox v-for="field in allHeaderFieldsLabel" :label="field.label" :key="field.id"
                         :disabled="field.disable ? field.disable : false">{{ field.label }}</el-checkbox>
          </el-checkbox-group>
        </div>
      </el-scrollbar>

      <el-button slot="reference" size="mini" @click="openOrClose">
        <svg-icon icon-class="icon_setting_outlined"/>
      </el-button>
    </el-popover>
  </div>
</template>

<script>
import {SYSTEM_FIELD_NAME_MAP} from "../utils/table-constants";
import {getAllFieldWithCustomFields, getCustomTableHeader, saveCustomTableHeader} from "../utils/tableUtils";

export default {
  name: "MsTableHeaderCustomPopover.vue",
  props: {
    // 列表头部自定义参数与MsTable一致, field: 展示字段, customFields: 自定义字段(可为空), fieldKey: 页面列表字段设置唯一标识
    fields: Array,
    customFields: Array,
    fieldKey: String
  },
  data() {
    return {
      checkAll: false,
      allHeaderFieldsLabel: null,
      checkedFieldsLabel: null,
      isIndeterminate: false,
      isHeaderFieldReset: false,
    };
  },
  methods: {
    openOrClose() {
      let allFields = getAllFieldWithCustomFields(this.fieldKey, this.customFields);
      this.allHeaderFieldsLabel = allFields;
      let checkFields = JSON.parse(JSON.stringify(this.fields));
      checkFields.forEach(it => {
        if (it.isCustom) {
          it.label = SYSTEM_FIELD_NAME_MAP[it.id] ? this.$t(SYSTEM_FIELD_NAME_MAP[it.id]) : it.label;
        }
      })
      this.checkedFieldsLabel = this.getFieldsLabel(checkFields);
      this.checkAll = this.checkedFieldsLabel.length === this.allHeaderFieldsLabel.length;
      this.isIndeterminate = this.checkedFieldsLabel.length > 0 && this.checkedFieldsLabel.length < this.allHeaderFieldsLabel.length;
    },
    handleCheckAllChange(val) {
      this.checkAll = val;
      this.checkedFieldsLabel = this.checkAll ? this.getFieldsLabel(this.allHeaderFieldsLabel) : [];
      this.isIndeterminate = false;
      this.isHeaderFieldReset = true;
    },
    handleCheckedChange(value) {
      let checkedCount = value.length;
      this.checkAll = checkedCount === this.allHeaderFieldsLabel.length;
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.allHeaderFieldsLabel.length;
      this.isHeaderFieldReset = true;
    },
    getFieldsLabel(fields) {
      return fields.map(f => f['label']);
    },
    reset() {
      this.isHeaderFieldReset = false;
    },
    setHeader() {
      if (this.isHeaderFieldReset) {
        let checkFields = [];
        let allFields = getAllFieldWithCustomFields(this.fieldKey, this.customFields);
        allFields.forEach(field => {
          if (this.checkedFieldsLabel.indexOf(field['label']) != -1) {
            checkFields.push(field)
          }
        })
        saveCustomTableHeader(this.fieldKey, checkFields);
        this.$emit('reload');
      }
    }
  }
}
</script>

<style scoped>
.header-popover-layout {
  display: inline-block;
}

.table-header-custom-tips {
  display: block;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  display: flex;
  align-items: center;
  color: #8F959E;
  margin: 13px 12px 5px 12px
}

:deep(button.el-button.el-button--default.el-button--mini.el-popover__reference) {
  box-sizing: border-box;
  width: 32px;
  height: 32px;
  background: #FFFFFF;
  border: 1px solid #BBBFC4;
  border-radius: 4px;
  flex: none;
  order: 5;
  align-self: center;
  flex-grow: 0;
  margin-left: 12px;
}

:deep(button.el-button.el-button--default.el-button--mini.el-popover__reference:hover) {
  color: #783887;
  border: 1px solid #783887;
}

:deep(button.el-button.el-button--default.el-button--mini.el-popover__reference:focus) {
  color: #783887;
  border: 1px solid #783887;
}

:deep(button.el-button.el-button--default.el-button--mini.el-popover__reference svg) {
  position: relative;
  right: 6px;
  top: 1px;
  width: 14px;
  height: 14px;
}

:deep(.el-checkbox) {
  width: 238px;
  height: 32px;
  background: #FFFFFF;
  align-self: stretch;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px!important;
  color: #1F2329;
  order: 1!important;
  flex-grow: 0!important;
  margin-right: 0 !important;
}

:deep(span.el-checkbox__input) {
  width: 16px;
  height: 16px;
  margin: 8px 8px 8px 11px;
}

:deep(span.el-checkbox__inner) {
  width: 16px;
  height: 16px;
}

:deep(span.el-checkbox__inner::after) {
  left: 5px;
  position: absolute;
  top: 2px;
}

:deep(span.el-checkbox__label) {
  padding: 0px;
}

.content {
  max-height: 387px
}
</style>

<style>
.header-custom-popover {
  padding: 0px;
  margin: 0px;
}
</style>
