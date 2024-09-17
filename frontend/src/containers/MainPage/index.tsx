import { Typography } from "@mui/material";
import { FC } from "react";
import { useTranslation } from "react-i18next";

import { BwLimits } from "~/components/BwLimits";
import { ExcludedExtensions } from "~/components/ExcludedExtensions";
import { SyncFolder } from "~/components/SyncFolder";

export const MainPage: FC = () => {
  const { t } = useTranslation("nextcloud");
  return (
    <>
      <Typography variant="h1">{t("nextcloud.console.title")}</Typography>
      <SyncFolder />
      <BwLimits />
      <ExcludedExtensions />
    </>
  );
};
