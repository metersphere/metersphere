<template>
  <div class="overflow-y-auto">
    <div class="flex flex-wrap items-start gap-[8px]">
      <div class="flex-1">
        <component
          :is="form.searchKey.type"
          v-bind="form.searchKey.props"
          v-model="form.searchKey.value"
          @change="searchKeyChange"
        >
          <a-optgroup
            v-for="(group, i) of props.selectGroupList"
            :key="`${group.label as string}-${i}`"
            :label="group.label"
          >
            <a-option
              v-for="groupOptions of group.options"
              :key="groupOptions.value"
              :value="groupOptions.value"
              :disabled="isDisabledList.indexOf(groupOptions.value) > -1"
              >{{ groupOptions.label }}</a-option
            >
          </a-optgroup>
        </component>
      </div>
      <div class="w-[100px]">
        <component
          :is="form.operatorCondition.type"
          v-bind="form.operatorCondition.props"
          v-model="form.operatorCondition.value"
          @change="operatorChangeHandler"
        >
          <a-option v-for="operator of form.operatorCondition.options" :key="operator.value" :value="operator.value">
            {{ t(operator.label) }}
          </a-option>
        </component>
      </div>
      <div class="flex flex-1">
        <a-form ref="queryContentFormRef" :model="form.queryContent">
          <a-form-item
            required
            field="value"
            :rules="form.queryContent.rules || [{ required: true, message: '请输入筛选内容' }]"
            hide-label
            hide-asterisk
            class="mb-0"
          >
            <TimerSelect
              v-if="form.queryContent.type === 'time-select'"
              :model-value="form.queryContent.value"
              v-bind="form.queryContent.props"
              :operation-type="form.operatorCondition.value"
              @update-time="updateTimeValue"
            />
            <component
              :is="form.queryContent.type"
              v-else
              v-bind="form.queryContent.props"
              v-model="form.queryContent.value"
              @change="filterKeyChange"
            >
              <template v-if="form.queryContent.type === 'a-select'">
                <a-option v-for="opt of form.queryContent.options" :key="opt.value" :value="opt.value">{{
                  opt.label
                }}</a-option>
              </template>
              <template v-else-if="form.queryContent.type === 'a-select-group'">
                <a-select v-model="form.queryContent.value" v-bind="form.queryContent.props">
                  <a-optgroup v-for="group of form.searchKey.options" :key="group.id" :label="group.label">
                    <a-option v-for="groupOptions of group.options" :key="groupOptions.id" :value="groupOptions.id">{{
                      groupOptions.label
                    }}</a-option>
                  </a-optgroup>
                </a-select>
              </template>
            </component>
          </a-form-item>
        </a-form>
        <div class="minus"> <slot></slot></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref, watchEffect } from 'vue';
  import { SelectOptionData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import TimerSelect from './time-select.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { TEST_PLAN_TEST_CASE } from './caseUtils';
  import type { SearchKeyType } from './type';
  import type { FormInstance } from '@arco-design/web-vue';

  const { t } = useI18n();

  const props = defineProps<{
    formItem: Record<string, any>; // 当前筛选项
    index: number; // 当前操作的项索引
    formList: Record<string, any>[]; // 全部项列表筛选项列表
    selectGroupList: SelectOptionData[]; // 条件字段筛选下拉列表
  }>();

  const emits = defineEmits(['dataUpdated']);

  const form = ref({ ...cloneDeep(props.formItem) });

  watchEffect(() => {
    form.value = { ...cloneDeep(props.formItem) };
  });

  // 一级属性变化回调
  const searchKeyChange = (value: string) => {
    const { operatorCondition, queryContent } = form.value;
    operatorCondition.value = '';
    operatorCondition.options = [];
    queryContent.value = '';
    // 获取当前选中查询Key属性的配置项
    const currentKeysConfig = TEST_PLAN_TEST_CASE.find((item: any) => item.key === value);
    if (currentKeysConfig) {
      operatorCondition.options = currentKeysConfig.operator.options;
      operatorCondition.value = currentKeysConfig.operator.options[0].value;
      queryContent.type = currentKeysConfig.type;
      if (currentKeysConfig.rules) {
        queryContent.rules = currentKeysConfig.rules;
      }
    }
    emits('dataUpdated', form.value, props.index);
  };

  // 禁用已选择选项
  const isDisabledList = computed(() => {
    return props.formList.map((item) => item.searchKey.value) || [];
  });

  // 运算符条件发生变化回调
  const operatorChangeHandler = (value: string) => {
    form.value.queryContent.value = value === 'between' ? [] : '';
    emits('dataUpdated', form.value, props.index);
  };

  // 更新数据
  const filterKeyChange = () => {
    emits('dataUpdated', form.value, props.index);
  };

  // 更新时间值
  const updateTimeValue = (time: string | []) => {
    form.value.queryContent.value = time;
    emits('dataUpdated', form.value, props.index);
  };
  const queryContentFormRef = ref<FormInstance>();

  // 校验表单
  const validateQueryContent = (callBack: (isSuccess: string) => void) => {
    queryContentFormRef.value?.validate((errors) => {
      if (!errors) {
        callBack('ok');
      } else {
        callBack('no');
      }
    });
  };

  defineExpose({
    validateQueryContent,
  });
</script>

<style scoped></style>
