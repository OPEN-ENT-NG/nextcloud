import { emptySplitApi } from "./emptySplitApi.service";

export const signetsApi = emptySplitApi.injectEndpoints({
  endpoints: (builder) => ({
    getLevels: builder.query({
      query: () => "levels",
    }),
  }),
});

export const { useGetLevelsQuery } = signetsApi;
