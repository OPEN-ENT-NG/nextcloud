import { ReactNode } from "react";

export interface GlobalProviderContextType {
  desktopConfigValues: DesktopConfig;
  setDesktopConfigValues: (values: DesktopConfig) => void;
  desktopConfigInputValues: DesktopConfig;
  setDesktopConfigInputValues: (values: DesktopConfig) => void;
  handleSubmitNewConfig: () => void;
  handleCancelNewConfig: () => void;
  handleSyncFolderChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  handleUploadLimitChange: (event: React.ChangeEvent<HTMLInputElement>) => void;
  handleDownloadLimitChange: (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => void;
  handleExcludedExtensionsChange: (
    event: React.ChangeEvent<HTMLInputElement>,
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
