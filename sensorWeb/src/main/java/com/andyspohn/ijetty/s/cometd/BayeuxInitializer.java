package com.andyspohn.ijetty.s.cometd;

import com.andyspohn.ijetty.s.services.LocationService;
import com.andyspohn.ijetty.s.services.OrientationService;
import org.cometd.bayeux.server.BayeuxServer;

import javax.servlet.*;
import java.io.IOException;

public class BayeuxInitializer extends GenericServlet {

    private OrientationService orientationService = null;
    private LocationService locationService = null;

    @Override
    public void init(ServletConfig config) throws ServletException {
        // Get our Comet context
        BayeuxServer bayeux = (BayeuxServer) config.getServletContext().getAttribute(BayeuxServer.ATTRIBUTE);

        // Get our Android context
        android.content.Context androidContext =
                (android.content.Context) config.getServletContext().getAttribute("org.mortbay.ijetty.context");

        // Initialize all our Comet aware services
        locationService = new LocationService(bayeux, androidContext);
        orientationService = new OrientationService(bayeux, androidContext);
    }

    @Override
    public void destroy() {
        orientationService.destroy();
        locationService.destroy();
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        throw new ServletException("Not Implemented");
    }
}
