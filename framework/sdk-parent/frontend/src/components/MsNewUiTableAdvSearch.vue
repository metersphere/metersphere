<template>
  <span class="adv-search-bar">
    <el-button size="mini" @click="open" ref="filter-btn">
      <svg-icon :icon-class="conditionNum == 0 ? 'icon-filter' : 'icon-filter-actived'"/>
      <span class="condition-num">{{conditionNum == 0 ? "" : "(" +  conditionNum  + ")" }}</span>
    </el-button>
    <el-dialog :title="$t('commons.adv_search.new_title')" :visible.sync="visible"
               custom-class="adv-dialog" :append-to-body="true" width="60%">

      <div class="search-items"  style="height: 300px">
        <el-scrollbar style="height: 100%" ref="scrollbar">
            <div class="search-item" v-for="(component) in optional.components" :key="component.key">
              <el-row>
                <el-col :span="23">
                  <component :is="component.name" :component="component" :components.sync="config.components"
                             @updateKey="changeSearchItemKey" :custom="condition.custom"/>
                </el-col>
                <el-col :span="1">
                  <i class="el-icon-delete delete-icon" @click="remove(component)" v-if="optional.components.length !==1"></i>
                </el-col>
              </el-row>
            </div>
        </el-scrollbar>
        <el-link type="primary" icon="el-icon-plus" v-if="showAddFilterLink" :underline="false"
                 class="add-filter-link" @click="addFilter">{{ $t('commons.adv_search.add_filter_link') }}</el-link>
      </div>
      <template v-slot:footer>
        <div class="dialog-footer">
          <el-button size="small" @click="reset">{{ $t('commons.adv_search.reset') }}</el-button>
          <el-button size="small" type="primary" @click="search">{{ $t('commons.adv_search.search') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </span>
</template>

<script>
import components from "./search/search-components";
import {cloneDeep, concat, slice} from "lodash-es";
import {_findByKey, _findIndexByKey} from "./search/custom-component";

export default {
  components: {...components},
  name: "MsNewUiTableAdvSearch",
  props: {
    condition: Object,
    showLink: {
      type: Boolean,
      default: true,
    },
    showItemSize: {
      type: Number,
      default() {
        return 6; // 默认展示的搜索条件数量
      }
    }
  },
  data() {
    return {
      visible: false,
      config: {
        components: []
      },
      optional: {
        components: []
      },
      showAddFilterLink: true,
      nullFilterKey: '',
      isInit: false,
      conditionNum: 0,
    }
  },
  updated() {
    this.setScrollToBottom();
  },
  methods: {
    doInit(handleCustom) {
      let config = cloneDeep(this.condition);
      config.components.forEach(component => {
        let operator = component.operator.value;
        component.operator.value = operator === undefined ? component.operator.options[0].value : operator;
      })
      if (!handleCustom) {
        return config;
      }
      if (this.condition.custom) {
        let components = [];
        this.systemFiled = config.components.filter(co => co.custom === undefined || co.custom === false);
        this.customFiled = config.components.filter(co => co.custom === true);
        // 选项分组
        this.$set(components, 0, {label: this.$t('custom_field.system_field'), child: this.systemFiled});
        this.$set(components, 1, {label: this.$t('custom_field.name'), child: this.customFiled});
        this.$set(config, "components", components);
      }
      return config;
    },
    search() {
      this.conditionNum = 0
      let condition = {}
      this.optional.components.forEach(component => {
        let value = component.value;
        if (Array.isArray(value)) {
          if (value.length > 0) {
            this.setCondition(condition, component);
            this.conditionNum += 1;
          }
        } else {
          if (value !== undefined && value !== null && value !== "") {
            this.setCondition(condition, component);
            this.conditionNum += 1;
          }
        }
      });

      if (this.conditionNum > 0) {
        this.$refs["filter-btn"].$el.focus();
        this.$refs["filter-btn"].$el.style.width = 'auto';
      } else {
        this.$refs["filter-btn"].$el.blur();
        this.$refs["filter-btn"].$el.style.width = '32px';
      }

      // 清除name
      if (this.condition.name) {
        this.condition.name = undefined;
      }
      // 添加组合条件
      this.condition.combine = condition;
      this.$emit('update:condition', this.condition);
      this.$emit('search', condition);
      this.visible = false;
    },
    setCondition(condition, component) {
      // 某些字段储存在自定义表但是其 custom 的值是 false
      // 因为需求要把这些字段在有选项分类时归为 系统字段 ？
      if (component.custom || ['严重程度', '处理人', '状态', '用例状态', '责任人', '用例等级'].indexOf(component.label) > -1) {
        this.handleCustomField(condition, component);
        return;
      }
      condition[component.key] = {
        operator: component.operator.value,
        value: component.value
      };
    },
    handleCustomField(condition, component) {
      if (!condition.customs) {
        condition['customs'] = [];
      }
      let value = component.value;
      if (component.label === '用例状态' && value.length === 1 && value.indexOf('Trash') > -1) {
        return;
      }
      if (component.type === "multipleMember" || component.type === "checkbox" || component.type === "multipleSelect") {
        try {
          value = JSON.stringify(component.value);
        } catch (e) {
          // nothing
        }
      }
      condition['customs'].push({
        id: component.key,
        operator: component.operator.value,
        value: value,
        type: component.type
      });
    },
    reset() {
      this.conditionNum = 0;
      this.$refs["filter-btn"].$el.blur();
      this.$refs["filter-btn"].$el.style.width = '32px';
      let source = this.condition.components;
      this.optional.components.forEach((component, index) => {
        if (component.operator.value !== undefined) {
          let operator = _findByKey(source, component.key).operator.value;
          component.operator.value = operator === undefined ? component.operator.options[0].value : operator;
        }
        if (component.value !== undefined) {
          component.value = source[index].value;
        }
        if (component.reset && component.reset instanceof Function) {
          component.reset();
        }
      })
      this.condition.combine = undefined;
      this.$emit('update:condition', this.condition);
      this.$emit('search');
    },
    init() {
      this.config = this.doInit(true);
      this.optional = this.doInit();
      if (this.optional.components.length && this.optional.components.length <= this.showItemSize) {
        this.showAddFilterLink = false;
      }
      // 默认显示几个搜索条件
      this.optional.components = slice(this.optional.components, 0, this.showItemSize);
      let allComponent = this.condition.custom ?
        concat(this.config.components[0].child, this.config.components[1].child) : this.config.components;
      for (let component of allComponent) {
        let co = _findByKey(this.optional.components, component.key);
        co ? this.$set(co, 'disable', true) : this.$set(component, 'disable', false);
      }
    },
    open() {
      this.visible = true;
      if (!this.isInit) {
        this.isInit = true;
        this.init();
      } else {
        this.setModulesParam();
        this.refreshComponentOption();
      }
    },
    refreshComponentOption() {
      // 当前已存在的搜索子组件中是否有需要进行刷新数据选项的
      let comps = this.optional.components.filter(cp => cp.init && cp.init instanceof Function);
      comps.forEach(comp => comp.init());
    },
    setModulesParam() {
      let comps = this.optional.components.filter(c => c.key === 'moduleIds' && c.options.type === 'POST');
      comps.forEach(comp => comp.options.params = {"projectId": this.condition.projectId});
    },
    addFilter() {
      const index = _findIndexByKey(this.optional.components, this.nullFilterKey);
      if (index > -1) {
        this.$warning(this.$t('commons.adv_search.add_filter_link_tip'));
        return;
      }
      let data = {
        key: this.nullFilterKey,
        name: 'MsTableSearchInput',
        label: '',
        operator: {
          options: []
        },
        disable: false
      };
      this.optional.components.push(data);
    },
    remove(component) {
      this.showAddFilterLink = true;
      if (!this.condition.custom) {
        this.enableOptional(component, this.config.components);
      } else {
        // 系统字段和自定义字段选项合并
        const components = concat(this.config.components[0].child, this.config.components[1].child);
        this.enableOptional(component, components);
      }
      let index = _findIndexByKey(this.optional.components, component.key);
      if (index !== -1) {
        this.optional.components.splice(index, 1);
      }
    },
    enableOptional(component, components) {
      let data = _findByKey(components, component.key);
      if (data) {
        this.$set(data, 'disable', false);
      }
    },
    // 搜索组件的字段变换时触发
    changeSearchItemKey(newData, oldData) {
      let key = oldData ? oldData.key : this.nullFilterKey;
      const index = _findIndexByKey(this.optional.components, key);
      this.optional.components.splice(index, 1, newData);
      this.showAddFilterLink = false;
      let components = [];
      if (!this.condition.custom) {
        components = this.config.components;
      } else {
        components = concat(this.config.components[0].child, this.config.components[1].child);
      }
      for (let op of components) {
        if (op.disable !== undefined && op.disable === false) {
          this.showAddFilterLink = true;
          break;
        }
      }
    },
    setScrollToBottom() {
      this.$refs['scrollbar'].wrap.scrollTop = this.$refs['scrollbar'].wrap.scrollHeight;
    }
  }
  ,
  computed: {
    isAuto() {
      return this.conditionNum > 0
    }
  }
}
</script>

<style>
@media only screen and (min-width: 1870px) {
  .el-dialog.adv-dialog {
    width: 70%;
  }
}

@media only screen and (min-width: 1650px) and (max-width: 1869px) {
  .el-dialog.adv-dialog {
    width: 80%;
  }
}

@media only screen and (min-width: 1470px) and (max-width: 1649px) {
  .el-dialog.adv-dialog {
    width: 90%;
  }
}

@media only screen and (max-width: 1469px) {
  .el-dialog.adv-dialog {
    width: 70%;
    min-width: 695px;
  }
}
</style>

<style scoped>

.dialog-footer {
  text-align: right;
}

.search-items {
  width: 100%;
}

@media only screen and (max-width: 1469px) {
  .search-item {
    width: 100%;
  }
}

@media only screen and (min-width: 1470px) {
  .search-item {
    width: 50%;
  }
}

.search-item {
  display: inline-block;
}

.delete-icon {
  font-size: 17px;
  margin-top: 8px;
}

.delete-icon:hover {
  cursor: pointer;
}

.add-filter-link {
  position: relative;
  top: -10px;
  padding: 1px 0px 1px 0px;
  height: 26px;
  width: 112px;
  border-radius: 4px;
  float: left;
}

:deep(span.el-select__tags-text) {
  display: inline-block;
  max-width: 500px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(i.el-tag__close.el-icon-close) {
  position: relative;
  left: 3px;
  top: -6px;
}

:deep(.el-dialog__header) {
  padding: 24px 24px 24px 24px;
}

:deep(.el-dialog__body) {
  padding: 0px 24px 0px 24px;
}

:deep(.el-dialog__footer) {
  padding: 10px 24px 24px;
}

:deep(.el-row) {
  margin: 0px 0px 12px 0px!important;
}

:deep(.search-label) {
  padding-left: 0px;
  width: 30%;
}

:deep(.search-operator) {
  width: 15%;
}

:deep(.search-content) {
  width: 50%;
}

span.condition-num {
  width: 18px;
  height: 22px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  /* line-height: 22px; */
  text-align: center;
  /* color: #783887; */
  flex: none;
  order: 1;
  flex-grow: 0;
  font-family: 'PingFang SC';
  font-style: normal;
  line-height: 22px;
  display: flex;
  align-items: center;
  letter-spacing: -0.1px;
  color: #783887;
  position: relative;
  top: -19px;
  left: 12px;
}

:deep(button.el-button.el-button--default.el-button--mini svg) {
  position: relative;
  right: 7px;
  top: 1px;
  width: 14px;
  height: 14px;
}

.add-filter-link:hover {
  background: rgba(120, 56, 135, 0.1);
  border-radius: 2px;
}

:deep(span.el-tag.el-tag--info.el-tag--mini.el-tag--light) {
  flex-direction: row;
  align-items: center;
  padding: 1px 6px;
  gap: 4px;
  height: 24px;
  background: rgba(31, 35, 41, 0.1);
  border-radius: 2px;
  flex: none;
  flex-grow: 0;

  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
}

:deep(i.el-tag__close.el-icon-close) {
  position: relative;
  left: 6px;
  top: -6px;
}

.adv-search-bar {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
}

:deep(input.el-input__inner) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
}

:deep (.el-button--mini, .el-button--small) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  text-align: center;
  color: #1F2329;
}
</style>
