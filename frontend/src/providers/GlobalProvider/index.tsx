import {
  ChangeEvent,
  createContext,
  FC,
  KeyboardEvent,
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
import {
  initialDesktopConfigValues,
  processFolderPath,
  processInputValue,
} from "./utils";
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
  const { useGetDesktopConfigQuery, useUpdateDesktopConfigMutation } =
    desktopConfigApi;
  const { data } = useGetDesktopConfigQuery(null);
  const [updateDesktopConfig] = useUpdateDesktopConfigMutation();
  const [desktopConfigValues, setDesktopConfigValues] = useState<DesktopConfig>(
    initialDesktopConfigValues,
  );
  const [inputValues, setInputValues] = useState<DesktopConfig>(
    initialDesktopConfigValues,
  );
  const [inputExtension, setInputExtension] = useState<string>("");
  const [disabledSave, setDisabledSave] = useState<boolean>(true);
  const [showSuccessAlert, setShowSuccessAlert] = useState<boolean>(false);

  useEffect(() => {
    if (data) {
      setDesktopConfigValues(data);
      setInputValues(data);
    }
  }, [data]);

  useEffect(() => {
    if (
      JSON.stringify(inputValues) === JSON.stringify(desktopConfigValues) ||
      !inputValues.syncFolder
    ) {
      setDisabledSave(true);
    } else {
      setDisabledSave(false);
    }
  }, [inputValues, desktopConfigValues]);

  const showSuccessAlertTimeout = () => {
    setShowSuccessAlert(true);
    setTimeout(() => {
      setShowSuccessAlert(false);
    }, 500000);
  };

  const handleSubmitNewConfig = () => {
    updateDesktopConfig(inputValues);
    setInputExtension("");
    showSuccessAlertTimeout();
  };

  const handleCancelNewConfig = () => {
    setInputValues(desktopConfigValues);
    setInputExtension("");
  };

  const handleSyncFolderChange = (event: ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    const processedValue = processFolderPath(value);
    setInputValues((prev) => ({
      ...prev,
      syncFolder: processedValue,
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

  const handleExcludedExtensionsChange = (
    event: ChangeEvent<HTMLInputElement>,
  ) => {
    const value = event.target.value;
    const isValid = /^[.][a-zA-Z0-9]+$/.test(value) || value === ".";
    if (isValid) setInputExtension(value);
  };

  const handleAddExcludedExtensions = (
    event: KeyboardEvent<HTMLDivElement>,
  ) => {
    if (event.key === "Enter" && inputExtension !== ".") {
      setInputValues((prev) => ({
        ...prev,
        excludedExtensions: [...prev.excludedExtensions, inputExtension],
      }));
      setInputExtension(".");
    }
  };

  const handleRemoveExcludedExtension = (extension: string) => {
    setInputValues((prev) => ({
      ...prev,
      excludedExtensions: prev.excludedExtensions.filter(
        (excludedExtension) => excludedExtension !== extension,
      ),
    }));
  };

  const value = useMemo<GlobalProviderContextType>(
    () => ({
      desktopConfigValues,
      inputValues,
      inputExtension,
      setInputExtension,
      disabledSave,
      showSuccessAlert,
      setShowSuccessAlert,
      handleSubmitNewConfig,
      handleCancelNewConfig,
      handleSyncFolderChange,
      handleUploadLimitChange,
      handleDownloadLimitChange,
      handleExcludedExtensionsChange,
      handleAddExcludedExtensions,
      handleRemoveExcludedExtension,
    }),
    [
      desktopConfigValues,
      inputValues,
      inputExtension,
      disabledSave,
      showSuccessAlert,
    ],
  );

  return (
    <GlobalProviderContext.Provider value={value}>
      {children}
    </GlobalProviderContext.Provider>
  );
};
