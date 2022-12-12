<template>
  <div>
    <el-row v-for="i in customFieldRowNums" :key="i">
      <span class="custom-item" v-for="(item, j) in sortCustomFields" :key="j">
        <template v-if="j >= (i - 1) * 3 && j < (i - 1) * 3 + 3">
          <div class="custom-row case-wrap">
            <div class="case-title-wrap">
              <div class="name title-wrap">
                {{ item.system ? $t(systemNameMap[item.name]) : item.name }}
              </div>
              <div class="required required-item" v-if="item.required"></div>
            </div>
            <div class="side-content">
              <base-edit-item-component
                :editable="editable"
                :auto-save="!disabled"
                trigger="hover"
                :contentObject="{
                  content: item,
                  contentType: 'CUSTOM',
                }"
                :readonly-hover-event="!disabled"
                :content-click-event="!disabled"
                :model="form"
                :rules="rules"
              >
                <template v-slot:content="{ onClick, hoverEditable }">
                  <div :class="hoverEditable ? 'selectHover' : ''">
                    <el-form-item :prop="item.name">
                      <custom-filed-component
                        :data="item"
                        :form="form"
                        prop="defaultValue"
                        :disabled="
                          (item.type !== 'richText' && isPublic) || disabled
                        "
                        :default-open="defaultOpen"
                        @onClick="onClick"
                      />
                    </el-form-item>
                  </div>
                </template>
              </base-edit-item-component>
            </div>
          </div>
        </template>
      </span>
    </el-row>
  </div>
</template>

<script>
import { SYSTEM_FIELD_NAME_MAP } from "metersphere-frontend/src/utils/table-constants";
import CustomFiledComponent from "metersphere-frontend/src/components/template/CustomFiledComponent";
import { sortCustomFields } from "metersphere-frontend/src/utils/custom_field";
import BaseEditItemComponent from "../BaseEditItemComponent";

export default {
  name: "CaseCustomFiledFormRow",
  components: { CustomFiledComponent, BaseEditItemComponent },
  props: {
    rules: Object,
    editable: Boolean,
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
    disabled: {
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
      return size % 3 === 0 ? val : val + 1;
    },
    systemNameMap() {
      return SYSTEM_FIELD_NAME_MAP;
    },
    sortCustomFields() {
      return sortCustomFields(this.issueTemplate.customFields);
    },
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";
.selectHover {
  // background: rgba(31, 35, 41, 0.1);
  border-radius: 4px;
  cursor: pointer;
  :deep(.el-select) {
    // background-color: rgba(31, 35, 41, 0.1) !important;
    border: none !important;
  }
  :deep(.el-input__inner) {
    border: none !important;
    background-color: rgba(31, 35, 41, 0.1) !important;
    color: #1f2329;
  }
}
.edit-content-container {
  width: 100%;
  height: 100%;
  display: flex;
  background-color: #fff;
  .required-item:after {
    content: "*";
    color: #f54a45;
    margin-left: px2rem(4);
    width: px2rem(8);
    height: 22px;
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
  }
  .case-title-wrap {
    display: flex;
    margin-top: px2rem(24);
    margin-bottom: px2rem(8);
    .title-wrap {
      height: 22px;
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 500;
      font-size: 14px;
      line-height: 22px;
      color: #1f2329;
    }
  }
  .case-wrap {
    margin-left: px2rem(24);
    margin-top: px2rem(24);
  }
  .case-title-wrap {
    display: flex;
    .title-wrap {
      font-weight: 500;
      height: 22px;
      font-size: 14px;
      line-height: 22px;
      color: #1f2329;
    }
    margin-bottom: px2rem(8);
  }
  .side-content {
    width: px2rem(256);
    min-height: 32px;
    :deep(.el-select) {
      width: 100%;
    }
    :deep(.el-form-item__content) {
      margin-left: 0px !important;
    }
  }
}
</style>
