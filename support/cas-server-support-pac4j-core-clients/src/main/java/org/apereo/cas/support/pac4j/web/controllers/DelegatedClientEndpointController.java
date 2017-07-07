package org.apereo.cas.support.pac4j.web.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Processes delegation response over /delegatedAuthn/ context path.
 * <p>
 * This controller extracts the client name from the context path, enriches the request by client_name attribute
 * and forwards it for further processing by the webflow for /login?client_name </p>
 *
 * @author Ghenadii Batalski
 * @see org.apereo.cas.support.pac4j.web.flow.DelegatedClientAuthenticationAction
 * @since 5.2.0
 */
@Controller("delegatedClientEndpointController")
public class DelegatedClientEndpointController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedClientEndpointController.class);

    /**
     * Forwards request to login webflow.
     *
     * @param clientType - delegating client type (eg. oidc)
     * @param clientName - delegating client name to be forwarded as client_name parameter
     * @param model - model
     * @return forward to login webflow
     */
    @RequestMapping("/delegatedAuthn/{clientType}/{clientName}")
    public ModelAndView delegateAuthnClient(@PathVariable final String clientType, @PathVariable final String clientName, final ModelMap model) {
        LOGGER.debug("request delegated to client [{}]", clientName);
        return new ModelAndView("forward:/login?client_name=" + clientName, model);
    }
}
