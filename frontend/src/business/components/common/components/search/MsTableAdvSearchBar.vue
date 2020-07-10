<template>
  <span class="adv-search-bar">
    <el-link type="primary" @click="open">{{$t('commons.adv_search.title')}}</el-link>
    <el-dialog :title="$t('commons.adv_search.combine')" :visible.sync="visible" width="70%">
      <div>
        <div class="search-label">{{$t('commons.adv_search.combine')}}: </div>
        <el-select v-model="condition.logic" :placeholder="$t('commons.please_select')" size="small"
                   class="search-combine">
          <el-option :label="$t('commons.adv_search.and')" value="and"></el-option>
          <el-option :label="$t('commons.adv_search.or')" value="or"></el-option>
        </el-select>
        <div class="search-items">
          <component class="search-item" v-for="(component, index) in condition.components" :key="index"
                     :is="component.name" v-model="component.value" :component="component"/>
        </div>
      </div>
      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button @click="visible = false">{{$t('commons.cancel')}}</el-button>
          <el-button type="primary" @click="search">{{$t('commons.search')}}</el-button>
        </div>
      </template>
    </el-dialog>
  </span>
</template>

<script>
  import components from "./search-components";

  export default {
    components: {...components},
    name: "MsTableAdvSearchBar",
    props: {
      condition: Object,
    },
    data() {
      return {
        visible: false,
        operator: ""
      }
    },
    methods: {
      search() {
        let condition = {
          logic: this.condition.logic
        }
        this.condition.components.forEach(component => {
          if (component.value !== undefined && component.value !== null) {
            condition[component.key] = {
              operator: component.operator,
              value: component.value
            }
          }
        });
        this.$emit('search', condition);
      },
      open() {
        this.visible = true;
      }
    },
    created() {
      if (this.condition.logic === undefined) {
        this.condition.logic = 'and';
      }
    }
  }
</script>

<style scoped>
  .adv-search-bar {
    margin-left: 5px;
  }

  .dialog-footer {
    text-align: center;
  }

  .search-label {
    display: inline-block;
    width: 80px;
    box-sizing: border-box;
    padding-left: 5px;
  }

  .search-combine {
    width: 160px;
  }

  .search-items {
    width: 100%;
  }

  .search-item {
    display: inline-block;
    width: 50%;
    max-width: 50%;
    margin-top: 10px;
  }
</style>
