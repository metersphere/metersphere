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

/**
 * 动态设置 favicon
 * @param url favicon 地址
 */
export function setFavicon(url: string) {
  const head = document.querySelector('head');
  const link = document.createElement('link');
  link.rel = 'shortcut icon';
  link.href = url;
  link.type = 'image/x-icon';

  // 移除之前的 favicon
  const oldFavicon = document.querySelector('link[rel="shortcut icon"]');
  if (oldFavicon) {
    head?.removeChild(oldFavicon);
  }

  // 添加新的 favicon
  head?.appendChild(link);
}

/**
 * 设置暗黑主题
 */
export function setDarkTheme() {
  const styleTag = document.createElement('style');
  styleTag.id = 'MS-CUSTOM-THEME';
  let styleStr = ``;
  const white = Color('#fff');
  const N1 = Color('#e3e6f3');
  const N7 = Color('#5b5e6a');
  const N8 = Color('#434552');
  const N9 = Color('#2e313d');
  const N10 = Color('#242633');
  // 中性色
  styleStr += `
    --color-text-1: ${N1.hex()};
    --color-text-2: #c0c2cf;
    --color-text-3: #adb0bc;
    --color-text-4: #9597a4;
    --color-white: #90929f;
    --color-text-brand: #787b88;
    --color-text-input: ${N7.hex()};
    --color-text-input-border: ${N7.hex()};
    --color-text-n8: ${N8.hex()};
    --color-text-n9: ${N9.hex()};
    --color-text-fff: ${N10.hex()};
    --color-text-000: #000;
    --gray-3: ${N8.toString().replace(/rgba?\(|\)/g, '')};
  `;
  // 主题色
  const primary = new Color('#cd3bff');
  const P = primary.toString().replace(/rgba?\(|\)/g, '');
  const P1 = getRGBinnerVal(primary.mix(white, 0.25));
  const P4 = getRGBinnerVal(primary.mix(N10, 0.55));
  const P5 = getRGBinnerVal(primary.mix(N10, 0.85));
  const P6 = getRGBinnerVal(primary.mix(N10, 0.75));
  const P7 = getRGBinnerVal(primary.mix(N10, 0.65));
  const P0 = getRGBinnerVal(primary.mix(N10, 0.25));
  styleStr += `
    --primary-1: ${P7};
    --primary-2: ${P5};
    --primary-3: ${P4};
    --primary-4: ${P1};
    --primary-5: ${P};
    --primary-6: ${P};
    --primary-7: ${P0};
    --primary-9: ${P6};
  `;
  // 红色
  const red = new Color('#e2324f');
  const Red = red.toString().replace(/rgba?\(|\)/g, '');
  const R1 = getRGBinnerVal(red.mix(white, 0.25));
  const R2 = getRGBinnerVal(red.mix(N10, 0.3));
  const R3 = getRGBinnerVal(red.mix(N10, 0.55));
  const R4 = getRGBinnerVal(red.mix(N10, 0.75));
  const R5 = getRGBinnerVal(red.mix(N10, 0.65));
  const R0 = getRGBinnerVal(red.mix(N10, 0.25));
  styleStr += `
    --danger-1: ${R5};
    --danger-2: ${R4};
    --danger-3: ${R3};
    --danger-4: ${R2};
    --danger-5: ${R1};
    --danger-6: ${Red};
    --danger-7: ${R0};
  `;
  // 绿色
  const green = new Color('#11b671');
  const Green = green.toString().replace(/rgba?\(|\)/g, '');
  const G1 = getRGBinnerVal(green.mix(white, 0.25));
  const G2 = getRGBinnerVal(green.mix(N10, 0.3));
  const G3 = getRGBinnerVal(green.mix(N10, 0.55));
  const G4 = getRGBinnerVal(green.mix(N10, 0.75));
  const G5 = getRGBinnerVal(green.mix(N10, 0.65));
  const G0 = getRGBinnerVal(green.mix(N10, 0.25));
  styleStr += `
    --success-1: ${G5};
    --success-2: ${G4};
    --success-3: ${G3};
    --success-4: ${G2};
    --success-5: ${G1};
    --success-6: ${Green};
    --success-7: ${G0};
  `;
  // 蓝色
  const blue = new Color('#3d8eff');
  const Blue = blue.toString().replace(/rgba?\(|\)/g, '');
  const B1 = getRGBinnerVal(blue.mix(white, 0.25));
  const B2 = getRGBinnerVal(blue.mix(N10, 0.3));
  const B3 = getRGBinnerVal(blue.mix(N10, 0.55));
  const B4 = getRGBinnerVal(blue.mix(N10, 0.75));
  const B5 = getRGBinnerVal(blue.mix(N10, 0.65));
  const B0 = getRGBinnerVal(blue.mix(N10, 0.25));
  styleStr += `
    --link-1: ${B5};
    --link-2: ${B4};
    --link-3: ${B3};
    --link-4: ${B2};
    --link-5: ${B1};
    --link-6: ${Blue};
    --link-7: ${B0};
  `;
  // 黄色
  const yellow = new Color('#edac2c');
  const Yellow = yellow.toString().replace(/rgba?\(|\)/g, '');
  const Y1 = getRGBinnerVal(yellow.mix(white, 0.25));
  const Y2 = getRGBinnerVal(yellow.mix(N10, 0.3));
  const Y3 = getRGBinnerVal(yellow.mix(N10, 0.55));
  const Y4 = getRGBinnerVal(yellow.mix(N10, 0.75));
  const Y5 = getRGBinnerVal(yellow.mix(N10, 0.65));
  const Y0 = getRGBinnerVal(yellow.mix(N10, 0.25));
  styleStr += `
    --waring-1: ${Y5};
    --waring-2: ${Y4};
    --waring-3: ${Y3};
    --waring-4: ${Y2};
    --waring-5: ${Y1};
    --waring-6: ${Yellow};
    --waring-7: ${Y0};
  `;
  // 背景色
  styleStr += `
    --color-fill-1: ${N9.hex()};
    --color-fill-2: ${N9.hex()};
    --color-bg-white: ${N10.hex()};
    --color-bg-1: ${N10.hex()};
    --color-bg-2: ${N10.hex()};
    --color-bg-3: ${N9.hex()};
    --color-bg-5: ${N9.hex()};
    --color-tooltip-bg: ${N1.hex()};
    --color-spin-layer-bg: ${N9.rgb().fade(0.25).toString()};
    --color-warning-light-1: rgb(${Y5})
  `;
  styleTag.innerHTML = `
    body{
      ${styleStr}
    }
  `;
  // 移除之前的 style 标签（如果有）
  const prevStyleTag = document.getElementById('MS-CUSTOM-THEME');
  if (prevStyleTag) {
    prevStyleTag.remove();
  }
  document.body.appendChild(styleTag);
}
