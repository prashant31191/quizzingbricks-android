package com.quizzingbricks.communication.apiObjects.apiCalls;

import org.json.JSONObject;

import com.quizzingbricks.exceptions.APIException;
import com.quizzingbricks.tools.AsyncTaskResult;

public class AsyncApiDeleteCall extends AbstractApiCall<Void, Void, AsyncTaskResult<JSONObject>> {

	public AsyncApiDeleteCall(String token) {
		super(token);
	}

	@Override
	protected AsyncTaskResult<JSONObject> doInBackground(Void... params) {
		try	{
			return new AsyncTaskResult<JSONObject>(requestParser.sendDeleteRequestToServer(url, token));
		}
		catch(APIException e)	{
			return new AsyncTaskResult<JSONObject>(e);
		}
	}

}
