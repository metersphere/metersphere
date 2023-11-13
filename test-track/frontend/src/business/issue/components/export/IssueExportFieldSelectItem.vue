<template>
  <div>
    <div class="field-title" v-if="fields && fields.length > 0">
      <span>{{ title }}</span>
      <el-checkbox
        v-model="selectAll"
        @change="selectAllChange"/>
    </div>
    <issue-export-field-list
      :fields="fields"
      @enableChange="enableChange"
    />
  </div>
</template>

<script>

import IssueExportFieldList from "@/business/issue/components/export/IssueExportFieldList";

export default {
  name: "IssueExportFieldSelectItem",
  components: {IssueExportFieldList},
  props: {
    fields: Array,
    title: String,
    type: String
  },
  data() {
    return {
      selectAll: false,
    }
  },
  watch: {
    selectAll() {
      this.$emit('selectAllChange', this.selectAll);
    },
    fields() {
      this.checkEnable();
    }
  },
  created() {
    this.checkEnable();
  },
  methods: {
    getExportParam() {
      return this.fields.filter(item => item.enable);
    },
    enableChange(enable) {
     this.persistenceValues();
      if (enable) {
        for (let head of this.fields) {
          if (!head.enable) {
            // 单个启用，如果有未启用的则返回
            return;
          }
        }
      }
      // 如果全部都开启了就启用总开关，或者一个关闭了则禁用总开关
      this.selectAll = enable;
    },
    persistenceValues() {
      // 将勾选情况保存在浏览器
      let enableKeys = this.fields.filter(i => i.enable)
        .map(i => i.key);
      localStorage.setItem(this.type, JSON.stringify(enableKeys));
    },
    selectAllChange(value) {
      this.selectAll = value;
      this.fields.forEach(i => {
        if (!i.disabled) {
          i.enable = value;
        }
      });
      this.persistenceValues();
    },
    checkEnable() {
      // 获取保存在浏览器的选项值
      let enableKeys = localStorage.getItem(this.type);
      if (enableKeys) {
        enableKeys = JSON.parse(enableKeys);
      }

      let isSelectAll = true;
      for (let field of this.fields) {
        if (field.id === 'id' || field.id === 'title') {
          // 缺陷的ID, 标题默认必须勾选
          field.enable = true;
        } else {
          if (enableKeys) {
            field.enable = enableKeys.indexOf(field.key) > -1;
          } else {
            field.enable = false;
          }
        }
        if (!field.enable) {
          isSelectAll = false;
        }
      }

      this.selectAll = isSelectAll;
    }
  }
}
</script>

<style scoped>
.field-title {
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 15px;
  font-weight: bold;
}

.field-title span:first-child {
  margin-right: 10px;
}
</style>
