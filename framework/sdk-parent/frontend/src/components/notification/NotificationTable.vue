<template>
  <el-table
    :data="tableData"
    class="tb-edit"
    border
    :cell-style="rowClass"
    :header-cell-style="headClass"
    v-loading="loading"
  >
    <el-table-column :label="$t('schedule.event')" min-width="15%" prop="events">
      <template slot-scope="scope">
        <el-select v-model="scope.row.event" :placeholder="$t('organization.message.select_events')" size="mini"
                   @change="handleReceivers(scope.row)"
                   prop="event" :disabled="!scope.row.isSet">
          <el-option
            v-for="item in eventOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </template>
    </el-table-column>
    <el-table-column :label="$t('schedule.receiver')" prop="receiver" min-width="20%">
      <template v-slot:default="{row}">
        <el-select v-model="row.userIds" filterable multiple size="mini"
                   :placeholder="$t('commons.please_select')"
                   style="width: 100%;" :disabled="!row.isSet">
          <el-option
            v-for="item in row.receiverOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id">
          </el-option>
        </el-select>
      </template>
    </el-table-column>
    <el-table-column :label="$t('schedule.receiving_mode')" min-width="20%" prop="type">
      <template slot-scope="scope">
        <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')"
                   size="mini"
                   style="width: 100%;"
                   :disabled="!scope.row.isSet" @change="handleEdit(scope.$index, scope.row)">
          <el-option
            v-for="item in (hasLicense() ? receiveTypeOptions: receiveTypeOptions.filter(v => v.value !=='WEBHOOK'))"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </template>
    </el-table-column>
    <el-table-column prop="webhook" min-width="25%">
      <template v-slot:header>
        Webhook
        <el-tooltip effect="dark" placement="top-start"
                    style="padding-left: 10px;">
          <template v-slot:content>
            支持企业微信、钉钉、飞书以及自定义Webhook（X-Pack）
            <div>
              自定义 Webhook 需要配置自定义模版才能发送成功，请自行查询对应的消息模版
            </div>
          </template>
          <i class="el-icon-info pointer"/>
        </el-tooltip>
      </template>
      <template v-slot:default="scope">
        <el-input v-model="scope.row.webhook" size="mini"
                  :disabled="!scope.row.isSet||!scope.row.isReadOnly"></el-input>
      </template>
    </el-table-column>
    <el-table-column :label="$t('commons.operating')" width="150" prop="result">
      <template v-slot:default="scope">
        <ms-tip-button
          circle
          type="success"
          size="mini"
          v-if="scope.row.isSet"
          v-xpack
          @click="handleTemplate(scope.$index,scope.row)"
          :tip="$t('organization.message.template')"
          icon="el-icon-tickets"/>
        <ms-tip-button
          circle
          type="primary"
          size="mini"
          v-show="scope.row.isSet"
          @click="handleAddTask(scope.$index,scope.row)"
          :tip="$t('commons.add')"
          icon="el-icon-check"/>
        <ms-tip-button
          circle
          size="mini"
          v-show="scope.row.isSet"
          @click="removeRowTask(scope.$index,tableData)"
          :tip="$t('commons.cancel')"
          icon="el-icon-refresh-left"/>
        <ms-tip-button
          el-button
          circle
          type="primary"
          size="mini"
          icon="el-icon-edit"
          v-show="!scope.row.isSet"
          :tip="$t('commons.edit')"
          @click="handleEditTask(scope.$index,scope.row)"
          v-permission="['PROJECT_MESSAGE:READ+EDIT']"/>
        <ms-tip-button
          circle
          type="danger"
          icon="el-icon-delete"
          size="mini"
          v-show="!scope.row.isSet"
          @click="deleteRowTask(scope.$index,scope.row)"
          :tip="$t('commons.delete')"
          v-permission="['PROJECT_MESSAGE:READ+DELETE']"/>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
import MsTipButton from "../MsTipButton";
import {hasLicense} from "../../utils/permission";
import {deleteNoticeTask, saveNoticeTask, updateNoticeTask} from "../../api/notification";

export default {
  name: "NotificationTable",
  components: {MsTipButton},
  props: {
    eventOptions: {
      type: Array
    },
    receiveTypeOptions: {
      type: Array
    },
    tableData: {
      type: Array
    }
  },
  data() {
    return {
      loading: false
    }
  },
  methods: {
    hasLicense,
    rowClass() {
      return "text-align:center";
    },
    headClass() {
      return "text-align:center;background:'#ededed'";
    },
    handleReceivers(row) {
      this.$emit('handleReceivers', row);
    },
    handleTemplate(index, row) {
      this.$emit('handleTemplate', index, row);
    },
    handleEdit(index, data) {
      data.isReadOnly = true;
      if (data.type === 'EMAIL' || data.type === 'IN_SITE') {
        data.isReadOnly = !data.isReadOnly;
        data.webhook = '';
      }
    },
    handleAddTask(index, data) {
      if (data.event && data.userIds.length > 0 && data.type) {
        if (['WEBHOOK', 'WECHAT_ROBOT', 'LARK', 'NAIL_ROBOT'].indexOf(data.type) >= 0) {
          if (!data.webhook) {
            this.$warning(this.$t('organization.message.message_webhook'));
          } else {
            this.addTask(data);
          }
        } else {
          this.addTask(data);
        }
      } else {
        this.$warning(this.$t('organization.message.message'));
      }
    },
    addTask(data) {
      let promise = data.isUpdate ? updateNoticeTask(data) : saveNoticeTask(data);
      this.loading = promise.then(() => {
        data.isSet = false;
        this.$set(data, "isUpdate", false);
        this.$emit('refresh');
        this.$success(this.$t('commons.save_success'));
      });
    },
    removeRowTask(index, data) { //移除
      if (!data[index].identification) {
        data.splice(index, 1);
      } else {
        data[index].isSet = false;
      }
      this.$emit("refresh");
    },
    deleteRowTask(index, data) { //删除
      this.loading = deleteNoticeTask(data.identification).then(() => {
        this.$success(this.$t('commons.delete_success'));
        this.$emit('refresh');
      })
    },
    handleEditTask(index, data) {
      this.handleReceivers(data);
      data.isSet = true;
      this.$set(data, "isUpdate", true);
      if (data.type === 'EMAIL' || data.type === 'IN_SITE') {
        data.isReadOnly = false;
        data.webhook = '';
      } else {
        data.isReadOnly = true;
      }
    },
  }
}
</script>

<style scoped>

</style>
