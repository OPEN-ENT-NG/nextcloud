import { Box, IconButton, Typography } from "@mui/material";
import CloseIcon from '@mui/icons-material/Close';
import { FC } from "react";
import { chipStyle, closeStyle } from "./style";
import { flexStartBoxStyle } from "~/styles/boxStyles";
import { useGlobalProvider } from "~/providers/GlobalProvider";

export const DeletableChip: FC<{extension:string}> = ({extension}) => {
  const { handleRemoveExcludedExtension } = useGlobalProvider();
  
  return (
    <Box sx={{...flexStartBoxStyle, gap:".5rem", width:"15rem"}}>
      <Typography variant="body1" sx={chipStyle}>
        {extension}
      </Typography>
      <IconButton>
        <CloseIcon sx={closeStyle}/>
      </IconButton>
    </Box>
  );
};
