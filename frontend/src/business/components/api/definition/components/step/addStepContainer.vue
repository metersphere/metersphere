<template>
  <div>
    <p>
      <el-select v-model="operateModel" size="mini" class="ms-select-step">
        <el-option
          v-for="item in operates"
          :key="item.id"
          :label="item.name"
          :value="item.id">
        </el-option>
      </el-select>

      <el-button
        size="mini"
        type="primary"
        @click="add">
        {{ $t('api_test.request.assertions.add') }}
      </el-button>
    </p>

    <slot></slot>

  </div>
</template>

<script>

export default {
  name: "addStepContainer",
  components: {
  },
  props: {
    showButton: {
      type: Boolean,
      default: true,
    },
    operates: Array,
    operate: String
  },
  data() {
    return {
      operateModel: ''
    }
  },
  created() {
    this.operateModel = this.operate;
  },
  watch: {
    operateModel() {
      this.$emit('update:operate', this.operateModel);
    }
  },
  methods: {
    add() {
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
