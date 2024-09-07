import { emptySplitApi } from "./emptySplitApi.service";

export const signetsApi = emptySplitApi.injectEndpoints({
  endpoints: (builder) => ({
    getPublishedSignets: builder.query({
      query: () => "signets",
    }),
    getMySignets: builder.query({
      query: () => "mysignets",
    }),
    createSignet: builder.mutation({
      query: ({ payload }) => ({
        url: `signets`,
        method: "POST",
        body: payload,
      }),
    }),
    updateSignet: builder.mutation({
      query: ({ idSignet, payload }) => ({
        url: `signets/${idSignet}`,
        method: "PUT",
        body: payload,
      }),
    }),
    deleteSignet: builder.mutation({
      query: ({ idSignet }) => ({
        url: `signets/${idSignet}`,
        method: "DELETE",
      }),
    }),
    publishSignet: builder.mutation({
      query: ({ idSignet, payload }) => ({
        url: `signet/publish/${idSignet}`,
        method: "POST",
        body: payload,
      }),
    }),
    updateShareResource: builder.mutation({
      query: ({ idSignet, payload }) => ({
        url: `share/resource/${idSignet}`,
        method: "PUT",
        body: payload,
      }),
    }),
    getMyPublishedSignets: builder.query({
      query: () => "signets/public/",
    }),
    deleteSignetPublic: builder.mutation({
      query: ({ idSignet }) => ({
        url: `signets/public/${idSignet}`,
        method: "DELETE",
      }),
    }),
  }),
});

export const {
  useGetPublishedSignetsQuery,
  useGetMySignetsQuery,
  useCreateSignetMutation,
  useUpdateSignetMutation,
  usePublishSignetMutation,
  useUpdateShareResourceMutation,
  useDeleteSignetMutation,
  useDeleteSignetPublicMutation,
  useGetMyPublishedSignetsQuery,
} = signetsApi;
