package com.spinn3r.noxy.reverse.filters;

import com.spinn3r.noxy.reverse.meta.OnlineServerMetaIndexProvider;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import java.net.InetSocketAddress;

/**
 * Provides our default HTTP request handling.
 */
public class ReverseProxyHttpFiltersSourceAdapter implements HttpFiltersSource {

    private HttpFiltersSourceAdapter httpFiltersSourceAdapterDelegate;

    private final OnlineServerMetaIndexProvider onlineServerMetaIndexProvider;

    public ReverseProxyHttpFiltersSourceAdapter(HttpFiltersSourceAdapter httpFiltersSourceAdapterDelegate, OnlineServerMetaIndexProvider onlineServerMetaIndexProvider) {
        this.httpFiltersSourceAdapterDelegate = httpFiltersSourceAdapterDelegate;
        this.onlineServerMetaIndexProvider = onlineServerMetaIndexProvider;
    }

    @Override
    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        return new ReverseProxyHttpFiltersAdapter( httpFiltersSourceAdapterDelegate.filterRequest( originalRequest, ctx ) );
    }

    @Override
    public int getMaximumRequestBufferSizeInBytes() {
        return 0;
    }

    @Override
    public int getMaximumResponseBufferSizeInBytes() {
        return 0;
    }

    class ReverseProxyHttpFiltersAdapter implements HttpFilters {

        private HttpFilters delegate;

        private HttpRequest httpRequest;

        public ReverseProxyHttpFiltersAdapter(HttpFilters delegate) {
            this.delegate = delegate;
        }

        @Override
        public HttpResponse clientToProxyRequest(HttpObject httpObject) {

            if ( httpObject instanceof HttpRequest ) {
                this.httpRequest = (HttpRequest)httpObject;
            }

            return delegate.clientToProxyRequest( httpObject );
        }

        @Override
        public HttpObject proxyToClientResponse(HttpObject httpObject) {

            if ( onlineServerMetaIndexProvider.get().getBalancer().size() == 0 && httpObject instanceof HttpResponse ) {
                return new DefaultHttpResponse( httpRequest.getProtocolVersion(), HttpResponseStatus.BAD_GATEWAY );
            }

            return delegate.proxyToClientResponse( httpObject );
        }


        @Override
        public HttpResponse proxyToServerRequest(HttpObject httpObject) {
            return delegate.proxyToServerRequest( httpObject );
        }

        @Override
        public void proxyToServerRequestSending() {
            delegate.proxyToServerRequestSending();
        }

        @Override
        public void proxyToServerRequestSent() {
            delegate.proxyToServerRequestSent();
        }

        @Override
        public HttpObject serverToProxyResponse(HttpObject httpObject) {
            return delegate.serverToProxyResponse( httpObject );
        }

        @Override
        public void serverToProxyResponseTimedOut() {
            delegate.serverToProxyResponseTimedOut();
        }

        @Override
        public void serverToProxyResponseReceiving() {
            delegate.serverToProxyResponseReceiving();
        }

        @Override
        public void serverToProxyResponseReceived() {
            delegate.serverToProxyResponseReceived();
        }

        @Override
        public void proxyToServerConnectionQueued() {
            delegate.proxyToServerConnectionQueued();
        }

        @Override
        public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
            return delegate.proxyToServerResolutionStarted( resolvingServerHostAndPort );
        }

        @Override
        public void proxyToServerResolutionFailed(String hostAndPort) {
            delegate.proxyToServerResolutionFailed( hostAndPort );
        }

        @Override
        public void proxyToServerResolutionSucceeded(String serverHostAndPort, InetSocketAddress resolvedRemoteAddress) {
            delegate.proxyToServerResolutionSucceeded( serverHostAndPort, resolvedRemoteAddress );
        }

        @Override
        public void proxyToServerConnectionStarted() {
            delegate.proxyToServerConnectionStarted();
        }

        @Override
        public void proxyToServerConnectionSSLHandshakeStarted() {
            delegate.proxyToServerConnectionSSLHandshakeStarted();
        }

        @Override
        public void proxyToServerConnectionFailed() {
            delegate.proxyToServerConnectionFailed();
        }

        @Override
        public void proxyToServerConnectionSucceeded(ChannelHandlerContext serverCtx) {
            delegate.proxyToServerConnectionSucceeded( serverCtx );
        }

    }

}