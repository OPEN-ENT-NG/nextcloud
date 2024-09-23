import { flexStartBoxStyle } from "~/styles/boxStyles";

export const consoleTitleStyle = {
  ...flexStartBoxStyle,
  marginTop: "2rem",
  gap: "1rem",
  padding: "1rem 0rem",
};

export const consoleContentStyle = {
  backgroundColor: "white",
  padding: "3rem 5rem",
  marginTop: "1rem",
  marginBottom: "3.5rem",
  borderRadius: "1.2rem",
  boxShadow: "0 4px 8px rgba(0, 0, 0, 0.1)",
};

export const alertStyle = {
  position: "fixed",
  marginTop: "1rem",
  padding: "1rem",
  marginRight: "1rem",
};
