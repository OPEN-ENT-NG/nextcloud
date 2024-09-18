import { FC } from "react";

import { Box, Button, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";

import { consoleContentStyle, consoleTitleStyle } from "./style";
import { BwLimits } from "~/components/BwLimits";
import { ExcludedExtensions } from "~/components/ExcludedExtensions";
import { SyncFolder } from "~/components/SyncFolder";
import { useGlobalProvider } from "~/providers/GlobalProvider";
import { columnBoxStyle, flexEndBoxStyle } from "~/styles/boxStyles";

export const MainPage: FC = () => {
  const { t } = useTranslation("nextcloud");
  const { handleSubmitNewConfig, handleCancelNewConfig } = useGlobalProvider();
  return (
    <>
      <Typography variant="h1" sx={consoleTitleStyle}>
        {t("nextcloud.console.title")}
      </Typography>
      <Typography variant="body2">{t("nextcloud.console.subtitle")}</Typography>
      <Box sx={consoleContentStyle}>
        <Box sx={{ ...columnBoxStyle, gap: "2rem" }}>
          <SyncFolder />
          <BwLimits />
          <ExcludedExtensions />
        </Box>
        <Box sx={{ ...flexEndBoxStyle, gap: "2rem" }}>
          <Button variant="outlined" onClick={handleCancelNewConfig}>
            {t("nextcloud.console.cancel")}
          </Button>
          <Button variant="contained" onClick={handleSubmitNewConfig}>
            {t("nextcloud.console.save")}
          </Button>
        </Box>
      </Box>
    </>
  );
};
