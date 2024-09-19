import { FC } from "react";

import { Box, TextField, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";

import { excludedContentStyle, excludedInputStyle } from "./style";
import { DeletableChip } from "../DeletableChip";
import { useGlobalProvider } from "~/providers/GlobalProvider";
import { flexStartBoxStyle } from "~/styles/boxStyles";

export const ExcludedExtensions: FC = () => {
  const { t } = useTranslation("nextcloud");

  const {
    inputValues: { excludedExtensions },
    inputExtension,
    setInputExtension,
    handleExcludedExtensionsChange,
    handleAddExcludedExtensions,
  } = useGlobalProvider();

  const handleFocus = () => {
    if (inputExtension === "") setInputExtension(".");
  };

  const handleBlur = () => {
    if (inputExtension === ".") setInputExtension("");
  };

  return (
    <Box>
      <Typography variant="h2" sx={flexStartBoxStyle}>
        {t("nextcloud.console.excluded.extensions")}
      </Typography>
      <Box sx={excludedContentStyle} id="excluded-extensions">
        {[...excludedExtensions]
          .sort((a, b) => a.localeCompare(b)) // Sorts extensions in alphabetical order
          .map((extension, index) => (
            <DeletableChip
              key={`${extension}-${index}`}
              extension={extension}
            />
          ))}
        <TextField
          id="outlined-basic"
          variant="outlined"
          value={inputExtension}
          onChange={handleExcludedExtensionsChange}
          onKeyDown={handleAddExcludedExtensions}
          onFocus={handleFocus}
          onBlur={handleBlur}
          sx={excludedInputStyle}
          placeholder={"+ " + t("nextcloud.console.add")}
        />
      </Box>
    </Box>
  );
};
