export enum AuthorizationFactoryStatus
{
  Disconnected, Connected
}

// TODO: Common AccessIdentityModels?  Flexible or closely coupled evolution?
export enum AccessDecision
{
  Granted, Pending, Denied, Error, Unknown
}

// TODO: Common AccessIdentityModels?  Flexible or closely coupled evolution?
export interface AccessCheckResult
{
  readonly decision: AccessDecision;
  readonly resource: string;
  readonly displayMessage?: string;
}

export interface State
{
  authzFactoryState: AuthorizationFactoryStatus;
  lastAccessCheck?: AccessCheckResult;
}
