package com.appenjoyment.lfnw;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class UpdateSessionsService extends Service
{
	/**
	 * Broadcast action indicating an update has started.
	 */
	public static final String UPDATE_STARTED_ACTION = UpdateSessionsService.class.getCanonicalName() + ".UPDATE_STARTED";

	/**
	 * Broadcast action indicating an update has completed.
	 */
	public static final String UPDATE_COMPLETED_ACTION = UpdateSessionsService.class.getCanonicalName() + ".UPDATE_COMPLETED";

	/**
	 * Extra for sending the START_KIND.
	 */
	public static final String EXTRA_START_KIND = "startKind";

	/**
	 * Force an update of the sessions list
	 */
	public static final String START_KIND_FORCED = "forced";

	/**
	 * Update the sessions list if it's been long enough
	 */
	public static final String START_KIND_INTERVAL = "interval";

	/**
	 * Binder for clients to interact with us.
	 */
	public final class UpdateSessionsBinder extends Binder
	{
		UpdateSessionsService getService()
		{
			return UpdateSessionsService.this;
		}
	}

	public UpdateSessionsService()
	{
		m_binder = new UpdateSessionsBinder();
	}

	public boolean isUpdating()
	{
		return m_currentTask != null;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return m_binder;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		// TODO: how to notify clients if task is killed? can I send the nessecary broadcasts here?
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (m_currentTask == null)
		{
			// unless we were forced, don't start unless it's been long enough since our last successful update
			boolean start = START_KIND_FORCED.equals(intent.getStringExtra(EXTRA_START_KIND));
			if (!start)
			{
				SharedPreferences prefs = getSharedPreferences("UpdateSessionsService", MODE_PRIVATE);
				start = new Date().getTime() - prefs.getLong(PREF_LAST_SUCCESSFUL_UPDATE, 0) >= INTERVAL_UPDATE_DURATION;
			}

			if (start)
			{
				m_currentTask = new UpdateSessionsTask();
				m_currentTask.execute();
			}
		}

		return START_NOT_STICKY;
	}

	private class UpdateSessionsTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			sendBroadcast(new Intent(UPDATE_STARTED_ACTION));

			// TODO: neither of these approaches are working correctly...
			// 04-25 04:10:35.130: E/SessionData(3039): Error parsing Sessions Json
			// 04-25 04:10:35.130: E/SessionData(3039): org.json.JSONException: Unterminated object at character 36406 of
			// ...., 2013 - 10:00 to 10:50","node_title":"Free Software????????????????????...

			// // connect to the json stream
			// InputStream inputStream;
			// try
			// {
			// URL url = new URL("http://www.linuxfestnorthwest.org/mobile/sessions.json");
			// Object content = url.getContent();
			// if (content == null)
			// {
			// Log.w(TAG, "url.getContent() failed");
			// return false;
			// }
			//
			// if (!(content instanceof InputStream))
			// {
			// Log.e(TAG, "url.getContent() returned type of " + content.getClass().getCanonicalName());
			// return false;
			// }
			//
			// inputStream = (InputStream) content;
			// }
			// catch (MalformedURLException e)
			// {
			// Log.e(TAG, "This should never happen", e);
			// return false;
			// }
			// catch (IOException e)
			// {
			// Log.w(TAG, "IOException while opening content stream", e);
			// return false;
			// }
			//
			// // load the json stream into a string
			// String json = null;
			// try
			// {
			// byte[] bytes = new byte[8192];
			// while (inputStream.read(bytes) != -1)
			// stringOutput.write(bytes);
			// json = stringOutput.toString("utf-8");
			// }
			// catch (IOException e)
			// {
			// Log.w(TAG, "IOException while reading content stream", e);
			// return false;
			// }
			// finally
			// {
			// try
			// {
			// inputStream.close();
			// }
			// catch (IOException e)
			// {
			// }
			// }

			// // load the json stream into a string
			// String json;
			// ByteArrayOutputStream stringOutput = null;
			// InputStream inputStream = null;
			// try
			// {
			// // TODO: ua string?
			// String userAgent = System.getProperty("http.agent");
			// AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent);
			// HttpGet request = new HttpGet(new URI("http://www.linuxfestnorthwest.org/mobile/sessions.json"));
			// AndroidHttpClient.modifyRequestToAcceptGzipResponse(request);
			// HttpResponse response = client.execute(request);
			//
			// inputStream = AndroidHttpClient.getUngzippedContent(response.getEntity());
			// stringOutput = new ByteArrayOutputStream();
			//
			// byte[] bytes = new byte[8192];
			// while (inputStream.read(bytes) != -1)
			// stringOutput.write(bytes);
			// json = stringOutput.toString("utf-8");
			// }
			// catch (IOException e)
			// {
			// Log.w(TAG, "IOException while getting content stream", e);
			// return false;
			// }
			// catch (URISyntaxException e)
			// {
			// Log.w(TAG, "Should never happen", e);
			// return false;
			// }
			// finally
			// {
			// try
			// {
			// if (inputStream != null)
			// inputStream.close();
			// }
			// catch (IOException e)
			// {
			// }
			// try
			// {
			// if (stringOutput != null)
			// stringOutput.close();
			// }
			// catch (IOException e)
			// {
			// }
			// }
			//
			// // parse the json
			// List<SessionData> sessionData = SessionData.parseFromJson(json);
			//
			// // insert/update the records
			// if (sessionData.size() != 0)
			// SessionsManager.getInstance(UpdateSessionsService.this).insertOrUpdate(sessionData);
			//
			// return true;
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			m_currentTask = null;

			// remember that we succesfully updated now
			if (result != null && result.booleanValue())
			{
				SharedPreferences prefs = getSharedPreferences("UpdateSessionsService", MODE_PRIVATE);
				prefs.edit().putLong(PREF_LAST_SUCCESSFUL_UPDATE, new Date().getTime()).commit();

			}
			sendBroadcast(new Intent(UPDATE_COMPLETED_ACTION));
		}

	}

	private static final String TAG = "UpdateSessionsService";
	private static final int INTERVAL_UPDATE_DURATION = 1000 * 60 * 10;
	private static final String PREF_LAST_SUCCESSFUL_UPDATE = "LastSuccessfulUpdate";

	private UpdateSessionsTask m_currentTask;
	private UpdateSessionsBinder m_binder;
}
