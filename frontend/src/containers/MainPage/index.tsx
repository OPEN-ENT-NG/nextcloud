import { FC } from "react";

import { BwLimits } from "~/components/BwLimits";
import { ExcludedExtensions } from "~/components/ExcludedExtensions";
import { SyncFolder } from "~/components/SyncFolder";

export const MainPage: FC = () => {
  return (
    <>
      <h1>titre</h1>
      <SyncFolder />
      <BwLimits />
      <ExcludedExtensions />
    </>
  );
};
