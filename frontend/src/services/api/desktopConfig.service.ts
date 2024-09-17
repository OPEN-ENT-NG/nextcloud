import { emptySplitApi } from "./emptySplitApi.service";
import { DesktopConfig } from "~/providers/GolobalProvider/types";

export const desktopConfigApi = emptySplitApi.injectEndpoints({
  endpoints: (builder) => ({
    getDesktopConfig: builder.query({
      query: () => `/desktop/config`,
      providesTags: ["desktopConfig"],
    }),
    updateDesktopConfig: builder.mutation({
      query: (newConfig: DesktopConfig) => ({
        url: `/desktop/config`,
        method: "PUT",
        body: newConfig,
      }),
      invalidatesTags: ["desktopConfig"],
    }),
  }),
});

export const { useGetDesktopConfigQuery, useUpdateDesktopConfigMutation } =
  desktopConfigApi;
