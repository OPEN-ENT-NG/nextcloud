import { ThemeProvider } from "@mui/material";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { describe, it } from "vitest";
import { GlobalProvider } from "~/providers/GlobalProvider";
import { App } from "~/routes/app/index.tsx";
import theme from "~/styles/theme";

const queryClient = new QueryClient();

describe("App Test", () => {
  it("should render the App component", () => {
    render(
      <QueryClientProvider client={queryClient}>
        <GlobalProvider>
          <ThemeProvider theme={theme}>
            <BrowserRouter>
              <App />
            </BrowserRouter>
          </ThemeProvider>
        </GlobalProvider>
      </QueryClientProvider>,
    );
  });
});
