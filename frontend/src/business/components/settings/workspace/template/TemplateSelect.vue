<template>
  <el-select filterable v-model="data[prop]">
    <el-option
      v-for="(item, index) in templateOptions"
      :key="index"
      :label="item.name"
      :value="item.id">
    </el-option>
  </el-select>
</template>

<script>
import {getCurrentWorkspaceId} from "@/common/js/utils";

export default {
  name: "TemplateSelect",
  props: {
    scene: String,
    prop: String,
    data: {
      type: Object,
      default() {
        return {
          caseTemplateId: '',
          issueTemplateId: ''
        };
      }
    }
  },
  data() {
    return {
      templateOptions: []
    };
  },
  mounted() {
    this.getTemplateOptions();
  },
  methods: {
    getTemplateOptions() {
      let url = 'field/template/case/option/';
      if (this.scene === 'ISSUE') {
        url = 'field/template/issue/option/';
      }
      this.$get(url + getCurrentWorkspaceId(), (response) => {
        this.templateOptions = response.data;
        if (!this.data[this.prop]) {
          for(let item of this.templateOptions) {
            if (item.system) {
              this.$set(this.data, this.prop, item.id);
              break;
            }
          }
        }
      });
    }
  }
};
</script>

<style scoped>

</style>
