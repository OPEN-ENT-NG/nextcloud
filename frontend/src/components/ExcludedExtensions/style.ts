import { SxProps } from "@mui/material";
import { columnBoxStyle } from "~/styles/boxStyles";

export const excludedContentStyle:SxProps = {
    ...columnBoxStyle,
  margin: "2rem",
  gap: "1rem",
  height: "29rem",
  alignContent: "flex-start",
  columnGap: "3rem",
  flexWrap: "wrap",
  overflowY: "auto",
};

export const excludedInputStyle:SxProps = {
    width: "10rem",
    border: "1px solid #EBEBEB",
    borderRadius: "0.5rem",
    padding: "0.5rem 1rem",
    overflow: "hidden",
    whiteSpace: "nowrap",
    textOverflow: "ellipsis",
  };