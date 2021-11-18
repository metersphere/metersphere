<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col>
        <el-select v-model="document.type" size="small">
          <el-option label="JSON" value="JSON"/>
          <el-option label="XML" value="XML"/>
        </el-select>
      </el-col>
      <el-col class="assertion-btn">
        <el-button :disabled="isReadOnly" type="primary" size="small" @click="add">
          {{ $t('api_test.request.assertions.add') }}
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>

import {AssertionDocument} from "@/business/components/api/definition/model/ApiTestModel";

export default {
  name: "DocumentHeader",

  props: {
    value: [Number, String],
    document: {},
    edit: Boolean,
    callback: Function,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },

  methods: {
    add() {
      let obj = new AssertionDocument({id: "root"});
      if (this.document.type === "JSON" && this.document.data.json.length === 0) {
        this.document.data.json.push(obj);
      }
      if (this.document.type === "XML" && this.document.data.xml.length === 0) {
        this.document.data.xml.push(obj);
      }
      this.callback();
    },
    remove() {
    },
    change(value) {

    },
    input(value) {

    },
    validate() {

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
