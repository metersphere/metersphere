import Color from 'color';

import type { PageConfig, Style, Theme } from '@/models/setting/config';

/**
 * 获取颜色对象的 rgb 色值
 * @param color Color对象
 * @returns 颜色值
 */
export function getRGBinnerVal(color: Color) {
  return color
    .rgb()
    .toString()
    .replace(/rgba?\(|\)/g, '');
}

/**
 * 设置自定义颜色的主题色
 * @param primaryColor 主题色
 */
export function setCustomTheme(primaryColor: string) {
  const styleTag = document.createElement('style');
  styleTag.id = 'MS-CUSTOM-THEME';
  const primary = new Color(primaryColor);
  const white = Color('#fff');
  const P = primary.toString().replace(/rgba?\(|\)/g, '');
  const P1 = getRGBinnerVal(primary.mix(white, 0.95));
  const P2 = getRGBinnerVal(primary.mix(white, 0.8));
  const P3 = getRGBinnerVal(primary.mix(white, 0.7));
  const P4 = getRGBinnerVal(primary.mix(white, 0.15));
  const P7 = getRGBinnerVal(primary.mix(Color('#000'), 0.15));
  const P9 = getRGBinnerVal(primary.mix(white, 0.9));
  styleTag.innerHTML = `
    body{
      --primary-1: ${P1};
      --primary-2: ${P2};
      --primary-3: ${P3};
      --primary-4: ${P4};
      --primary-5: ${P};
      --primary-6: ${P};
      --primary-7: ${P7};
      --primary-9: ${P9};
    }
  `;
  // 移除之前的 style 标签（如果有）
  const prevStyleTag = document.getElementById('MS-CUSTOM-THEME');
  if (prevStyleTag) {
    prevStyleTag.remove();
  }
  document.body.appendChild(styleTag);
}

/**
 * 主题重置为默认主题
 */
export function resetTheme() {
  const prevStyleTag = document.getElementById('MS-CUSTOM-THEME');
  if (prevStyleTag) {
    prevStyleTag.remove();
  }
}

/**
 * 设置平台色
 * @param color 平台色
 */
export function setPlatformColor(color: string, isFollow = false) {
  const styleTag = document.createElement('style');
  styleTag.id = 'MS-CUSTOM-STYLE';
  const white = Color('#fff');
  // 跟随主题色，设置为P1
  const platformColor = isFollow ? new Color(color).mix(white, 0.95) : new Color(color);
  styleTag.innerHTML = `
    body{
      --color-bg-3: ${platformColor};
      --color-text-n9: ${platformColor};
    }
  `;
  // 移除之前的 style 标签（如果有）
  const prevStyleTag = document.getElementById('MS-CUSTOM-STYLE');
  if (prevStyleTag) {
    prevStyleTag.remove();
  }
  document.body.appendChild(styleTag);
}

/**
 * 平台风格重置为默认平台风格
 */
export function resetStyle() {
  const prevStyleTag = document.getElementById('MS-CUSTOM-STYLE');
  if (prevStyleTag) {
    prevStyleTag.remove();
  }
}

/**
 * 检测风格变化
 * @param val 风格
 * @param pageConfig 页面配置对象
 */
export function watchStyle(val: Style, pageConfig: PageConfig) {
  if (val === 'default') {
    // 默认就是系统自带的颜色
    resetStyle();
  } else if (val === 'custom') {
    // 自定义风格颜色
    setPlatformColor(pageConfig.customStyle);
  } else {
    // 跟随主题色
    setPlatformColor(pageConfig.customTheme, true);
  }
}

/**
 * 检测主题色变化
 * @param val 主题色
 * @param pageConfig 页面配置对象
 */
export function watchTheme(val: Theme, pageConfig: PageConfig) {
  if (val === 'default') {
    resetTheme();
    if (pageConfig.style === 'follow') {
      // 若平台风格跟随主题色
      resetStyle();
    }
  } else {
    setCustomTheme(pageConfig.customTheme);
    if (pageConfig.style === 'follow') {
      // 若平台风格跟随主题色
      setPlatformColor(pageConfig.customTheme, true);
    }
  }
}
