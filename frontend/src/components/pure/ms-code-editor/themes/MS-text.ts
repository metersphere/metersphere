import { primaryVars } from '@/hooks/useThemeVars';
import { rgbToHex } from '@/utils';

export default {
  base: 'vs',
  inherit: false,
  rules: [],
  colors: {
    'editorLineNumber.foreground': rgbToHex(primaryVars.P2),
    'editorLineNumber.activeForeground': rgbToHex(primaryVars.P4),
    'editorCursor.background': rgbToHex(primaryVars.P5),
    'editorCursor.foreground': rgbToHex(primaryVars.P5),
    'editor.wordHighlightBackground': rgbToHex(primaryVars.P1),
    'editor.selectionBackground': rgbToHex(primaryVars.P2),
    'editor.lineHighlightBorder': rgbToHex(primaryVars.P1),
    'editor.lineHighlightBackground': rgbToHex(primaryVars.P1),
    'editor.rangeHighlightBackground': rgbToHex(primaryVars.P1),
    'editor.findMatchBackground': rgbToHex(primaryVars.P2),
    'editor.findMatchHighlightBackground': rgbToHex(primaryVars.P9),
    'editor.findRangeHighlightBackground': rgbToHex(primaryVars.P5),
    'scrollbarSlider.activeBackground': rgbToHex(primaryVars.P4),
    'scrollbarSlider.background': rgbToHex(primaryVars.P2),
    'scrollbarSlider.hoverBackground': rgbToHex(primaryVars.P3),
    'scrollbar.shadow': rgbToHex(primaryVars.P2),
  },
};
