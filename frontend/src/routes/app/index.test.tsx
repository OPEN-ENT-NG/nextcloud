import { render } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { describe, it } from "vitest";

import { App } from "~/routes/app/index.tsx";

describe("App Test", () => {
  it("Rendering app page should render", () => {
    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>,
    );
  });
});
