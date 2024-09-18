import { DesktopConfig } from "./types";

export const initialDesktopConfigValues: DesktopConfig = {
  downloadLimit: 0,
  excludedExtensions: [],
  syncFolder: "",
  uploadLimit: 0,
};

export const processInputValue = (value: string): string | null => {
  if (value === "00") {
    return null;
  }

  if (value.trim() === "" || value === "0" || value === "+") {
    return "0";
  }

  if (value.length > 1 && value.startsWith("0")) {
    value = value.replace(/^0+/, "");
  }

  if (value.length > 1 && value.startsWith("-")) {
    value = value.replace(/^-+/, "");
  }

  return !isNaN(value as any) ? value : null;
};

export const processFolderPath = (value: string): string => {
  if (value.startsWith("/")) {
    value = value.substring(1);
  }

  const forbiddenChars = /[<>:"\\|?*]/g;
  value = value.replace(forbiddenChars, "");

  value = value.replace(/\/{2,}/g, "/");

  return value;
};
