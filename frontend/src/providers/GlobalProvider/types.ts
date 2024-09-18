import { ChangeEvent, ReactNode } from "react";

export interface GlobalProviderContextType {
  desktopConfigValues: DesktopConfig;
  setDesktopConfigValues: (values: DesktopConfig) => void;
  inputValues: DesktopConfig;
  setInputValues: (values: DesktopConfig) => void;
  handleSubmitNewConfig: () => void;
  handleCancelNewConfig: () => void;
  handleSyncFolderChange: (event: ChangeEvent<HTMLInputElement>) => void;
  handleUploadLimitChange: (event: ChangeEvent<HTMLInputElement>) => void;
  handleDownloadLimitChange: (event: ChangeEvent<HTMLInputElement>) => void;
  handleExcludedExtensionsChange: (
    event: ChangeEvent<HTMLInputElement>,
  ) => void;
}

export interface GlobalProviderProps {
  children: ReactNode;
}

export type DesktopConfig = {
  downloadLimit: number;
  excludedExtensions: string[];
  syncFolder: string;
  uploadLimit: number;
};
