<template>
  <el-select filterable v-model="data[prop]">
    <el-option
      :disabled="disabled"
      v-for="(item, index) in templateFilterOptions"
      :key="index"
      :label="item.name"
      :value="item.id">
    </el-option>
  </el-select>
</template>

<script>
import {getCurrentWorkspaceId} from "@/common/js/utils";
import {LOCAL} from "@/common/js/constants";

export default {
  name: "TemplateSelect",
  props: {
    scene: String,
    prop: String,
    platform: String,
    disabled: Boolean,
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
      templateOptions: [],
      templateFilterOptions: [],
    };
  },
  mounted() {
    this.getTemplateOptions();
  },
  watch: {
    platform() {
      this.filter();
    }
  },
  methods: {
    getTemplateOptions() {
      let url = 'field/template/case/option/';
      if (this.scene === 'ISSUE') {
        url = 'field/template/issue/option/';
      }
      this.$get(url + getCurrentWorkspaceId(), (response) => {
        this.templateOptions = response.data;
        this.templateFilterOptions = this.templateOptions;
        if (!this.data[this.prop]) {
          for (let item of this.templateOptions) {
            if (this.scene !== 'ISSUE') {
              if (item.system) {
                this.$set(this.data, this.prop, item.id);
                break;
              }
            } else {
              if (item.system && item.platform === LOCAL && item.name === 'default') {
                this.$set(this.data, this.prop, item.id);
                break;
              }
            }

          }
        }
        this.filter();
      });
    },
    filter() {
      if (this.platform) {
        let hasTemplate = false;
        this.templateFilterOptions = [];
        this.templateOptions.forEach(i => {
          if (i.platform === this.platform) {
            this.templateFilterOptions.push(i);
            if (i.id === this.data[this.prop])
              hasTemplate = true;
          }
        });
        if (!hasTemplate)
          this.data[this.prop] = null;
      } else {
        this.templateFilterOptions = this.templateOptions;
      }
    }
  }
};
</script>

<style scoped>

</style>
