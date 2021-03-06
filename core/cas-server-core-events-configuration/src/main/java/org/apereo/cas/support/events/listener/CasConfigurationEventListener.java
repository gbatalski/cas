package org.apereo.cas.support.events.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link CasConfigurationEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class CasConfigurationEventListener {

    @Autowired
    private ConfigurationPropertiesBindingPostProcessor binder;

    @Autowired
    private ObjectProvider<ContextRefresher> contextRefresher;

    @Autowired
    private ApplicationContext applicationContext;
    
    private final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager;
    
    public CasConfigurationEventListener(final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager) {
        this.configurationPropertiesEnvironmentManager = configurationPropertiesEnvironmentManager;
    }

    /**
     * Handle refresh event when issued to this CAS server locally.
     *
     * @param event the event
     */
    @EventListener
    public void handleRefreshEvent(final EnvironmentChangeEvent event) {
        LOGGER.debug("Received event [{}]", event);
        rebind();
    }

    /**
     * Handle configuration modified event.
     *
     * @param event the event
     */
    @EventListener
    public void handleConfigurationModifiedEvent(final CasConfigurationModifiedEvent event) {
        if (this.contextRefresher == null) {
            LOGGER.warn("Unable to refresh application context, since no refresher is available");
            return;
        }

        if (event.isEligibleForContextRefresh()) {
            LOGGER.info("Received event [{}]. Refreshing CAS configuration...", event);
            Collection<String> keys = null;
            try {
                final var refresher = this.contextRefresher.getIfAvailable();
                if (refresher != null) {
                    keys = refresher.refresh();
                    LOGGER.debug("Refreshed the following settings: [{}].", keys);
                }
            } catch (final Exception e) {
                LOGGER.trace(e.getMessage(), e);
            } finally {
                rebind();
                LOGGER.info("CAS finished rebinding configuration with new settings [{}]",
                        ObjectUtils.defaultIfNull(keys, new ArrayList<>(0)));
            }
        }
    }

    private void rebind() {
        LOGGER.info("Refreshing CAS configuration. Stand by...");
        if (configurationPropertiesEnvironmentManager != null) {
            configurationPropertiesEnvironmentManager.rebindCasConfigurationProperties(this.applicationContext);
        } else {
            CasConfigurationPropertiesEnvironmentManager.rebindCasConfigurationProperties(this.binder, this.applicationContext);
        }
    }
}
