import {
  ChangeEvent,
  createContext,
  FC,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  GlobalProviderProps,
  DesktopConfig,
  GlobalProviderContextType,
} from "./types";
import { initialDesktopConfigValues, processInputValue } from "./utils";
import { desktopConfigApi } from "~/services/api/desktopConfig.service";

const GlobalProviderContext = createContext<GlobalProviderContextType | null>(
  null,
);

export const useGlobalProvider = () => {
  const context = useContext(GlobalProviderContext);
  if (!context) {
    throw new Error("useGlobalProvider must be used within an GlobalProvider");
  }
  return context;
};

export const GlobalProvider: FC<GlobalProviderProps> = ({ children }) => {
  const { useGetDesktopConfigQuery } = desktopConfigApi;
  const { data } = useGetDesktopConfigQuery({});
  const [desktopConfigValues, setDesktopConfigValues] = useState<DesktopConfig>(
    initialDesktopConfigValues,
  );
  const [inputValues, setInputValues] = useState<DesktopConfig>(
    initialDesktopConfigValues,
  );

  useEffect(() => {
    if (data) {
      setDesktopConfigValues(data);
      setInputValues(data);
    }
  }, [data]);

  const handleSubmitNewConfig = () => {
    console.log("submit new config");
  };

  const handleCancelNewConfig = () => {
    console.log("cancel new config");
  };

  const handleSyncFolderChange = (event: ChangeEvent<HTMLInputElement>) => {
    setInputValues((prev) => ({
      ...prev,
      syncFolder: event.target.value,
    }));
  };

  const handleUploadLimitChange = (event: ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    const processedValue = processInputValue(value);

    if (processedValue) {
      setInputValues((prev) => ({
        ...prev,
        uploadLimit: parseInt(processedValue),
      }));
    }
  };

  const handleDownloadLimitChange = (event: ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    const processedValue = processInputValue(value);

    if (processedValue) {
      setInputValues((prev) => ({
        ...prev,
        downloadLimit: parseInt(processedValue),
      }));
    }
  };

  const handleExcludedExtensionsChange = () => {
    console.log("excluded extensions change");
  };

  const value = useMemo<GlobalProviderContextType>(
    () => ({
      desktopConfigValues,
      setDesktopConfigValues,
      inputValues,
      setInputValues,
      handleSubmitNewConfig,
      handleCancelNewConfig,
      handleSyncFolderChange,
      handleUploadLimitChange,
      handleDownloadLimitChange,
      handleExcludedExtensionsChange,
    }),
    [desktopConfigValues, inputValues],
  );

  return (
    <GlobalProviderContext.Provider value={value}>
      {children}
    </GlobalProviderContext.Provider>
  );
};
