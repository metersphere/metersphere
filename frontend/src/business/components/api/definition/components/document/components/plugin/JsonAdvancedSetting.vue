<template>
  <div v-if="jsonData" style="border-width: 1px; border-style: ridge;">
    <div style="margin: 0px 10px 10px 10px">
      <el-form label-position="left" v-model="advancedValue">
        <div :span="8" v-for="(item,key) in advancedValue" :key="key">
          <el-form-item v-if="isNotEmptyValue(jsonData[key])" :label="$t('schema.'+key)+' : '" style="margin: 0">
            {{ jsonData[key] }}
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import {TYPE} from "@/business/components/common/json-schema/schema/editor/type/type";
import {isNull} from "@/business/components/common/json-schema/schema/editor/util";

export default {
  name: "JsonAdvancedSetting",
  props: {
    jsonData: Object
  },
  data() {
    return {
      advancedValue: {},
      customProps: [],
    }
  },
  computed: {
    advanced() {
      return TYPE[this.jsonData.type]
    },
    advancedAttr() {
      return TYPE[this.jsonData.type].attr
    },
    advancedNotEmptyValue() {
      const jsonNode = Object.assign({}, this.advancedValue);
      for (let key in jsonNode) {
        isNull(jsonNode[key]) && delete jsonNode[key]
      }
      return jsonNode
    },
    completeNodeValue() {
      const t = {}
      for (const item of this.customProps) {
        t[item.key] = item.value
      }
      return Object.assign({}, this.jsonData, this.advancedNotEmptyValue, t)
    }
  },
  created() {
    this.advancedValue = {};
    this.advancedValue = this.advanced.value
    for (const k in this.advancedValue) {
      this.advancedValue[k] = this.jsonData[k]
    }
  },
  methods: {
    isNotEmptyValue(value) {
      return value && value !== '';
    }
  }
}
</script>

<style scoped>

</style>
