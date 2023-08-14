<template>
  <MsCard simple class="mb-[16px]" auto-height>
    <div class="outer-wrapper">
      <div class="mb-[16px] flex justify-between">
        <div class="font-medium text-[var(--color-text-000)]">{{ t('organization.service.serviceIntegration') }}</div>
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
        <a-scrollbar
          :style="{
            overflow: 'auto',
            height: `calc(100vh - ${collapseHeight} - 220px)`,
          }"
        >
          <div class="list">
            <div v-for="item of data" :key="item.id" class="item">
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
                  <a-button type="outline" class="arco-btn-outline--secondary" size="mini" @click="editHandler(item)">{{
                    t('organization.service.edit')
                  }}</a-button>
                  <a-button
                    v-if="item.isConfig"
                    type="outline"
                    class="arco-btn-outline--secondary"
                    size="mini"
                    @click="resetHandler(item)"
                    >{{ t('organization.service.reset') }}</a-button
                  >
                </a-space>
                <span>
                  <a-tooltip v-if="!item.isConfig" :content="t('organization.service.unconfiguredTip')" position="br">
                    <span><a-switch size="small" :disabled="true" /></span>
                  </a-tooltip>
                  <a-switch v-else size="small" />
                </span>
              </div>
            </div>
          </div>
        </a-scrollbar>
      </div>
    </div>
  </MsCard>
  <ConfigModal v-model:visible="serviceVisible" :rule="createRules" />
</template>

<script setup lang="ts">
  import { ref, onMounted, reactive } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import ConfigModal from './conifgModal.vue';

  const { t } = useI18n();

  const props = defineProps<{
    collapseHeight: string;
  }>();

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
      id: '3-3-3-3',
      name: 'xxx',
      enable: false,
    },
  ]);

  const serviceVisible = ref<boolean>(false);
  let createRules = reactive([]);

  const editHandler = (item: any) => {
    serviceVisible.value = true;
  };
  const resetHandler = (item: any) => {};

  onMounted(() => {
    setTimeout(() => {
      const result = JSON.stringify([
        {
          type: 'input',
          field: 'address',
          title: 'JIRA地址',
          value: 'JIRA地址',
          validate: [{ type: 'string', required: true, message: 'JIRA地址不能为空' }],
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
      createRules = JSON.parse(result);
    }, 1000);
  });
</script>

<style scoped lang="less">
  .ms-card-wrap {
    overflow: hidden;
    padding: 8px;
    min-width: 1150px;
    height: calc(100% - 58px) !important;
    min-height: 300px;
    border-radius: var(--border-radius-small);
    background: var(--color-text-n9);
    .list {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-start;
      align-content: flex-start;
      width: 100%;
      .item {
        margin: 8px;
        padding: 24px;
        height: 144px;
        border-radius: 4px;
        background: white;
        flex-basis: calc(25% - 16px);
        @apply flex flex-col justify-between;
        .icon {
          border: 1px solid var(--color-text-n9);
        }
        .ms-enable {
          border-radius: var(--border-radius-small);
          background: var(--color-text-n9);
          @apply px-2 py-1 text-xs;
        }
      }
      @media screen and (max-width: 992px) {
        .item {
          flex-basis: calc(50% - 16px);
        }
      }
      @media screen and (min-width: 1000px) and (max-width: 1440px) {
        .item {
          flex-basis: calc(33.3% - 16px);
        }
      }

      @media screen and (max-width: 1600px) and (min-width: 1800px) {
        .item {
          flex-basis: calc(25% - 16px);
        }
      }

      @media screen and (min-width: 1800px) {
        .item {
          flex-basis: calc(25% - 16px);
        }
      }
    }
  }
</style>
