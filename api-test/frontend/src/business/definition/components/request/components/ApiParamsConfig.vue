<template>
  <el-popover placement="top" width="100" v-model="isActive">
    <div v-for="item in apiParamsConfigFields" :key="item.value">
      <el-checkbox-group v-model="checkList">
        <el-checkbox :label="item.value">{{ item.text }}</el-checkbox>
      </el-checkbox-group>
    </div>
    <div style="text-align: right; margin: 0">
      <el-button size="mini" type="text" @click="cancel">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" size="mini" @click="confirm">{{ $t('commons.confirm') }}</el-button>
    </div>
    <i slot="reference" class="el-icon-setting" />
  </el-popover>
</template>

<script>
import { getShowFields } from 'metersphere-frontend/src/utils/custom_field';

export default {
  name: 'ApiParamsConfig',
  data() {
    return {
      isActive: false,
      checkList: [],
    };
  },
  props: {
    apiParamsConfigFields: Array,
    showColumns: Array,
    storageKey: {
      type: String,
      default() {
        return 'API_PARAMS_SHOW_FIELD';
      },
    },
  },
  watch: {
    isActive() {
      if (this.isActive) {
        this.checkList = this.getCheckList(this.storageKey);
      }
    },
  },
  methods: {
    cancel() {
      this.isActive = false;
    },
    confirm() {
      let apiParamsShowFields = JSON.stringify(this.checkList);
      localStorage.setItem(this.storageKey, apiParamsShowFields);
      this.$nextTick(() => {
        this.$emit('refresh');
        this.isActive = false;
      });
    },
    getCheckList(fieldKey) {
      return getShowFields(fieldKey);
    },
  },
};
</script>

<style scoped></style>
