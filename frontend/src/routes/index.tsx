import { createHashRouter } from "react-router-dom";

import Root from "~/app/root";
import ErrorPage from "~/components/page-error";
import "~/styles/page/home.scss";

const routes = [
  {
    path: "/",
    element: <Root />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        async lazy() {
          const { App } = await import("./app");
          return {
            Component: App,
          };
        },
      },
    ],
  },
];

// add # before roots to distinguish front roots (#/search) from back roots (/search)
export const router = createHashRouter(routes);
