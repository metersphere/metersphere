<template>
  <MsCard simple auto-height>
    <div class="filter-box">
      <a-input
        v-model:model-value="operUser"
        class="filter-item"
        :placeholder="t('system.log.operaterPlaceholder')"
        allow-clear
      >
        <template #prefix>
          {{ t('system.log.operater') }}
        </template>
      </a-input>
      <a-range-picker
        v-model:model-value="time"
        show-time
        :time-picker-props="{
          defaultValue: ['00:00:00', '00:00:00'],
        }"
        :disabled-date="disabledDate"
        class="filter-item"
      >
        <template #prefix>
          {{ t('system.log.operateTime') }}
        </template>
      </a-range-picker>
      <MsCascader
        v-model="operateRange"
        :options="rangeOptions"
        :prefix="t('system.log.operateRange')"
        :level-top="levelTop"
        :virtual-list-props="{ height: 200 }"
      />
      <a-select v-model:model-value="type" class="filter-item">
        <template #prefix>
          {{ t('system.log.operateType') }}
        </template>
        <a-option v-for="opt of typeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-option>
      </a-select>
      <MsCascader
        v-model="_module"
        :options="moduleOptions"
        mode="native"
        :prefix="t('system.log.operateTarget')"
        :virtual-list-props="{ height: 200 }"
        :placeholder="t('system.log.operateTargetPlaceholder')"
      />
      <a-input
        v-model:model-value="content"
        class="filter-item"
        :placeholder="t('system.log.operateNamePlaceholder')"
        allow-clear
      >
        <template #prefix>
          {{ t('system.log.operateName') }}
        </template>
      </a-input>
    </div>
    <a-button type="outline">{{ t('system.log.search') }}</a-button>
    <a-button type="outline" class="arco-btn-outline--secondary ml-[8px]" @click="resetFilter">
      {{ t('system.log.reset') }}
    </a-button>
  </MsCard>
</template>

<script lang="ts" setup>
  import { onBeforeMount, ref } from 'vue';
  import dayjs from 'dayjs';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import { useI18n } from '@/hooks/useI18n';
  import { getLogOptions } from '@/api/modules/setting/log';
  import MsCascader from '@/components/bussiness/ms-cascader/index.vue';

  import type { CascaderOption } from '@arco-design/web-vue';

  const { t } = useI18n();

  const operUser = ref(''); // 操作人
  const levelTop = ['SYSTEM', 'ORGANIZATION', 'PROJECT']; // 操作范围顶级
  const operateRange = ref<(string | number | Record<string, any>)[]>([levelTop[0]]); // 操作范围
  const type = ref(''); // 操作类型
  const _module = ref(''); // 操作对象
  const content = ref(''); // 名称
  const time = ref<(Date | string | number)[]>([]); // 操作时间
  const rangeOptions = ref<CascaderOption[]>([
    {
      value: {
        level: 0,
        value: levelTop[0],
      },
      label: t('system.log.system'),
      isLeaf: true,
    },
  ]);
  const moduleOptions = ref<CascaderOption[]>([
    {
      value: 'system',
      label: t('system.log.system'),
      isLeaf: true,
    },
  ]);
  const typeOptions = [
    {
      label: t('system.log.operateType.all'),
      value: '',
    },
    {
      label: t('system.log.operateType.add'),
      value: 'ADD',
    },
    {
      label: t('system.log.operateType.delete'),
      value: 'DELETE',
    },
    {
      label: t('system.log.operateType.update'),
      value: 'UPDATE',
    },
    {
      label: t('system.log.operateType.debug'),
      value: 'DEBUG',
    },
    {
      label: t('system.log.operateType.review'),
      value: 'REVIEW',
    },
    {
      label: t('system.log.operateType.copy'),
      value: 'COPY',
    },
    {
      label: t('system.log.operateType.execute'),
      value: 'EXECUTE',
    },
    {
      label: t('system.log.operateType.share'),
      value: 'SHARE',
    },
    {
      label: t('system.log.operateType.restore'),
      value: 'RESTORE',
    },
    {
      label: t('system.log.operateType.import'),
      value: 'IMPORT',
    },
    {
      label: t('system.log.operateType.export'),
      value: 'EXPORT',
    },
    {
      label: t('system.log.operateType.login'),
      value: 'LOGIN',
    },
    {
      label: t('system.log.operateType.select'),
      value: 'SELECT',
    },
    {
      label: t('system.log.operateType.recover'),
      value: 'RECOVER',
    },
    {
      label: t('system.log.operateType.logout'),
      value: 'LOGOUT',
    },
  ];

  onBeforeMount(async () => {
    try {
      const res = await getLogOptions();
      rangeOptions.value.push({
        value: {
          level: 0,
          value: levelTop[1],
        },
        label: t('system.log.orgnization'),
        children: res.organizationList.map((e) => ({
          value: {
            level: levelTop[1],
            value: e.id,
          },
          label: e.name,
          isLeaf: true,
        })),
      });
      rangeOptions.value.push({
        value: {
          level: 0,
          value: levelTop[2],
        },
        label: t('system.log.project'),
        children: res.projectList.map((e) => ({
          value: {
            level: levelTop[2],
            value: e.id,
          },
          label: e.name,
          isLeaf: true,
        })),
      });
    } catch (error) {
      console.log(error);
    }
  });

  function disabledDate(current?: Date) {
    const now = dayjs();
    const endDate = dayjs(current);
    return !!current && endDate.isAfter(now);
  }

  function resetFilter() {
    operUser.value = '';
    operateRange.value = [levelTop[0]];
    type.value = '';
    _module.value = '';
    content.value = '';
    time.value = [];
  }
</script>

<style lang="less" scoped>
  .filter-box {
    @apply grid;

    margin-bottom: 16px;
    gap: 16px;
  }
  @media screen and (max-width: 1400px) {
    .filter-box {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  @media screen and (min-width: 1400px) {
    .filter-box {
      grid-template-columns: repeat(3, 1fr);
    }
  }
</style>
