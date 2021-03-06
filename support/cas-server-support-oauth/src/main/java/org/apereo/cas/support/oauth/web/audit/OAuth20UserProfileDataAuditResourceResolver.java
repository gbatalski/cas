package org.apereo.cas.support.oauth.web.audit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;

import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

/**
 * The {@link OAuth20UserProfileDataAuditResourceResolver}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
public class OAuth20UserProfileDataAuditResourceResolver extends ReturnValueAsStringResourceResolver {
    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object retval) {
        Objects.requireNonNull(retval, "User profile data must not be null");
        final var profileMap = Map.class.cast(retval);
        final var accessToken = AccessToken.class.cast(auditableTarget.getArgs()[0]);

        final var result = new ToStringBuilder(this, NO_CLASS_NAME_STYLE)
            .append("id", profileMap.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID))
            .append("client_id", profileMap.get(OAuth20Constants.CLIENT_ID))
            .append("service", profileMap.get(CasProtocolConstants.PARAMETER_SERVICE))
            .append("scopes", accessToken.getScopes())
            .append("attributes", profileMap.get(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES))
            .toString();

        return new String[]{result};
    }
}
