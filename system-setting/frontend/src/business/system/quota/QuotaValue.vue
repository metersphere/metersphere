<template>
  <span v-if="unlimited && !isShowValue" class="unlimited">{{ $t('quota.unlimited') }}</span>
  <span v-else-if="isShowValue">{{ showValue }}</span>
  <span v-else class="limited">{{ value }}</span>
</template>

<script>
export default {
  name: "QuotaValue",
  props: {
    value: [String, Number, Object],
    isShowValue: {
      type: Boolean,
      default() {
        return false;
      }
    }
  },
  computed: {
    unlimited() {
      return this.value === undefined || this.value === null || this.value === 0 || this.value === "";
    },
    showValue() {
      return this.value === undefined || this.value === null || this.value === "" ? 0 : this.value;
    }
  }
}
</script>

<style scoped>
.unlimited {
  color: #67C23A;
}

.limited {
  color: #303133;
}
</style>
