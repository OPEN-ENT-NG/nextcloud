import { emptySplitApi } from "./emptySplitApi.service";

export const globalApi = emptySplitApi.injectEndpoints({
  endpoints: (builder) => ({
    getGlobal: builder.query({
      query: () => "global/resources",
    }),
  }),
});

export const { useGetGlobalQuery } = globalApi;
