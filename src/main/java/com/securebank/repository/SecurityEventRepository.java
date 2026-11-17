
package com.securebank.repository;

import com.securebank.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findTop100ByOrderByEventTimeDesc();

    long countBySeverityIgnoreCase(String severity);

    long countByEventTypeIgnoreCase(String eventType);

    long countByEventTimeAfter(LocalDateTime dateTime);

    List<SecurityEvent> findByIpAddressOrderByEventTimeDesc(
            String ipAddress
    );
    long countByEventTypeAndIpAddressAndEventTimeAfter(
            String eventType,
            String ipAddress,
            LocalDateTime dateTime
    );

    List<SecurityEvent> findByEmailOrderByEventTimeDesc(
            String email
    );
}
