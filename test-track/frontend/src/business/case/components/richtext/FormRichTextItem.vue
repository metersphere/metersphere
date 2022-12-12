<template>
  <el-form-item
    :disable="true"
    :label="title"
    :prop="prop"
    :label-width="labelWidth"
  >
    <div slot="label" class="required-item" v-if="slotTitleRequired">
      {{ slotTitleRequired }}
    </div>
    <ms-mark-down-text
      :prop="prop"
      :data="data"
      :placeholder="placeholder"
      :disabled="disabled"
      :default-open="defaultOpen"
    />
  </el-form-item>
</template>

<script>
import MsMarkDownText from "./MsMarkDownText";
export default {
  name: "FormRichTextItem",
  components: { MsMarkDownText },
  props: {
    slotTitleRequired: String,
    data: Object,
    prop: String,
    disabled: Boolean,
    title: String,
    placeholder: String,
    defaultOpen: {
      type: String,
      default() {
        return "preview";
      },
    },
    labelWidth: [String, Number],
  },
};
</script>

<style scoped>
/* :deep(.v-note-wrapper) {
  position: initial;
} */
:deep(.v-note-op) {
  background-color: #f8f9fa !important;
  border-bottom: 1px solid #bbbfc4;
  min-height: 34px !important;
  height: 34px !important;
}
:deep(.v-note-wrapper) {
  box-sizing: border-box;
  border: 1px solid #bbbfc4 !important;
  border-radius: 4px;
  box-shadow: none !important;
}
:deep(.v-note-show) {
  min-height: 64px;
}
:deep(.v-left-item) {
  flex: none !important;
  min-height: 34px !important;
}
:deep(.v-right-item) {
  text-align: left !important;
  min-height: 34px !important;
}
:deep(.op-icon) {
  padding: 0px !important;
}
.required-item:after {
  content: "*";
  color: #f54a45;
  margin-left: px2rem(4);
  width: px2rem(8);
  height: 32px;
  font-weight: 400;
  font-size: 14px;
  line-height: 32px;
}
</style>
