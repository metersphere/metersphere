<template>
  <div class="custom-textarea">
    <textarea
      :value="value"
      :class="{ 'c-content-inner': true, 'is-disabled': disabled }"
      :placeholder="placeholder"
      :disabled="disabled"
      @input="handleInput"
      :rows="rows"
      :cols="cols"
    />
  </div>
</template>
<script>
export default {
  name: 'CustomTextarea',
  props: {
    value: {
      type: String,
      default: '',
    },
    placeholder: {
      type: String,
      default: '',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    rows: {
      type: Number,
      default: 2,
    },
    cols: {
      type: Number,
      default: 100,
    },
  },
  methods: {
    handleInput(e) {
      this.$emit('input', e.target.value);
    },
  },
};
</script>
<style lang="scss" scoped>
.custom-textarea {
  .c-content-inner {
    display: block;
    height: 32px;
    width: 100%;
    line-height: 32px;
    background-color: #fff;
    background-image: none;
    border-radius: 4px;
    border: 1px solid #dcdfe6;
    box-sizing: border-box;
    color: #606266;
    display: inline-block;
    outline: 0;
    padding: 0 15px;
    transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
    font-size: 13px;
    resize: vertical;
  }
  .c-content-inner::placeholder{
    color: #C0C4CC;
  }
  .is-disabled {
    background-color: #f5f7fa;
    color: #c0c4cc;
    cursor: not-allowed;
  }
}
</style>
