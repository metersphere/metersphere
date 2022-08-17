<template>
  <div v-if="advancedValue" style="border-width: 1px; border-style: ridge;">
    <div style="margin: 0px 10px 10px 10px">
      <el-form label-position="left" v-model="advancedValue">
        <div :span="8" v-for="(item, key) in advancedValue" :key="key">
          <el-form-item :label="$t(key) + ' : '" style="margin: 0">
            <span style="display: inline-block;overflow-wrap: break-word;text-align: left;max-width: 100%;">
              {{ advancedValue[key] }}
            </span>
          </el-form-item>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "TableAdvancedSetting",
  props: {
    tableData: Object
  },
  data() {
    return {
      advancedValue: null,
      customProps: [],
    }
  },
  created() {
    this.initAdvancedValue();
  },
  methods: {
    isNotEmptyValue(value) {
      return value && value !== '';
    },
    initAdvancedValue() {
      if (this.tableData) {
        this.advancedValue = {};
        if (this.isNotEmptyValue(this.tableData["min"])) {
          this.advancedValue["schema.minLength"] = this.tableData["min"];
        }
        if (this.isNotEmptyValue(this.tableData["max"])) {
          this.advancedValue["schema.maxLength"] = this.tableData["max"];
        }
        if (this.tableData["urlEncode"]) {
          this.advancedValue["commons.encode"] = this.$t("commons.yes");
        }
        if (this.isNotEmptyValue(this.tableData["description"])) {
          this.advancedValue["commons.description"] = this.tableData["description"];
        }
        if (JSON.stringify(this.advancedValue) === "{}") {
          this.advancedValue = null;
        }
      } else {
        this.advancedValue = null;
      }
    }
  }
}
</script>

<style scoped>

</style>
