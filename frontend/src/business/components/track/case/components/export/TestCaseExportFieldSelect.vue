<template>
  <div v-loading="loading">
    <div class="export-title"
         @click="showSelect = !showSelect">
      <span>
       {{ $t('test_track.case.import.select_import_field') }}
      </span>
      <i class="el-icon-arrow-down"
         v-if="showSelect"/>
      <i class="el-icon-arrow-left"
         v-if="!showSelect"/>
    </div>

    <el-divider/>

    <div v-show="showSelect">
      <el-checkbox class="select-all-checkbox"
                   v-model="selectAll" @change="handleSelectAllChange">
        {{ $t('test_track.case.import.select_import_all_field') }}
      </el-checkbox>

      <test-case-export-field-select-item
        type="EXPORT_BASE_FIELD"
        :title="$t('test_track.case.import.base_field')"
        :fields="baseFields"
        @selectAllChange="handleItemSelectAllChange"
        ref="baseSelectItem"/>

      <test-case-export-field-select-item
        type="EXPORT_CUSTOM_FIELD"
        :title="$t('test_track.case.import.custom_field')"
        :fields="customFields"
        @selectAllChange="handleItemSelectAllChange"
        ref="customSelectItem"/>

      <test-case-export-field-select-item
        type="EXPORT_OTHER_FIELD"
        :title="$t('test_track.case.import.other_field')"
        :fields="otherFields"
        @selectAllChange="handleItemSelectAllChange"
        ref="otherSelectItem"/>

      <div class="other-field-tip">
        {{ $t('test_track.case.import.other_field_tip') }}
      </div>

    </div>
  </div>
</template>

<script>
import {getCurrentProjectID} from "@/common/js/utils";
import TestCaseExportFieldList from "@/business/components/track/case/components/export/TestCaseExportFieldList";
import {getTestTemplate} from "@/network/custom-field-template";
import TestCaseExportFieldSelectItem
  from "@/business/components/track/case/components/export/TestCaseExportFieldSelectItem";

export default {
  name: "TestCaseExportFieldSelect",
  components: {TestCaseExportFieldSelectItem, TestCaseExportFieldList},
  data() {
    return {
      selectAll: false,
      showSelect: true,
      baseFields: [],
      loading: false,
      originBaseFields: [
        {
          id: 'ID',
          key: 'A',
          name: 'ID',
          enable: true
        },
        {
          id: 'name',
          key: 'B',
          name: this.$t("test_track.case.name"),
          enable: true,
          disabled: true
        },
        {
          id: 'nodeId',
          key: 'C',
          name: this.$t("test_track.case.module"),
          enable: true
        },
        {
          id: 'prerequisite',
          key: 'D',
          name: this.$t("test_track.case.prerequisite"),
          enable: true
        },
        {
          id: 'remark',
          key: 'E',
          name: this.$t("commons.remark"),
          enable: true
        },
        {
          id: 'stepDesc',
          key: 'F',
          name: this.$t("test_track.case.step_describe"),
          enable: true
        },
        {
          id: 'stepResult',
          key: 'G',
          name: this.$t("test_track.case.expected_results"),
          enable: true
        },
        {
          id: 'stepModel',
          key: 'H',
          name: this.$t("test_track.case.step_model"),
          enable: true
        },
        {
          id: 'tags',
          key: 'I',
          name: this.$t("commons.tag"),
          enable: true
        },
      ],
      customFields: [],
      otherFields: [
        {
          id: 'version',
          key: 'A',
          name: this.$t("commons.version"),
          enable: false
        },
        {
          id: 'commend',
          key: 'B',
          name: this.$t("commons.comment"),
          enable: false
        },
        {
          id: 'executeResult',
          key: 'C',
          name: this.$t("test_track.plan.execute_result"),
          enable: false
        },
        {
          id: 'reviewResult',
          key: 'D',
          name: this.$t("test_track.review_view.execute_result"),
          enable: false
        },
        {
          id: 'creator',
          key: 'E',
          name: this.$t("commons.creator"),
          enable: false
        },
        {
          id: 'createTime',
          key: 'F',
          name: this.$t("commons.create_time"),
          enable: false
        },
        {
          id: 'updateTime',
          key: 'G',
          name: this.$t("commons.update_time"),
          enable: false
        },
      ]
    }
  },
  computed: {
    selectItems() {
      return [this.$refs.baseSelectItem, this.$refs.customSelectItem, this.$refs.otherSelectItem];
    }
  },
  created() {
    this.projectId = getCurrentProjectID();
    this.loading = true;
    getTestTemplate()
      .then((template) => {
        template.customFields.forEach(item => {
          item.enable = true;
        });
        this.customFields = [];
        this.baseFields = [...this.originBaseFields];
        template.customFields.forEach(item => {
          if (item.system) {
            this.baseFields.push(item);
          } else {
            this.customFields.push(item);
          }
        });
        this.loading = false;
      });
  },
  methods: {
    getExportParam() {
      return {
        baseHeaders: this.selectItems[0].getExportParam(),
        customHeaders: this.selectItems[1].getExportParam(),
        otherHeaders: this.selectItems[2].getExportParam(),
      }
    },
    handleSelectAllChange() {
      this.selectItems.forEach(item => {
        item.selectAllChange(this.selectAll);
      });
    },
    handleItemSelectAllChange() {
      let isSelectAll = true;
      this.selectItems.forEach(item => {
        if (!item.selectAll) {
          isSelectAll = false;
        }
      });
      this.selectAll = isSelectAll;
    }
  }
}
</script>

<style scoped>

.export-title {
  font-size: 16px;
  font-weight: bold;
  margin: 20px 5px 15px 0px;
}

.export-title span:first-child {
  margin-right: 5px;
}

.select-all-checkbox {
  margin-top: 10px
}

.export-title {
  cursor: pointer;
}

.other-field-tip {
  margin-top: 30px;
  font-size: 10px;
  color: #9ea0a3;
}
</style>
