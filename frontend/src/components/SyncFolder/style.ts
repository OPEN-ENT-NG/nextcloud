import { flexStartBoxStyle } from "~/styles/boxStyles";
import { italic } from "~/styles/fontStyles";

export const syncFolderStyle = {
  ...flexStartBoxStyle,
};

export const syncFolderContentStyle = {
  margin: "2rem",
};

export const syncFolderInputStyle = {
  marginRight: "1rem",
  minWidth: "45rem",
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
    padding: "0rem 0rem",
  },
};

export const syncFolderInputPropsStyle = {
  height: "3rem",
};

export const syncFolderInstructionsStyle = {
  ...italic,
  marginTop: "0.625rem",
};
