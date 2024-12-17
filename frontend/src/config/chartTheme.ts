import useAppStore from '@/store/modules/app';

const appStore = useAppStore();

const colorThemeConfig: Record<string, any> = {
  xLabelColor: {
    dark: '#C0C2CF',
    bright: '#646466',
  },
  legendColor: {
    dark: '#E3E6F3',
    bright: '#323233',
  },
  splitLineColor: {
    dark: '#434552',
    bright: '#EDEDF1',
  },
  yLabelColor: {
    dark: '#90929F',
    bright: '#AEAEB2',
  },
  subtextStyleColor: {
    dark: '#90929F',
    bright: '#323233',
  },
  itemStyleBorderColor: {
    dark: '#242633',
    bright: '#ffffff',
  },
  initItemStyleColor: {
    dark: '#434552',
    bright: '#D4D4D8',
  },
  pageIconColor: {
    dark: '#959598',
    bright: '#959598',
  },
  pageIconInactiveColor: {
    dark: '#959598',
    bright: '#C7C7CB',
  },
  pageTextStyleColor: {
    dark: '#C7C7CB',
    bright: '#959598',
  },
  graphicBackgroundColor: {
    dark: '#ADB0BC',
    bright: '#F9F9FE',
  },
  graphicFillColor: {
    dark: '#242633',
    bright: '#959598',
  },
};

function getVisualThemeColor(key: string): string {
  const theme = appStore.isDarkTheme ? 'dark' : 'bright';
  return colorThemeConfig[key]?.[theme];
}

export default getVisualThemeColor;
