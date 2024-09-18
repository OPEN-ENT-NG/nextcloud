import { Box, TextField, Typography } from "@mui/material";
import { FC } from "react";
import { useTranslation } from "react-i18next";
import { useGlobalProvider } from "~/providers/GlobalProvider";
import { flexStartBoxStyle } from "~/styles/boxStyles";
import { excludedContentStyle, excludedInputStyle } from "./style";
import { DeletableChip } from "../DeletableChip";

export const ExcludedExtensions: FC = () => {
  const { t } = useTranslation("nextcloud");

  const {
    inputValues: { excludedExtensions },
    inputExtension,
    handleExcludedExtensionsChange,
    // handleAddExcludedExtensions,
  } = useGlobalProvider();

  return (
    <Box>
      <Typography variant="h2" sx={flexStartBoxStyle}>
        {t("nextcloud.console.excluded.extensions")}
      </Typography>
      <Box sx={excludedContentStyle}>
        {[...excludedExtensions]
          .sort((a, b) => a.localeCompare(b)) // Sorts extensions in alphabetical order
          .map((extension) => (
            <DeletableChip key={extension} extension={extension} />
          ))}
        <TextField
          id="outlined-basic"
          variant="outlined"
          value={inputExtension}
          onChange={handleExcludedExtensionsChange}
          // onKeyDown={handleAddExcludedExtensions}
          sx={excludedInputStyle}
        />
      </Box>
    </Box>
  );
};
