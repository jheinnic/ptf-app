import {ActionReducer} from '@ngrx/store';

export function actionLogger(reducer: ActionReducer<any>): ActionReducer<any>
{
  return function (state: any, action: any): any {
    try {
      const retVal = reducer(state, action);
      console.log('Initial: ', state, '\nAction: ', action, '\nResult: ', retVal);
      return retVal;
    } catch (err) {
      console.log('Initial: ', state, '\nAction: ', action, '\nError: ', err);
      throw err;
    }
  };
}
