package org.jbnu.jdevops.jcodeportallogin.config

import org.slf4j.LoggerFactory
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager

enum class DataSourceKey {
    MASTER,
    REPLICA
}

class RoutingDataSource : AbstractRoutingDataSource() {

    companion object {
        private val log = LoggerFactory.getLogger(RoutingDataSource::class.java)
    }

    // DataSource Key를 결정함
    override fun determineCurrentLookupKey(): Any? {
        val isTransactionReadOnly: Boolean = TransactionSynchronizationManager.isCurrentTransactionReadOnly()

        if (isTransactionReadOnly) {
            log.info("Routing to Replica server")
            return DataSourceKey.REPLICA
        }

        log.info("Routing to Master server")
        return DataSourceKey.MASTER
    }
}