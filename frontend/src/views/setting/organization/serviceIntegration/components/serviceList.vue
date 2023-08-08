<template>
  <MsCard simple auto-height class="mb-[16px]">
    <div class="mb-[16px] flex justify-between">
      <div class="font-medium text-[var(--color-text-000)]">{{ t('system.config.baseInfo') }}</div>
      <div>
        <a-input-search
          v-model="keyword"
          :placeholder="t('organization.member.searchMember')"
          class="w-[230px]"
          :max-length="250"
        />
      </div>
    </div>
    <div class="ms-card-wrap">
      <CardList :data-source="data" :wrapper-height="496">
        <template #default="{ item }">
          <div class="item">
            <div class="flex">
              <span class="icon float-left mr-2 h-[40px] w-[40px]">{{ item.name }}</span>
              <div class="flex flex-col justify-start">
                <p>
                  <span class="mr-4 font-semibold">TAPD</span>
                  <span v-if="!item.isConfig" class="ms-enable">{{ t('organization.service.unconfigured') }}</span>
                  <span
                    v-else
                    class="ms-enable active"
                    :style="{
                      background: 'rgb(var(--success-1))',
                      color: 'rgb(var(--success-6))',
                    }"
                    >{{ t('organization.service.configured') }}</span
                  >
                </p>
                <p class="mt-2 text-sm text-[var(--color-text-4)]">一站式敏捷研发协作云平台</p>
              </div>
            </div>
            <div class="flex justify-between">
              <a-space>
                <a-tooltip v-if="!item.isConfig" :content="t('organization.service.unconfiguredTip')" position="tl">
                  <span>
                    <a-button
                      type="outline"
                      class="arco-btn-outline--secondary"
                      size="mini"
                      :disabled="!item.isConfig"
                      >{{ t('organization.service.testLink') }}</a-button
                    ></span
                  >
                </a-tooltip>
                <a-button v-else type="outline" class="arco-btn-outline--secondary" size="mini">{{
                  t('organization.service.testLink')
                }}</a-button>
                <a-button type="outline" class="arco-btn-outline--secondary" size="mini" @click="editHanlder(item)">{{
                  t('organization.service.edit')
                }}</a-button>
              </a-space>
              <span>
                <a-tooltip v-if="!item.isConfig" :content="t('organization.service.unconfiguredTip')" position="br">
                  <span><a-switch size="small" :disabled="true" /></span>
                </a-tooltip>
                <a-switch v-else size="small" />
              </span>
            </div>
          </div>
        </template>
      </CardList>
    </div>
  </MsCard>
  <ShowModal v-model:visible="serviceVisible" :rule="createRules" />
</template>

<script setup lang="ts">
  import { ref, onMounted, shallowRef } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import CardList from './cardList.vue';
  import ShowModal from './showModal.vue';
  import PassWord from '@/components/pure/ms-form-create/formcreate-password.vue';

  const { t } = useI18n();

  const keyword = ref('');
  const data = ref([
    {
      id: '1-1-1-1',
      name: 'xxx',
      enable: true,
      isConfig: false,
    },
    {
      id: '2-2-2-2',
      name: 'xxx',
      enable: false,
      isConfig: true,
    },
    {
      id: '3-3-3-3',
      name: 'xxx',
      enable: false,
    },
    {
      id: 'xxxx',
      name: 'xxx',
      enable: false,
    },
    {
      id: 'xxxx',
      name: 'xxx',
      enable: false,
    },
    {
      id: '3-3-3-3',
      name: 'xxx',
      enable: false,
    },
    {
      id: 'xxxx',
      name: 'xxx',
      enable: false,
    },
    {
      id: 'xxxx',
      name: 'xxx',
      enable: false,
    },
  ]);

  const serviceVisible = ref<boolean>(false);

  const createRules = ref<any>([]);

  const editHanlder = (item: any) => {
    serviceVisible.value = true;
  };
  onMounted(() => {
    setTimeout(() => {
      const result = JSON.stringify([
        {
          type: 'input',
          field: 'address',
          title: 'JIRA地址',
          value: 'JIRA地址',
          validate: [{ type: 'string', required: true, message: 'JIRA地址不能为空' }],
          emit: ['blur'],
          emitPrefix: 'prefix1',
        },
        {
          type: 'radio',
          title: '认证方式',
          field: 'method',
          value: '1',
          options: [
            {
              value: '1',
              label: 'Auth',
            },
            {
              value: '2',
              label: 'Token',
            },
          ],
          props: {
            type: 'button',
          },
          wrap: {
            tooltip: 'info提示',
          },
          control: [
            {
              value: '1',
              rule: [
                {
                  type: 'input',
                  field: 'info',
                  title: 'JIRA-账号',
                  value: '账号',
                  validate: [{ type: 'string', required: true, message: 'JIRA账号不能为空' }],
                  wrap: {},
                },
                {
                  type: 'PassWord',
                  value: '',
                  field: 'password',
                  title: 'JIRA密码',
                  validate: [{ type: 'string', required: true, message: 'JIRA密码不能为空' }],
                  component: shallowRef(PassWord),
                  props: {
                    placeholder: '请输入密码',
                  },
                },
              ],
            },
            {
              value: '2',
              rule: [
                {
                  type: 'input',
                  field: 'token',
                  title: 'Token',
                  value: 'token 账号',
                  validate: [{ type: 'string', required: true, message: 'JIRA账号不能为空' }],
                  wrap: {},
                },
              ],
            },
          ],
        },
      ]);
      createRules.value = JSON.parse(result);
    }, 1000);
  });
</script>

<style scoped lang="less">
  .ms-card-wrap {
    padding: 16px;
    min-width: 1150px;
    min-height: 300px;
    border-radius: var(--border-radius-small);
    background: var(--color-text-n9);
  }
  .item {
    padding: 16px;
    height: 100%;
    border-radius: 4px;
    background: white;
    @apply flex flex-col justify-between;
    .ms-enable {
      border-radius: var(--border-radius-small);
      background: var(--color-text-n9);
      @apply px-2 py-1 text-xs;
    }
  }
</style>
