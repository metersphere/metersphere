<template>
  <div>
    <div class="search-label" v-if="component.label">{{component.label}}:</div>
    <div class="search-label" v-else>{{$t(component.labelI18n)}}:</div>

    <el-select class="search-operator" v-model="operator" :placeholder="$t('commons.please_select')" size="small"
               @change="change" @input="input">
      <el-option v-for="op in operators" :key="op.value" :label="$t(op.label)" :value="op.value"/>
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
        operators: this.component.operators || [],
        operator: (() => {
          if (this.component.operator === undefined && this.component.operators.length > 0) {
            this.$emit('input', this.component.operators[0].value);
            return this.component.operators[0].value;
          } else {
            this.component.operator
          }
        })()
      }
    },
    methods: {
      change(value) {
        this.$emit('change', value);
      },
      input(value) {
        this.$emit('input', value);
      }
    },
    computed: {
      showContent() {
        return operator => {
          if (this.component.showContent) {
            return this.component.showContent(operator);
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
