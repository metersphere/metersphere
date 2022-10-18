<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col class="assertion-select">
        <el-select :disabled="isReadOnly" class="assertion-item" v-model="regex.subject" size="small"
                   :placeholder="$t('api_test.request.assertions.select_subject')">
          <el-option label="Response Code" :value="subjects.RESPONSE_CODE"/>
          <el-option label="Response Headers" :value="subjects.RESPONSE_HEADERS"/>
          <el-option label="Response Data" :value="subjects.RESPONSE_DATA"/>
        </el-select>
      </el-col>
      <el-col>
        <el-tooltip :disabled="showTip" placement="top" :content="regex.expression">
          <el-input :disabled="isReadOnly" v-model="regex.expression" size="small" show-word-limit
                    :placeholder="$t('api_test.request.assertions.expression')"/>
        </el-tooltip>
      </el-col>
      <el-col class="assertion-checkbox">
        <el-checkbox v-model="regex.assumeSuccess" :disabled="isReadOnly">
          {{ $t('api_test.request.assertions.ignore_status') }}
        </el-checkbox>
      </el-col>
      <el-col class="assertion-btn">
        <el-tooltip :content="$t('test_resource_pool.enable_disable')" placement="top" v-if="edit">
          <el-switch v-model="regex.enable" class="enable-switch" size="mini" :disabled="isReadOnly"
                     style="width: 30px;margin-right:10px"/>
        </el-tooltip>

        <el-button :disabled="isReadOnly" type="danger" size="mini" icon="el-icon-delete" circle @click="remove"
                   v-if="edit"/>
        <el-button :disabled="isReadOnly" type="primary" size="mini" @click="add" v-else>
          {{ $t('api_test.request.assertions.add') }}
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {ASSERTION_REGEX_SUBJECT, Regex} from "../../model/ApiTestModel";

export default {
  name: "MsApiAssertionRegex",

  props: {
    regex: {
      default: () => {
        return new Regex();
      }
    },
    edit: {
      type: Boolean,
      default: false
    },
    index: Number,
    list: Array,
    callback: Function,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      subjects: ASSERTION_REGEX_SUBJECT,
      showTip: true,
    }
  },

  watch: {
    'regex.subject'() {
      this.setRegexDescription();
    },
    'regex.expression'() {
      this.setRegexDescription();
    }
  },
  created() {
    this.showTip = !this.regex || !this.regex.description || this.regex.description.length < 80;
  },
  methods: {
    add: function () {
      this.list.push(this.getRegex());
      this.callback();
    },
    remove: function () {
      this.list.splice(this.index, 1);
    },
    getRegex() {
      let regex = new Regex(this.regex);
      regex.enable = true;
      regex.description = regex.subject + " has: " + regex.expression;
      return regex;
    },
    setRegexDescription() {
      this.showTip = !this.regex || !this.regex.description || this.regex.description.length < 80;
      this.regex.description = this.regex.subject + " has: " + this.regex.expression;
    }
  }
}
</script>

<style scoped>
.assertion-select {
  width: 250px;
}

.assertion-item {
  width: 100%;
}

.assertion-checkbox {
  text-align: center;
  width: 120px;
}

.assertion-btn {
  text-align: center;
  width: 80px;
}
</style>
