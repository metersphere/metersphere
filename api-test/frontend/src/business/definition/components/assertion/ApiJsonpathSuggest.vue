<template>
  <ms-drawer
    class="json-path-picker"
    :visible="visible"
    :size="30"
    @close="close"
    direction="right"
    v-clickoutside="close"
    appendToBody>
    <template v-slot:header>
      <ms-instructions-icon :content="tip" />
      {{ tip }}
      <div class="jsonpath-tip">
        {{ $t('api_test.request.extract.json_path_tip') }}
      </div>
    </template>
    <jsonpath-picker :code="data" v-on:path="pathChangeHandler" ref="jsonpathPicker" />
  </ms-drawer>
</template>

<script>
import Vue from 'vue';
import JsonPathPicker from 'vue-jsonpath-picker';
import MsDrawer from 'metersphere-frontend/src/components/MsDrawer';
import MsInstructionsIcon from 'metersphere-frontend/src/components/MsInstructionsIcon';
import { parse, isSafeNumber } from 'lossless-json';

let dotReplace = '#DOT_MASK#';

const clickoutside = {
  // 初始化指令
  bind(el, binding, vnode) {
    function documentHandler(e) {
      // 这里判断点击的元素是否是本身，是本身，则返回
      if (el.contains(e.target)) {
        return false;
      }
      // 判断指令中是否绑定了函数
      if (binding.expression) {
        // 如果绑定了函数 则调用那个函数，此处binding.value就是handleClose方法
        binding.value(e);
      }
    }

    // 给当前元素绑定个私有变量，方便在unbind中可以解除事件监听
    el.__vueClickOutside__ = documentHandler;
    document.addEventListener('click', documentHandler);
  },
  update() {},
  unbind(el, binding) {
    // 解除事件监听
    document.removeEventListener('click', el.__vueClickOutside__);
    delete el.__vueClickOutside__;
  },
};
Vue.use(JsonPathPicker);
export default {
  name: 'MsApiJsonpathSuggest',
  components: { MsInstructionsIcon, MsDrawer },
  directives: { clickoutside },
  data() {
    return {
      visible: false,
      isCheckAll: false,
      data: {},
    };
  },
  props: {
    tip: {
      type: String,
      default() {
        return '';
      },
    },
  },
  methods: {
    close() {
      this.visible = false;
    },
    parseAndValidateNumber(value) {
      if (!isSafeNumber(value) || Number(value).toString().length < value.length) {
        // 大数、超长小数、科学计数法、小数位全为 0 等情况下，JS 精度丢失，所以需要用字符串存储
        return `Number(${value.toString()})`;
      }
      return Number(value);
    },
    removeNumberFunctionFromString(string) {
      const regex = /"?Number\(([\d.e+-]+)\)"?/g;
      return string.replace(regex, '$1');
    },
    open(objStr) {
      this.data = {};
      try {
        let param;
        let JSONBig = require('json-bigint')({ storeAsString: true });
        // 解决精度丢失问题
        try {
          param = parse(objStr, undefined, this.parseAndValidateNumber);
        } catch (e) {
          param = JSONBig.parse(objStr);
        }
        if (param instanceof Array) {
          this.$warning('不支持解析JSON数组');
          return;
        }
        this.data = param;
      } catch (e) {
        this.$warning(this.$t('api_test.request.assertions.json_path_err'));
        return;
      }
      this.visible = true;
    },
    pathChangeHandler(data) {
      let paramNames = [];
      let result = {};
      try {
        paramNames = this.parseSpecialChar(data);
        result = this.getParamValue(this.data, 0, paramNames);
      } catch (e) {
        result = {};
        result.key = 'var';
      }
      result.path = '$.' + data;
      this.$emit('addSuggest', result);
    },
    // 替换. 等特殊字符
    parseSpecialChar(data) {
      let paramNames = [];
      let reg = /\['.*'\]/;
      let searchStr = reg.exec(data);
      if (searchStr) {
        searchStr.forEach((item) => {
          if (data.startsWith("['")) {
            data = data.replace(item, item.replace('.', dotReplace));
          } else {
            data = data.replace(item, '.' + item.replace('.', dotReplace));
          }
        });
        paramNames = data.split('.');
      } else {
        paramNames = data.split('.');
      }
      for (let i in paramNames) {
        if (paramNames[i].search(reg) > -1) {
          paramNames[i] = paramNames[i].substring(2, paramNames[i].length - 2);
        }
        paramNames[i] = paramNames[i].replace(dotReplace, '.');
      }
      return paramNames;
    },
    getParamValue(obj, index, params) {
      if (params.length < 1) {
        return {};
      }

      let param = params[index];
      let childObj;

      let reg = /\[[0-9]+\]$/;
      let regIndex = param.search(reg);
      if (regIndex > -1) {
        let paramName = param.substring(0, regIndex);
        let paramIndex = param.substring(regIndex + 1, param.length - 1);
        param = paramIndex;
        childObj = obj[paramName][paramIndex];
      } else {
        childObj = obj[params[index]];
      }
      if (index === params.length - 1) {
        if (childObj instanceof Object) {
          childObj = JSON.stringify(childObj);
        } else {
          childObj = childObj + '';
        }
        childObj = this.removeNumberFunctionFromString(childObj);
        return {
          key: param,
          value: childObj,
        };
      }
      index++;
      return this.getParamValue(childObj, index, params);
    },
  },
};
</script>

<style scoped>
.json-path-picker :deep(.ms-drawer-header){
  margin-left: 13px;
}
.json-path-picker {
  padding: 10px 13px;
}

.json-path-picker :deep(.json-tree) {
  margin-top: 0px;
  margin-left: 6px;
}

.jsonpath-tip {
  font-size: 12px;
  padding: 5px;
  color: grey;
}

:deep(.el-icon-close:hover) {
  font-size: 18px;
  font-weight: bold;
}

:deep(.fulls-screen-btn) {
  position: fixed !important;
}
</style>
