<template>
  <div v-loading="loading" class="export-table-layout">
    <span class="export-field-select-tip">{{$t('test_track.case.export.export_field_select_tips')}}</span>
    <ms-table :data="tableData" ref="table" :enable-selection="false" style="margin-top: 8px">
      <el-table-column
        prop="fieldType"
        width="150"
        :label="$t('custom_field.field_type')">
        <template v-slot:default="scope">
          <el-checkbox v-model="scope.row.isSelectAll" :indeterminate="scope.row.isIndeterminate" @change="fieldRowChange(scope.row)">{{scope.row.fieldType}}</el-checkbox>
        </template>
      </el-table-column>
      <el-table-column
        prop="fieldName"
        :label="$t('custom_field.field_name')">
        <template v-slot:default="scope">
          <test-case-export-field-list :fields="scope.row.fieldName" @enableChange="enableChange(scope.row)"/>
        </template>
      </el-table-column>
    </ms-table>
  </div>
</template>

<script>
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import {getCurrentProjectID} from "@/business/utils/sdk-utils";
import TestCaseExportFieldSelectItem from "@/business/case/components/export/TestCaseExportFieldSelectItem";
import TestCaseExportFieldList from "@/business/case/components/export/TestCaseExportFieldList";
import {getTestTemplate} from "@/api/custom-field-template";

export default {
  name: "TestCaseExportFieldSelect",
  components: {MsTable, TestCaseExportFieldList, TestCaseExportFieldSelectItem},
  data() {
    return {
      tableData: [],
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
        this.initTableData();
        this.loading = false;
      });
  },
  methods: {
    initTableData() {
      if (this.baseFields && this.baseFields.length > 0) {
        let unSelectFields = this.baseFields.filter(item => !item.enable);
        let selectFields = this.baseFields.filter(item => item.enable);
        this.tableData.push({
          "fieldType": this.$t('test_track.case.import.base_field'),
          "isSelectAll": unSelectFields.length == 0,
          "isIndeterminate": selectFields.length > 0 && selectFields.length < this.baseFields.length,
          "fieldName": this.baseFields
        });
      }
      if (this.customFields && this.customFields.length > 0) {
        let unSelectFields = this.customFields.filter(item => !item.enable);
        let selectFields = this.customFields.filter(item => item.enable);
        this.tableData.push({
          "fieldType": this.$t('test_track.case.import.custom_field'),
          "isSelectAll": unSelectFields.length == 0,
          "isIndeterminate": selectFields.length > 0 && selectFields.length < this.customFields.length,
          "fieldName": this.customFields
        });
      }
      if (this.otherFields && this.otherFields.length > 0) {
        let unSelectFields = this.otherFields.filter(item => !item.enable);
        let selectFields = this.otherFields.filter(item => item.enable);
        this.tableData.push({
          "fieldType": this.$t('test_track.case.import.other_field'),
          "isSelectAll": unSelectFields.length == 0,
          "isIndeterminate": selectFields.length > 0 && selectFields.length < this.otherFields.length,
          "fieldName": this.otherFields
        });
      }
    },
    fieldRowChange(row) {
      row.isIndeterminate = false
      row.fieldName.forEach(item => {
        item.enable = row.isSelectAll;
      });
    },
    enableChange(row) {
      let selectFields = row.fieldName.filter(item => item.enable);
      let unSelectFields = row.fieldName.filter(item => !item.enable);
      row.isSelectAll = unSelectFields.length == 0;
      row.isIndeterminate = selectFields.length > 0 && selectFields.length < row.fieldName.length;
    },
    getExportParam() {
      let baseSelectFields = this.baseFields.filter(item => item.enable);
      let customSelectFields = this.customFields.filter(item => item.enable);
      let otherSelectFields = this.otherFields.filter(item => item.enable);
      return {
        baseHeaders: baseSelectFields,
        customHeaders: customSelectFields,
        otherHeaders: otherSelectFields,
      }
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

.export-table-layout {
  margin-top: 24px;
}

.export-field-select-tip {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  order: 0;
  flex-grow: 0;
}
</style>
