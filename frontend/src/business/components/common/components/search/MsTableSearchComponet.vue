<template>
  <div>
    <el-select class="search-label" v-model="selectKey" v-if="!custom"
               filterable placeholder="请选择" size="small">
      <el-option
        v-for="item in components"
        :disabled="item.disable !== undefined ? item.disable : true"
        :key="item.key"
        :label="$t(item.label)"
        :value="item.key">
        <span style="float: left">{{ $t(item.label) }}</span>
      </el-option>
    </el-select>
    <el-select class="search-label" v-model="selectKey" v-else
               filterable placeholder="请选择" size="small">
      <el-option-group
        v-for="group in components"
        :key="group.key"
        :label="$t(group.label)">
        <el-option
          v-for="item in group.child"
          :key="item.key"
          :label="$t(item.label)"
          :disabled="item.disable !== undefined ? item.disable : true"
          :value="item.key">
          <span style="float: left">{{ $t(item.label) }}</span>
        </el-option>
      </el-option-group>
    </el-select>

    <el-select class="search-operator" v-model="component.operator.value" :placeholder="$t('commons.please_select')"
               size="small"
               @change="change" @input="input" v-bind="component.operator.props">
      <el-option v-for="o in operators" :key="o.value" :label="$t(o.label)" :value="o.value"/>
    </el-select>

    <div class="search-content" v-if="showContent">
      <slot v-bind:component="component"></slot>
    </div>
  </div>
</template>

<script>
import {concat} from "lodash";
import {_findByKey} from "@/business/components/common/components/search/custom-component";

export default {
  name: "MsTableSearchComponent",
  props: ['component', 'components', 'custom'],
  data() {
    return {
      operators: this.component.operator.options || [],
      selectKey: this.component.key
    }
  },
  watch: {
    selectKey(newVal, oldVal) {
      this.componentTypeChange(newVal, oldVal);
    }
  },
  methods: {
    componentTypeChange(newVal, oldVal) {
      let components = undefined;
      if (!this.custom) {
        components = this.enableOrDisableOptional(newVal, oldVal, this.components)
      } else {
        const array = concat(this.components[0].child, this.components[1].child);
        components = this.enableOrDisableOptional(newVal, oldVal, array);
      }
      this.$emit("update:components", this.components);
      this.$emit("updateKey", ...components);
    },
    enableOrDisableOptional(newVal, oldVal, options) {
      // 切换选项后，修改旧选项为可用，新选项为禁用
      const oldComponent = _findByKey(options, oldVal);
      if (oldVal) {
        if (oldComponent) {
          this.$set(oldComponent, 'disable', false);
        }
      }
      const newComponent = _findByKey(options, newVal);
      if (newVal || newComponent) {
        this.$set(newComponent, 'disable', true);
      }
      return [newComponent, oldComponent];
    },
    change(value) {
      if (this.component.operator.change) {
        this.component.operator.change(this.component, value)
      }
      this.$emit('change', value);
    },
    input(value) {
      this.$emit('input', value);
    }
  },
  computed: {
    showContent() {
      if (this.component.isShow) {
        return this.component.isShow(this.component.operator.value);
      }
      return true;
    }
  }
}
</script>

<style scoped>
.search-label {
  display: inline-block;
  width: 125px;
  box-sizing: border-box;
  padding-left: 5px;
  margin-right: 8px;
}

.search-operator {
  display: inline-block;
  width: 125px;
}

.search-content {
  display: inline-block;
  padding: 0 5px 0 8px;
  width: calc(100% - 260px);
  box-sizing: border-box;
}
</style>
