<script lang="tsx">
  import { compile, computed, defineComponent, h, ref } from 'vue';
  import { RouteRecordRaw, useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsPersonInfoDrawer from '@/components/business/ms-personal-drawer/index.vue';

  import { getOrgOptions, switchUserOrg } from '@/api/modules/system';
  import { useI18n } from '@/hooks/useI18n';
  import useUser from '@/hooks/useUser';
  import { BOTTOM_MENU_LIST } from '@/router/constants';
  import { useAppStore, useUserStore } from '@/store';
  import { openWindow, regexUrl } from '@/utils';
  import { listenerRouteChange } from '@/utils/route-listener';

  import { WorkbenchRouteEnum } from '@/enums/routeEnum';

  import useMenuTree from './use-menu-tree';
  import type { RouteMeta } from 'vue-router';

  export default defineComponent({
    emit: ['collapse'],
    setup() {
      const { t } = useI18n();
      const appStore = useAppStore();
      const userStore = useUserStore();
      const { logout } = useUser();
      const router = useRouter();
      const route = useRoute();
      const { menuTree } = useMenuTree();
      const collapsed = computed({
        get() {
          if (appStore.device === 'desktop') return appStore.menuCollapse;
          return false;
        },
        set(value: boolean) {
          appStore.updateSettings({ menuCollapse: value });
        },
      });

      const openKeys = ref<string[]>([]);
      const selectedKey = ref<string[]>([]);

      const goto = (item: RouteRecordRaw | null) => {
        if (item) {
          // 如果菜单是外链
          if (regexUrl.test(item.path)) {
            openWindow(item.path);
            selectedKey.value = [item.name as string];
            return;
          }
          // 已激活的菜单重复点击不处理
          const { hideInMenu, activeMenu } = item.meta as RouteMeta;
          if (route.name === item.name && !hideInMenu && !activeMenu) {
            selectedKey.value = [item.name as string];
            return;
          }
          router.push({
            name: item.name,
          });
        } else {
          router.push({
            name: 'notFound',
          });
        }
      };
      /**
       * 查找激活的菜单项
       * @param target 目标菜单名
       */
      const findMenuOpenKeys = (target: string) => {
        const result: string[] = [];
        let isFind = false;
        const backtrack = (item: RouteRecordRaw | null, keys: string[]) => {
          if (target.includes(item?.name as string)) {
            result.push(...keys);
            if (result.length >= 2) {
              // 由于目前存在三级子路由，所以至少会匹配到三层才算结束
              isFind = true;
              return;
            }
          }
          if (item?.children?.length) {
            item.children.forEach((el) => {
              backtrack(el, [...keys, el.name as string]);
            });
          }
        };
        menuTree.value?.forEach((el: RouteRecordRaw | null) => {
          if (isFind) return; // 节省性能
          backtrack(el, [el?.name as string]);
        });
        return result;
      };
      /**
       * 监听路由变化，存储打开及选中的菜单
       */
      listenerRouteChange((newRoute) => {
        const { requiresAuth, activeMenu, hideInMenu } = newRoute.meta;
        if (requiresAuth !== false && (!hideInMenu || activeMenu)) {
          const menuOpenKeys = findMenuOpenKeys((activeMenu || newRoute.name) as string);

          const keySet = new Set([...menuOpenKeys, ...openKeys.value]);
          openKeys.value = [...keySet];

          selectedKey.value = [activeMenu || menuOpenKeys[menuOpenKeys.length - 1]];
        }
      }, true);
      const setCollapse = (val: boolean) => {
        if (appStore.device === 'desktop') appStore.updateSettings({ menuCollapse: val });
      };

      const personalMenusVisible = ref(false);
      const personalDrawerVisible = ref(false);
      const switchOrgVisible = ref(false);
      const orgKeyword = ref('');
      const originOrgList = ref<{ id: string; name: string }[]>([]);
      const orgList = computed(() => originOrgList.value.filter((e) => e.name.includes(orgKeyword.value)));

      async function switchOrg(id: string) {
        try {
          Message.loading(t('personal.switchOrgLoading'));
          await switchUserOrg(id, userStore.id || '');
          switchOrgVisible.value = false;
          Message.clear();
          Message.success(t('personal.switchOrgSuccess'));
          personalMenusVisible.value = false;
          orgKeyword.value = '';
          await router.replace({ name: WorkbenchRouteEnum.WORKBENCH });
          userStore.isLogin();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      }

      onBeforeMount(async () => {
        try {
          const res = await getOrgOptions();
          originOrgList.value = res || [];
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      });

      const isActiveSwitchOrg = ref(false);
      const personalMenus = [
        {
          label: t('personal.info'),
          icon: <MsIcon type="icon-icon-contacts" class="text-[var(--color-text-4)]" />,
          event: () => {
            personalDrawerVisible.value = true;
          },
        },
        {
          label: t('personal.switchOrg'),
          icon: () => (
            <MsIcon
              type="icon-icon_switch_outlined1"
              class={isActiveSwitchOrg.value ? 'text-[rgb(var(--primary-5))]' : 'text-[var(--color-text-4)]'}
            />
          ),
          isTrigger: true,
          event: () => {
            switchOrgVisible.value = true;
            isActiveSwitchOrg.value = true;
          },
        },
        {
          divider: <a-divider class="ms-dropdown-divider" />,
        },
        {
          label: t('personal.exit'),
          icon: <MsIcon type="icon-icon_into-item_outlined" class="text-[var(--color-text-4)]" />,
          event: () => logout(),
        },
      ];

      watch(
        () => personalMenusVisible.value,
        (val) => {
          if (!val) {
            isActiveSwitchOrg.value = false;
          }
        }
      );
      const personalInfoMenu = () => {
        return (
          <a-trigger
            v-model:popup-visible={personalMenusVisible.value}
            trigger="click"
            unmount-on-close={false}
            popup-offset={4}
            position="right"
            class={['arco-trigger-menu absolute', personalMenusVisible.value ? 'block' : 'hidden']}
            v-slots={{
              content: () => (
                <div class="arco-trigger-menu-inner">
                  {personalMenus.map((e) => {
                    if (e.divider) {
                      return e.divider;
                    }
                    if (e.isTrigger) {
                      return (
                        <a-trigger
                          v-model:popup-visible={switchOrgVisible.value}
                          trigger="click"
                          unmount-on-close={false}
                          popup-offset={14}
                          position="right"
                          class={['arco-trigger-menu switch-org-dropdown', switchOrgVisible.value ? 'block' : 'hidden']}
                          v-slots={{
                            content: () => (
                              <div class="arco-trigger-menu-inner">
                                <a-input-search
                                  v-model:model-value={orgKeyword.value}
                                  placeholder={t('personal.searchOrgPlaceholder')}
                                />
                                <a-divider margin="4px" />
                                <div class="switch-org-dropdown-list">
                                  {orgList.value.map((item) => (
                                    <div
                                      key={item.id}
                                      class={[
                                        'arco-trigger-menu-item !w-[calc(100%-12px)]',
                                        item.id === appStore.currentOrgId ? 'active-org' : '',
                                      ]}
                                      onClick={() => {
                                        switchOrg(item.id);
                                      }}
                                    >
                                      <a-tooltip content={item.name}>
                                        <div class="one-line-text max-w-[220px]">{item.name}</div>
                                      </a-tooltip>
                                      {item.id === appStore.currentOrgId ? (
                                        <MsTag
                                          type="primary"
                                          theme="light"
                                          size="small"
                                          class="ml-[4px] !bg-[rgb(var(--primary-9))] px-[4px]"
                                        >
                                          {t('personal.currentOrg')}
                                        </MsTag>
                                      ) : null}
                                    </div>
                                  ))}
                                </div>
                              </div>
                            ),
                          }}
                        >
                          <div
                            class={
                              isActiveSwitchOrg.value ? 'active-org arco-trigger-menu-item' : 'arco-trigger-menu-item'
                            }
                            onClick={() => {
                              if (typeof e.event === 'function') {
                                e.event();
                              }
                            }}
                          >
                            {e.icon()}
                            {e.label}
                          </div>
                        </a-trigger>
                      );
                    }
                    return (
                      <div
                        class="arco-trigger-menu-item"
                        onClick={() => {
                          if (typeof e.event === 'function') {
                            e.event();
                          }
                          personalMenusVisible.value = false;
                        }}
                      >
                        {e.icon}
                        {e.label}
                      </div>
                    );
                  })}
                </div>
              ),
            }}
          >
            <a-menu-item class="flex items-center justify-between" key="personalInfo">
              <div class="hover:!bg-transparent">
                <MsIcon type="icon-icon_that_person" />
                {userStore.name}
              </div>
              <icon-caret-down class="!m-0" />
            </a-menu-item>
          </a-trigger>
        );
      };
      const personalInfoDrawer = () => {
        return (
          <MsPersonInfoDrawer
            visible={personalDrawerVisible.value}
            onUpdate:visible={(e) => {
              personalDrawerVisible.value = e;
            }}
          />
        );
      };

      const renderSubMenu = () => {
        function travel(_route: (RouteRecordRaw | null)[] | null, nodes = []) {
          if (_route) {
            _route.forEach((element) => {
              const icon = element?.meta?.icon ? () => <MsIcon type={element?.meta?.icon as string} /> : null;
              const node =
                element?.children && element?.children.length !== 0 ? (
                  <a-sub-menu
                    key={element?.name}
                    v-slots={{
                      icon,
                      title: () => h(compile(t(element?.meta?.locale || ''))),
                    }}
                    class={BOTTOM_MENU_LIST.includes(element?.name as string) ? 'arco-menu-inline--bottom' : ''}
                  >
                    {travel(element?.children)}
                  </a-sub-menu>
                ) : (
                  <a-menu-item key={element?.name} v-slots={{ icon }} onClick={() => goto(element)}>
                    {t(element?.meta?.locale || '')}
                  </a-menu-item>
                );
              nodes.push(node as never);
            });
          }
          return nodes;
        }
        return travel(menuTree.value);
      };

      return () => (
        <>
          <a-menu
            mode={'vertical'}
            v-model:collapsed={collapsed.value}
            v-model:open-keys={openKeys.value}
            show-collapse-button={appStore.device !== 'mobile'}
            auto-open={false}
            selected-keys={selectedKey.value}
            auto-open-selected={true}
            level-indent={34}
            style="height: 100%;width:100%;"
            onCollapse={setCollapse}
            trigger-props={{
              'show-arrow': false,
              'popup-offset': -4,
            }}
            v-slots={{
              'collapse-icon': () => (appStore.menuCollapse ? <icon-right /> : <icon-left />),
            }}
          >
            {renderSubMenu()}
            {personalInfoMenu()}
          </a-menu>
          {personalInfoDrawer()}
        </>
      );
    },
  });
</script>

<style lang="less">
  .arco-menu-vertical {
    .menu-wrapper {
      background-color: var(--color-bg-3);
    }
    .arco-menu-inner {
      @apply flex flex-col;

      padding: 16px 28px 16px 16px !important;
      .arco-menu-inline {
        &--bottom {
          @apply mt-auto;
        }
      }
      .arco-menu-pop-header {
        @apply leading-none;

        padding: 11px;
      }
      .arco-menu-inline-header {
        @apply flex items-center;
      }
      .arco-menu-inline-header,
      .arco-menu-item {
        @apply mb-0 !bg-transparent;

        color: var(--color-text-1) !important;
        &:hover,
        .arco-icon:hover {
          background-color: rgb(var(--primary-1)) !important;
        }
        .arco-menu-item-inner,
        .arco-menu-item-inner:hover {
          @apply !bg-transparent;
        }
        .arco-menu-icon {
          .arco-icon {
            &:not(.arco-icon-down) {
              font-size: 18px;
            }

            color: var(--color-text-4);
          }
        }
        .arco-menu-title {
          color: var(--color-text-1);
        }
      }
      .arco-menu-selected {
        color: rgb(var(--primary-5)) !important;
        &:not(.arco-menu-inline-header) {
          background-color: rgb(var(--primary-9)) !important;
        }
        .arco-icon {
          color: rgb(var(--primary-5)) !important;
          &:hover {
            background-color: var(--color-bg-6) !important;
          }
        }
        .arco-menu-title {
          color: rgb(var(--primary-5)) !important;
        }
      }
      .arco-menu-pop {
        @apply bg-transparent;
      }
    }
  }
  .arco-menu-overflow-sub-menu {
    min-width: 60px;
  }
  .arco-menu-collapsed {
    width: 86px;
  }
  .arco-menu {
    &:hover {
      .arco-menu-collapse-button {
        @apply flex;
      }
    }
    .arco-menu-collapse-button {
      @apply hidden rounded-full;

      top: 22px;
      right: 4px;
      border: 1px solid #ffffff;
      background: linear-gradient(90deg, rgb(var(--primary-9)) 3.36%, #ffffff 100%);
      box-shadow: 0 0 7px rgb(15 0 78 / 9%);
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
  .switch-org-dropdown {
    @apply absolute max-h-none;

    width: 300px;
    .arco-trigger-popup-wrapper {
      @apply max-h-full;
      .switch-org-dropdown-list {
        @apply overflow-y-auto;
        .ms-scroll-bar();

        max-height: 200px;
      }
    }
  }
  .active-org {
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
</style>
