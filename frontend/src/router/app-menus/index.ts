import appRoutes from '../routes';

const mixinRoutes = [...appRoutes];

// 菜单信息根据路由配置推导
const appClientMenus = mixinRoutes.map((el) => {
  const { name, path, meta, redirect, children } = el;
  return {
    name,
    path,
    meta,
    redirect,
    children,
  };
});

export default appClientMenus;
