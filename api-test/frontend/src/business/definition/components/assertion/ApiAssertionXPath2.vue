<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col>
        <el-input :disabled="isReadOnly" v-model="xPath2.expression" maxlength="500" size="small" show-word-limit
                  :placeholder="$t('api_test.request.extract.xpath_expression')"/>
      </el-col>
      <el-col class="assertion-btn">
        <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top" v-if="edit">
          <el-switch v-model="xPath2.enable" class="enable-switch" size="mini" :disabled="isReadOnly" style="width: 30px;margin-right:10px"/>
        </el-tooltip>
        <el-button :disabled="isReadOnly" type="danger" size="mini" icon="el-icon-delete" circle @click="remove" v-if="edit"/>
        <el-button :disabled="isReadOnly" type="primary" size="mini" @click="add" v-else>
          {{ $t('api_test.request.assertions.add') }}
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {XPath2} from "../../model/ApiTestModel";

export default {
  name: "MsApiAssertionXPath2",

  props: {
    xPath2: {
      default: () => {
        return new XPath2();
      }
    },
    edit: {
      type: Boolean,
      default: false
    },
    index: Number,
    list: Array,
    callback: Function,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },

  methods: {
    add: function () {
      this.list.push(this.getXPath2());
      this.callback();
    },
    remove: function () {
      this.list.splice(this.index, 1);
    },
    getXPath2() {
      return new XPath2(this.xPath2);
    },
  }
}
</script>

<style scoped>
.assertion-btn {
  text-align: center;
  width: 80px;
}
</style>
