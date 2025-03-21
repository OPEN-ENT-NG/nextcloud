import { createTheme } from "@mui/material";

const theme = createTheme({
  palette: {
    primary: {
      main: "#3C2386",
    },
    red: {
      main: "#E20037",
    },
    background: {
      default: "#F9F9F9",
    },
    text: {
      primary: "#5F5F5F",
      secondary: "#000000",
    },
  },
  typography: {
    fontFamily: "Confortaa",
    fontSize: 16,
    h1: {
      fontSize: "2.5rem",
      color: "#2A9CC8",
      fontWeight: "bold",
      fontFamily: "Comfortaa",
    },
    h2: {
      fontWeight: "400",
      fontSize: "1.8rem",
      color: "#434343",
      fontFamily: "Roboto",
    },
    body1: {
      fontSize: "1.6rem",
      color: "#4A4A4A",
      fontFamily: "Arimo",
    },
    body2: {
      fontSize: "1.4rem",
      color: "#909090",
      fontFamily: "Roboto",
    },
  },
  components: {
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          padding: "12pxs",
          gap: "1rem",
          borderRadius: "1.2rem",
          border: "#B0B0B0",
        },
      },
    },
    MuiButton: {
      variants: [
        {
          props: { variant: "contained" },
          style: {
            fontSize: "1.6rem",
            fontFamily: "Arimo",
            fontWeight: "bold",
            backgroundColor: "#FF8D2E",
            color: "white",
            textTransform: "none",
            padding: "0.8rem 1.6rem",
            borderRadius: "0.8rem",
            boxShadow: "0 0 0 0",
            "&:hover": {
              backgroundColor: "#b35d00",
              boxShadow: "0 0 0 0",
            },
            "&.Mui-disabled": {
              backgroundColor: "#FFCBA0",
              color: "white",
              border: "none",
            },
          },
        },
        {
          props: { variant: "outlined" },
          style: {
            fontSize: "1.6rem",
            color: "#4A4A4A",
            fontWeight: "bold",
            fontFamily: "Arimo",
            backgroundColor: "white",
            textTransform: "none",
            padding: "0.8rem 1.6rem",
            borderRadius: "0.8rem",
            border: "none",
            "&:hover": {
              backgroundColor: "#F2F2F2",
              border: "none",
            },
          },
        },
      ],
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: "0.8rem",
          fontSize: "1.4rem",
          padding: "1rem 1.6rem",
          whiteSpace: "nowrap",
          marginRight: "auto",
        },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          color: "white",
          fontSize: "1.4rem",
          padding: "0.8rem 1rem",
          borderRadius: "0.8rem",
        },
        arrow: {
          color: "#3C2386",
        },
      },
    },
  },
});

export default theme;
