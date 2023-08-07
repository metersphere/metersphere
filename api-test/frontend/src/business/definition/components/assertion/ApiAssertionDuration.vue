<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col>
        <el-input
          :disabled="(isReadOnly && !duration.label) || caseEnable"
          :value="value"
          v-bind="$attrs"
          step="100"
          size="small"
          type="number"
          @change="change"
          @input="input"
          :min="1"
          :placeholder="$t('api_test.request.assertions.response_in_time')"
          onKeypress="return (/[\d]/.test(String.fromCharCode(event.keyCode)))" />
      </el-col>
      <el-col class="assertion-btn">
        <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top" v-if="edit">
          <el-switch
            v-model="duration.enable"
            class="enable-switch"
            size="mini"
            :disabled="(isReadOnly && !duration.label) || caseEnable"
            style="width: 30px; margin-right: 10px" />
        </el-tooltip>
        <el-button
          :disabled="(isReadOnly && !duration.label) || caseEnable"
          type="danger"
          size="mini"
          icon="el-icon-delete"
          circle
          @click="remove"
          v-if="edit" />
        <el-button :disabled="(isReadOnly && !duration.label) || caseEnable" type="primary" size="mini" @click="add" v-else>
          {{ $t('api_test.request.assertions.add') }}
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'MsApiAssertionDuration',

  props: {
    value: [Number, String],
    duration: {},
    edit: Boolean,
    callback: Function,
    isReadOnly: {
      type: Boolean,
      default: false,
    },
    caseEnable: {
      type: Boolean,
      default: false,
    },
  },

  created() {
    if (this.duration && !this.duration.valid && this.duration.value === 0 && this.isReadOnly) {
      this.duration.label = "SCENARIO-REF-STEP";
    }
  },
  methods: {
    add() {
      if (this.validate()) {
        this.duration.value = this.value;
        this.$set(this.duration, 'enable', true);
        this.callback();
      }
    },
    remove() {
      this.duration.value = undefined;
    },
    change(value) {
      if (this.validate()) {
        this.$emit('change', value);
      }
    },
    input(value) {
      if (value === '' || Number(value) <= 0) {
        this.$warning(this.$t('commons.response_time_warning'));
        if (this.duration.value) {
          this.value = this.duration.value;
        }
        return;
      }
      this.$emit('input', value);
    },
    validate() {
      if (this.value === '' || Number(this.value) <= 0) {
        this.$warning(this.$t('commons.response_time_warning'));
        return false;
      }
      return true;
    },
  },
};
</script>

<style scoped>
.assertion-btn {
  text-align: center;
  width: 120px;
}
</style>
