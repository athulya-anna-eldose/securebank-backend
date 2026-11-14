
package com.securebank.service;

import com.securebank.dto.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(String email);
}
