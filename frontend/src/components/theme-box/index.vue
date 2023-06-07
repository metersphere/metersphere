<template>
  <a-badge class="theme-badge bottom-[124px] right-[70px]" :count="theme ? 1 : 0" dot>
    <a-button
      class="theme-badge-button"
      :shape="hover ? 'round' : 'circle'"
      size="large"
      @click="modalVisible = true"
      @mouseenter="hover = true"
      @mouseleave="hover = false"
    >
      <IconSkin />
      <span v-if="hover" style="margin-left: 8px">
        {{ t('themeBox.installTheme') }}
      </span>
    </a-button>
  </a-badge>
  <a-modal :visible="modalVisible" :width="900" @ok="modalVisible = false" @cancel="modalVisible = false">
    <template #title>
      <div class="theme-box-header pr-[24px]">
        <span>{{ t('themeBox.installTheme') }}</span>
      </div>
    </template>
    <a-row :gutter="[20, 20]">
      <template v-if="isLoading">
        <a-col v-for="(_, index) of loadingFillArray" :key="index" :span="8">
          <a-card v-if="isLoading" class="theme-box-card">
            <template #cover>
              <a-skeleton animation>
                <a-skeleton-shape style="width: 272px; height: 160px" />
              </a-skeleton>
            </template>
            <a-card-meta>
              <template #title>
                <a-skeleton animation>
                  <a-skeleton-line :line-height="25" />
                </a-skeleton>
              </template>
            </a-card-meta>
            <a-skeleton animation>
              <a-skeleton-shape style="margin-top: 20px; margin-left: auto; width: 100px; height: 24px" />
            </a-skeleton>
          </a-card>
        </a-col>
      </template>
      <template v-else-if="themeList.length > 0">
        <a-col v-for="item of themeList" :key="item.themeId" :span="8">
          <a-card class="theme-box-card">
            <template #cover>
              <img :src="item.cover" style="height: 160px" alt="theme-cover" />
            </template>
            <template #actions>
              <a-button
                class="theme-box-card-link"
                type="text"
                size="mini"
                :href="`https://arco.design/themes/design/${item.themeId}`"
              >
                <template #icon>
                  <IconLink />
                </template>
                {{ t('themeBox.openInDesignLab') }}
              </a-button>
              <a-tag v-if="theme && theme.themeId === item.themeId" color="arcoblue">
                {{ t('themeBox.currentTheme') }}
              </a-tag>
              <a-button v-else type="primary" size="mini" @click="() => useTheme(item)">
                {{ t('themeBox.install') }}
              </a-button>
            </template>
            <a-card-meta :title="item.themeName" />
          </a-card>
        </a-col>
      </template>
      <template v-else>
        <Empty style="margin: 200px 0">
          <template #description>
            {{ t('themeBox.noResult') }}
            <a-link :href="`https://arco.design/themes`">
              {{ t('themeBox.createTheme') }}
            </a-link>
          </template>
        </Empty>
      </template>
    </a-row>
    <div class="theme-box-bottom mt-[20px]">
      <a-pagination :total="total" :current="page" :page-size="6" @change="onPageChange" />
    </div>
    <template v-if="theme" #footer>
      <div class="theme-box-footer">
        <a-typography-text bold> {{ t('themeBox.currentTheme') }}: {{ theme.themeName }} </a-typography-text>
        <a-button type="primary" status="danger" size="small" @click="onResetClick">
          {{ t('themeBox.resetTheme') }}
        </a-button>
      </div>
    </template>
  </a-modal>
</template>

<script lang="ts">
  import { defineComponent, onMounted, ref, watch } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { Notification } from '@arco-design/web-vue';
  import axios from 'axios';
  import { IconSkin, IconLink } from '@arco-design/web-vue/es/icon';
  import { getLocalStorage, removeLocalStorage, setLocalStorage } from '../../utils/local-storage';
  import { apiBasename } from '../../utils/api';
  import { ThemeData } from './interface';

  const THEME_LINK_ID = 'arco-custom-theme';
  const loadingFillArray = Array(3).fill(1);

  export default defineComponent({
    name: 'ThemeBox',
    components: {
      IconSkin,
      IconLink,
    },
    setup() {
      const { t } = useI18n();
      const theme = ref<ThemeData>();
      const themeList = ref<ThemeData[]>([]);
      const total = ref(0);
      const page = ref(1);
      const modalVisible = ref(false);
      const searchValue = ref('');
      const isLoading = ref(false);
      const hover = ref(false);

      const removeTheme = () => {
        const linkElement = document.getElementById(THEME_LINK_ID);
        if (linkElement) {
          document.body.removeChild(linkElement);
        }
        theme.value = undefined;
      };

      const addTheme = (_theme: ThemeData, notice: boolean) => {
        const url = `${_theme.unpkgHost}${_theme.packageName}/css/arco.css`;
        axios
          .get(url)
          .then(() => {
            if (theme.value) {
              removeTheme();
            }
            const linkElement = document.createElement('link');
            linkElement.id = THEME_LINK_ID;
            linkElement.href = url;
            linkElement.type = 'text/css';
            linkElement.rel = 'stylesheet';
            document.body.appendChild(linkElement);
            theme.value = _theme;

            if (notice) {
              Notification.success({
                id: 'theme',
                title: t('themeBox.installTheme'),
                content: t('themeBox.installThemeSuccess'),
                duration: 2000,
              });
            }
          })
          .catch(() => {
            Notification.error({
              id: 'theme',
              title: t('themeBox.installTheme'),
              content: t('themeBox.installThemeError'),
              duration: 2000,
            });
          });
      };

      const useTheme = (_theme: ThemeData, notice = true) => {
        addTheme(_theme, notice);
        setLocalStorage('vue-custom-theme', _theme);
      };

      onMounted(() => {
        // const _theme = getLocalStorage<ThemeData>('vue-custom-theme', true) as ThemeData;
        // if (_theme) {
        //   useTheme(_theme, false);
        // }
      });

      const fetchThemeList = async (current: number, search: string) => {
        isLoading.value = true;
        try {
          const data = await axios.get(
            `${apiBasename}/themes/api/open/themes/list?pageSize=6&currentPage=${current}&depLibrary=@arco-design/web-vue&keyword=vue-ms-theme-${search}`
          );

          themeList.value = data.data.list;
          total.value = data.data.total;
        } catch (e) {
          themeList.value = [];
          total.value = 0;
        }
        isLoading.value = false;
      };

      watch(modalVisible, (visible) => {
        if (visible) {
          fetchThemeList(page.value, searchValue.value);
        }
      });

      const onResetClick = () => {
        removeTheme();
        removeLocalStorage('vue-custom-theme');
      };

      const onSearchInput = (value: string) => {
        searchValue.value = value;
        page.value = 1;
        fetchThemeList(1, value);
      };

      const onPageChange = (_page: number) => {
        page.value = _page;
        fetchThemeList(_page, searchValue.value);
      };

      return {
        hover,
        modalVisible,
        themeList,
        theme,
        total,
        page,
        isLoading,
        loadingFillArray,
        searchValue,
        useTheme,
        onSearchInput,
        onPageChange,
        onResetClick,
        t,
      };
    },
  });
</script>

<style scoped lang="less">
  .theme-box {
    &-header {
      @apply flex w-full items-center justify-between;
    }
    &-card {
      &-link {
        @apply opacity-0;

        transition: opacity 100ms;
      }
      &:hover &-link {
        @apply opacity-100;
      }
      :deep(.arco-card-meta-title) {
        line-height: 25px;
      }
    }
    &-bottom {
      @apply flex justify-end;
    }
    &-footer {
      @apply flex items-center justify-between;
    }
  }
  .theme-badge {
    @apply fixed;
    &-button {
      border: 1px solid var(--color-fill-3) !important;
      background: var(--color-bg-5) !important;
      box-shadow: 0 2px 12px 0 rgb(0 0 0 / 10%);
    }
  }
</style>
