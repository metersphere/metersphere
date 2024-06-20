<script lang="tsx">
  import { computed, defineComponent, h, ref } from 'vue';
  import { RouteRecordRaw, useRoute, useRouter } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import { cloneDeep, debounce } from 'lodash-es';

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsPersonInfoDrawer from '@/components/business/ms-personal-drawer/index.vue';

  import { getOrgOptions, switchUserOrg } from '@/api/modules/system';
  import { useI18n } from '@/hooks/useI18n';
  import useUser from '@/hooks/useUser';
  import { BOTTOM_MENU_LIST } from '@/router/constants';
  import { useAppStore, useUserStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';
  import { openWindow, regexUrl } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { getFirstRouteNameByPermission, getFirstRouterNameByCurrentRoute } from '@/utils/permission';
  import { listenerRouteChange } from '@/utils/route-listener';

  import { SettingRouteEnum } from '@/enums/routeEnum';

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
      // @desc: 初始化配置的项目模块
      appStore.getProjectInfos();
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
          if (item.meta?.hideChildrenInMenu) {
            // 顶级菜单路由跳转到该菜单下有权限的第一个顶部子菜单
            const childName = getFirstRouterNameByCurrentRoute(item.name as string);
            router.push({
              name: childName,
            });
          } else {
            router.push({
              name: item.name,
            });
          }
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
      const menuSwitchOrgVisible = ref(false);
      const orgKeyword = ref('');
      const originOrgList = ref<{ id: string; name: string }[]>([]);
      const orgList = computed(() =>
        originOrgList.value.filter((e) => e.name.toLowerCase().includes(orgKeyword.value.toLowerCase()))
      );

      async function switchOrg(id: string) {
        try {
          appStore.showLoading(t('personal.switchOrgLoading'));
          await switchUserOrg(id, userStore.id || '');
          switchOrgVisible.value = false;
          appStore.hideLoading();
          Message.success(t('personal.switchOrgSuccess'));
          personalMenusVisible.value = false;
          orgKeyword.value = '';
          await userStore.checkIsLogin(true);
          appStore.hideLoading();
          router.replace({
            name: getFirstRouteNameByPermission(router.getRoutes()),
            query: {
              orgId: appStore.currentOrgId,
              pId: appStore.currentProjectId,
            },
          });
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      }

      const isActiveSwitchOrg = ref(false);
      const personalMenus = ref([
        {
          label: t('personal.center'),
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
      ]);

      const copyPersonalMenus = ref(cloneDeep(personalMenus));

      const licenseStore = useLicenseStore();
      const xPack = computed(() => licenseStore.hasLicense());

      const orgListLoading = ref(false);
      async function getOrgList() {
        try {
          orgListLoading.value = true;
          const res = await getOrgOptions();
          originOrgList.value = res || [];
          appStore.setOrdList(originOrgList.value);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          orgListLoading.value = false;
        }
      }

      watch(
        () => xPack.value,
        async (val) => {
          if (val) {
            personalMenus.value = [...copyPersonalMenus.value];
            if (appStore.packageType === 'enterprise' && licenseStore.hasLicense()) {
              getOrgList();
            }
          } else {
            personalMenus.value.splice(1, 1);
          }
        },
        {
          immediate: true,
        }
      );

      watch(
        () => personalMenusVisible.value,
        (val) => {
          if (!val) {
            isActiveSwitchOrg.value = false;
          }
        }
      );

      watchEffect(() => {
        if (switchOrgVisible.value || menuSwitchOrgVisible.value) {
          if (appStore.packageType === 'enterprise' && licenseStore.hasLicense()) {
            getOrgList();
          }
          nextTick(() => {
            // 打开组织列表时，滚动到当前组织
            const activeOrgDom = document.querySelector('.switch-org-dropdown-list')?.querySelector('.active-org');
            if (activeOrgDom) {
              scrollIntoView(activeOrgDom);
            }
          });
        } else {
          orgKeyword.value = '';
        }
      });

      // 组织切换的 trigger
      const orgTrigger = (e: Record<string, any>, visible: Ref, slot: (item: Record<string, any>) => VNode) => (
        <a-trigger
          v-model:popup-visible={visible.value}
          trigger="click"
          unmount-on-close={true}
          popup-offset={14}
          position="right"
          mouse-enter-delay={500}
          class={['arco-trigger-menu switch-org-dropdown', visible.value ? 'block' : 'hidden']}
          v-slots={{
            content: () => (
              <div class="arco-trigger-menu-inner">
                <a-input-search
                  v-model:model-value={orgKeyword.value}
                  placeholder={t('personal.searchOrgPlaceholder')}
                />
                <a-divider margin="4px" />
                <a-spin class="flex w-full" loading={orgListLoading.value}>
                  {orgList.value.length === 0 ? (
                    <a-empty>{t('common.noData')}</a-empty>
                  ) : (
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
                            <div class="one-line-text flex-1">{item.name}</div>
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
                  )}
                </a-spin>
              </div>
            ),
          }}
        >
          {slot(e)}
        </a-trigger>
      );
      // 个人信息菜单
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
                  {personalMenus.value.map((e) => {
                    if (e.divider) {
                      return e.divider;
                    }
                    if (e.isTrigger) {
                      return orgTrigger(e, switchOrgVisible, (item) => (
                        <div
                          class={
                            isActiveSwitchOrg.value ? 'active-org arco-trigger-menu-item' : 'arco-trigger-menu-item'
                          }
                          onClick={() => {
                            if (typeof item.event === 'function') {
                              item.event();
                            }
                          }}
                        >
                          {item.icon()}
                          {item.label}
                        </div>
                      ));
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
            <a-menu-item
              class={['flex w-full items-center justify-between', collapsed.value ? 'h-[56px] w-[56px]' : '']}
              key="personalInfo"
            >
              {
                <div
                  class={[
                    collapsed.value
                      ? 'relative flex h-full items-center justify-center hover:!bg-transparent'
                      : 'relative flex items-center gap-[8px] hover:!bg-transparent',
                  ]}
                >
                  <MsAvatar is-user size={20} class="!mr-0 hover:!bg-transparent" />
                  {collapsed.value ? null : userStore.name}
                </div>
              }
              {collapsed.value ? null : <icon-caret-down class="!m-0" />}
            </a-menu-item>
          </a-trigger>
        );
      };
      // 个人信息抽屉
      const personalInfoDrawer = () => {
        return (
          <MsPersonInfoDrawer
            visible={personalDrawerVisible.value}
            onUpdate:visible={(e: boolean) => {
              personalDrawerVisible.value = e;
            }}
          />
        );
      };

      let mouseEnterTimer: NodeJS.Timeout;
      // 渲染菜单项
      const renderMenuItem = (element: RouteRecordRaw | null, icon: (() => any) | null) =>
        element?.name === SettingRouteEnum.SETTING_ORGANIZATION ? (
          <a-menu-item key={element?.name} v-slots={{ icon }} onClick={debounce(() => goto(element), 100)}>
            <div class="inline-flex w-[calc(100%-34px)] items-center justify-between !bg-transparent">
              {collapsed.value ? (
                <div class="text-center text-[12px] leading-[16px]">
                  {t(element?.meta?.collapsedLocale || element?.meta?.locale || '')}
                </div>
              ) : (
                t(element?.meta?.locale || '')
              )}
              {xPack.value
                ? orgTrigger(element, menuSwitchOrgVisible, () => (
                    <div
                      class={collapsed.value ? 'hidden' : '!bg-transparent'} // 菜单折叠时隐藏切换组织按钮
                      onMouseenter={() => {
                        mouseEnterTimer = setTimeout(() => {
                          menuSwitchOrgVisible.value = true;
                        }, 500);
                      }}
                      onMouseleave={() => {
                        clearTimeout(mouseEnterTimer);
                      }}
                    >
                      <MsIcon type="icon-icon_switch_outlined1" class="text-[var(--color-text-4)]" />
                    </div>
                  ))
                : ''}
            </div>
          </a-menu-item>
        ) : (
          <a-menu-item key={element?.name} v-slots={{ icon }} onClick={debounce(() => goto(element), 100)}>
            {collapsed.value ? (
              <div class="text-center text-[12px] leading-[16px]">
                {t(element?.meta?.collapsedLocale || element?.meta?.locale || '')}
              </div>
            ) : (
              t(element?.meta?.locale || '')
            )}
          </a-menu-item>
        );

      // 渲染子菜单
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
                      title: () =>
                        h('div', {
                          innerHTML: t(
                            collapsed.value ? element?.meta?.collapsedLocale || '' : element?.meta?.locale || ''
                          ),
                          class: collapsed.value ? 'text-[12px] leading-[16px]' : '',
                        }),
                    }}
                    class={BOTTOM_MENU_LIST.includes(element?.name as string) ? 'arco-menu-inline--bottom' : ''}
                  >
                    {travel(element?.children)}
                  </a-sub-menu>
                ) : (
                  renderMenuItem(element, icon)
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
            class={collapsed.value ? 'arco-menu-collapsed' : ''}
            style="height: 100%;width:100%;"
            onCollapse={setCollapse}
            trigger-props={{
              'show-arrow': false,
              'popup-offset': -4,
            }}
            v-slots={{
              'collapse-icon': () => (collapsed.value ? <icon-right /> : <icon-left />),
            }}
          >
            <div class="flex flex-1 flex-col gap-[4px]">{renderSubMenu()}</div>
            <div class="flex flex-col items-center">{personalInfoMenu()}</div>
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
      @apply flex flex-col justify-between;

      padding: 16px !important;
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
        .arco-menu-indent-list {
          @apply !bg-transparent;
        }
        .arco-menu-item-inner,
        .arco-menu-item-inner:hover {
          @apply !bg-transparent;
        }
        .arco-menu-icon {
          margin-right: 8px;
          .arco-icon {
            &:not(.arco-icon-down) {
              font-size: 16px;
            }

            color: var(--color-text-4);
          }
        }
        .arco-menu-title {
          color: var(--color-text-1);
        }
      }
      .arco-menu-inline--bottom {
        justify-self: end;
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
        &:hover {
          background-color: rgb(var(--primary-1)) !important;
        }
      }
    }
  }
  .arco-menu-overflow-sub-menu {
    min-width: 60px;
  }
  .arco-menu-vertical.arco-menu-collapsed {
    margin-left: 8px;
    width: 56px;
    .arco-menu-inner {
      @apply !p-0;

      padding-top: 12px !important;
      padding-bottom: 32px !important;
      .arco-menu-item,
      .arco-menu-inline--bottom {
        @apply flex-col p-0;

        gap: 4px;
        line-height: normal;
        padding: 8px 14px;
        .arco-menu-icon {
          @apply mr-0;
        }
        .arco-menu-title {
          @apply opacity-100;
        }
      }
    }
  }
  .arco-menu {
    &:hover {
      .arco-menu-collapse-button {
        @apply flex;
      }
    }
    .arco-menu-collapse-button {
      @apply hidden rounded-full;

      top: 24px;
      right: -12px;
      z-index: 101;
      border: 1px solid #ffffff;
      background: linear-gradient(90deg, rgb(var(--primary-9)) 3.36%, #ffffff 100%);
      box-shadow: 0 0 7px rgb(15 0 78 / 9%);
      .arco-icon {
        color: rgb(var(--primary-5));
      }
    }
  }
  .arco-menu-collapsed {
    .arco-menu-collapse-button {
      top: 12px;
      right: -16px;
    }
  }
  .arco-menu-item-tooltip {
    @apply hidden;
  }
  .switch-org-dropdown {
    @apply absolute max-h-none;

    width: 300px;
    .arco-trigger-popup-wrapper {
      @apply max-h-full;
      .switch-org-dropdown-list {
        @apply w-full overflow-y-auto;
        .ms-scroll-bar();

        min-height: 30px;
        max-height: 200px;
      }
    }
  }
  .active-org {
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-1));
  }
</style>
