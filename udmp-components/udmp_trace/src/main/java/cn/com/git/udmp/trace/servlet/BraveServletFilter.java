package cn.com.git.udmp.trace.servlet;

import com.github.kristofa.brave.http.HttpRequest;
import com.github.kristofa.brave.http.HttpResponse;
import com.github.kristofa.brave.http.HttpServerRequestAdapter;
import com.github.kristofa.brave.http.HttpServerResponseAdapter;
import com.github.kristofa.brave.http.SpanNameProvider;

import cn.com.git.udmp.trace.BraveFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Servlet filter that will extract trace headers from the request and send
 * sr (server received) and ss (server sent) annotations.
 */
public class BraveServletFilter implements Filter {


    private FilterConfig filterConfig;

    

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String alreadyFilteredAttributeName = getAlreadyFilteredAttributeName();
        boolean hasAlreadyFilteredAttribute = request.getAttribute(alreadyFilteredAttributeName) != null;
        if (hasAlreadyFilteredAttribute) {
            // Proceed without invoking this filter...
            filterChain.doFilter(request, response);
        } else {

            final StatusExposingServletResponse statusExposingServletResponse = new StatusExposingServletResponse((HttpServletResponse) response);
            BraveFactory.getBrave().serverRequestInterceptor().handle(new HttpServerRequestAdapter(new ServletHttpServerRequest((HttpServletRequest) request), new SpanNameProvider() {
				@Override
				public String spanName(HttpRequest request) {
					String path=request.getUri().getPath();
					if(path!=null&&path.indexOf(";")>=0){
						path = path.substring(0,path.indexOf(";"));
					}
					return "servlet_"+request.getHttpMethod()+"_"+path;
				}
			}));

            try {
                filterChain.doFilter(request, statusExposingServletResponse);
            } finally {
            	 BraveFactory.getBrave().serverResponseInterceptor().handle(new HttpServerResponseAdapter(new HttpResponse() {
                    @Override
                    public int getHttpStatusCode() {
                        return statusExposingServletResponse.getStatus();
                    }
                }));
            }
        }
    }

    @Override
    public void destroy() {

    }

    private String getAlreadyFilteredAttributeName() {
        String name = getFilterName();
        if (name == null) {
            name = getClass().getName();
        }
        return name + ".FILTERED";
    }

    private final String getFilterName() {
        return (this.filterConfig != null ? this.filterConfig.getFilterName() : null);
    }
    


    private static class StatusExposingServletResponse extends HttpServletResponseWrapper {
        // The Servlet spec says: calling setStatus is optional, if no status is set, the default is OK.
        private int httpStatus = HttpServletResponse.SC_OK;

        public StatusExposingServletResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(int sc) throws IOException {
            httpStatus = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            httpStatus = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void setStatus(int sc) {
            httpStatus = sc;
            super.setStatus(sc);
        }

        public int getStatus() {
            return httpStatus;
        }
    }
}
