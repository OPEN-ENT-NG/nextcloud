import { FC } from "react";

import CloseIcon from "@mui/icons-material/Close";
import { Box, IconButton, Typography } from "@mui/material";

import { chipStyle, closeStyle } from "./style";
import { useGlobalProvider } from "~/providers/GlobalProvider";
import { flexStartBoxStyle } from "~/styles/boxStyles";

export const DeletableChip: FC<{ extension: string }> = ({ extension }) => {
  const { handleRemoveExcludedExtension } = useGlobalProvider();

  return (
    <Box sx={{ ...flexStartBoxStyle, gap: ".5rem", width: "15rem" }}>
      <Typography variant="body1" sx={chipStyle}>
        {extension}
      </Typography>
      <IconButton onClick={() => handleRemoveExcludedExtension(extension)}>
        <CloseIcon sx={closeStyle} />
      </IconButton>
    </Box>
  );
};
