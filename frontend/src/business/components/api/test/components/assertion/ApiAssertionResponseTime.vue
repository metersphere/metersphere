<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col>
        <el-input :value="value" v-bind="$attrs" step="100" size="small" type="number" @change="change" @input="input"
                  :placeholder="$t('api_test.request.assertions.response_in_time')"/>
      </el-col>
      <el-col class="assertion-btn">
        <el-button type="danger" size="mini" icon="el-icon-delete" circle @click="remove" v-if="edit"/>
        <el-button type="primary" size="small" icon="el-icon-plus" plain @click="add" v-else/>
      </el-col>
    </el-row>
  </div>
</template>

<script>

  import {ResponseTime} from "../../model/ScenarioModel";

  export default {
    name: "MsApiAssertionResponseTime",

    props: {
      duration: ResponseTime,
      value: [Number, String],
      edit: Boolean,
      callback: Function
    },

    methods: {
      add() {
        this.duration.value = this.value;
        this.callback();
      },
      remove() {
        this.duration.value = undefined;
      },
      change(value) {
        this.$emit('change', value);
      },
      input(value) {
        this.$emit('input', value);
      }
    }
  }
</script>

<style scoped>
  .assertion-btn {
    text-align: center;
    width: 60px;
  }
</style>
