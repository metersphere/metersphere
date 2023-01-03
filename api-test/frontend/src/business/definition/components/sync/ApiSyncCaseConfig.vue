<template>
  <el-dialog
    :visible.sync="batchSyncApiVisible" :close-on-click-modal="false"
    :title="$t('commons.save') + '&' + $t('workstation.sync') + $t('commons.setting')"
    v-if="isXpack">
    <el-row style="margin-bottom: 10px; box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)">
      <div class="timeClass">
          <span style="font-size: 16px; font-weight: bold; padding-left: 10px">{{
              $t('api_test.definition.one_click_sync') + 'case'
            }}</span>
        <el-switch v-model="apiSyncRuleRelation.syncCase" style="float:right; padding-right: 10px"></el-switch>
      </div>
      <br/>
      <span style="font-size: 12px; padding-left: 10px">{{ $t('workstation.batch_sync_api_tips') }}</span
      ><br/><br/>
      <span v-if="apiSyncRuleRelation.syncCase" style="font-size: 16px; font-weight: bold; padding-left: 10px">
          {{ $t('workstation.sync') + $t('commons.setting') }}
          <i class="el-icon-arrow-down" v-if="showApiSyncConfig" @click="showApiSyncConfig = false"/>
          <i class="el-icon-arrow-right" v-if="!showApiSyncConfig" @click="showApiSyncConfig = true"/> </span
      ><br/><br/>
      <div v-if="showApiSyncConfig">
        <sync-setting
          style="padding-left: 20px"
          v-if="apiSyncRuleRelation.syncCase"
          v-bind:sync-data="apiSyncRuleRelation.apiSyncConfig"
          ref="synSetting"
          @updateSyncData="updateSyncData"></sync-setting>
      </div>
    </el-row>
    <el-row style="margin-bottom: 10px; box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1)">
      <div class="timeClass">
          <span style="font-size: 16px; font-weight: bold; padding-left: 10px">
            {{ $t('api_test.definition.change_notification') }}
            <el-tooltip
              class="ms-num"
              effect="dark"
              :content="$t('project_application.workstation.api_receiver_tip')"
              placement="top">
              <i class="el-icon-warning"/>
            </el-tooltip>
          </span>
        <el-switch v-model="apiSyncRuleRelation.sendNotice" style="float:right;padding-right: 10px"></el-switch>
      </div>
      <span style="font-size: 12px; padding-left: 10px"> {{ $t('api_test.definition.recipient_tips') }} </span><br/>
      <p
        style="
            font-size: 12px;
            color: var(--primary_color);
            margin-bottom: 20px;
            text-decoration: underline;
            cursor: pointer;
            padding-left: 10px;
          "
        @click="gotoApiMessage">
        {{ $t('project_application.workstation.go_to_api_message') }}
      </p>
      <el-row v-if="apiSyncRuleRelation.sendNotice" style="margin-bottom: 5px; margin-top: 5px">
        <el-col :span="4"
        ><span style="font-weight: bold; padding-left: 10px">{{ $t('api_test.definition.recipient') + ':' }}</span>
        </el-col>
        <el-col :span="20" style="color: var(--primary_color)">
          <el-checkbox v-model="apiSyncRuleRelation.caseCreator">{{ 'CASE' + $t('api_test.creator') }}</el-checkbox>
          <el-checkbox v-model="apiSyncRuleRelation.scenarioCreator">
            {{ $t('commons.scenario') + $t('api_test.creator') }}
          </el-checkbox>
        </el-col>
      </el-row>
    </el-row>
    <el-row>
      <el-checkbox v-model="apiSyncRuleRelation.showUpdateRule" style="padding-left: 10px"
      >{{ $t('project_application.workstation.no_show_setting') }}
      </el-checkbox>
      <el-tooltip
        class="ms-num"
        effect="dark"
        :content="$t('project_application.workstation.no_show_setting_tip')"
        placement="top">
        <i class="el-icon-warning"/>
      </el-tooltip>
    </el-row>
    <span slot="footer" class="dialog-footer">
        <el-button @click="batchSyncApiVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="batchSync()">{{ $t('commons.confirm') }}</el-button>
      </span>
  </el-dialog>
</template>

<script>
import SyncSetting from "@/business/definition/util/SyncSetting";

export default {
  name: "ApiSyncCaseConfig",
  components: {
    SyncSetting,
  },
  props: {
    apiSyncRuleRelation: {},
    showApiSyncConfig: {
      type: Boolean,
      default: false,
    },
    isXpack: {
      type: Boolean,
      default: false,
    }
  },
  data() {
    return {
      batchSyncApiVisible: false,
    }
  },
  methods: {
    updateSyncData(value) {
      this.apiSyncRuleRelation.apiSyncConfig = value;
    },
    gotoApiMessage() {
      let apiResolve = this.$router.resolve({
        path: '/project/messagesettings',
      });
      window.open(apiResolve.href, '_blank');
    },
    batchSync() {
      let fromData;
      if (this.$refs.synSetting && this.$refs.synSetting.fromData) {
        fromData = this.$refs.synSetting.fromData;
        fromData.method = true;
        fromData.path = true;
        fromData.protocol = true;
      }
      this.$emit('batchSync', fromData)
    },
    show() {
      this.batchSyncApiVisible = true
    },
    close() {
      this.batchSyncApiVisible = false
    }
  },
}
</script>

<style scoped>

</style>
