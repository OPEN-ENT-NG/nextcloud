import { emptySplitApi } from "./emptySplitApi.service";

export const searchApi = emptySplitApi.injectEndpoints({
  endpoints: (builder) => ({
    search: builder.query({
      query: ({ jsondata }) => ({
        url: `search?jsondata=${encodeURI(JSON.stringify(jsondata))}`,
      }),
    }),
  }),
});

export const { useSearchQuery } = searchApi;
