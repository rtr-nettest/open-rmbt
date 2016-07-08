/*******************************************************************************
 * Copyright 2013-2015 alladin-IT GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.alladin.rmbt.android.test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Process;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import at.alladin.rmbt.android.util.AsyncHtmlStatusCodeRetriever;
import at.alladin.rmbt.android.util.AsyncHtmlStatusCodeRetriever.ContentRetrieverListener;
import at.alladin.rmbt.client.v2.task.service.WebsiteTestService;

/**
 * 
 * @author lb
 *
 */
public class WebsiteTestServiceImpl implements WebsiteTestService {
	
	/**
	 * <p>
	 * 	if set to true the traffic will be recorded using 
	 * 	{@link TrafficStats#getUidRxBytes(int)} and {@link TrafficStats#getUidTxPackets(int)}
	 * </p>
	 * <p>
	 * 	otherwise this service will call: 
	 * {@link TrafficStats#getTotalRxBytes()} and {@link TrafficStats#getTotalTxPackets()}
	 * </p> 
	 */
	private final static boolean USE_PROCESS_UID_FOR_TRAFFIC_MEASUREMENT = true;
	
	private WebView webView;
	
	private final Context context;
	
	private final AtomicBoolean isRunning = new AtomicBoolean(false);
	
	private final AtomicBoolean hasFinished = new AtomicBoolean(false);
	
	private final AtomicBoolean hasError = new AtomicBoolean(false);
	
	private final AtomicInteger resourceCount = new AtomicInteger(0);
	
	private int statusCode = -1; 
	
	private long duration = -1;
	
	private RenderingListener listener;
	
	private long trafficRxStart;
	
	private long trafficTxStart;
	
	private long trafficRxEnd;
	
	private long trafficTxEnd;
	
	private final Handler handler;
	
	private int processUid;
	
	public WebsiteTestServiceImpl(final Context context) {
		this.context = context;
		this.handler = new Handler(context.getMainLooper());
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public WebsiteTestServiceImpl getInstance() {
		return new WebsiteTestServiceImpl(context);
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.WebsiteTest#getHash()
	 */
	@Override
	public String getHash() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.WebsiteTest#getDownloadDuration()
	 */
	@Override
	public long getDownloadDuration() {
		return duration;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.WebsiteTest#run(java.lang.String, long)
	 */
	@Override
	public void run(final String targetUrl, final long timeOut) {
	    handler.post(new Runnable() {
			@Override
			public void run() {
				WebsiteTestServiceImpl.this.processUid = Process.myUid();
				
				//webView.removeAllViews();
				
			    if (webView == null)
			        webView = new WebView(context);
				webView.clearCache(true);
				
				final long start = System.nanoTime();
				
				System.out.println("Running WEBSITETASK " + targetUrl);
				
				boolean isTrafficServiceSupported = USE_PROCESS_UID_FOR_TRAFFIC_MEASUREMENT ?
					TrafficStats.getUidRxBytes(processUid) != TrafficStats.UNSUPPORTED :
					TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED;
				
				
				if (!isTrafficServiceSupported) {
					trafficRxStart = -1;
					trafficTxStart = -1;
					trafficRxEnd = -1;
					trafficTxEnd = -1;
				}
				else {
					if (USE_PROCESS_UID_FOR_TRAFFIC_MEASUREMENT) {
						trafficTxStart = TrafficStats.getUidTxBytes(processUid);
						trafficRxStart = TrafficStats.getUidRxBytes(processUid);
					}
					else {
						trafficTxStart = TrafficStats.getTotalTxBytes();
						trafficRxStart = TrafficStats.getTotalRxBytes();						
					}
				}
				
				Thread timeoutThread = new Thread(new Runnable() {
					@Override
			        public void run() {
						try {
							System.out.println("WEBSITETASK STARTING TIMEOUT THREAD: " + timeOut + " ms");
							Thread.sleep(timeOut);
						} catch (InterruptedException e) {
							e.printStackTrace();
							Thread.currentThread().interrupt(); // restore interrupt state
							return;
						}
			                
						if (!WebsiteTestServiceImpl.this.hasFinished() && listener != null) {
							setEndTrafficCounter();
							
							if (listener.onTimeoutReached(WebsiteTestServiceImpl.this)) {
								System.out.println("WEBSITETESTTASK TIMEOUT");
								WebsiteTestServiceImpl.this.handler.post(new Runnable() {											
									@Override
									public void run() {
										WebsiteTestServiceImpl.this.webView.stopLoading();
									}
								});
							}
						}
					}
			     });
				
				timeoutThread.start();
				
				webView.getSettings().setJavaScriptEnabled(true);
				webView.setWebViewClient(new WebViewClient() {
					
					/*
					@Override
					public boolean shouldOverrideUrlLoading(WebView view,
							String url) {
						view.loadUrl(url);
						return true;
					}
					*/

					@Override
					public void onLoadResource(WebView view, String url) {
						System.out.println("getting resource: " + url + " progress: " + view.getProgress());
						resourceCount.incrementAndGet();
						super.onLoadResource(view, url);
					}
					
					@Override
					public void onPageFinished(WebView view, String url) {						
						super.onPageFinished(view, url);
						
						WebsiteTestServiceImpl.this.isRunning.set(false);
						WebsiteTestServiceImpl.this.hasFinished.set(true);
						WebsiteTestServiceImpl.this.hasError.set(false);
						WebsiteTestServiceImpl.this.duration = System.nanoTime() - start;

						if (WebsiteTestServiceImpl.this.trafficRxStart != -1) {
							setEndTrafficCounter();
						}
						
						System.out.println("PAGE FINISHED " + targetUrl + " progress: " + view.getProgress() + "%, resources counter: " + resourceCount.get());
						if (listener != null) {
							listener.onRenderFinished(WebsiteTestServiceImpl.this);
						}
					}

					@Override
					public void onPageStarted(final WebView view, String url, Bitmap favicon) {
						WebsiteTestServiceImpl.this.isRunning.set(true);
						WebsiteTestServiceImpl.this.hasFinished.set(false);
						WebsiteTestServiceImpl.this.hasError.set(false);
						
						if (listener != null) {
							listener.onDownloadStarted(WebsiteTestServiceImpl.this);
						}
						
						System.out.println("PAGE STARTED " + targetUrl);

						super.onPageStarted(view, url, favicon);
					}
					
					@Override
					public void onReceivedError(WebView view, int errorCode,
							String description, String failingUrl) {						
						super.onReceivedError(view, errorCode, description, failingUrl);
						
						WebsiteTestServiceImpl.this.isRunning.set(false);
						WebsiteTestServiceImpl.this.hasFinished.set(true);
						WebsiteTestServiceImpl.this.hasError.set(true);
						WebsiteTestServiceImpl.this.duration = System.nanoTime() - start;
																		
						if (WebsiteTestServiceImpl.this.trafficRxStart != -1) {
							setEndTrafficCounter();
						}
						
						if (listener != null) {
							listener.onError(WebsiteTestServiceImpl.this);
						}
					}
				});
				
				AsyncHtmlStatusCodeRetriever task = new AsyncHtmlStatusCodeRetriever();
				task.setContentRetrieverListener(new ContentRetrieverListener() {
					
					@Override
					public void onContentFinished(Integer statusCode) {
					    if (statusCode == null)
					        statusCode = -1;
						WebsiteTestServiceImpl.this.statusCode = statusCode;
						if (statusCode >= 0) {
							//webView.loadDataWithBaseURL(targetUrl, htmlContent, "text/html", "utf-8", null);
							webView.loadUrl(targetUrl);
							//webView.loadData(htmlContent, "text/html", "utf-8");
						}
						else {							
							WebsiteTestServiceImpl.this.isRunning.set(false);
							WebsiteTestServiceImpl.this.hasFinished.set(true);
							WebsiteTestServiceImpl.this.hasError.set(true);
							WebsiteTestServiceImpl.this.duration = System.nanoTime() - start;

							if (WebsiteTestServiceImpl.this.trafficRxStart != -1) {
								setEndTrafficCounter();
							}
							
							if (listener != null) {								
								listener.onError(WebsiteTestServiceImpl.this);
							}
						}
					}
				});
				
				task.execute(targetUrl);
				
				//webView.loadUrl(targetUrl);
			}
		});

	}
	
	/**
	 * 
	 */
	private void setEndTrafficCounter() {
		if (USE_PROCESS_UID_FOR_TRAFFIC_MEASUREMENT) {
			this.trafficRxEnd = TrafficStats.getUidRxBytes(processUid);
			this.trafficTxEnd = TrafficStats.getUidTxBytes(processUid);			
		}
		else {
			this.trafficRxEnd = TrafficStats.getTotalRxBytes();
			this.trafficTxEnd = TrafficStats.getTotalTxBytes();			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.WebsiteTest#isRunning()
	 */
	@Override
	public boolean isRunning() {
		final boolean isRunning = this.isRunning.get();
		return isRunning;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.WebsiteTest#hasFinished()
	 */
	@Override
	public boolean hasFinished() {
		final boolean hasFinished = this.hasFinished.get();
		return hasFinished;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.WebsiteTest#setOnRenderingFinishedListener(at.alladin.rmbt.client.v2.task.WebsiteTest.RenderingFinishedListener)
	 */
	@Override
	public void setOnRenderingFinishedListener(RenderingListener listener) {
		this.listener = listener;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.WebsiteTest#hasError()
	 */
	@Override
	public boolean hasError() {
		return this.hasError.get();
	}
	
    /*
     * (non-Javadoc)
     * @see at.alladin.rmbt.client.v2.task.WebsiteTest#getStatusCode()
     */
	@Override
	public int getStatusCode() {
		return statusCode;
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.WebsiteTest#getTxBytes()
	 */
	@Override
	public long getTxBytes() {
		return (trafficTxStart != -1 ? trafficTxEnd - trafficTxStart : -1);
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.WebsiteTest#getRxBytes()
	 */
	@Override
	public long getRxBytes() {
		return (trafficRxStart != -1 ? trafficRxEnd - trafficRxStart : -1);
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.WebsiteTest#getTotalTrafficBytes()
	 */
	@Override
	public long getTotalTrafficBytes() {
		return (getRxBytes() != -1 ? getRxBytes() + getTxBytes() : -1);
	}

	/*
	 * (non-Javadoc)
	 * @see at.alladin.rmbt.client.v2.task.service.WebsiteTestService#getResourceCount()
	 */
	@Override
	public int getResourceCount() {
		return resourceCount.get();
	}

}
