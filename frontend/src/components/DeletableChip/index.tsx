import { FC, useEffect, useRef, useState } from "react";

import CloseIcon from "@mui/icons-material/Close";
import { Box, IconButton, Tooltip, Typography } from "@mui/material";

import { chipStyle, closeStyle } from "./style";
import { useGlobalProvider } from "~/providers/GlobalProvider";
import { flexStartBoxStyle } from "~/styles/boxStyles";

export const DeletableChip: FC<{ extension: string }> = ({ extension }) => {
  const { handleRemoveExcludedExtension } = useGlobalProvider();

  const textRef = useRef<HTMLSpanElement | null>(null);
  const [isEllipsis, setIsEllipsis] = useState(false);

  useEffect(() => {
    const element = textRef.current;
    if (element) {
      setIsEllipsis(element.scrollWidth > element.clientWidth);
    }
  }, [extension]);

  return (
    <Box
      sx={{
        ...flexStartBoxStyle,
        gap: ".5rem",
        width: "15rem",
        marginRight: "2rem",
      }}
    >
      <Tooltip title={isEllipsis ? extension : ""} followCursor={true}>
        <Typography variant="body1" sx={chipStyle} ref={textRef}>
          {extension}
        </Typography>
      </Tooltip>
      <IconButton onClick={() => handleRemoveExcludedExtension(extension)}>
        <CloseIcon sx={closeStyle} />
      </IconButton>
    </Box>
  );
};
