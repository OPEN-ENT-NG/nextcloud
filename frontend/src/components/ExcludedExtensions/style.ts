import { SxProps } from "@mui/material";

import { columnBoxStyle } from "~/styles/boxStyles";

export const excludedContentStyle: SxProps = {
  ...columnBoxStyle,
  margin: "2rem",
  gap: "1rem",
  height: "25rem",
  alignContent: "flex-start",
  columnGap: "3rem",
  flexWrap: "wrap",
  overflowY: "auto",
};

export const excludedInputStyle: SxProps = {
  width: "10rem",
  "& .MuiOutlinedInput-root": {
    border: "1px solid #EBEBEB",
    borderRadius: "0.5rem",
    padding: "0.5rem 1rem",
  },
  "& .MuiOutlinedInput-notchedOutline": {
    border: "none",
  },
  "& .MuiInputBase-input": {
    padding: "0.5rem 1rem",
  },
};
