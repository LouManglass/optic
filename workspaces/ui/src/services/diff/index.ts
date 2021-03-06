import { IHttpInteraction } from '@useoptic/domain-types';
import { ILearnedBodies } from '@useoptic/cli-shared/build/diffs/initial-types';

export interface ICaptureService {
  baseUrl: string;
  startDiff(
    events: any[],
    ignoreRequests: string[],
    additionalCommands: IRfcCommand[],
    filters: { pathId: string; method: string }[]
  ): Promise<IStartDiffResponse>;
  loadInteraction(
    interactionPointer: string
  ): Promise<ILoadInteractionResponse>;
}

export interface IDiffService {
  diffId(): string;
  // backend
  listDiffs(): Promise<IListDiffsResponse>;
  listUnrecognizedUrls(): Promise<IListUnrecognizedUrlsResponse>;
  loadStats(): Promise<ILoadStatsResponse>;
  // frontend for now
  loadDescription(diff: any): Promise<IGetDescriptionResponse>;
  listSuggestions(
    diff: any,
    interaction: any
  ): Promise<IListSuggestionsResponse>;
  loadInitialPreview(
    diff: any,
    currentInteraction: any,
    inferPolymorphism: boolean
  );
  learnInitial(
    rfcService: any,
    rfcId: any,
    pathId: String,
    method: string
  ): Promise<ILearnedBodies>;
}

export interface IRfcCommand {}

export interface ILoadStatsResponse {
  diffedInteractionsCounter: string;
  skippedInteractionsCounter: string;
}

export interface IStartDiffResponse {
  diffId: string;
  notificationsUrl?: string;
}
export interface ILoadInteractionResponse {
  interaction: IHttpInteraction;
}
export interface IListDiffsResponse {
  diffs: any[];
}

export interface IListUnrecognizedUrlsResponse {
  urls: any[];
}

export interface IGetDescriptionResponse {}
export interface IListSuggestionsResponse {}
