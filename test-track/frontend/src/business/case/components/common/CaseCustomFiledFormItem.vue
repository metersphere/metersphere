<template>
  <div class="field-item-wrap">
    <div v-for="i in customFieldRowNums" :key="i" class="field-item-iterator">
      <div
        class="custom-field-item"
        v-for="item in sortCustomFields"
        :key="item.id"
      >
        <div class="not-rich-row" v-if="item.type !== 'richText'">
          <el-form-item
            :label="item.system ? $t(systemNameMap[item.name]) : item.name"
            :prop="item.name"
            :label-width="formLabelWidth"
          >
            <div slot="label" :class="item.required ? 'required-item' : ''">
              {{ item.system ? $t(systemNameMap[item.name]) : item.name }}
            </div>
            <custom-filed-component
              prop="defaultValue"
              :data="item"
              :form="form"
              :default-open="defaultOpen"
              :disabled="isPublic"
            />
          </el-form-item>
        </div>
        <div class="other-row" v-else>
          <el-form-item
            :label="item.system ? $t(systemNameMap[item.name]) : item.name"
            :prop="item.name"
            :default-open="defaultOpen"
            :label-width="formLabelWidth"
          >
            <div slot="label" :class="item.required ? 'required-item' : ''">
              {{ item.system ? $t(systemNameMap[item.name]) : item.name }}
            </div>
            <custom-filed-component
              :data="item"
              :form="form"
              prop="defaultValue"
            />
          </el-form-item>
        </div>
      </div>
    </div>

    <!-- <el-row v-for="(i) in (customFieldRowNums)" :key="i">
      <span class="custom-item" v-for="(item, j) in sortCustomFields" :key="j">
        <span v-if="j >= (i - 1)*3 && j < (i - 1)*3+3">
          <el-col :span="8" v-if="item.type !== 'richText'">
            <el-form-item :label="item.system ? $t(systemNameMap[item.name]) : item.name" :prop="item.name"
                          :label-width="formLabelWidth">
              <custom-filed-component
                prop="defaultValue"
                :data="item"
                :form="form"
                :default-open="defaultOpen"
                :disabled="isPublic"/>
            </el-form-item>
          </el-col>
          <div v-else>
            <el-col :span="24">
              <el-form-item
                :label="item.system ? $t(systemNameMap[item.name]) : item.name"
                :prop="item.name"
                :default-open="defaultOpen"
                :label-width="formLabelWidth">
                 <custom-filed-component :data="item" :form="form" prop="defaultValue"/>
              </el-form-item>
            </el-col>
          </div>
        </span>
      </span>
      </el-row> -->
  </div>
</template>

<script>
import CustomFiledComponent from "metersphere-frontend/src/components/template/CustomFiledComponent";
import { SYSTEM_FIELD_NAME_MAP } from "metersphere-frontend/src/utils/table-constants";

export default {
  name: "CaseCustomFiledFormItem",
  components: { CustomFiledComponent },
  props: {
    issueTemplate: {
      type: Object,
      default() {
        return {};
      },
    },
    formLabelWidth: String,
    form: Object,
    isPublic: {
      type: Boolean,
      default() {
        return false;
      },
    },
    defaultOpen: {
      type: String,
      default() {
        return "preview";
      },
    },
  },
  computed: {
    customFieldRowNums() {
      let size = this.issueTemplate.customFields
        ? this.issueTemplate.customFields.length
        : 0;
      let val = parseInt(size / 3);
      return size % 3 == 0 ? val : val + 1;
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    sortCustomFields() {
      let total = 0; //定义total用于控制循环结束
      let customFields = this.issueTemplate.customFields;
      for (let i = 0; total < customFields.length; total++) {
        if (customFields[i].type === "richText") {
          //循环到是0的位置就删除该元素0并且在arr末尾push进这个元素0，由于splice删除了该位置元素，所以i不用+1，下次循环仍然检查i位置的元素
          customFields.push(customFields.splice(i, 1)[0]);
        } else {
          i++; //循环到不是0的位置就继续往后循环
        }
      }
      return customFields;
    },
  },
};
</script>

<style scoped lang="scss">
.field-item-wrap {
  .field-item-iterator {
    display: flex;
    flex-wrap: wrap;
    :deep(.el-form-item__label:before) {
      display: none;
    }
  }
  .custom-field-item {
    display: flex;
    flex-wrap: wrap;
  }
  .custom-field-item:not(:first-child) {
    :deep(.el-form-item) {
      margin-left: 26px !important;
    }
  }
  .custom-field-item {
    :deep(.not-rich-row .el-form-item) {
      width: 300px !important;
    }
    :deep(.other-row .el-form-item) {
      width: 100% !important;
    }
    :deep(.el-form-item__content) {
      margin-left: 0px !important;
    }
  }
}

.required-item:after {
  content: "*";
  color: #f54a45;
  margin-left: px2rem(4);
  width: px2rem(8);
  height: 32px;
  font-weight: 400;
  font-size: 14px;
  line-height: 32px;
}
.required-item {
  font-weight: 500;
  font-size: 14px;
  color: #1f2329;
}
</style>
