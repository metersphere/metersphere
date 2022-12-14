<template>
  <div class="form-section">
    <div>
      <div class="title">{{ title }}</div>

      <el-tooltip class="item" effect="dark" :content="content" placement="top-start">
      <span
        :class="{ 'el-icon-arrow-left pointer' : !active, 'el-icon-arrow-down pointer' : active}"
        @click="active=!active"></span>
      </el-tooltip>
      <div v-show="active">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "FormSection",
  props: {
    title: {
      type: String,
      default: ""
    },
    initActive: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      active: false,
      content: this.$t('api_test.definition.document.open')
    }
  },
  watch: {
    initActive: {
      handler(val) {
        this.active = val;
      },
      immediate: true
    },
    active: {
      handler(val) {
        if (val) {
          this.content = this.$t('api_test.definition.document.close');
        } else {
          this.content = this.$t('api_test.definition.document.open');
        }
      },
      immediate: true
    },

  }
}
</script>

<style scoped>
.form-section {
  padding-top: 10px;
}

.title {
  margin-right: 15px;
  display: inline-block;
}

.pointer {
  cursor: pointer;
}
</style>
