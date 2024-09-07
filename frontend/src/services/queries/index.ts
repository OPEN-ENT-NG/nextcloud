import { useQuery } from "@tanstack/react-query";
import { IAction } from "edifice-ts-client";

import { sessionHasWorkflowRights } from "../api";
import { workflows } from "~/config";

/**
 * useActions query
 * set actions correctly with workflow rights
 * @returns actions data
 */
// const { actions } = getAppParams();
export const useActions = () => {
  const { signets, pins } = workflows;

  return useQuery<Record<string, boolean>, Error, IAction[]>({
    queryKey: ["actions"],
    queryFn: async () => {
      const availableRights = await sessionHasWorkflowRights([signets, pins]);
      return availableRights;
    },
    select: (data) => {
      const actions: any[] = [
        {
          id: "signets",
          workflow: signets,
          available: data[signets] || false,
        },
        {
          id: "pins",
          workflow: pins,
          available: data[pins] || false,
        },
      ];
      return actions;
    },
  });
};
