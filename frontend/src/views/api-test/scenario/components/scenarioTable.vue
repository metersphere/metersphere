<template>
  <div :class="['p-[16px_16px]', props.class]">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="flex items-center"> </div>
      <div class="items-right flex gap-[8px]">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('api_scenario.table.searchPlaceholder')"
          allow-clear
          class="mr-[8px] w-[240px]"
          @clear="loadScenarioList(true)"
          @search="loadScenarioList(true)"
          @press-enter="loadScenarioList"
        />
        <a-button type="outline" class="arco-btn-outline--secondary !p-[8px]" @click="loadScenarioList(true)">
          <template #icon>
            <icon-refresh class="text-[var(--color-text-4)]" />
          </template>
        </a-button>
      </div>
    </div>
    <ms-base-table
      v-bind="propsRes"
      :action-config="batchActions"
      :first-column-width="44"
      no-disable
      filter-icon-align-left
      v-on="propsEvent"
      @selected-change="handleTableSelect"
      @batch-action="handleTableBatch"
      @module-change="loadScenarioList(false)"
    >
      <template #statusFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="statusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <MsButton type="text" class="arco-btn-text--secondary ml-[10px]" @click="statusFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="statusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="ml-[6px] flex items-center justify-start px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="statusFilters" direction="vertical" size="small">
                  <a-checkbox v-for="val of Object.values(ApiScenarioStatus)" :key="val" :value="val">
                    <apiStatus :status="val" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetStatusFilter">
                  {{ t('common.reset') }}
                </a-button>
                <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                  {{ t('system.orgTemplate.confirm') }}
                </a-button>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #priorityFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="priorityFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <MsButton type="text" class="arco-btn-text--secondary ml-[10px]" @click="priorityFilterVisible = true">
            {{ t(columnConfig.title as string) }}
            <icon-down :class="priorityFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </MsButton>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="ml-[6px] flex items-center justify-start px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="priorityFilters" direction="vertical" size="small">
                  <a-checkbox v-for="item of casePriorityOptions" :key="item.value" :value="item.value">
                    <caseLevel :case-level="item.label as CaseLevel" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetPriorityFilter">
                  {{ t('common.reset') }}
                </a-button>
                <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                  {{ t('system.orgTemplate.confirm') }}
                </a-button>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #num="{ record }">
        <div>
          <MsButton type="text" class="float-left" style="margin-right: 4px" @click="openScenarioTab(record)">{{
            record.num
          }}</MsButton>
          <div v-if="record.scheduleConfig && record.scheduleConfig.enable" class="float-right">
            <a-tooltip position="top">
              <template #content>
                <span>
                  {{ t('apiScenario.schedule.table.tooltip.enable.one') }}
                </span>
                <br />
                <span>
                  {{
                    t('apiScenario.schedule.table.tooltip.enable.two', {
                      time: dayjs(record.nextTriggerTime).format('YYYY-MM-DD HH:mm:ss'),
                    })
                  }}
                </span>
              </template>
              <a-tag
                size="small"
                style="border-color: #00c261; color: #00c261; background-color: transparent"
                bordered
                @click="openScheduleModal(record)"
                >{{ t('apiScenario.schedule.abbreviation') }}</a-tag
              >
            </a-tooltip>
          </div>
          <div v-if="record.scheduleConfig && !record.scheduleConfig.enable" class="float-right">
            <a-tooltip :content="t('apiScenario.schedule.table.tooltip.disable')" position="top">
              <a-tag
                size="small"
                style="border-color: #d4d4d8; color: #323233; background-color: transparent"
                bordered
                @click="openScheduleModal(record)"
                >{{ t('apiScenario.schedule.abbreviation') }}</a-tag
              >
            </a-tooltip>
          </div>
        </div>
      </template>
      <template #status="{ record }">
        <a-select
          v-if="hasAnyPermission(['PROJECT_API_SCENARIO:READ+UPDATE'])"
          v-model:model-value="record.status"
          class="param-input w-full"
          size="mini"
          @change="() => handleStatusChange(record)"
        >
          <template #label>
            <apiStatus :status="record.status" size="small" />
          </template>
          <a-option v-for="item of Object.values(ApiScenarioStatus)" :key="item" :value="item">
            <apiStatus :status="item" size="small" />
          </a-option>
        </a-select>
        <apiStatus v-else :status="record.status" size="small" />
      </template>
      <template #priority="{ record }">
        <a-select
          v-if="hasAnyPermission(['PROJECT_API_SCENARIO:READ+UPDATE'])"
          v-model:model-value="record.priority"
          :placeholder="t('common.pleaseSelect')"
          class="param-input w-full"
          size="mini"
          @change="() => handlePriorityStatusChange(record)"
        >
          <template #label>
            <span class="text-[var(--color-text-2)]">
              <caseLevel :case-level="record.priority as CaseLevel" />
            </span>
          </template>
          <a-option v-for="item of scenarioPriorityList" :key="item.value" :value="item.value">
            <caseLevel :case-level="item.text as CaseLevel" />
          </a-option>
        </a-select>
        <span v-else class="text-[var(--color-text-2)]"> <caseLevel :case-level="record.priority" /></span>
      </template>
      <!-- 报告结果筛选 -->
      <template #lastReportStatusFilter="{ columnConfig }">
        <a-trigger
          v-model:popup-visible="lastReportStatusFilterVisible"
          trigger="click"
          @popup-visible-change="handleFilterHidden"
        >
          <a-button
            type="text"
            class="arco-btn-text--secondary p-[8px_4px]"
            @click="lastReportStatusFilterVisible = true"
          >
            <div class="font-medium">
              {{ t(columnConfig.title as string) }}
            </div>
            <icon-down :class="lastReportStatusFilterVisible ? 'text-[rgb(var(--primary-5))]' : ''" />
          </a-button>
          <template #content>
            <div class="arco-table-filters-content">
              <div class="flex items-center justify-center px-[6px] py-[2px]">
                <a-checkbox-group v-model:model-value="lastReportStatusListFilters" direction="vertical" size="small">
                  <a-checkbox v-for="key of lastReportStatusFilters" :key="key" :value="key">
                    <ExecutionStatus :module-type="ReportEnum.API_SCENARIO_REPORT" :status="key" />
                  </a-checkbox>
                </a-checkbox-group>
              </div>
              <div class="filter-button">
                <a-button size="mini" class="mr-[8px]" @click="resetLastReportStatusFilter">
                  {{ t('common.reset') }}
                </a-button>
                <a-button type="primary" size="mini" @click="handleFilterHidden(false)">
                  {{ t('system.orgTemplate.confirm') }}
                </a-button>
              </div>
            </div>
          </template>
        </a-trigger>
      </template>
      <template #lastReportStatus="{ record }">
        <ExecutionStatus
          :module-type="ReportEnum.API_SCENARIO_REPORT"
          :status="record.lastReportStatus ? record.lastReportStatus : 'PENDING'"
          :script-identifier="record.scriptIdentifier"
        />
      </template>
      <template #createUserFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="createUserFilterVisible"
          v-model:status-filters="createUserFilters"
          :title="(columnConfig.title as string)"
          :list="memberOptions"
          @search="loadScenarioList"
        >
          <template #item="{ item }">
            {{ item.label }}
          </template>
        </TableFilter>
      </template>
      <template #updateUserFilter="{ columnConfig }">
        <TableFilter
          v-model:visible="updateUserFilterVisible"
          v-model:status-filters="updateUserFilters"
          :title="(columnConfig.title as string)"
          :list="memberOptions"
          @search="loadScenarioList"
        >
          <template #item="{ item }">
            {{ item.label }}
          </template>
        </TableFilter>
      </template>
      <template #stepTotal="{ record }">
        {{ record.stepTotal }}
      </template>
      <template #operation="{ record }">
        <MsButton
          v-permission="['PROJECT_API_SCENARIO:READ+UPDATE']"
          type="text"
          class="!mr-0"
          @click="openScenarioTab(record)"
        >
          {{ t('common.edit') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_SCENARIO:READ+UPDATE']" direction="vertical" :margin="8"></a-divider>
        <MsButton
          v-permission="['PROJECT_API_SCENARIO:READ+EXECUTE']"
          type="text"
          class="!mr-0"
          @click="openScenarioTab(record, 'execute')"
        >
          {{ t('apiScenario.execute') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_SCENARIO:READ+EXECUTE']" direction="vertical" :margin="8"></a-divider>
        <MsButton
          v-permission="['PROJECT_API_SCENARIO:READ+ADD']"
          type="text"
          class="!mr-0"
          @click="openScenarioTab(record, 'copy')"
        >
          {{ t('common.copy') }}
        </MsButton>
        <a-divider v-permission="['PROJECT_API_SCENARIO:READ+ADD']" direction="vertical" :margin="8"></a-divider>
        <MsTableMoreAction
          v-permission="['PROJECT_API_SCENARIO:READ+EXECUTE', 'PROJECT_API_SCENARIO:READ+DELETE']"
          :list="getTableMoreActionList(record)"
          @select="handleTableMoreActionSelect($event, record)"
        />
      </template>
      <template v-if="hasAnyPermission(['PROJECT_API_SCENARIO:READ+ADD'])" #empty>
        <div class="flex w-full items-center justify-center p-[8px] text-[var(--color-text-4)]">
          {{ t('api_scenario.table.tableNoDataAndPlease') }}
          <MsButton
            v-permission="['PROJECT_API_SCENARIO:READ+ADD']"
            class="float-right ml-[8px]"
            @click="emit('createScenario')"
          >
            {{ t('apiScenario.createScenario') }}
          </MsButton>
        </div>
      </template>
    </ms-base-table>
  </div>
  <!--  定时任务配置-->
  <a-modal v-model:visible="showScheduleModal" title-align="start" class="ms-modal-upload ms-modal-medium" :width="600">
    <template #title>
      <div class="float-left">
        {{ scheduleModalTitle }}
        <a-tooltip v-if="translateTextToPX(tableRecord?.name) > 300">
          <template #content>
            <span>
              {{ tableRecord?.name }}
            </span>
          </template>
          <div class="float-right flex text-[var(--color-text-4)]">
            {{ '（' }}
            <div class="one-line-text" style="max-width: 300px">{{ tableRecord?.name }}</div>
            {{ '）' }}
          </div>
        </a-tooltip>
        <div v-else class="float-right text-[var(--color-text-4)]">
          {{ '（' + tableRecord?.name + '）' }}
        </div>
      </div>
    </template>
    <a-form ref="scheduleConfigRef" class="rounded-[4px]" :model="scheduleConfig" layout="vertical">
      <!--      触发时间-->
      <a-form-item :label="t('apiScenario.schedule.task.schedule')">
        <a-select v-model:model-value="scheduleConfig.cron">
          <template #label="{ data }">
            <div class="flex items-center">
              {{ data.value }}
              <div class="ml-[4px] text-[var(--color-text-4)]">{{ data.label.split('?')[1] }}</div>
            </div>
          </template>
          <a-option v-for="item of syncFrequencyOptions" :key="item.value" :value="item.value" class="block">
            <div class="flex w-full items-center justify-between">
              {{ item.value }}
              <div class="ml-[4px] text-[var(--color-text-4)]">{{ item.label }}</div>
            </div>
          </a-option>
        </a-select>
      </a-form-item>
      <!--      环境选择-->
      <a-form-item :label="t('case.execute.selectEnv')">
        <a-radio-group v-model:model-value="scheduleUseNewEnv" type="radio">
          <a-radio :value="false"
            >{{ t('case.execute.defaultEnv') }}
            <a-tooltip :content="t('case.execute.defaultEnvTip')" position="top">
              <icon-question-circle
                class="text-[var(--color-text-brand)] hover:text-[rgb(var(--primary-5))]"
                size="16"
              />
            </a-tooltip>
          </a-radio>
          <a-radio :value="true">{{ t('case.execute.newEnv') }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <!--    新环境 -->
      <a-form-item
        v-if="scheduleUseNewEnv"
        field="config.environmentId"
        :label="t('case.execute.newEnv')"
        :rules="[{ required: true, message: t('apiTestManagement.envRequired') }]"
        asterisk-position="end"
        required
      >
        <a-select v-model="scheduleConfig.config.environmentId" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of environmentList" :key="item.id" :value="item.id">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <!--      运行资源池   设计稿中最后一行取消margin-bottom-->
      <a-form-item
        field="config.poolId"
        :label="t('apiScenario.schedule.config.resource_pool')"
        :rules="[{ required: true, message: t('apiTestManagement.poolRequired') }]"
        asterisk-position="end"
        required
        class="mb-0"
      >
        <a-select v-model="scheduleConfig.config.poolId" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of resourcePoolList" :key="item.id" :value="item.id">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="flex" :class="['justify-between']">
        <div class="flex flex-row items-center justify-center">
          <a-switch v-model="scheduleConfig.enable" class="mr-1" size="small" type="line" />
          <a-tooltip>
            <template #content>
              <span>
                {{ t('apiScenario.schedule.task.status.tooltip.one') }}
              </span>
              <br />
              <span>
                {{ t('apiScenario.schedule.task.status.tooltip.two') }}
              </span>
            </template>

            <span class="flex items-center">
              <span class="mr-1">{{ t('apiScenario.schedule.task.status') }}</span>
              <span class="mt-[2px]">
                <IconQuestionCircle class="h-[16px] w-[16px] text-[rgb(var(--primary-5))]" />
              </span>
            </span>
          </a-tooltip>
        </div>

        <div class="flex justify-end">
          <a-button type="secondary" :disabled="scheduleModalLoading" @click="cancelScheduleModal">
            {{ t('common.cancel') }}
          </a-button>
          <a-button class="ml-3" type="primary" :loading="scheduleModalLoading" @click="saveScheduleModal">
            {{ t('common.save') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>

  <!--   表格批量操作-->
  <a-modal
    v-model:visible="showBatchModal"
    title-align="start"
    class="ms-modal-upload ms-modal-medium"
    :width="480"
    @close="cancelBatch"
  >
    <template #title>
      {{ t('common.batchEdit') }}
      <div class="text-[var(--color-text-4)]">
        {{
          t('api_scenario.table.batchModalSubTitle', {
            count: batchParams.currentSelectCount || tableSelected.length,
          })
        }}
      </div>
    </template>
    <a-form ref="batchFormRef" class="rounded-[4px]" :model="batchForm" layout="vertical">
      <a-form-item
        field="attr"
        :label="t('api_scenario.table.chooseAttr')"
        :rules="[{ required: true, message: t('api_scenario.table.attrRequired') }]"
        asterisk-position="end"
      >
        <a-select v-model="batchForm.attr" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of fullAttrs" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-if="batchForm.attr === 'Tags'"
        field="values"
        :label="t('api_scenario.table.batchUpdate')"
        :validate-trigger="['blur', 'input']"
        :rules="[{ required: true, message: t('api_scenario.table.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
        required
      >
        <MsTagsInput
          v-model:model-value="batchForm.values"
          placeholder="common.tagsInputPlaceholder"
          allow-clear
          unique-value
          retain-input-value
        />
      </a-form-item>
      <a-form-item
        v-else-if="batchForm.attr === 'Priority'"
        field="value"
        :label="t('api_scenario.table.batchUpdate')"
        :rules="[{ required: true, message: t('api_scenario.table.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <a-select v-model="batchForm.value" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of scenarioPriorityList" :key="item.value" :value="item.value">
            <caseLevel :case-level="item.text as CaseLevel" />
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-else-if="batchForm.attr === 'Environment'"
        field="value"
        :label="t('api_scenario.table.batchUpdate')"
        :rules="[{ required: true, message: t('api_scenario.table.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <a-select v-model="batchForm.value" :placeholder="t('common.pleaseSelect')">
          <a-option v-for="item of environmentList" :key="item.id" :value="item.id">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
      <a-form-item
        v-else
        field="value"
        :label="t('api_scenario.table.batchUpdate')"
        :rules="[{ required: true, message: t('api_scenario.table.valueRequired') }]"
        asterisk-position="end"
        class="mb-0"
      >
        <a-select v-model="batchForm.value" :placeholder="t('common.pleaseSelect')" :disabled="batchForm.attr === ''">
          <a-option v-for="item of valueOptions" :key="item.value" :value="item.value">
            {{ t(item.name) }}
          </a-option>
        </a-select>
      </a-form-item>
    </a-form>
    <template #footer>
      <div class="flex" :class="[batchForm.attr === 'Tags' ? 'justify-between' : 'justify-end']">
        <div
          v-if="batchForm.attr === 'Tags'"
          class="flex flex-row items-center justify-center"
          style="padding-top: 10px"
        >
          <a-switch v-model="batchForm.append" class="mr-1" size="small" type="line" />
          <span class="flex items-center">
            <span class="mr-1">{{ t('caseManagement.featureCase.appendTag') }}</span>
            <span class="mt-[2px]">
              <a-tooltip>
                <IconQuestionCircle class="h-[16px] w-[16px] text-[rgb(var(--primary-5))]" />
                <template #content>
                  <div>{{ t('caseManagement.featureCase.enableTags') }}</div>
                  <div>{{ t('caseManagement.featureCase.closeTags') }}</div>
                </template>
              </a-tooltip>
            </span>
          </span>
        </div>
        <div class="flex justify-end">
          <a-button type="secondary" :disabled="batchUpdateLoading" @click="cancelBatch">
            {{ t('common.cancel') }}
          </a-button>
          <a-button class="ml-3" type="primary" :loading="batchUpdateLoading" @click="batchUpdate">
            {{ t('common.update') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
  <!--  </MsDialog>-->
  <a-modal
    v-model:visible="moveModalVisible"
    title-align="start"
    class="ms-modal-no-padding ms-modal-small"
    :mask-closable="false"
    :ok-text="
      t('api_scenario.table.batchMoveConfirm', {
        opt: batchOptionType,
        count: batchOptionScenarioCount,
      })
    "
    :ok-button-props="{ disabled: selectedBatchOptModuleKey === '' }"
    :cancel-button-props="{ disabled: scenarioBatchOptTreeLoading }"
    :on-before-ok="handleScenarioTreeOperation"
    @close="handleCancelScenarioTreeModal"
  >
    <template #title>
      <div class="flex items-center">
        <div class="float-left">
          {{ isBatchCopy ? t('common.batchCopy') : t('common.batchMove') }}
          <div
            class="one-line-text float-right ml-[4px] max-w-[100%] text-[var(--color-text-4)]"
            :title="t('api_scenario.table.batchModalSubTitle', { count: batchOptionScenarioCount })"
          >
            {{ t('api_scenario.table.batchModalSubTitle', { count: batchOptionScenarioCount }) }}
          </div>
        </div>
      </div>
    </template>
    <operationScenarioModuleTree
      v-if="moveModalVisible"
      :is-expand-all="true"
      :is-modal="true"
      :active-module="props.activeModule"
      @folder-node-select="folderNodeSelect"
    />
  </a-modal>
  <batch-run-modal
    v-model:visible="showBatchExecute"
    :batch-condition-params="batchConditionParams"
    :batch-params="batchParams"
    :table-selected="tableSelected"
    :batch-run-func="batchRunScenario"
    @finished="loadScenarioList"
  />
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { BatchActionParams, BatchActionQueryParams, MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import caseLevel from '@/components/business/ms-case-associate/caseLevel.vue';
  import type { CaseLevel } from '@/components/business/ms-case-associate/types';
  import type { MsTreeNodeData } from '@/components/business/ms-tree/types';
  import apiStatus from '@/views/api-test/components/apiStatus.vue';
  import ExecutionStatus from '@/views/api-test/report/component/reportStatus.vue';
  import BatchRunModal from '@/views/api-test/scenario/components/batchRunModal.vue';
  import operationScenarioModuleTree from '@/views/api-test/scenario/components/operationScenarioModuleTree.vue';
  import TableFilter from '@/views/case-management/caseManagementFeature/components/tableFilter.vue';

  import { getEnvList, getPoolId, getPoolOption } from '@/api/modules/api-test/management';
  import {
    batchEditScenario,
    batchOptionScenario,
    batchRecycleScenario,
    batchRunScenario,
    deleteScheduleConfig,
    getScenarioPage,
    recycleScenario,
    scenarioScheduleConfig,
    updateScenarioPro,
    updateScenarioStatus,
  } from '@/api/modules/api-test/scenario';
  import { getProjectOptions } from '@/api/modules/project-management/projectMember';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useTableStore from '@/hooks/useTableStore';
  import useAppStore from '@/store/modules/app';
  import { translateTextToPX } from '@/utils/css';
  import { hasAnyPermission } from '@/utils/permission';

  import { Environment } from '@/models/apiTest/management';
  import { ApiScenarioScheduleConfig, ApiScenarioTableItem, ApiScenarioUpdateDTO } from '@/models/apiTest/scenario';
  import { ResourcePoolItem } from '@/models/setting/resourcePool';
  import { ApiScenarioStatus } from '@/enums/apiEnum';
  import { ReportEnum, ReportStatus } from '@/enums/reportEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { casePriorityOptions } from '@/views/api-test/components/config';

  const props = defineProps<{
    class?: string;
    activeModule: string;
    offspringIds: string[];
    readOnly?: boolean; // 是否是只读模式
  }>();
  const emit = defineEmits<{
    (e: 'openScenario', record: ApiScenarioTableItem, action?: 'copy' | 'execute'): void;
    (e: 'refreshModuleTree', params: any): void;
    (e: 'createScenario'): void;
  }>();

  const lastReportStatusFilterVisible = ref(false);
  const lastReportStatusListFilters = ref<string[]>([]);
  const lastReportStatusFilters = computed(() => {
    return Object.keys(ReportStatus[ReportEnum.API_SCENARIO_REPORT]);
  });
  const createUserFilterVisible = ref(false);
  const createUserFilters = ref<string[]>([]);
  const updateUserFilterVisible = ref(false);
  const updateUserFilters = ref<string[]>([]);
  const memberOptions = ref<{ label: string; value: string }[]>([]);
  const appStore = useAppStore();
  const { t } = useI18n();
  const { openModal } = useModal();
  const tableRecord = ref<ApiScenarioTableItem>();
  const scheduleModalTitle = ref('');
  const priorityFilterVisible = ref(false);
  const priorityFilters = ref<string[]>([]);
  const scheduleConfig = ref<ApiScenarioScheduleConfig>({
    scenarioId: '',
    enable: true,
    cron: '',
    config: {
      poolId: '',
      grouped: false,
    },
  });

  const scheduleUseNewEnv = ref(false);

  const environmentList = ref<Environment[]>();
  const defaultPoolId = ref('');
  const resourcePoolList = ref<ResourcePoolItem[]>();

  // 初始化环境列表
  async function initEnvList() {
    environmentList.value = await getEnvList(appStore.currentProjectId);
  }
  // 初始化资源池
  async function initResourcePool() {
    try {
      defaultPoolId.value = await getPoolId(appStore.currentProjectId);
      resourcePoolList.value = await getPoolOption(appStore.currentProjectId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const scenarioPriorityList = ref([
    {
      value: 'P0',
      text: 'P0',
    },
    {
      value: 'P1',
      text: 'P1',
    },
    {
      value: 'P2',
      text: 'P2',
    },
    {
      value: 'P3',
      text: 'P3',
    },
  ]);
  const keyword = ref('');
  const moveModalVisible = ref(false);
  const isBatchMove = ref(false); // 是否批量移动场景
  const isBatchCopy = ref(false); // 是否批量复制场景
  const showBatchExecute = ref(false);

  let columns: MsTableColumn = [
    {
      title: 'ID',
      dataIndex: 'num',
      slotName: 'num',
      sortIndex: 1,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      fixed: 'left',
      width: 140,
      showTooltip: false,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiScenario.table.columns.name',
      dataIndex: 'name',
      slotName: 'name',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 134,
      showTooltip: true,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiScenario.table.columns.level',
      dataIndex: 'priority',
      slotName: 'priority',
      showDrag: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      titleSlotName: 'priorityFilter',
      width: 140,
    },
    {
      title: 'apiScenario.table.columns.status',
      dataIndex: 'status',
      slotName: 'status',
      titleSlotName: 'statusFilter',
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
      width: 140,
    },
    {
      title: 'apiScenario.table.columns.runResult',
      titleSlotName: 'lastReportStatusFilter',
      dataIndex: 'lastReportStatus',
      slotName: 'lastReportStatus',
      showTooltip: false,
      showDrag: true,
      width: 200,
    },
    {
      title: 'apiScenario.table.columns.tags',
      dataIndex: 'tags',
      isTag: true,
      isStringTag: true,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.scenarioEnv',
      dataIndex: 'environmentName',
      showDrag: true,
      width: 159,
      showTooltip: true,
    },
    {
      title: 'apiScenario.table.columns.steps',
      dataIndex: 'stepTotal',
      slotName: 'stepTotal',
      showInTable: false,
      showDrag: true,
      width: 100,
    },
    {
      title: 'apiScenario.table.columns.passRate',
      dataIndex: 'requestPassRate',
      showDrag: true,
      showInTable: false,
      width: 100,
    },
    {
      title: 'apiScenario.table.columns.module',
      dataIndex: 'modulePath',
      showTooltip: true,
      showInTable: false,
      showDrag: true,
      width: 176,
    },
    {
      title: 'apiScenario.table.columns.createTime',
      dataIndex: 'createTime',
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 189,
      showDrag: true,
    },
    {
      title: 'apiScenario.table.columns.updateTime',
      dataIndex: 'updateTime',
      showInTable: false,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      showDrag: true,
      width: 189,
    },
    {
      title: 'apiScenario.table.columns.createUser',
      dataIndex: 'createUserName',
      slotName: 'createUserName',
      showInTable: false,
      titleSlotName: 'createUserFilter',
      showTooltip: true,
      showDrag: true,
      width: 109,
    },
    {
      title: 'apiScenario.table.columns.updateUser',
      dataIndex: 'updateUserName',
      slotName: 'updateUserName',
      showInTable: false,
      titleSlotName: 'updateUserFilter',
      showTooltip: true,
      showDrag: true,
      width: 109,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 220,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getScenarioPage,
    {
      columns: props.readOnly ? columns : [],
      scroll: { x: '100%' },
      tableKey: TableKeyEnum.API_SCENARIO,
      showSetting: !props.readOnly,
      selectable: hasAnyPermission([
        'PROJECT_API_SCENARIO:READ+UPDATE',
        'PROJECT_API_SCENARIO:READ+EXECUTE',
        'PROJECT_API_SCENARIO:READ+DELETE',
      ]),
      showSelectAll: !props.readOnly,
      draggable: hasAnyPermission(['PROJECT_API_SCENARIO:READ+UPDATE']) ? { type: 'handle', width: 32 } : undefined,
      heightUsed: 374,
      showSubdirectory: true,
    },
    (item) => ({
      ...item,
      createTime: dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss'),
      updateTime: dayjs(item.updateTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  );
  const batchActions = {
    baseAction: [
      {
        label: 'common.edit',
        eventTag: 'edit',
        permission: ['PROJECT_API_SCENARIO:READ+UPDATE'],
      },
      {
        label: 'apiScenario.execute',
        eventTag: 'execute',
        permission: ['PROJECT_API_SCENARIO:READ+EXECUTE'],
      },
      {
        label: 'msTable.batch.moveTo',
        eventTag: 'moveTo',
        permission: ['PROJECT_API_SCENARIO:READ+UPDATE'],
      },
      {
        label: 'msTable.batch.copyTo',
        eventTag: 'copyTo',
        permission: ['PROJECT_API_SCENARIO:READ+ADD'],
      },
    ],
    moreAction: [
      {
        label: 'common.delete',
        eventTag: 'delete',
        permission: ['PROJECT_API_SCENARIO:READ+DELETE'],
        danger: true,
      },
    ],
  };

  function getTableMoreActionList(tableRow: ApiScenarioTableItem) {
    if (tableRow.scheduleConfig) {
      // 删除定时任务
      return [
        {
          eventTag: 'updateSchedule',
          label: t('apiScenario.schedule.update'),
          permission: ['PROJECT_API_SCENARIO:READ+EXECUTE'],
          danger: false,
        },
        {
          eventTag: 'deleteSchedule',
          label: t('apiScenario.schedule.delete'),
          permission: ['PROJECT_API_SCENARIO:READ+EXECUTE'],
          danger: false,
        },
        {
          eventTag: 'delete',
          label: t('common.delete'),
          permission: ['PROJECT_API_SCENARIO:READ+DELETE'],
          danger: true,
        },
      ];
    }
    return [
      {
        eventTag: 'schedule',
        label: t('apiScenario.schedule.create'),
        permission: ['PROJECT_API_SCENARIO:READ+EXECUTE'],
        danger: false,
      },
      {
        eventTag: 'delete',
        label: t('common.delete'),
        permission: ['PROJECT_API_SCENARIO:READ+DELETE'],
        danger: true,
      },
    ];
  }

  const statusFilterVisible = ref(false);
  const statusFilters = ref<string[]>([]);
  const tableStore = useTableStore();

  const activeModules = computed(() => {
    return props.activeModule === 'all' ? [] : [props.activeModule];
  });

  const batchConditionParams = computed(() => {
    return {
      condition: {
        keyword: keyword.value,
        filter: {
          status: statusFilters.value,
        },
      },
      projectId: appStore.currentProjectId,
      moduleIds: activeModules.value,
    };
  });

  async function loadScenarioList(refreshTreeCount?: boolean) {
    let moduleIds: string[] = [];
    if (props.activeModule && props.activeModule !== 'all') {
      moduleIds = [props.activeModule];
      const getAllChildren = await tableStore.getSubShow(TableKeyEnum.API_SCENARIO);
      if (getAllChildren) {
        moduleIds = [props.activeModule, ...props.offspringIds];
      }
    }
    memberOptions.value = await getProjectOptions(appStore.currentProjectId, keyword.value);
    memberOptions.value = memberOptions.value.map((e: any) => ({ label: e.name, value: e.id }));
    const params = {
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
      moduleIds,
      filter: {
        lastReportStatus: lastReportStatusListFilters.value,
        status: statusFilters.value,
        priority: priorityFilters.value,
        createUser: createUserFilters.value,
        updateUser: updateUserFilters.value,
      },
    };
    setLoadListParams(params);
    await loadList();
    if (refreshTreeCount) {
      emit('refreshModuleTree', params);
    }
  }

  function handleFilterHidden(val: boolean) {
    if (!val) {
      lastReportStatusFilterVisible.value = false;
      statusFilterVisible.value = false;
      priorityFilterVisible.value = false;
      loadScenarioList(true);
    }
  }

  function resetStatusFilter() {
    statusFilterVisible.value = false;
    statusFilters.value = [];
    loadScenarioList(true);
  }

  function resetPriorityFilter() {
    priorityFilterVisible.value = false;
    priorityFilters.value = [];
    loadScenarioList(true);
  }

  function resetLastReportStatusFilter() {
    lastReportStatusFilterVisible.value = false;
    lastReportStatusListFilters.value = [];
    loadScenarioList(true);
  }

  async function handleStatusChange(record: ApiScenarioUpdateDTO) {
    try {
      await updateScenarioStatus(record.id, record.status);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  async function handlePriorityStatusChange(record: ApiScenarioUpdateDTO) {
    try {
      await updateScenarioPro(record.id, record.priority);
      Message.success(t('common.updateSuccess'));
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  const tableSelected = ref<(string | number)[]>([]);

  const batchParams = ref<BatchActionQueryParams>({
    selectedIds: [],
    selectAll: false,
    excludeIds: [],
    currentSelectCount: 0,
  });

  /**
   * 删除接口
   */
  function deleteScenario(record?: ApiScenarioTableItem, isBatch?: boolean, params?: BatchActionQueryParams) {
    let title = t('api_scenario.table.deleteScenarioTipTitle', { name: record?.name });
    let selectIds = [record?.id || ''];
    if (isBatch) {
      title = t('api_scenario.table.batchDeleteScenarioTip', {
        count: params?.currentSelectCount || tableSelected.value.length,
      });
      selectIds = tableSelected.value as string[];
    }
    let content = t('api_scenario.table.deleteScenarioTip');
    if (isBatch) {
      content = `${t('api_scenario.table.deleteScenarioTip.schedule1')}${t(
        'api_scenario.table.deleteScenarioTip.schedule2'
      )}`;
    } else if (record?.scheduleConfig) {
      content = `${t('api_scenario.table.deleteScenarioTip.schedule1')}${t(
        'api_scenario.table.deleteScenarioTip.schedule2'
      )}`;
    }

    openModal({
      type: 'error',
      title,
      content,
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          if (isBatch) {
            await batchRecycleScenario({
              selectIds,
              selectAll: !!params?.selectAll,
              excludeIds: params?.excludeIds || [],
              condition: { keyword: keyword.value },
              projectId: appStore.currentProjectId,
              moduleIds: props.activeModule === 'all' ? [] : [props.activeModule],
              deleteAll: true,
            });
          } else {
            await recycleScenario(record?.id as string);
          }
          Message.success(t('common.deleteSuccess'));
          resetSelector();
          loadScenarioList(true);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const showScheduleModal = ref(false);
  const syncFrequencyOptions = [
    { label: t('apiTestManagement.timeTaskHour'), value: '0 0 0/1 * * ?' },
    { label: t('apiTestManagement.timeTaskSixHour'), value: '0 0 0/6 * * ?' },
    { label: t('apiTestManagement.timeTaskTwelveHour'), value: '0 0 0/12 * * ?' },
    { label: t('apiTestManagement.timeTaskDay'), value: '0 0 0 * * ?' },
  ];

  async function resetScheduleConfig(record: ApiScenarioTableItem) {
    // 初始化资源池
    await initResourcePool();

    // 初始化已选择的表格数据
    tableRecord.value = record;
    if (record.scheduleConfig) {
      scheduleConfig.value = cloneDeep(record.scheduleConfig);
    } else {
      // 初始化定时任务配置
      scheduleConfig.value = {
        scenarioId: record.id,
        enable: true,
        cron: '0 0 0/1 * * ?',
        config: {
          poolId: defaultPoolId?.value,
          grouped: false,
        },
      };
    }
    scheduleUseNewEnv.value = !!scheduleConfig.value.config.environmentId;
    // 初始化环境
    await initEnvList();

    // 初始化弹窗标题
    if (tableRecord.value.scheduleConfig) {
      scheduleModalTitle.value = t('apiScenario.schedule.update');
    } else {
      scheduleModalTitle.value = t('apiScenario.schedule.create');
    }
  }

  async function openScheduleModal(record: ApiScenarioTableItem) {
    await resetScheduleConfig(record);
    showScheduleModal.value = true;
  }
  async function deleteScenarioSchedule(scenarioId: string) {
    try {
      await deleteScheduleConfig(scenarioId);
      Message.success(t('common.deleteSuccess'));
      loadScenarioList(false);
    } catch (error) {
      console.log(error);
    }
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleTableMoreActionSelect(item: ActionsItem, record: ApiScenarioTableItem) {
    switch (item.eventTag) {
      case 'delete':
        deleteScenario(record);
        break;
      case 'schedule':
        openScheduleModal(record);
        break;
      case 'deleteSchedule':
        deleteScenarioSchedule(record.id);
        break;
      case 'updateSchedule':
        openScheduleModal(record);
        break;
      default:
        break;
    }
  }

  const scheduleConfigRef = ref<FormInstance>();
  const scheduleModalLoading = ref(false);

  function cancelScheduleModal() {
    showScheduleModal.value = false;
  }
  function saveScheduleModal() {
    scheduleConfigRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          scheduleModalLoading.value = true;
          await scenarioScheduleConfig({ ...scheduleConfig.value });
          // 初始化弹窗标题
          if (tableRecord.value?.scheduleConfig) {
            Message.success(t('common.updateSuccess'));
          } else {
            Message.success(t('common.createSuccess'));
          }
          cancelScheduleModal();
          resetSelector();
          loadScenarioList(true);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          scheduleModalLoading.value = false;
        }
      }
    });
  }

  /**
   * 处理表格选中
   */
  function handleTableSelect(arr: (string | number)[]) {
    tableSelected.value = arr;
  }

  const showBatchModal = ref(false);
  const batchUpdateLoading = ref(false);

  const batchFormRef = ref<FormInstance>();

  const batchForm = ref({
    attr: '',
    value: '',
    values: [],
    append: false,
  });
  const fullAttrs = [
    {
      name: 'apiScenario.table.columns.level',
      value: 'Priority',
    },
    {
      name: 'api_scenario.table.apiStatus',
      value: 'Status',
    },
    {
      name: 'apiScenario.params.tag',
      value: 'Tags',
    },
    {
      name: 'project.environmental.env',
      value: 'Environment',
    },
  ];

  const valueOptions = computed(() => {
    switch (batchForm.value.attr) {
      case 'Status':
        return [
          {
            name: 'api_scenario.table.status.underway',
            value: ApiScenarioStatus.UNDERWAY,
          },
          {
            name: 'api_scenario.table.status.completed',
            value: ApiScenarioStatus.COMPLETED,
          },
          {
            name: 'api_scenario.table.status.deprecate',
            value: ApiScenarioStatus.DEPRECATED,
          },
        ];
      default:
        return [];
    }
  });

  function cancelBatch() {
    showBatchModal.value = false;
    batchFormRef.value?.resetFields();
    batchForm.value = {
      attr: '',
      value: '',
      values: [],
      append: false,
    };
  }

  function batchUpdate() {
    batchFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          batchUpdateLoading.value = true;

          // value: 'PRIORITY',
          //    value: 'STATUS',
          const batchEditParam = {
            selectIds: batchParams.value?.selectedIds || [],
            selectAll: !!batchParams.value?.selectAll,
            excludeIds: batchParams.value?.excludeIds || [],
            condition: { keyword: keyword.value },
            projectId: appStore.currentProjectId,
            moduleIds: props.activeModule === 'all' ? [] : [props.activeModule],
            type: batchForm.value?.attr,
            priority: '',
            status: '',
            tags: [],
            append: batchForm.value.append,
            grouped: false,
            envId: '',
          };

          if (batchForm.value.attr === 'Priority') {
            batchEditParam.priority = batchForm.value.value;
          } else if (batchForm.value.attr === 'Status') {
            batchEditParam.status = batchForm.value.value;
          } else if (batchForm.value.attr === 'Tags') {
            batchEditParam.tags = batchForm.value.values;
          } else if (batchForm.value.attr === 'Environment') {
            batchEditParam.envId = batchForm.value.value;
          }

          await batchEditScenario(batchEditParam);
          Message.success(t('common.updateSuccess'));
          cancelBatch();
          resetSelector();
          loadScenarioList(true);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          batchUpdateLoading.value = false;
        }
      }
    });
  }

  const selectedBatchOptModuleKey = ref(''); // 移动文件选中节点
  const selectedBatchOptModuleName = ref(''); // 移动文件选中节点  用于页面文案显示
  const batchOptionType = ref(''); // 批量操作类型  用于页面提示语
  const batchOptionScenarioCount = ref<number>(0);
  const activeScenario = ref<ApiScenarioTableItem | null>(null); // 当前查看的接口项
  const scenarioBatchOptTreeLoading = ref(false); // 批量移动文件loading

  /**
   * 批量接口
   */
  async function handleScenarioTreeOperation() {
    try {
      scenarioBatchOptTreeLoading.value = true;
      let optionType = '';
      if (isBatchMove.value) {
        optionType = 'batchMove';
      } else if (isBatchCopy.value) {
        optionType = 'batchCopy';
      }
      const res = await batchOptionScenario(optionType, {
        selectIds: batchParams.value?.selectedIds || [],
        selectAll: !!batchParams.value?.selectAll,
        excludeIds: batchParams.value?.excludeIds || [],
        condition: { keyword: keyword.value },
        projectId: appStore.currentProjectId,
        moduleIds: props.activeModule === 'all' ? [] : [props.activeModule],
        targetModuleId: selectedBatchOptModuleKey.value,
      });

      Message.success(
        t('api_scenario.batch_operation.success', {
          opt: batchOptionType.value,
          success: res?.success,
          error: res?.error,
        })
      );
      tableSelected.value = [];
      activeScenario.value = null;
      loadScenarioList(true);
      resetSelector();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      scenarioBatchOptTreeLoading.value = false;
    }
  }

  function handleCancelScenarioTreeModal() {
    moveModalVisible.value = false;
    isBatchMove.value = false;
    isBatchCopy.value = false;
    selectedBatchOptModuleKey.value = '';
    selectedBatchOptModuleName.value = '';
    batchOptionType.value = '';
    batchOptionScenarioCount.value = 0;
  }

  /**
   * 处理文件夹树节点选中事件
   */
  function folderNodeSelect(node: MsTreeNodeData) {
    selectedBatchOptModuleKey.value = node.id;
    if (node.name != null) {
      selectedBatchOptModuleName.value = node.name;
    }
  }

  /**
   * 处理表格选中后批量操作
   * @param event 批量操作事件对象
   */
  function handleTableBatch(event: BatchActionParams, params: BatchActionQueryParams) {
    tableSelected.value = params?.selectedIds || [];
    batchParams.value = params;
    switch (event.eventTag) {
      case 'delete':
        deleteScenario(undefined, true, batchParams.value);
        break;
      case 'edit':
        initEnvList();
        showBatchModal.value = true;
        break;
      case 'moveTo':
        batchOptionType.value = t('common.move');
        if (params.currentSelectCount != null) {
          batchOptionScenarioCount.value = params.currentSelectCount;
        }
        isBatchMove.value = true;
        moveModalVisible.value = true;
        break;
      case 'copyTo':
        batchOptionType.value = t('common.copy');
        if (params.currentSelectCount != null) {
          batchOptionScenarioCount.value = params.currentSelectCount;
        }
        isBatchCopy.value = true;
        moveModalVisible.value = true;
        break;
      case 'execute':
        showBatchExecute.value = true;
        break;
      default:
        break;
    }
  }

  function openScenarioTab(record: ApiScenarioTableItem, action?: 'copy' | 'execute') {
    emit('openScenario', record, action);
  }

  defineExpose({
    loadScenarioList,
  });

  if (!props.readOnly) {
    await tableStore.initColumn(TableKeyEnum.API_SCENARIO, columns, 'drawer', true);
  } else {
    columns = columns.filter(
      (item) => !['version', 'createTime', 'updateTime', 'operation'].includes(item.dataIndex as string)
    );
  }

  onBeforeMount(() => {
    loadScenarioList();
  });
  watch(
    () => props.activeModule,
    () => {
      resetSelector();
      loadScenarioList();
    }
  );
  watch(
    () => batchForm.value.attr,
    () => {
      batchForm.value.value = '';
    }
  );
</script>

<style lang="less" scoped>
  :deep(.param-input:not(.arco-input-focus, .arco-select-view-focus)) {
    &:not(:hover) {
      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-brand);
      }
    }
  }
</style>
