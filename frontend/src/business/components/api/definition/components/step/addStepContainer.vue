<template>
  <div>
    <p>
      <el-select v-model="operateModel" size="small" class="ms-select-step" :disabled="isReadonly">
        <el-option
          v-for="item in operates"
          :key="item.id"
          :label="item.name"
          :value="item.id">
        </el-option>
      </el-select>

      <el-select v-model="operateSubModel" size="small" class="ms-select-step" v-if="subOperates" :disabled="isReadonly">
        <el-option
          v-for="item in subOperates"
          :key="item.value"
          :label="item.label"
          :value="item.value">
        </el-option>
      </el-select>

      <el-button
        :disabled="isReadonly"
        size="small"
        type="primary"
        @click="add">
        {{ $t('api_test.request.assertions.add') }}
      </el-button>
    </p>

    <slot></slot>

  </div>
</template>

<script>

const requireComponent = require.context('@/business/components/xpack/', true, /\.js$/);
const CMD_CONSTANTS = requireComponent.keys().length > 0 ? requireComponent("./ui/definition/command/cmd-constants-utils.js") : {};

export default {
  name: "addStepContainer",
  components: {},
  props: {
    isReadonly:{
      type: Boolean,
      default: false,
    },
    showButton: {
      type: Boolean,
      default: true,
    },
    operates: Array,
    operate: String
  },
  data() {
    return {
      operateModel: '',
      //数据提取或者断言的子分类模型
      operateSubModel: '',
      subOperates: null,
    }
  },
  created() {
    this.operateModel = this.operate;
  },
  watch: {
    operateModel() {
      this.$emit('update:operate', this.operateModel);
      this.operateSubModel = null;
      if (this.operateModel == "cmdExtraction") {
        this.subOperates = null;
      } else if (this.operateModel == "cmdValidation") {
        this.subOperates = null;
      } else if (this.operateModel == "cmdExtractElement") {
        this.subOperates = CMD_CONSTANTS.EXTRACT_ELEMENT_OPTIONS
      } else if (this.operateModel == "cmdExtractWindow") {
        this.subOperates = CMD_CONSTANTS.EXTRACT_WINDOW_OPTIONS;
      }
    },
    operateSubModel() {
      if (this.operateSubModel) {
        this.$emit('update:operate', this.operateSubModel);
      }
    }
  },
  methods: {
    add() {
      if (this.subOperates && this.subOperates.length && !this.operateSubModel) {
        this.$message({
          message: this.$t('ui.check_subitem'),
          type: 'error'
        });
        return;
      }
      this.$emit('add');
    }
  }
}
</script>

<style scoped>
.ms-select-step {
  margin-left: 10px;
  margin-right: 10px;
  width: 200px;
}
</style>
