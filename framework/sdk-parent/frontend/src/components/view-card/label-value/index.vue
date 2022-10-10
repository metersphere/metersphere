<template>
  <div :class="['label-value', 'label-value--' + computeSize]">
    <label :style="{width: width}" v-if="label">{{ label }}</label>
    <div>
      <slot></slot>
    </div>
  </div>
</template>

<script>
import ElSize from "../../../mixins/el-size";

export default {
  name: "LabelValue",
  mixins: [ElSize],
  props: {
    label: String,
    value: String,
    labelWidth: String,
    size: {
      type: String,
      validator: value => {
        ["medium", "small", "mini"].includes(value)
      }
    }
  },
  inject: ["itemLabelWidth"],
  computed: {
    width({itemLabelWidth, labelWidth}) {
      return labelWidth || itemLabelWidth
    }
  }
}
</script>

<style lang="scss">
.label-value {
  display: flex;
  justify-content: flex-start;
  line-height: 2;

  .label-value--medium {
    font-size: 14px;
  }

  .label-value--small {
    font-size: 13px;
  }

  .label-value--mini {
    font-size: 12px;
  }

  label {
    flex: 0 0 auto;
    text-align: left;
    font-weight: 700;
    margin-right: 20px;
  }
}
</style>
