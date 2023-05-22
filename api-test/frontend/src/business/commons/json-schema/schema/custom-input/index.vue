<template>
  <div class="custom-input">
    <input
      :class="{'c-input-inner': true, 'is-disabled': disabled }"
      :placeholder="placeholder"
      :type="type"
      :disabled="disabled"
      :value="value"
      @input="handleInput" />
  </div>
</template>
<script>
export default {
  name: 'CustomInput',
  props: {
    placeholder: {
      type: String,
      default: '',
    },
    type: {
      type: String,
      default: '',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    value: {
      type: String,
      default: '',
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
.custom-input{
  .c-input-inner{
    height: 32px;
    line-height: 32px;
    background-color: #FFF;
    background-image: none;
    border-radius: 4px;
    border: 1px solid #DCDFE6;
    box-sizing: border-box;
    color: #606266;
    display: inline-block;
    font-size: inherit;
    outline: 0;
    padding: 0 15px;
    transition: border-color .2s cubic-bezier(.645,.045,.355,1);
    width: 100%;
    font-size: 13px;
  }
  .c-input-inner::placeholder{
    color: #C0C4CC;
  }
  .is-disabled {
    cursor: not-allowed;
    background-color: #FBFBFB !important;
    color: #606266 !important;
  }
}

</style>
