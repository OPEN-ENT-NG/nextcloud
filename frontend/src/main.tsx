import React from "react";

import { EdificeClientProvider, EdificeThemeProvider } from "@edifice.io/react";
import { ThemeProvider as ThemeProviderMUI } from "@mui/material";
import {
  QueryCache,
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { createRoot } from "react-dom/client";
import { Provider } from "react-redux";
import { RouterProvider } from "react-router-dom";
import "~/i18n";

import "@edifice.io/bootstrap/dist/index.css";
import { GlobalProvider } from "./providers/GlobalProvider";
import { router } from "./routes";
import { setupStore } from "./store";
import theme from "./styles/theme";

const rootElement = document.getElementById("root");
const root = createRoot(rootElement!);

if (process.env.NODE_ENV !== "production") {
  // eslint-disable-next-line global-require
  import("@axe-core/react").then((axe) => {
    axe.default(React, root, 1000);
  });
}

const store = setupStore();

const queryClient = new QueryClient({
  queryCache: new QueryCache({
    onError: (error: unknown) => {
      if (error === "0090") window.location.replace("/auth/login");
    },
  }),
  defaultOptions: {
    queries: {
      retry: false,
      refetchOnWindowFocus: false,
    },
  },
});

root.render(
  <QueryClientProvider client={queryClient}>
    <Provider store={store}>
      <EdificeClientProvider
        params={{
          app: "nextcloud",
        }}
      >
        <EdificeThemeProvider>
          <ThemeProviderMUI theme={theme}>
            <GlobalProvider>
              <RouterProvider router={router} />
            </GlobalProvider>
          </ThemeProviderMUI>
        </EdificeThemeProvider>
      </EdificeClientProvider>
    </Provider>
    <ReactQueryDevtools initialIsOpen={false} />
  </QueryClientProvider>,
);
