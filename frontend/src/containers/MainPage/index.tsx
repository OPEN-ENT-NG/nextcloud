import { FC } from "react";

import { Box, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";

import { consoleContentStyle, consoleTitleStyle } from "./style";
import { BwLimits } from "~/components/BwLimits";
import { ExcludedExtensions } from "~/components/ExcludedExtensions";
import { SyncFolder } from "~/components/SyncFolder";

export const MainPage: FC = () => {
  const { t } = useTranslation("nextcloud");
  return (
    <>
      <Typography variant="h1" sx={consoleTitleStyle}>
        {t("nextcloud.console.title")}
      </Typography>
      <Typography variant="body2">{t("nextcloud.console.subtitle")}</Typography>
      <Box sx={consoleContentStyle}>
        <SyncFolder />
        <BwLimits />
        <ExcludedExtensions />
      </Box>
    </>
  );
};
