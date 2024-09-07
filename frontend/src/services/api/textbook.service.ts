import { emptySplitApi } from "./emptySplitApi.service";

export const textbooksApi = emptySplitApi.injectEndpoints({
  endpoints: (builder) => ({
    getTextbooks: builder.query({
      query: (idStructure) => ({
        url: `textbooks/refresh?structureIds=${idStructure}`,
      }),
    }),
  }),
});

export const { useGetTextbooksQuery } = textbooksApi;
