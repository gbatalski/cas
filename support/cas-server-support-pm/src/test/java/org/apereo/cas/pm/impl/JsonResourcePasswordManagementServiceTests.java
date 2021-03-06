package org.apereo.cas.pm.impl;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordValidationService;
import org.apereo.cas.pm.config.PasswordManagementConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link JsonResourcePasswordManagementServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuditConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreUtilConfiguration.class,
    PasswordManagementConfiguration.class})
@TestPropertySource(locations = {"classpath:/pm.properties"})
@Slf4j
public class JsonResourcePasswordManagementServiceTests {

    @Autowired
    @Qualifier("passwordChangeService")
    private PasswordManagementService passwordChangeService;

    @Autowired
    @Qualifier("passwordValidationService")
    private PasswordValidationService passwordValidationService;

    @Test
    public void verifyUserEmailCanBeFound() {
        final var email = passwordChangeService.findEmail("casuser");
        assertEquals("casuser@example.org", email);
    }

    @Test
    public void verifyUserEmailCanNotBeFound() {
        final var email = passwordChangeService.findEmail("casusernotfound");
        assertNull(email);
    }

    @Test
    public void verifyUserQuestionsCanBeFound() {
        final Map questions = passwordChangeService.getSecurityQuestions("casuser");
        assertEquals(2, questions.size());

    }

    @Test
    public void verifyUserPasswordChange() {
        final Credential c = new UsernamePasswordCredential("casuser", "password");
        final var bean = new PasswordChangeBean();
        bean.setConfirmedPassword("newPassword");
        bean.setPassword("newPassword");
        final var res = passwordChangeService.change(c, bean);
        assertTrue(res);
    }

    @Test
    public void verifyPasswordValidationService() {
        final var c = new UsernamePasswordCredential("casuser", "password");
        final var bean = new PasswordChangeBean();
        bean.setConfirmedPassword("Test@1234");
        bean.setPassword("Test@1234");
        final var isValid = passwordValidationService.isValid(c, bean);
        assertTrue(isValid);
    }
}
