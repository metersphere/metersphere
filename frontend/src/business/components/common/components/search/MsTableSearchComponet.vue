<template>
  <div>
    <div class="search-label">{{component.label}}:</div>

    <el-select class="search-operator" v-model="operator" :placeholder="$t('commons.please_select')" size="small"
               @change="change" @input="input" v-bind="component.operator.props">
      <el-option v-for="o in operators" :key="o.value" :label="o.label" :value="o.value"/>
    </el-select>

    <div class="search-content" v-if="showContent(operator)">
      <slot v-bind:component="component"></slot>
    </div>
  </div>
</template>

<script>
  export default {
    name: "MsTableSearchComponent",
    props: ['component'],
    data() {
      return {
        operators: this.component.operator.options || [],
        operator: (() => {
          if (this.component.operator.value === undefined && this.component.operator.options.length > 0) {
            this.$emit('input', this.component.operator.options[0].value);
            return this.component.operator.options[0].value;
          } else {
            return this.component.operator.value
          }
        })()
      }
    },
    methods: {
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
        return operator => {
          if (this.component.isShow) {
            return this.component.isShow(operator);
          }
          return true;
        }
      }
    }
  }
</script>

<style scoped>
  .search-label {
    display: inline-block;
    width: 80px;
    box-sizing: border-box;
    padding-left: 5px;
  }

  .search-operator {
    display: inline-block;
    width: 160px;
  }

  .search-content {
    display: inline-block;
    margin: 0 5px 0 10px;
    width: calc(100% - 255px);
  }
</style>
