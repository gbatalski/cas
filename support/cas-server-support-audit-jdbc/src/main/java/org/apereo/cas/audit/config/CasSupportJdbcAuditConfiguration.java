package org.apereo.cas.audit.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.entity.AuditTrailEntity;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.audit.support.JdbcAuditTrailManager;
import org.apereo.inspektr.audit.support.MaxAgeWhereClauseMatchCriteria;
import org.apereo.inspektr.audit.support.WhereClauseMatchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * This is {@link CasSupportJdbcAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casJdbcAuditConfiguration")
@EnableAspectJAutoProxy
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class CasSupportJdbcAuditConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public AuditTrailManager jdbcAuditTrailManager() {
        final var jdbc = casProperties.getAudit().getJdbc();
        final var t = new JdbcAuditTrailManager(inspektrAuditTransactionTemplate());
        t.setCleanupCriteria(auditCleanupCriteria());
        t.setDataSource(inspektrAuditTrailDataSource());
        t.setAsynchronous(jdbc.isAsynchronous());
        t.setColumnLength(jdbc.getColumnLength());
        var tableName = AuditTrailEntity.AUDIT_TRAIL_TABLE_NAME;
        if (StringUtils.isNotBlank(jdbc.getDefaultSchema())) {
            tableName = jdbc.getDefaultSchema().concat(".").concat(tableName);
        }
        if (StringUtils.isNotBlank(jdbc.getDefaultCatalog())) {
            tableName = jdbc.getDefaultCatalog().concat(".").concat(tableName);
        }
        t.setTableName(tableName);
        return t;
    }

    @Bean
    public AuditTrailExecutionPlanConfigurer jdbcAuditTrailExecutionPlanConfigurer() {
        return plan -> plan.registerAuditTrailManager(jdbcAuditTrailManager());
    }

    @Lazy
    @Bean
    public LocalContainerEntityManagerFactoryBean inspektrAuditEntityManagerFactory() {
        return JpaBeans.newHibernateEntityManagerFactoryBean(
            new JpaConfigDataHolder(
                JpaBeans.newHibernateJpaVendorAdapter(casProperties.getJdbc()),
                "jpaInspektrAuditContext",
                CollectionUtils.wrap(AuditTrailEntity.class.getPackage().getName()),
                inspektrAuditTrailDataSource()),
            casProperties.getAudit().getJdbc());
    }

    @Bean
    @RefreshScope
    public WhereClauseMatchCriteria auditCleanupCriteria() {
        return new MaxAgeWhereClauseMatchCriteria(casProperties.getAudit().getJdbc().getMaxAgeDays());
    }

    @Bean
    public PlatformTransactionManager inspektrAuditTransactionManager() {
        return new DataSourceTransactionManager(inspektrAuditTrailDataSource());
    }

    @Bean
    public DataSource inspektrAuditTrailDataSource() {
        return JpaBeans.newDataSource(casProperties.getAudit().getJdbc());
    }

    @Bean
    public TransactionTemplate inspektrAuditTransactionTemplate() {
        final var t = new TransactionTemplate(inspektrAuditTransactionManager());
        t.setIsolationLevelName(casProperties.getAudit().getJdbc().getIsolationLevelName());
        t.setPropagationBehaviorName(casProperties.getAudit().getJdbc().getPropagationBehaviorName());
        return t;
    }
}
