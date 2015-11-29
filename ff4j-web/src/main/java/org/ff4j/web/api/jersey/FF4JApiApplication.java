package org.ff4j.web.api.jersey;

import javax.ws.rs.core.Context;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.ff4j.FF4j;
import org.ff4j.web.api.FF4jWebConstants;
import org.ff4j.web.api.conf.FF4jApiConfig;
import org.ff4j.web.api.resources.FF4jResource;
import org.ff4j.web.api.security.FF4jRolesResourceFilterFactory;
import org.ff4j.web.api.security.FF4jSecurityContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.BeanConfig;


/**
 * 
 * 
 * @author <a href="mailto:cedrick.lunven@gmail.com">Cedrick LUNVEN</a>
 *
 */
//@ApplicationPath("api")
public abstract class FF4JApiApplication extends PackagesResourceConfig implements FF4jWebConstants {

    /** logger for this class. */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Child class must fullfil the configuration of the apicd
     * 
     * @return
     */
    protected abstract FF4jApiConfig getApiConfig();

    /**
     * Injection of bean ff4j within this static class.
     *
     * @author <a href="mailto:cedrick.lunven@gmail.com">Cedrick LUNVEN</a>
     */
    public static class FF4jInjectableProvider extends SingletonTypeInjectableProvider<Context, FF4j> {
        public FF4jInjectableProvider(FF4j ff4j) {
            super(FF4j.class, ff4j);
        }
    }

    /**
     * Constructor to defined resources.
     */
    public FF4JApiApplication() {
        super(FF4jResource.class.getPackage().getName());
        log.info("  __  __ _  _   _ ");
        log.info(" / _|/ _| || | (_)");
        log.info("| |_| |_| || |_| |");
        log.info("|  _|  _|__   _| |");
        log.info("|_| |_|    |_|_/ |");
        log.info("             |__/   WEB API Initialization...");
        log.info(" ");
        
        // Initialize through configuration
        FF4jApiConfig conf = getApiConfig();
        FF4jSecurityContextFilter.securityConfig = conf;
        FF4jRolesResourceFilterFactory.apiConfig = conf;
        
        // Register ff4J bean to be injected into resources.
        getSingletons().add(new FF4jInjectableProvider(conf.getFF4j()));
        
        // Pojo Mapping to 'ON'
        getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        
        // Mapping Jackson Custom
        getSingletons().add(new JacksonJsonProvider());
        getSingletons().add(new FF4jJacksonMapper());
        
        // Authorization, JSR250
        if (conf.isEnableAuthorization()) {
            getProperties().put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
                    FF4jRolesResourceFilterFactory.class.getCanonicalName());
            log.info("ff4j webApi security has been set up with both authentication and authorization");

        } else if (conf.isEnableAuthentication()) {
            // Only Authenticated here
            StringBuilder filters = new StringBuilder();
            filters.append(FF4jSecurityContextFilter.class.getCanonicalName());
            if (conf.isEnableLogging()) {
                filters.append(";" + LoggingFilter.class.getCanonicalName());
            }
            // Pas authorization
            getProperties().put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, filters.toString());
            log.info("ff4j webApi security has been set up with authentication only");

        } else {
            // No security
            log.info("ff4j webApi security has been set up with no security");
        }
        
        // Enable Documentation if required
        if (conf.isEnableDocumentation()) {
           
           BeanConfig beanConfig = new BeanConfig();
           beanConfig.setVersion("1.0.2");
           beanConfig.setSchemes(new String[]{"http"});
           beanConfig.setHost("localhost:8282");
           beanConfig.setBasePath("/api");
           beanConfig.setResourcePackage("org.ff4j.web.api.resources");
           beanConfig.setScan(true);
           ScannerFactory.setScanner(beanConfig);
            
           getSingletons().add(io.swagger.jaxrs.listing.ApiListingResource.class);
           getSingletons().add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
           
          
           // <---
        }
    }

}
